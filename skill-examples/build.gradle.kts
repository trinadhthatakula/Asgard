import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
}

// Compile the exact files shipped with the skill as a consumer on both supported targets.
// No global Material opt-ins: examples must declare any opt-in they need themselves.
kotlin {
    androidLibrary {
        namespace = "com.valhalla.asgard.skillexamples"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_21 }
    }
    wasmJs { browser() }
    sourceSets {
        commonMain {
            kotlin.srcDir(rootProject.file(".claude/skills/asgard-ui/examples"))
            dependencies {
                implementation(project(":asgard"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
            }
        }
    }
}
