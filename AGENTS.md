# AGENTS.md

Guidance for AI coding agents working on **WTFU** — a Kotlin Multiplatform alarm app with a `shared` module, `androidApp` (Jetpack Compose), and `iosApp` (SwiftUI + CocoaPods).

## Project layout

| Module | Role |
|--------|------|
| `shared/` | Business logic, SQLDelight, Koin DI, ViewModels — `commonMain` + `androidMain` / `iosMain` |
| `androidApp/` | Android UI, platform schedulers, Compose |
| `iosApp/` | SwiftUI shell; consumes `shared` via CocoaPods |

Put portable code in `shared/src/commonMain`. Use `expect`/`actual` only when a platform API is unavoidable (schedulers, drivers, etc.).

## Build and test

```bash
# Android debug APK
./gradlew :androidApp:assembleDebug

# Shared module (all targets)
./gradlew :shared:build

# Unit tests (common + platform where configured)
./gradlew :shared:cleanTest :shared:test
./gradlew :androidApp:testDebugUnitTest

# Clean
./gradlew clean
```

iOS: open `iosApp/iosApp.xcworkspace` in Xcode after Gradle/CocoaPods sync. Prefer Gradle-driven pod integration (`shared` cocoapods block) over hand-editing generated Pod artifacts.

## Kotlin Multiplatform

- **Single source of truth** for domain models, repositories, use cases, and ViewModels in `commonMain`.
- **No Android/iOS imports** in `commonMain` — use abstractions (`AlarmScheduler`, SQLDelight drivers) with platform implementations.
- Prefer **`kotlinx.coroutines`** (`Flow`, structured concurrency) over callbacks; collect in UI layers, not in pure domain code.
- Use **`kotlinx-datetime`** for portable date/time; avoid `java.time` in shared code.
- **Immutable** `data class` / `sealed interface` models; avoid mutable shared state.
- **Koin** for DI: declare modules in `shared/.../di/`, platform-specific bindings in `androidMain` / `iosMain`.
- **SQLDelight** for persistence; keep queries in `.sq` files and map to domain types in Kotlin.
- **SKIE** is enabled for smoother Swift interop — design public APIs in `shared` with ObjC/Swift consumers in mind (stable names, avoid overly nested generics where SKIE struggles).
- Avoid using expect/actual. Platform implementations should conform to an interface that can be used by shared code and be injected via DI. 
- Write code with multiplatform extensibility in mind. 
  - Any code that is not platform dependent should be in shared commonMain
  - Any functionality that is required for both platforms but with a different implementation should be used by common code as an interface that can be implemented by each platform and injected via DI. 

## Android

- **Jetpack Compose** + Material 3 for UI; keep composables dumb — state from ViewModels / `StateFlow`.
- **Navigation 3** (`NavDisplay`, `NavKey`, `rememberNavBackStack`) for routing; pass IDs/primitives, not heavy objects, when possible.
- **Lifecycle**: use `viewModelScope` in ViewModels; cancel work automatically; avoid `GlobalScope`.
- **Min/target SDK** values live in `gradle/libs.versions.toml` and module `build.gradle.kts` — align new dependencies with them.
- Platform-only code (notifications, `AlarmManager`, permissions) stays in `androidApp` or `androidMain`, behind interfaces defined in `commonMain`.
- Prefer **Kermit** (`co.touchlab:kermit`) for logging in shared code; use Android log adapters only at the edge if needed.

## iOS
- Stub out the iOS implementations unless explicitly told otherwise. All tasks should be assumed to be for the android target only if not otherwise stated 
- UI stays in Swift/SwiftUI under `iosApp/`; call into `shared` framework APIs.
- Do not duplicate business rules in Swift — implement once in `commonMain`.
- After changing `shared` public API or CocoaPods config, re-run the Gradle sync / `pod install` path the project already uses.

## Code style

- Idiomatic Kotlin: `when`, extension functions, `?.let`, early returns — not Java patterns.
- **Explicit API** for public `shared` surfaces when the module enables it.
- Name packages under `com.mfriend.wtfu`; keep file names aligned with primary types.
- Small, focused types; one responsibility per class/function.
- Comments only for non-obvious business rules — not for narrating obvious code.
- Any class that could have alternate implementations or interacts with platform dependencies should implement an interface for ease of testing and multiplatform implementations
- All code should be clearly documented. Methods and classes should have appropriate and concises kdoc comments and any logic within functions that is unintuitive or is a workaround based on limits like android permissions should be explained with a comment containing a link to the relevant docs.

## Error handling with Arrow Kt

Use **[Arrow Kt](https://arrowkt.io/)** ([`io.arrow-kt`](https://github.com/arrow-kt/arrow) on Maven Central — **not** [Apache Arrow](https://arrow.apache.org/), which is unrelated) for **typed, recoverable errors** in `commonMain` and shared domain layers. Start with `arrow-core`; add other Arrow Kt modules only when needed. Do **not** use exceptions for expected failure modes (validation, not found, network 4xx, etc.).

### Principles

1. Model failures as a **`sealed interface`** (or hierarchy) per bounded context, e.g. `AlarmError`.
2. Mark fallible operations with a **`Raise<Error>` context parameter** and use **`raise(...)`** to exit early — do not `throw` for domain errors.
3. Expose **composable entry points** with `either { }` (or `nullable { }` / `result { }` when appropriate) so callers get `Either<Error, T>`.
4. **Convert to exceptions only at system boundaries** where the platform forces it (e.g. some Android callbacks) — and document why.
5. Reserve **unchecked exceptions** for truly exceptional cases (programmer bugs, OOM). Catch and map to a logical error hierarchy (or log + crash) at the outermost layer when they cannot be avoided.

### Preferred pattern: `raise` with context parameters

Use Kotlin **context parameters** so callers must provide `Raise<E>` (via `either { }`, tests, or another `Raise` scope):

```kotlin
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.raise

sealed interface AlarmError {
  data class NotFound(val id: Int) : AlarmError
  data class InvalidTime(val hour: Int, val minute: Int) : AlarmError
}

context(_: Raise<AlarmError>)
fun validateAlarmTime(hour: Int, minute: Int) {
  ensure(hour in 0..23) { AlarmError.InvalidTime(hour, minute) }
  ensure(minute in 0..59) { AlarmError.InvalidTime(hour, minute) }
}

context(_: Raise<AlarmError>)
fun Alarm.Companion.fromStorage(row: AlarmRow): Alarm {
  validateAlarmTime(row.hour, row.minute)
  return Alarm(/* map fields */)
}

fun loadAlarm(id: Int, db: DatabaseHelper): Either<AlarmError, Alarm> = either {
  val row = db.getAlarmSync(id) ?: raise(AlarmError.NotFound(id))
  Alarm.fromStorage(row)
}
```

**Prefer** `ensure` / `ensureNotNull` over manual `if` + `raise` when expressing guards.

### Calling from coroutines / ViewModels

Bind at the edge to UI state — do not leak `Raise` into Compose or Swift:

```kotlin
viewModelScope.launch {
  loadAlarm(id, database)
    .fold(
      ifLeft = { /* emit error state */ },
      ifRight = { /* emit success state */ },
    )
}
```

For flows, use `map` / `catch` for unexpected failures, but keep **domain validation** in `Raise`-typed functions.

### What to avoid

```kotlin
// ❌ Expected failure via exception
fun getAlarm(id: Int): Alarm =
  database.find(id) ?: throw IllegalStateException("not found")

// ❌ Swallowing errors
try { schedule(alarm) } catch (_: Exception) { }

// ✅ Typed error
fun getAlarm(id: Int): Either<AlarmError, Alarm> = either {
  val row = database.find(id) ?: raise(AlarmError.NotFound(id))
  Alarm.fromStorage(row)
}
```

When adding Arrow Kt to Gradle, pin the version in `gradle/libs.versions.toml` and depend on `io.arrow-kt:arrow-core` in `commonMain` (e.g. `implementation(libs.arrow.core)`). Add test helpers only in `commonTest` if needed.

## Testing

- Unit test domain logic and `either`/`Raise` paths in `shared/src/commonTest`.
- Use **`kotest`**  **FunSpec** and **coroutines-test**; assert on `Either` with `fold` or Arrow Kt test utilities.
- Use `kotest-assertions` and its arrow extensions for assertions
- For `Flow`, use **Turbine** where the project already includes it.
- Android UI tests belong in `androidApp`; do not require Android runtime for pure `commonMain` tests.

## Dependencies and versions

- Centralize versions in **`gradle/libs.versions.toml`**; reference via `libs.*` in Gradle scripts.
- Do not bump Kotlin, AGP, or Compose without checking KMP + SKIE compatibility.
- New shared libraries must support **all** `shared` targets (Android + iOS architectures).

## Security and hygiene

- Never commit secrets, keystores, or API keys.
- Do not edit generated files under `iosApp/Pods/` unless fixing a documented, reproducible issue — prefer fixing the source (`shared`, `Podfile`, Gradle).

## Pull requests

- Keep changes scoped; prefer separate PRs for large refactors vs. feature work.
- Run relevant `./gradlew` tasks above before marking ready.
- Match existing commit tone: short, imperative subject lines.
