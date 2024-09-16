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
                api(libs.lifecycle.viewmodel)
                api(libs.kotlinx.dateTime)
                implementation(libs.coroutines.core)
                implementation(libs.sqlDelight.coroutinesExt)
                api(libs.koin.core)
                implementation(libs.primitive.adapters)
                api(libs.touchlab.kermit)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.sqlDelight.android)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.sqlDelight.native)
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