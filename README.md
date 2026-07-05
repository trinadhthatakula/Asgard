<div align="center">

# ⚡ Asgard

**A theme-agnostic Jetpack Compose component library, built on Expressive Material 3.**

[![Maven Central](https://img.shields.io/maven-central/v/com.trinadhthatakula/asgard?style=flat-square&label=Maven%20Central&color=4c1)](https://central.sonatype.com/artifact/com.trinadhthatakula/asgard)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20wasmJs-0A7EA4?style=flat-square)

**[▶ Live gallery](https://trinadhthatakula.github.io/Asgard/)** &nbsp;·&nbsp; [Migrating from 1.x → 2.0](MIGRATION.md)

</div>

---

Asgard ships the **structure and motion** of a polished UI — a signature header, expressive
navigation, connected segmented toggles, stat tiles, banners, charts, and tactile press animations —
while reading **all** colors, typography, and shapes from the **host app's `MaterialTheme`**. The
same component renders as "Thor" in one app and "Loki" in another; the library never imposes a
palette or font.

> Born from the [Thor](https://github.com/trinadhthatakula/Thor) app's design system, extracted so
> every app — yours included — can share one component layer.

## Why Asgard?

- 🎨 **Theme-agnostic by design** — every color, type, shape and motion value comes from *your*
  `MaterialTheme`. Drop it into any app and it looks like *that* app.
- 🧩 **~30 components + 5 charts** — headers, navigation, inputs, stat cards, banners, settings rows,
  a pro-gate kit, progress rings, charts, and more.
- 🛠️ **Deeply customizable** — flat params for text truncation, typography, color, shape, padding,
  and accessibility on every component — with defaults that just work.
- 🪶 **Featherweight** — pure Compose, **no icon pack** and **no third-party** dependencies.
- 🌐 **Kotlin Multiplatform** — one artifact for **Android** and **wasmJs**.

## ▶ Live gallery

**[trinadhthatakula.github.io/Asgard](https://trinadhthatakula.github.io/Asgard/)** — browse every
component and chart live, drag the interactive knobs, and flip **light/dark** and the **seed color**
to watch each one inherit the theme. The gallery is itself built from Asgard, running in the browser
via Compose Multiplatform (wasm).

## 📦 Install

Asgard is on Maven Central. Add the dependency — Gradle resolves the correct platform artifact
automatically:

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.trinadhthatakula:asgard:2.0.0")
}
```

**Artifacts.** Asgard is a Kotlin Multiplatform library published as **three** Maven Central
coordinates. Depend on the umbrella `asgard` and Gradle resolves the right one per target — or depend
on a single platform artifact directly if you only want one:

| Coordinate | Platform | Format |
|---|---|---|
| `com.trinadhthatakula:asgard` | **all** — auto-resolves the variant *(recommended)* | Gradle Module Metadata |
| `com.trinadhthatakula:asgard-android` | Android only | `.aar` |
| `com.trinadhthatakula:asgard-wasm-js` | wasmJs only | `.klib` |

<details>
<summary><b>Kotlin Multiplatform</b> (shared module)</summary>

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.trinadhthatakula:asgard:2.0.0")
        }
    }
}
```
Asgard targets **Android** and **wasmJs** from `commonMain`; an Android project transparently gets
the `asgard-android` variant from the same coordinate.
</details>

<details>
<summary><b>Building a Thor extension?</b></summary>

If the host app already bundles Asgard, depend on it as `compileOnly` so it isn't duplicated in your
extension APK — the host provides it at runtime:

```kotlin
compileOnly("com.trinadhthatakula:asgard:2.0.0")
```
Asgard shares the `com.trinadhthatakula` namespace with the
[`thor-extension-api`](https://github.com/trinadhthatakula/Thor-extension-api) contract; both are
provided by the Thor host at runtime.
</details>

> **Upgrading from 1.x?** See the **[Migration guide](MIGRATION.md)** — 2.0 is mostly additive with
> a few small breaking changes (usually a 0–2 line change).

## 🚀 Quick start

Wrap your app in any `MaterialTheme` (Asgard inherits it) and compose:

```kotlin
val tabs = listOf(
    AsgardNavItem(icon = Icons.Rounded.Apps, label = "Apps"),
    AsgardNavItem(icon = Icons.Rounded.AcUnit, label = "Freezer"),
)
var selected by remember { mutableIntStateOf(0) }

MaterialExpressiveTheme { // or your own MaterialTheme — Asgard follows it
    Scaffold(
        bottomBar = {
            AsgardNavigationBar(items = tabs, selectedIndex = selected, onSelect = { selected = it })
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            AsgardHeader(
                title = "Apps",
                icon = Icons.Rounded.Dashboard,
                actions = {
                    ConnectedButtonGroup(
                        items = listOf(
                            ConnectedButtonGroupItem.Icon(Icons.Rounded.GridView, "Grid"),
                            ConnectedButtonGroupItem.Icon(Icons.AutoMirrored.Rounded.List, "List"),
                        ),
                        selectedIndex = 0,
                        onItemSelected = { /* … */ },
                    )
                },
            )

            AsgardStatCard(
                label = "Battery",
                value = "82",
                unit = "%",
                icon = Icons.Rounded.Bolt,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
```

> All icons are supplied by **you** as `ImageVector` — Asgard bundles no icon pack. The only built-in
> glyph is `AsgardHeader`'s back arrow, a self-contained vector, so the library forces **no**
> `material-icons` artifact onto your app.

## 🧩 Component catalog

Every component reads color/type/shape from the ambient `MaterialTheme`, takes a `modifier`, and
exposes flat customization params. Browse them all — live — in the
**[gallery](https://trinadhthatakula.github.io/Asgard/)**.

**Structure & navigation**
| Component | What it is |
|---|---|
| `AsgardHeader` | Signature screen header: optional brand icon / back button + heavy primary-tinted title + trailing actions. No `TopAppBar`. |
| `AsgardNavigationBar` · `AsgardNavigationRail` | Expressive bottom bar / side rail with an animated selected pill and label reveal. Driven by `AsgardNavItem` (with optional `badge`). |
| `AsgardSectionCard` | Titled, rounded tonal surface that groups related rows. |

**Inputs & actions**
| Component | What it is |
|---|---|
| `ConnectedButtonGroup` | Single-select connected segmented control; crash-safe at any font scale. Items via `ConnectedButtonGroupItem` (`Label` / `Icon` / `IconWithLabel`). |
| `AsgardLabeledSlider` | Label + formatted value over an M3 `Slider`. |
| `AsgardStepperRow` | Labeled numeric stepper with decrement/increment. |
| `AsgardSearchBar` | Single-line search field with an auto-managed clear button. |
| `AsgardTonalIconButton` | Thin tonal icon-button wrapper. |
| `AsgardActionItem` | Vertical icon-chip + label action tile with press feedback. |

**Data display**
| Component | What it is |
|---|---|
| `AsgardStatCard` | Vertical metric card (icon-above-value or inline "bento"), optional unit + click. |
| `AsgardStatTile` | Compact metric tile: label + value, status dot, icon badge, secondary value, count-up. |
| `AsgardAnimatedNumeral` | Numeric text that slides/counts on change (odometer style). |
| `AsgardListRow` · `AsgardFeatureRow` | Generic list row (icon/title/subtitle/trailing) and onboarding feature row. |
| `AsgardBadge` · `StatusChip` | Label pills — filled/outlined badge, and a compact status chip. |
| `AsgardProgressRing` | Circular progress / gauge ring with a centered content slot. |

**Feedback & states**
| Component | What it is |
|---|---|
| `AsgardBanner` | Full-width tonal callout: icon + title + description + optional action. |
| `AsgardEmptyState` · `AsgardLoadingState` | Centered empty / loading placeholders. |
| `AsgardShimmer` | Skeleton shimmer placeholder. |
| `AsgardDialogScaffold` | Thin M3 `AlertDialog` wrapper for confirm/dismiss dialogs. |

**Settings & monetization**
| Component | What it is |
|---|---|
| `AsgardSettingRow` · `AsgardSettingToggleRow` | Preference rows — value/navigation and a trailing switch. |
| `AsgardProBadge` · `AsgardUpgradeCard` · `AsgardLockedOverlay` | A pro-gate kit: a "PRO" pill, an upgrade card, and a blur-and-scrim content gate. |
| `AsgardOnboardingScaffold` | Onboarding page scaffold: title, subtitle, content, actions. |

**Motion**
| API | What it is |
|---|---|
| `Modifier.expressivePress()` | Tactile squish-on-press, sourced from `MaterialTheme.motionScheme`. |
| `Modifier.animateExpressiveResize()` | Spring resize using the host motion scheme. |

## 📈 Charts

A pure-`Canvas` charting set (no extra dependencies) in `com.valhalla.asgard.charts`, theme-agnostic
like everything else:

| Chart | What it draws |
|---|---|
| `AsgardLineChart` | Multi-series smoothed (or straight) line + gradient area, auto Y-scaling, grid, axis labels, end markers. Data via `AsgardChartPoint` / `AsgardLineSeries`. |
| `AsgardStackedBarChart` | N-segment stacked bars with a shared scale and dimmed "partial" bars (`AsgardBarStack`). |
| `AsgardTimelineBar` | Colored interval spans across a time window (`AsgardTimelineSegment`). |
| `AsgardChartLegend` | Wrapping swatch + label key shared by every chart (`AsgardLegendEntry`). |
| `AsgardPulseRing` | Decorative expanding-ring attention pulse behind any content. |

```kotlin
AsgardLineChart(
    series = listOf(
        AsgardLineSeries(
            points = prices.mapIndexed { i, p -> AsgardChartPoint(i.toFloat(), p.value, p.date) },
            color = MaterialTheme.colorScheme.primary,
        ),
    ),
    yValueFormatter = { "₹${it.roundToInt()}" },
    modifier = Modifier.fillMaxWidth().height(220.dp),
)
```

## 🎨 Theming & customization

**Nothing is hardcoded.** Asgard resolves color, typography, shape and motion from the host
`MaterialTheme`, so it adopts your brand automatically:

```kotlin
MaterialExpressiveTheme(colorScheme = myBrandScheme, shapes = myShapes) {
    // every Asgard component now uses myBrandScheme + myShapes
}
```

- **Shapes** flow through `AsgardDefaults`, which reads `MaterialTheme.shapes` — override
  `MaterialTheme.shapes` and Asgard reshapes with your app. Any component also accepts a per-call
  `shape`.
- **Customization params** are flat and discoverable. Constrain text, restyle, recolor, repad, or
  add accessibility — all optional, all defaulted to sensible values:

```kotlin
AsgardActionItem(
    icon = Icons.Rounded.Notifications,
    label = "Notification settings",
    onClick = { /* … */ },
    labelMaxLines = 1,                                  // constrain text
    labelStyle = MaterialTheme.typography.titleSmall,   // restyle
    shape = RoundedCornerShape(12.dp),                  // reshape
)
```

The **Expressive** Material 3 theme gives the richest motion; any `MaterialTheme` works.

## ✅ Requirements

- **Kotlin Multiplatform** — Android (`minSdk` 28) + wasmJs, JDK 21
- **Compose Multiplatform** with **Material 3**. Asgard uses a few Material 3 *Expressive* APIs
  internally; wrapping your app in `MaterialExpressiveTheme` gives the richest motion, but any
  `MaterialTheme` works.

## 🤖 For AI / agent integration

An integration skill for coding agents lives at
[`.claude/skills/asgard-ui/SKILL.md`](.claude/skills/asgard-ui/SKILL.md) — it teaches an agent how to
add the dependency and use Asgard's components and charts correctly in a KMP or Android project.

## 🛠️ Publishing (maintainer)

Releases are automated: bump `VERSION_NAME` in `gradle.properties` on `main` and the
**Publish to Maven Central** workflow publishes via the Vanniktech plugin, then tags + creates a
GitHub Release. Credentials live in repository secrets. To publish manually on **JDK 21**:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew publishToMavenCentral
```

Local dry-run without a GPG key: `./gradlew publishToMavenLocal -PVERSION_NAME=2.0.1-SNAPSHOT`.

## 📄 License

[Apache License 2.0](LICENSE).
