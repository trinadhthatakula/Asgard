import java.security.MessageDigest
import java.util.HexFormat
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

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
        androidResources.enable = true
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
            implementation(project(":asgard-glass"))
            implementation(compose.components.resources)
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
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val glassGuide: RegularFileProperty
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
        // The installed skill and both galleries use the same component ratings.
        val row = Regex("""^\| `(\w+)` \| ([1-5])/5 \| ([^|]+) \|$""")
        val ratings = glassGuide.get().asFile.readLines().filter { it.startsWith("| `") }.associate { line ->
            val (component, score, guidance) = requireNotNull(row.matchEntire(line)) { line }.destructured
            component to "    ${quoted(component)} to GlassCompatibility($score, ${quoted(guidance)}),"
        }
        require(ratings.isNotEmpty()) { "The glass compatibility table is empty" }
        target.writeText("""
            package com.valhalla.asgard.demo
            internal object GalleryBuildInfo {
                const val version = ${quoted(version)}
                const val revision = ${quoted(revision)}
                const val released = $released
            }
        """.trimIndent() + "\n\ninternal val glassCompatibility = mapOf(\n" +
            ratings.values.joinToString("\n") + "\n)\n")
    }
}
val generateGalleryBuildInfo = tasks.register<GenerateGalleryBuildInfo>("generateGalleryBuildInfo") {
    sourceVersion.set(galleryVersion)
    sourceRevision.set(galleryRevision)
    sourceTag.set(galleryExactTag)
    glassGuide.set(rootProject.layout.projectDirectory.file(".claude/skills/asgard-ui/references/glass.md"))
    outputDirectory.set(galleryInfoDirectory)
}
kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(galleryInfoDirectory)
tasks.named("compileKotlinWasmJs") { dependsOn(generateGalleryBuildInfo) }
tasks.named("compileAndroidMain") { dependsOn(generateGalleryBuildInfo) }

// GitHub and the standalone Play policy page display the same maintained text.
val galleryPrivacyPolicy = providers.fileContents(rootProject.layout.projectDirectory.file("PRIVACY.md")).asText
tasks.named<ProcessResources>("wasmJsProcessResources") {
    val policy = galleryPrivacyPolicy.get()
    inputs.property("privacyPolicy", policy)
    filesMatching("privacy/index.html") { expand(mapOf("policy" to policy)) }
}

// A cached entry script can request Wasm files from a previous Pages deployment.
tasks.named<Sync>("wasmJsBrowserDistribution") {
    val bundle = tasks.named<KotlinWebpack>("wasmJsBrowserProductionWebpack").flatMap { it.mainOutputFile }
    val digest = providers.fileContents(bundle).asBytes.map {
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(it))
    }
    inputs.property("galleryBundleDigest", digest)
    filesMatching("index.html") {
        val versionedScript = "demo.js?v=${digest.get()}"
        filter { line -> line.replace("demo.js?v=development", versionedScript) }
    }
}
