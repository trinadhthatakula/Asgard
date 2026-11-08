plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidLibrary {
        namespace = "com.valhalla.asgard.compatibility"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21 }
    }
    sourceSets {
        androidMain.dependencies {
            // Deliberately no project(:asgard) dependency. Compile these calls against the
            // independently published 2.0.1 AAR; the Android test app supplies current Asgard.
            compileOnly("com.trinadhthatakula:asgard-android:2.0.1") {
                version { strictly("2.0.1") }
            }
            implementation(libs.compose.material3)
        }
    }
}
