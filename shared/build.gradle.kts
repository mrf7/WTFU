plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("app.cash.sqldelight") version "2.0.0"
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")
                api("io.insert-koin:koin-core:3.4.1")
                implementation("app.cash.sqldelight:primitive-adapters:2.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.0")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.0")
            }
        }
    }
}

android {
    namespace = "com.mfriend.wtfu"
    compileSdk = 33
    defaultConfig {
        minSdk = 29
    }
}
sqldelight {
    databases {
        create("AlarmDb") {
            packageName.set("com.mfriend")
        }
    }
}