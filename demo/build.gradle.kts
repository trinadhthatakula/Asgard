plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidLibrary {
        namespace = "com.valhalla.asgard.demo"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.compileSdkMinor.get().toInt()
            }
        }
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21 }
    }
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "demo.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
        commonMain.dependencies {
            implementation(project(":asgard"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(compose.materialIconsExtended)
        }
        androidMain.dependencies { implementation(libs.androidx.activity.compose) }
    }
}

// Identify the source behind a preview without claiming that an untagged API is published.
val galleryVersion = providers.gradleProperty("VERSION_NAME").orElse("development")
val galleryRevision = providers.exec {
    commandLine("git", "describe", "--always", "--dirty")
}.standardOutput.asText.map { it.trim() }
val galleryExactTag = providers.exec {
    commandLine("git", "describe", "--tags", "--exact-match", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim() }
val galleryInfoDirectory = layout.buildDirectory.dir("generated/galleryInfo")
abstract class GenerateGalleryBuildInfo : DefaultTask() {
    @get:org.gradle.api.tasks.Input abstract val sourceVersion: org.gradle.api.provider.Property<String>
    @get:org.gradle.api.tasks.Input abstract val sourceRevision: org.gradle.api.provider.Property<String>
    @get:org.gradle.api.tasks.Input abstract val sourceTag: org.gradle.api.provider.Property<String>
    @get:org.gradle.api.tasks.OutputDirectory abstract val outputDirectory: org.gradle.api.file.DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun generate() {
        fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("$", "\\$").replace("\n", "\\n") + "\""
        val target = outputDirectory.get().file("com/valhalla/asgard/demo/GalleryBuildInfo.kt").asFile
        target.parentFile.mkdirs()
        val version = sourceVersion.get()
        val revision = sourceRevision.get()
        val released = sourceTag.get() == "v$version" && !revision.endsWith("-dirty")
        target.writeText("""
            package com.valhalla.asgard.demo
            internal object GalleryBuildInfo {
                const val version = ${quoted(version)}
                const val revision = ${quoted(revision)}
                const val released = $released
            }
        """.trimIndent() + "\n")
    }
}
val generateGalleryBuildInfo = tasks.register<GenerateGalleryBuildInfo>("generateGalleryBuildInfo") {
    sourceVersion.set(galleryVersion)
    sourceRevision.set(galleryRevision)
    sourceTag.set(galleryExactTag)
    outputDirectory.set(galleryInfoDirectory)
}
kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(galleryInfoDirectory)
tasks.named("compileKotlinWasmJs") { dependsOn(generateGalleryBuildInfo) }
tasks.named("compileAndroidMain") { dependsOn(generateGalleryBuildInfo) }
