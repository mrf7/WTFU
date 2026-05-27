import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.cocoapods)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    android {
        namespace = "com.mfriend.wtfu.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
                implementation(libs.kotlin.test)
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

sqldelight {
    databases {
        create("AlarmDb") {
            packageName.set("com.mfriend")
            dialect(libs.sqlDelight.sqlite.dialect)
        }
    }
}
