import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.valhalla.asgard.app"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = libs.versions.compileSdkMinor.get().toInt()
        }
    }
    defaultConfig {
        applicationId = "com.valhalla.asgard.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = 1
        versionName = providers.gradleProperty("VERSION_NAME").get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    buildTypes {
        create("benchmark") {
            initWith(getByName("release"))
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".benchmark"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    lint { warningsAsErrors = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin.compilerOptions {
    jvmTarget = JvmTarget.JVM_21
    optIn.addAll("androidx.compose.material3.ExperimentalMaterial3Api", "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
}

dependencies {
    implementation(project(":demo"))
    implementation(project(":asgard"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)
    constraints {
        implementation(libs.androidx.lifecycle.runtime) {
            because("Lifecycle 2.9.x lint checks use Kotlin APIs removed by the current AGP toolchain")
        }
    }
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(project(":compatibilityConsumer"))
}
