pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // PREFER_PROJECT (not FAIL_ON_PROJECT_REPOS): the Kotlin wasm toolchain adds project-scoped
    // Node.js/Yarn download repositories at build time. Modules without their own repositories
    // still fall back to the settings repositories below for the Compose dependencies.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "asgard"
include(":asgard")
include(":demo")
