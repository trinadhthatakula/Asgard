import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    // AGP 9 KMP: Android config nests inside kotlin { androidLibrary { } } via the
    // com.android.kotlin.multiplatform.library plugin — NOT a top-level android { } block.
    androidLibrary {
        namespace = "com.valhalla.asgard"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        all {
            // Asgard leans on Expressive Material 3 (MotionScheme, ButtonGroup) — opt in once.
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
        commonMain.dependencies {
            // api: Compose types (Modifier, Color, Shape, Brush, ImageVector, TextStyle, Dp,
            // BorderStroke, PaddingValues …) appear in every public component signature, so
            // foundation + ui + material3 must be exposed transitively rather than relying on
            // material3's POM to re-export them.
            implementation(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            // NOTE: material-icons-extended intentionally dropped in 1.2.0 — AsgardHeader's only
            // icon (Icons.AutoMirrored.Filled.ArrowBack) lives in material-icons-core (pulled in
            // transitively by material3). Consumers pass their own ImageVectors for everything else.
        }
    }
}

// Vanniktech's signAllPublications() requires a GPG key for non-SNAPSHOT versions, including
// publishToMavenLocal. To verify publishing locally WITHOUT a key, append -PVERSION_NAME=1.0.0-SNAPSHOT
// (SNAPSHOT versions are exempt from signing). Real releases sign via the key in ~/.gradle/gradle.properties.
mavenPublishing {
    // group + version come from GROUP / VERSION_NAME in gradle.properties (auto-read by
    // vanniktech); artifactId defaults to the module name ("asgard"). Setting coordinates()
    // again here is rejected as "final" under the KMP plugin, so it is intentionally omitted.
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Empty(),
            sourcesJar = true
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
