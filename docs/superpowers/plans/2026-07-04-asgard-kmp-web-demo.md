# Asgard KMP Conversion + Compose/WASM Web Demo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the Asgard Android library into a Kotlin Multiplatform library (Android + wasmJs) and add a live Compose/WASM component-gallery web demo deployed to GitHub Pages.

**Architecture:** `asgard` becomes a KMP module with `androidTarget()` + `wasmJs { browser() }`; its Compose components move to `commonMain` (imports stay `androidx.compose.*` because Compose Multiplatform ships the same packages). A new `demo` CMP application module (wasmJs) depends on `asgard` and renders a gallery with live previews, light/dark + seed-color theming, and code snippets. Publishing switches to `KotlinMultiplatform`, yielding both `com.valhalla:asgard` and `com.valhalla:asgard-android`.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform (`org.jetbrains.compose`), Compose Material 3 (Expressive), Gradle version catalogs, vanniktech maven-publish, GitHub Actions + Pages.

## Global Constraints

- Namespace/package stays `com.valhalla.asgard`; artifactId stays `asgard`.
- Components stay theme-agnostic: colors/typography default to `MaterialTheme`, no hardcoded `Color(0x…)`, icons passed as `ImageVector` params (the library ships only `material-icons-core`).
- Preserve Android parity: existing Android consumers must build unchanged against `com.valhalla:asgard` / `com.valhalla:asgard-android`.
- Targets this phase: `androidTarget()` + `wasmJs` only. No desktop/iOS (YAGNI).
- **Verified toolchain baseline** = the exact versions MindLoom already ships (`~/StudioProjects/MindLoom/gradle/libs.versions.toml`): Kotlin `2.3.20`, `org.jetbrains.compose.material3:material3` `1.10.0-alpha05`, and MindLoom's `composeMultiplatform` plugin version. Prefer these proven values over Asgard's current Kotlin `2.4.0` unless a CMP release explicitly lists Kotlin `2.4.0` support.
- No `@Preview` in library components (keeps the tooling dep out); previews live only in the demo.

---

## File Structure

**`asgard` module (converted):**
- `asgard/build.gradle.kts` — KMP config (rewritten).
- `asgard/src/commonMain/kotlin/com/valhalla/asgard/**` — all components (moved from `src/main/java`).
- `asgard/src/androidMain/kotlin/com/valhalla/asgard/AsgardBlur.android.kt` — `actual` blur (if needed).
- `asgard/src/wasmJsMain/kotlin/com/valhalla/asgard/AsgardBlur.wasmJs.kt` — `actual` blur no-op (if needed).
- `asgard/src/androidMain/AndroidManifest.xml` — minimal manifest for the android target.

**`demo` module (new):**
- `demo/build.gradle.kts` — CMP application, wasmJs.
- `demo/src/wasmJsMain/kotlin/com/valhalla/asgard/demo/Main.kt` — wasm entry point.
- `demo/src/wasmJsMain/resources/index.html` — page host.
- `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/ComponentEntry.kt` — catalog model.
- `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/AsgardCatalog.kt` — the component registry.
- `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/DemoTheme.kt` — theme controls (light/dark + seed).
- `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/GalleryApp.kt` — the gallery UI shell.
- `demo/src/commonTest/kotlin/com/valhalla/asgard/demo/AsgardCatalogTest.kt` — catalog sanity test.

**Root:**
- `settings.gradle.kts` — add `include(":demo")`.
- `gradle/libs.versions.toml` — add KMP/CMP versions + libs/plugins.
- `.github/workflows/deploy-demo.yml` — build + deploy to Pages.

---

### Task 1: Convert `asgard` build to KMP (Android parity preserved, empty commonMain)

Prove the toolchain compiles both targets **before** moving component source. Existing components temporarily live in `androidMain` so the Android build is unchanged; `commonMain` is empty and the wasm target compiles nothing.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `asgard/build.gradle.kts` (rewrite)
- Move (git mv): `asgard/src/main/java/**` → `asgard/src/androidMain/kotlin/**`
- Create: `asgard/src/androidMain/AndroidManifest.xml`

**Interfaces:**
- Produces: a KMP `asgard` module with `androidTarget()` + `wasmJs { browser() }`; catalog keys `libs.plugins.kotlinMultiplatform`, `libs.plugins.composeMultiplatform`, `libs.compose.material3`, `libs.compose.foundation`, `libs.compose.ui`, `libs.compose.runtime`, `libs.compose.materialIconsCore`.

- [ ] **Step 1: Add version-catalog entries.** In `gradle/libs.versions.toml`, under `[versions]` add (copy `composeMultiplatform` and `kotlin` values verbatim from `~/StudioProjects/MindLoom/gradle/libs.versions.toml`):

```toml
composeMultiplatform = "<copy from MindLoom>"
material3Cmp = "1.10.0-alpha05"
```

Under `[libraries]`:

```toml
compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "composeMultiplatform" }
compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "composeMultiplatform" }
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "composeMultiplatform" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "material3Cmp" }
compose-materialIconsCore = { module = "org.jetbrains.compose.material:material-icons-core", version = "1.7.3" }
```

Under `[plugins]`:

```toml
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
```

Set `kotlin = "2.3.20"` (the verified baseline) unless the chosen CMP lists 2.4.0 support.

- [ ] **Step 2: Rewrite `asgard/build.gradle.kts`** to KMP:

```kotlin
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
        publishLibraryVariants("release")
    }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            api(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

android {
    namespace = "com.valhalla.asgard"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
```

(Note: `compose.material3` etc. are provided by the `org.jetbrains.compose` plugin's `compose` DSL; the catalog libs from Step 1 are the fallback if the DSL accessor is unavailable. Keep the `mavenPublishing { … }` block from the current file but change `configure(AndroidSingleVariantLibrary(...))` to `configure(KotlinMultiplatform(...))` — see Task 4.)

- [ ] **Step 3: Move existing sources into the android source set.**

```bash
cd ~/StudioProjects/Asgard
mkdir -p asgard/src/androidMain/kotlin
git mv asgard/src/main/java/com/valhalla asgard/src/androidMain/kotlin/com/valhalla
```

- [ ] **Step 4: Create `asgard/src/androidMain/AndroidManifest.xml`:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **Step 5: Compile both targets.**

Run: `./gradlew :asgard:compileReleaseKotlin :asgard:compileKotlinWasmJs`
Expected: BUILD SUCCESSFUL. Android compiles the existing components (now in `androidMain`); wasm compiles the empty `commonMain`.

- [ ] **Step 6: Commit.**

```bash
git add gradle/libs.versions.toml asgard/build.gradle.kts asgard/src
git commit -m "build(asgard): convert to KMP (android + wasmJs), sources in androidMain"
```

---

### Task 2: Move components to `commonMain`; resolve wasm-incompatible APIs

**Files:**
- Move (git mv): `asgard/src/androidMain/kotlin/com/valhalla/**` → `asgard/src/commonMain/kotlin/com/valhalla/**`
- Create (only if Step 3 shows blur is unavailable in common): `asgard/src/commonMain/kotlin/com/valhalla/asgard/AsgardBlur.kt`, `.../androidMain/.../AsgardBlur.android.kt`, `.../wasmJsMain/.../AsgardBlur.wasmJs.kt`
- Modify: `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/AsgardProGate.kt` (only if using the blur expect/actual)

**Interfaces:**
- Produces: all `com.valhalla.asgard.*` components in `commonMain`, compiling on Android + wasm. If the blur fallback is needed, produces `expect fun Modifier.asgardContentBlur(radius: Dp): Modifier`.

- [ ] **Step 1: Move all sources to commonMain.**

```bash
cd ~/StudioProjects/Asgard
mkdir -p asgard/src/commonMain/kotlin
git mv asgard/src/androidMain/kotlin/com/valhalla asgard/src/commonMain/kotlin/com/valhalla
```

- [ ] **Step 2: Compile both targets to surface incompatibilities.**

Run: `./gradlew :asgard:compileKotlinWasmJs :asgard:compileReleaseKotlin`
Expected: Android passes. wasm MAY fail — capture every unresolved reference (likely only `androidx.compose.ui.draw.blur`). If wasm passes with no errors, skip Steps 3-4 and go to Step 5.

- [ ] **Step 3 (only if blur failed on wasm): Introduce a blur expect/actual.** Create `asgard/src/commonMain/kotlin/com/valhalla/asgard/AsgardBlur.kt`:

```kotlin
package com.valhalla.asgard

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** Content blur used by locked/gated overlays; a no-op on platforms without a blur effect. */
expect fun Modifier.asgardContentBlur(radius: Dp): Modifier
```

Create `asgard/src/androidMain/kotlin/com/valhalla/asgard/AsgardBlur.android.kt`:

```kotlin
package com.valhalla.asgard

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp

actual fun Modifier.asgardContentBlur(radius: Dp): Modifier = this.blur(radius)
```

Create `asgard/src/wasmJsMain/kotlin/com/valhalla/asgard/AsgardBlur.wasmJs.kt`:

```kotlin
package com.valhalla.asgard

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

actual fun Modifier.asgardContentBlur(radius: Dp): Modifier = this
```

- [ ] **Step 4 (only if Step 3 done): Use the expect fn in `AsgardProGate.kt`.** In `AsgardLockedOverlay`, replace `Modifier.blur(blurRadius)` with `Modifier.asgardContentBlur(blurRadius)` and remove the `import androidx.compose.ui.draw.blur`, adding `import com.valhalla.asgard.asgardContentBlur`.

- [ ] **Step 5: Re-compile both targets.**

Run: `./gradlew :asgard:compileKotlinWasmJs :asgard:compileReleaseKotlin`
Expected: BUILD SUCCESSFUL for both.

- [ ] **Step 6: Commit.**

```bash
git add asgard/src
git commit -m "refactor(asgard): move components to commonMain; wasm-safe blur fallback"
```

---

### Task 3: Switch publishing to KotlinMultiplatform (`asgard` + `asgard-android`)

**Files:**
- Modify: `asgard/build.gradle.kts` (`mavenPublishing` block)

**Interfaces:**
- Produces: Maven coordinates `com.valhalla:asgard` (root/metadata), `com.valhalla:asgard-android`, `com.valhalla:asgard-wasm-js`.

- [ ] **Step 1: Change the publish variant.** In `asgard/build.gradle.kts` `mavenPublishing { … }`, replace the `configure(AndroidSingleVariantLibrary(...))` call with:

```kotlin
    configure(
        KotlinMultiplatform(
            javadocJar = com.vanniktech.maven.publish.JavadocJar.Empty(),
            sourcesJar = true,
            androidVariantsToPublish = listOf("release"),
        )
    )
```

Keep `coordinates(groupId = …, artifactId = "asgard", version = …)`, `publishToMavenCentral(...)`, `signAllPublications()`, and the `pom { … }` block unchanged.

- [ ] **Step 2: Publish to Maven Local and inspect coordinates.**

Run: `./gradlew :asgard:publishToMavenLocal -PVERSION_NAME=0.0.0-SNAPSHOT`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Verify both coordinates exist.**

Run: `ls ~/.m2/repository/com/valhalla/ | sort`
Expected: directories `asgard`, `asgard-android`, and `asgard-wasm-js` are present.

- [ ] **Step 4: Commit.**

```bash
git add asgard/build.gradle.kts
git commit -m "build(asgard): publish as KotlinMultiplatform (asgard + asgard-android)"
```

---

### Task 4: Verify an Android-only consumer still resolves Asgard

Confirm the KMP change is transparent to existing Android consumers.

**Files:**
- Create (throwaway): `/tmp/asgard-consumer-check/` minimal Android-less resolution check via Gradle, OR use Thor on a scratch branch.

**Interfaces:**
- Consumes: `com.valhalla:asgard-android:0.0.0-SNAPSHOT` from Maven Local.

- [ ] **Step 1: Point a scratch consumer at Maven Local.** In `~/StudioProjects/Thor` on a throwaway branch, add `mavenLocal()` to the repositories and add `implementation("com.valhalla:asgard:0.0.0-SNAPSHOT")` (root coordinate) to `app/build.gradle.kts` dependencies (temporarily, alongside the existing Asgard dep if any — comment the existing one).

```bash
cd ~/StudioProjects/Thor && git checkout -b scratch/asgard-kmp-check
```

- [ ] **Step 2: Resolve + compile the consumer.**

Run: `./gradlew :app:compileFossDebugKotlin --refresh-dependencies`
Expected: BUILD SUCCESSFUL — Gradle metadata auto-selects the `asgard-android` variant and the app compiles against the same `androidx.compose.*` types.

- [ ] **Step 3: Discard the scratch branch (no commit to Thor).**

```bash
git checkout . && git checkout master && git branch -D scratch/asgard-kmp-check
```

- [ ] **Step 4: Record the result.** Append a line to `Asgard/COMPONENT_BACKLOG.md` under a new `## Verification log` heading: `- 2026-07-04: KMP publish verified — Thor (Android-only) resolves com.valhalla:asgard via metadata, compiles unchanged.` Commit in Asgard:

```bash
cd ~/StudioProjects/Asgard
git add COMPONENT_BACKLOG.md
git commit -m "docs: record Android-consumer resolution check for KMP asgard"
```

---

### Task 5: Scaffold the `demo` CMP wasmJs app (renders into the browser)

**Files:**
- Modify: `settings.gradle.kts`
- Create: `demo/build.gradle.kts`
- Create: `demo/src/wasmJsMain/kotlin/com/valhalla/asgard/demo/Main.kt`
- Create: `demo/src/wasmJsMain/resources/index.html`

**Interfaces:**
- Produces: runnable target `:demo:wasmJsBrowserRun` and distribution `:demo:wasmJsBrowserDistribution`.

- [ ] **Step 1: Register the module.** In root `settings.gradle.kts`, add after the existing `include`s:

```kotlin
include(":demo")
```

- [ ] **Step 2: Create `demo/build.gradle.kts`:**

```kotlin
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":asgard"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.compose.materialIconsExtended)
        }
    }
}
```

Add to `gradle/libs.versions.toml` `[libraries]`: `compose-materialIconsExtended = { module = "org.jetbrains.compose.material:material-icons-extended", version = "1.7.3" }`.

- [ ] **Step 3: Create `demo/src/wasmJsMain/resources/index.html`:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Asgard Components</title>
    <style>html,body{margin:0;height:100%}</style>
</head>
<body>
    <canvas id="ComposeTarget"></canvas>
    <script src="demo.js"></script>
</body>
</html>
```

- [ ] **Step 4: Create `demo/src/wasmJsMain/kotlin/com/valhalla/asgard/demo/Main.kt`:**

```kotlin
package com.valhalla.asgard.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MaterialTheme {
            Text("Asgard demo — hello")
        }
    }
}
```

- [ ] **Step 5: Build and run locally to confirm it mounts.**

Run: `./gradlew :demo:wasmJsBrowserDevelopmentExecutableDistribution`
Expected: BUILD SUCCESSFUL producing `demo/build/dist/wasmJs/developmentExecutable/`. (Optionally `./gradlew :demo:wasmJsBrowserRun` opens it at `http://localhost:8080` showing "Asgard demo — hello".)

- [ ] **Step 6: Commit.**

```bash
git add settings.gradle.kts demo gradle/libs.versions.toml
git commit -m "feat(demo): scaffold Compose/WASM demo app"
```

---

### Task 6: `ComponentEntry` model + `AsgardCatalog` registry + sanity test

**Files:**
- Create: `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/ComponentEntry.kt`
- Create: `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/AsgardCatalog.kt`
- Create: `demo/src/commonTest/kotlin/com/valhalla/asgard/demo/AsgardCatalogTest.kt`
- Modify: `demo/build.gradle.kts` (add `commonTest` deps)

**Interfaces:**
- Produces: `data class ComponentEntry(name: String, category: String, description: String, code: String, content: @Composable () -> Unit)` and `val asgardCatalog: List<ComponentEntry>`.

- [ ] **Step 1: Add the kotlin-test dependency.** In `demo/build.gradle.kts` `sourceSets`, add:

```kotlin
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
```

- [ ] **Step 2: Write the failing catalog test.** Create `demo/src/commonTest/kotlin/com/valhalla/asgard/demo/AsgardCatalogTest.kt`:

```kotlin
package com.valhalla.asgard.demo

import kotlin.test.Test
import kotlin.test.assertTrue

class AsgardCatalogTest {
    @Test fun catalogIsNonEmpty() = assertTrue(asgardCatalog.isNotEmpty())

    @Test fun entryNamesAreUnique() {
        val names = asgardCatalog.map { it.name }
        assertTrue(names.size == names.toSet().size, "duplicate component names: $names")
    }

    @Test fun everyEntryHasCodeAndDescription() = assertTrue(
        asgardCatalog.all { it.name.isNotBlank() && it.code.isNotBlank() && it.description.isNotBlank() }
    )
}
```

- [ ] **Step 3: Run it to confirm it fails.**

Run: `./gradlew :demo:wasmJsTest`
Expected: FAIL / unresolved reference `asgardCatalog` (not defined yet).

- [ ] **Step 4: Create `ComponentEntry.kt`:**

```kotlin
package com.valhalla.asgard.demo

import androidx.compose.runtime.Composable

/** One showcase entry: metadata + a live preview + the usage snippet. */
data class ComponentEntry(
    val name: String,
    val category: String,
    val description: String,
    val code: String,
    val content: @Composable () -> Unit,
)
```

- [ ] **Step 5: Create `AsgardCatalog.kt` with two seed entries** (full population is Task 8):

```kotlin
package com.valhalla.asgard.demo

import androidx.compose.material3.Text
import com.valhalla.asgard.components.AsgardBadge
import com.valhalla.asgard.components.StatusChip

val asgardCatalog: List<ComponentEntry> = listOf(
    ComponentEntry(
        name = "StatusChip",
        category = "Chips & badges",
        description = "A compact, pill-shaped status label that inherits the theme.",
        code = "StatusChip(text = \"Frozen\")",
        content = { StatusChip(text = "Frozen") },
    ),
    ComponentEntry(
        name = "AsgardBadge",
        category = "Chips & badges",
        description = "A neutral label pill with optional icon; filled or outlined.",
        code = "AsgardBadge(text = \"PRO\")",
        content = { AsgardBadge(text = "PRO") },
    ),
)
```

- [ ] **Step 6: Run the test to confirm it passes.**

Run: `./gradlew :demo:wasmJsTest`
Expected: PASS.

- [ ] **Step 7: Commit.**

```bash
git add demo
git commit -m "feat(demo): ComponentEntry model + AsgardCatalog + sanity test"
```

---

### Task 7: `DemoTheme` controls + `GalleryApp` shell

**Files:**
- Create: `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/DemoTheme.kt`
- Create: `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/GalleryApp.kt`
- Modify: `demo/src/wasmJsMain/kotlin/com/valhalla/asgard/demo/Main.kt`

**Interfaces:**
- Consumes: `asgardCatalog: List<ComponentEntry>`.
- Produces: `@Composable fun GalleryApp()`; `@Composable fun DemoTheme(dark: Boolean, seed: Color, content: @Composable () -> Unit)`.

- [ ] **Step 1: Create `DemoTheme.kt`** (seed-based scheme so components visibly re-tint):

```kotlin
package com.valhalla.asgard.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Seed colors offered in the demo toolbar. */
val demoSeeds: List<Pair<String, Color>> = listOf(
    "Indigo" to Color(0xFF5B5BD6),
    "Teal" to Color(0xFF0E7C7B),
    "Amber" to Color(0xFFB26A00),
    "Rose" to Color(0xFFB3315F),
)

@Composable
fun DemoTheme(dark: Boolean, seed: Color, content: @Composable () -> Unit) {
    val scheme = if (dark) {
        darkColorScheme(primary = seed, secondary = seed, tertiary = seed)
    } else {
        lightColorScheme(primary = seed, secondary = seed, tertiary = seed)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
```

- [ ] **Step 2: Create `GalleryApp.kt`** (top bar with toggles; left nav list; detail pane with live preview + code):

```kotlin
package com.valhalla.asgard.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun GalleryApp() {
    var dark by remember { mutableStateOf(false) }
    var seed by remember { mutableStateOf(demoSeeds.first().second) }
    var selected by remember { mutableStateOf(asgardCatalog.first()) }

    DemoTheme(dark = dark, seed = seed) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Asgard", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    demoSeeds.forEach { (label, color) ->
                        FilterChip(selected = seed == color, onClick = { seed = color }, label = { Text(label) })
                    }
                    Text(if (dark) "Dark" else "Light")
                    Switch(checked = dark, onCheckedChange = { dark = it })
                }
                HorizontalDivider()
                Row(Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.width(240.dp).fillMaxHeight()) {
                        items(asgardCatalog) { entry ->
                            Text(
                                text = entry.name,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { selected = entry }
                                    .background(
                                        if (entry == selected) MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(16.dp),
                            )
                        }
                    }
                    HorizontalDivider(Modifier.fillMaxHeight().width(1.dp))
                    DetailPane(selected)
                }
            }
        }
    }
}

@Composable
private fun RowScopeDummy() {}

@Composable
private fun DetailPane(entry: ComponentEntry) {
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text(entry.name, style = MaterialTheme.typography.headlineSmall)
        Text(entry.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.padding(vertical = 24.dp)) { entry.content() }
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(12.dp)) {
            Text(entry.code, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp))
        }
    }
}
```

(Note: remove the unused `RowScopeDummy`/`clip` if the compiler flags them; `fillMaxWidth` import — add `import androidx.compose.foundation.layout.fillMaxWidth`.)

- [ ] **Step 3: Point `Main.kt` at `GalleryApp`.** Replace the `MaterialTheme { Text(...) }` body with `GalleryApp()` and drop the now-unused `MaterialTheme`/`Text` imports.

- [ ] **Step 4: Build the demo.**

Run: `./gradlew :demo:wasmJsBrowserDevelopmentExecutableDistribution`
Expected: BUILD SUCCESSFUL. Manually load the output (or `:demo:wasmJsBrowserRun`) and confirm: the nav lists both seed entries, clicking switches the detail preview, and toggling dark / seed re-tints the live component.

- [ ] **Step 5: Commit.**

```bash
git add demo
git commit -m "feat(demo): theme controls + gallery shell (nav + live detail + snippet)"
```

---

### Task 8: Populate the catalog with all components

**Files:**
- Modify: `demo/src/commonMain/kotlin/com/valhalla/asgard/demo/AsgardCatalog.kt`

**Interfaces:**
- Consumes: every public component in `com.valhalla.asgard.*`.

- [ ] **Step 1: Add one `ComponentEntry` per public component.** Extend `asgardCatalog` with entries for: `AsgardHeader`, `AsgardActionItem`, `AsgardNavigationBar`/`Rail` (shown static), `ConnectedButtonGroup`, `AsgardStatTile`, `AsgardBanner`, `AsgardListRow`, `AsgardSectionCard`, `AsgardSettingRow`, `AsgardSettingToggleRow`, `AsgardProBadge`, `AsgardUpgradeCard`, `AsgardLockedOverlay`, `AsgardProgressRing`, `AsgardEmptyState`, `AsgardLoadingState`, `AsgardFeatureRow`, `AsgardOnboardingScaffold`, `AsgardAnimatedNumeral`, `AsgardDialogScaffold` (shown via a "Show dialog" button), `AsgardStepperRow`, `AsgardSearchBar`, `AsgardLabeledSlider`, `AsgardShimmer`, `AsgardTonalIconButton`. Use `material-icons-extended` vectors (e.g. `Icons.Rounded.Bolt`) for icon params. Each entry: real `content` lambda + a copy-paste `code` string. Stateful previews (`AsgardSettingToggleRow`, `AsgardStepperRow`, `AsgardSearchBar`, `AsgardLabeledSlider`) hold local `remember { mutableStateOf(...) }` inside `content`.

Example additions (pattern to follow for all):

```kotlin
    ComponentEntry(
        name = "AsgardStatTile",
        category = "Data display",
        description = "Compact metric tile: label over an emphasized value, optional icon/status dot.",
        code = "AsgardStatTile(label = \"Uptime\", value = \"12h 30m\")",
        content = { AsgardStatTile(label = "Uptime", value = "12h 30m") },
    ),
    ComponentEntry(
        name = "AsgardSettingToggleRow",
        category = "Settings",
        description = "A settings row with a trailing switch.",
        code = "var on by remember { mutableStateOf(true) }\nAsgardSettingToggleRow(\"Wi-Fi only\", on) { on = it }",
        content = {
            var on by remember { mutableStateOf(true) }
            AsgardSettingToggleRow(title = "Wi-Fi only", checked = on, onCheckedChange = { on = it })
        },
    ),
```

- [ ] **Step 2: Update the catalog test's Android-parity awareness.** No test code change needed; `AsgardCatalogTest` already enforces non-empty, unique names, non-blank code/description across the fuller list.

- [ ] **Step 3: Run the catalog test.**

Run: `./gradlew :demo:wasmJsTest`
Expected: PASS (unique names, all fields populated).

- [ ] **Step 4: Build + eyeball every entry.**

Run: `./gradlew :demo:wasmJsBrowserDevelopmentExecutableDistribution`
Expected: BUILD SUCCESSFUL. Load it and click through every nav item in both light and dark, confirming each renders and re-tints.

- [ ] **Step 5: Commit.**

```bash
git add demo
git commit -m "feat(demo): populate catalog with all Asgard components"
```

---

### Task 9: Deploy the demo to GitHub Pages

**Files:**
- Create: `.github/workflows/deploy-demo.yml`

**Interfaces:**
- Consumes: `:demo:wasmJsBrowserDistribution` → `demo/build/dist/wasmJs/productionExecutable`.

- [ ] **Step 1: Create the workflow** `.github/workflows/deploy-demo.yml`:

```yaml
name: Deploy Demo
on:
  push:
    branches: [ "main" ]
  workflow_dispatch:
permissions:
  contents: read
  pages: write
  id-token: write
concurrency:
  group: pages
  cancel-in-progress: true
jobs:
  build-deploy:
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '21'
      - name: Build wasm distribution
        run: ./gradlew :demo:wasmJsBrowserDistribution --no-daemon
      - uses: actions/upload-pages-artifact@v3
        with:
          path: demo/build/dist/wasmJs/productionExecutable
      - id: deployment
        uses: actions/deploy-pages@v4
```

- [ ] **Step 2: Verify the production bundle builds locally** (matches what CI runs).

Run: `./gradlew :demo:wasmJsBrowserDistribution`
Expected: BUILD SUCCESSFUL producing `demo/build/dist/wasmJs/productionExecutable/index.html` + `demo.wasm` + `demo.js`.

- [ ] **Step 3: Commit.**

```bash
git add .github/workflows/deploy-demo.yml
git commit -m "ci(demo): deploy Compose/WASM component gallery to GitHub Pages"
```

- [ ] **Step 4: Enable Pages (manual, one-time).** In the GitHub repo settings → Pages → Source: "GitHub Actions". Note in the PR description that Pages must be enabled once. After merge to `main`, confirm the published URL renders the gallery.

---

## Self-Review

**Spec coverage:**
- KMP conversion (androidTarget + wasmJs, sources→commonMain, deps swap) → Tasks 1-2. ✅
- `Modifier.blur` on wasm (expect/actual fallback) → Task 2 Steps 3-4. ✅
- Dual-coordinate publishing (`asgard` + `asgard-android`) → Task 3. ✅
- Android-consumer resolution validation → Task 4. ✅
- Demo module (gallery, live previews, light/dark + seed toggles, snippets) → Tasks 5-8. ✅
- Hosting (Pages via Actions, `wasmJsBrowserDistribution`) → Task 9. ✅
- Kotlin/CMP version alignment → Task 1 Step 1 (baseline pinned to MindLoom's verified versions). ✅
- Every public component in the gallery → Task 8. ✅

**Placeholder scan:** No "TBD"/"handle edge cases"; the two version values sourced "from MindLoom" are exact copy instructions (a real file), not placeholders. Blur steps are conditioned on an observed compile result, with full code provided for both branches.

**Type consistency:** `ComponentEntry(name, category, description, code, content)` is used identically in Tasks 6-8. `asgardCatalog: List<ComponentEntry>` consumed in Tasks 6-8. `DemoTheme(dark, seed, content)` defined in Task 7 Step 1, used in Task 7 Step 2. `asgardContentBlur(radius: Dp)` defined and consumed in Task 2.

## Notes / decisions deferred to the implementer
- Exact `composeMultiplatform` plugin version: copy MindLoom's verified value; only raise Kotlin to 2.4.0 if that CMP release supports it.
- If the `compose.*` Gradle DSL accessors (`compose.material3`, `compose.components.resources`) are unavailable, use the catalog `libs.compose.*` entries added in Task 1/5 instead.
