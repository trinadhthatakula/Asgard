import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "com.valhalla.asgard"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // api: Compose types appear in every public component signature, and consumers
    // build their UIs against them — they must be exposed transitively.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.foundation)
    api(libs.androidx.animation)
    // Explicit alpha for Expressive Material 3 (ButtonGroup, ToggleButton, MotionScheme).
    api(libs.androidx.material3)
    // Only for AsgardHeader's default back-arrow icon; consumers pass their own ImageVectors.
    implementation(libs.androidx.material.icons.core)
}

// Vanniktech's signAllPublications() requires a GPG key for non-SNAPSHOT versions, including
// publishToMavenLocal. To verify publishing locally WITHOUT a key, append -PVERSION_NAME=1.0.0-SNAPSHOT
// (SNAPSHOT versions are exempt from signing). Real releases sign via the key in ~/.gradle/gradle.properties.
mavenPublishing {
    coordinates(
        groupId = providers.gradleProperty("GROUP").get(),
        artifactId = "asgard",
        version = providers.gradleProperty("VERSION_NAME").get()
    )
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    pom {
        name.set("Asgard")
        description.set("Theme-agnostic Jetpack Compose component library (Expressive Material 3) shared across Valhalla apps and beyond. Components read styling from the host app's MaterialTheme.")
        inceptionYear.set("2026")
        url.set("https://github.com/trinadhthatakula/Asgard")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("trinadhthatakula")
                name.set("Trinadh Thatakula")
                url.set("https://github.com/trinadhthatakula")
            }
        }
        scm {
            url.set("https://github.com/trinadhthatakula/Asgard")
            connection.set("scm:git:https://github.com/trinadhthatakula/Asgard.git")
            developerConnection.set("scm:git:ssh://git@github.com/trinadhthatakula/Asgard.git")
        }
    }
}
