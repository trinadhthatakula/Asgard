# Asgard Web Demo + Multiplatform Conversion — Design

**Date:** 2026-07-04
**Status:** approved direction, pending spec review
**Repo:** Asgard

## Goal

Give people a **live, interactive web page** where they can browse every Asgard
component, flip **light/dark** and **seed color** to see it inherit any theme, and copy the
usage snippet. Delivering this the "real" way (not screenshots) requires the components to run
in the browser, which means converting Asgard from an Android-only library into a **Kotlin
Multiplatform** library and adding a Compose/WASM **demo** app.

Secondary, deliberate benefit: a multiplatform Asgard becomes consumable by the developer's KMP
apps (e.g. MindLoom) — not just Android.

## Approach (chosen)

**Live demo via Compose Multiplatform (WASM/Canvas).** Rejected alternatives: a *static gallery*
(screenshots — not interactive, drifts from source) and *Compose HTML/DOM* (a different API that
cannot render real Compose UI components).

De-risking finding: Compose Multiplatform's `org.jetbrains.compose.material3` (MindLoom already
uses `1.10.0-alpha05`) includes the Expressive M3 APIs (`ButtonGroup`, `ToggleButton`,
`MotionScheme`) that `ConnectedButtonGroup` needs, **and** CMP ships the same `androidx.compose.*`
package names — so the component source moves to `commonMain` almost verbatim; the churn is in
`build.gradle`, not the `.kt` files.

## Architecture

### 1. `asgard` → Kotlin Multiplatform module

- Plugins: `org.jetbrains.kotlin.multiplatform`, `org.jetbrains.compose`,
  `org.jetbrains.kotlin.plugin.compose`, `com.android.library` (for the Android target).
- Targets (this phase): `androidTarget()` and `wasmJs { browser() }`. Desktop (`jvm`) and iOS are
  intentionally deferred (YAGNI) but the structure leaves room for them.
- Source layout: move `asgard/src/main/java/com/valhalla/asgard/**` →
  `asgard/src/commonMain/kotlin/com/valhalla/asgard/**`. Imports stay `androidx.compose.*`.
- Dependencies swap androidx artifacts → `org.jetbrains.compose.*`
  (`compose.runtime`, `compose.foundation`, `compose.material3`, `compose.ui`,
  `compose.material.material-icons-core`), keeping the current `api` exposure so consumers still
  get Compose transitively.
- **`Modifier.blur`** (used by `AsgardLockedOverlay`) is the one API to verify on `wasmJs`. If it
  is unsupported there, add a tiny `expect fun Modifier.asgardContentBlur(radius: Dp): Modifier`
  with an `androidMain` actual (real blur) and a `wasmJsMain` actual (no-op; the scrim already
  communicates "locked"). Everything else the 16 components use (Row/Column/Text/Icon/Surface/
  Slider/Switch/Canvas/AlertDialog/OutlinedTextField/IconButton/FilledTonalIconButton/
  CircularProgressIndicator/AnimatedContent/rememberInfiniteTransition/ButtonGroup) is in CMP
  common.

### 2. Publishing & consumption (both `asgard` and `asgard-android`)

Switch the vanniktech `mavenPublishing` config from `AndroidSingleVariantLibrary` to
`KotlinMultiplatform`. One publication produces, from one source:

| Coordinate | For whom | How they add it |
|---|---|---|
| `com.valhalla:asgard` | KMP consumers (and Android via metadata) | `implementation("com.valhalla:asgard:<v>")` |
| `com.valhalla:asgard-android` | Android-only consumers who want the concrete AAR | `implementation("com.valhalla:asgard-android:<v>")` |
| `com.valhalla:asgard-wasm-js` | web/wasm consumers | (used by the demo) |

The Android variant is compiled against androidx Compose (CMP's Android target *is* androidx
Compose, identical `androidx.compose.*` types), so it interoperates with any plain androidx-Compose
app exactly like today's AAR. No behavioral change for existing Android consumers; the dependency
line is unchanged.

### 3. `demo` module — the web gallery

A new Compose Multiplatform **application** module targeting `wasmJs { browser() }`, depending on
`asgard`. Structure for isolation:

- **`ComponentEntry`** — a small data model: `name`, `category`, `description`, a `content:
  @Composable () -> Unit` preview, and a `code: String` snippet.
- **`AsgardCatalog`** — a plain list of `ComponentEntry` (one per public component), the single
  source of truth for what the gallery shows.
- **`GalleryScreen`** — the UI shell: a left nav list of components (grouped by category) and a
  detail pane that renders the selected entry's live preview + code snippet. Responsive: nav
  collapses on narrow viewports.
- **`DemoThemeControls`** — top-bar toggles for **light/dark** and a small set of **seed colors**
  (built with `dynamicColorScheme`/`ColorScheme` from a seed) wrapping the whole app in a
  `MaterialTheme`. This is the money shot: the same component visibly re-tints with the theme,
  proving theme-agnosticism.
- App entry point uses `ComposeViewport`/`CanvasBasedWindow` (per current CMP) to mount into the
  page.

Every public component appears in the catalog: the 8 pre-existing (`StatusChip`,
`ConnectedButtonGroup`, `AsgardHeader`, `AsgardActionItem`, nav bar/rail, `AsgardNavItem`) and the
16 newly extracted (`AsgardStatTile`, `AsgardBanner`, `AsgardBadge`, `AsgardListRow`, the settings
kit, the pro-gate kit, `AsgardProgressRing`, `AsgardEmptyState`/`AsgardLoadingState`,
`AsgardFeatureRow`/`AsgardOnboardingScaffold`, `AsgardAnimatedNumeral`, `AsgardDialogScaffold`,
`AsgardStepperRow`, `AsgardSearchBar`, `AsgardLabeledSlider`, `AsgardShimmer`,
`AsgardTonalIconButton`). Components requiring icons are shown with `material-icons-extended`
vectors supplied by the demo (not by the library).

### 4. Hosting

GitHub Actions workflow: on push to the default branch, run
`./gradlew :demo:wasmJsBrowserDistribution`, then publish the
`demo/build/dist/wasmJs/productionExecutable` output via the GitHub Pages **Actions** flow
(`actions/upload-pages-artifact` + `actions/deploy-pages` — no `gh-pages` branch). The demo is a
static bundle (`.wasm` + `.js` + `index.html`), so Pages serves it directly.

## Data flow

`AsgardCatalog` (static list) → `GalleryScreen` renders nav from it → user selects an entry →
detail pane composes `entry.content()` inside the current `DemoThemeControls` `MaterialTheme` →
theme toggles recompose the preview live. No network, no backend, no persistence.

## Risks & validations

- **Kotlin/CMP version alignment.** Asgard is on Kotlin 2.4.0; the chosen CMP + `compose-plugin`
  versions must support Kotlin 2.4.0 and `wasmJs`. Pin these in the version catalog first and do a
  throwaway `:asgard:compileKotlinWasmJs` before migrating all sources.
- **`Modifier.blur` on wasm** — see the `expect/actual` fallback above.
- **Android consumer resolution** — after switching to KMP publishing, verify a sample Android app
  (e.g. Thor on a scratch branch) still resolves `com.valhalla:asgard` and compiles unchanged.
- **Compose version coupling** — the Android variant brings a specific (Expressive-alpha) material3
  transitively; this already happens today via `api(...)`, so no regression, but document the
  required Compose baseline in the README.

## Out of scope (YAGNI)

- Desktop (`jvm`) and iOS targets — add later if wanted.
- An interactive per-component **prop playground** (Storybook-style controls) — phase 2; the first
  version shows curated sample states + snippet.
- Auto-generating snippets from source — hand-write concise snippets in the catalog for now.

## Verification

- `asgard` builds for both targets: `:asgard:compileReleaseKotlin` (Android parity preserved) and
  `:asgard:compileKotlinWasmJs` (wasm target compiles).
- Demo builds and runs locally (`:demo:wasmJsBrowserRun`) and every catalog entry renders in
  light + dark and re-tints when the seed color changes.
- `:demo:wasmJsBrowserDistribution` produces the static bundle that CI deploys.
- A scratch Android consumer builds against the KMP-published artifact unchanged.
