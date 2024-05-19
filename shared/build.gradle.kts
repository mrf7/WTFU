plugins {
    alias(libs.plugins.cocoapods)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqlDelight)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {

    androidTarget {
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
        commonMain {
            dependencies {
                api("androidx.lifecycle:lifecycle-viewmodel:2.8.0-rc01")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")
                api("io.insert-koin:koin-core:3.4.1")
                implementation("app.cash.sqldelight:primitive-adapters:2.0.0")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        androidMain {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.0.0")
            }
        }
        iosMain {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.0")
            }
        }
    }
}

android {
    namespace = "com.mfriend.wtfu"
    compileSdk = 34
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