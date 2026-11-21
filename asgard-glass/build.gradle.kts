import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    androidLibrary {
        namespace = "com.valhalla.asgard.glass"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":asgard"))
            implementation(libs.compose.runtime)
            implementation(libs.haze.core)
            implementation(libs.haze.blur)
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn("compileAndroidMain", "compileKotlinWasmJs")
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Empty(),
            sourcesJar = true
        )
    )
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    pom {
        name.set("Asgard Glass")
        description.set("Optional frosted glass surfaces for the Asgard Compose library.")
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
