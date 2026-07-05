---
name: asgard-ui
description: >-
  Integrate the Asgard Compose UI library (theme-agnostic components + charts, built on Expressive
  Material 3) into a Kotlin Multiplatform or Android Jetpack Compose project. Use when a task asks to
  add/build UI with Asgard, use com.trinadhthatakula:asgard, or reach for headers, navigation bars,
  stat cards/tiles, banners, settings rows, a pro-gate, progress rings, or charts (line / stacked-bar
  / timeline / legend / pulse) that inherit the app's MaterialTheme. Covers dependency setup, the
  component + chart API, theming, and customization params. Targets Asgard 2.0.x.
---

# Asgard UI integration

Asgard is a **theme-agnostic** Compose component library on **Expressive Material 3**. It provides
the *structure and motion* of a UI; it reads **all** color, typography, shape, and motion from the
host `MaterialTheme`. Never hardcode brand colors into Asgard call sites — theme the app instead.

Coordinate: `com.trinadhthatakula:asgard` · package `com.valhalla.asgard` · Maven Central.
Targets: **Android** (`minSdk` 28) + **wasmJs**, JDK 21.

## 1. Add the dependency

Ensure `mavenCentral()` is in the repositories, then:

```kotlin
// Android app — build.gradle.kts
dependencies { implementation("com.trinadhthatakula:asgard:2.0.0") }

// Kotlin Multiplatform — build.gradle.kts
kotlin { sourceSets { commonMain.dependencies { implementation("com.trinadhthatakula:asgard:2.0.0") } } }
```

Use the latest 2.0.x. **Always depend on the umbrella coordinate `com.trinadhthatakula:asgard`** — in
both Android and KMP modules. Asgard is a Kotlin Multiplatform library published as three artifacts:

| Coordinate | Contents | When to use |
|---|---|---|
| `com.trinadhthatakula:asgard` | KMP root + Gradle Module Metadata (`.module`) | **This is the one you depend on.** Gradle reads the metadata and resolves the right platform variant automatically. |
| `com.trinadhthatakula:asgard-android` | Android library (`.aar`) | The variant Gradle resolves for Android. Reference it directly only if a non-KMP-metadata-aware setup can't resolve the umbrella coordinate. |
| `com.trinadhthatakula:asgard-wasm-js` | wasmJs library (`.klib`) | The variant Gradle resolves for wasmJs. Reference it directly only for a wasm-only pin. |

So `implementation("com.trinadhthatakula:asgard:2.0.0")` is correct everywhere — Gradle picks
`asgard-android` or `asgard-wasm-js` per target via Gradle Module Metadata (enabled by default).
Asgard already exposes `compose.ui` / `compose.foundation` / `compose.material3` as `api`, so their
types are available transitively.

## 2. Core rules

- **Theme, don't hardcode.** Wrap the app in `MaterialExpressiveTheme { }` (richest motion) or any
  `MaterialTheme`. To brand it, pass your `colorScheme` / `typography` / `shapes` to the theme —
  every Asgard component follows them.
- **Icons are yours.** Every icon is an `ImageVector` you pass in. Asgard bundles **no** icon pack;
  add `androidx.compose.material:material-icons-extended` (or your own vectors) if you need icons.
- **Every component** takes a `modifier: Modifier = Modifier` (second param, after content) and a set
  of optional flat customization params (text `maxLines`/`overflow`/`style`/color, `shape`,
  `contentPadding`, `contentDescription`, …). Defaults are sensible — only pass what you need.
- **Opt-ins are handled inside the library**; consumers do not need `@OptIn` for Asgard components.

## 3. Component reference (2.0)

Condensed public signatures (required params first, then key optionals). All are `@Composable`.

**Header & navigation**
```kotlin
AsgardHeader(title: String, modifier, icon: ImageVector? = null, onNavigateBack: (() -> Unit)? = null,
    titleMaxLines: Int = 1, titleStyle: TextStyle? = null, titleColor: Color? = null,
    leading: (@Composable () -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {})

AsgardNavItem(icon: ImageVector, label: String, selectedIcon: ImageVector? = null,
    contentDescription: String? = null, badge: String? = null)   // data class
AsgardNavigationBar(items: List<AsgardNavItem>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier,
    showLabel: Boolean = true, containerColor, selectedIndicatorColor, selectedContentColor,
    unselectedContentColor, unselectedAlpha: Float = 0.7f, shape: Shape? = null, labelStyle: TextStyle? = null)
AsgardNavigationRail(/* same params */, header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null)
```

**Inputs & actions**
```kotlin
ConnectedButtonGroup(items: List<ConnectedButtonGroupItem>, selectedIndex: Int,
    onItemSelected: (Int) -> Unit, modifier, enabled: Boolean = true, colors: ToggleButtonColors? = null,
    labelMaxLines: Int = 1)
// items: ConnectedButtonGroupItem.Label(text), .Icon(icon, contentDescription),
//        .IconWithLabel(icon, contentDescription, text)   — each also has `enabled: Boolean = true`
AsgardLabeledSlider(label: String, value: Float, onValueChange: (Float) -> Unit, modifier,
    valueRange = 0f..1f, steps: Int = 0, valueLabel: String? = null, enabled: Boolean = true)
AsgardStepperRow(label, value: String, decrementIcon, incrementIcon, onDecrement, onIncrement, modifier,
    canDecrement = true, canIncrement = true)
AsgardSearchBar(query: String, onQueryChange: (String) -> Unit, modifier, placeholder = "Search",
    leadingIcon: ImageVector? = null, onSearch: (() -> Unit)? = null, enabled = true)
AsgardTonalIconButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier, enabled = true)
AsgardActionItem(icon: ImageVector, label: String, onClick: () -> Unit, modifier, enabled = true,
    labelMaxLines: Int = 2, iconTint, containerColor)
```

**Data display**
```kotlin
AsgardStatCard(label: String, value: String, modifier, icon: ImageVector? = null, iconTint,
    iconInlineWithLabel: Boolean = false, unit: String? = null, onClick: (() -> Unit)? = null,
    labelMaxLines = 1, valueMaxLines = 1, unitSeparator: String = " ")   // NOTE: iconTint (was iconColor in 1.x)
AsgardStatTile(label: String, value: String, modifier, icon: ImageVector? = null, iconTint: Color? = null,
    iconContainerColor: Color? = null, statusDotColor: Color? = null, secondaryValue: String? = null,
    animateValue: Boolean = false, valueFirst: Boolean = false, onClick: (() -> Unit)? = null, border: BorderStroke? = null)
AsgardAnimatedNumeral(value: String, modifier, style, color, countUp: Boolean = false, format: (Int) -> String = { it.toString() })
AsgardListRow(title: String, modifier, subtitle: String? = null, caption: String? = null,
    icon: ImageVector? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
    titleMaxLines = 1, subtitleMaxLines = 2)
AsgardBadge(text: String, modifier, icon: ImageVector? = null, outlined: Boolean = false, onClick: (() -> Unit)? = null)
StatusChip(text: String, modifier, containerColor, contentColor)
AsgardProgressRing(progress: Float, modifier, size = 220.dp, strokeWidth = 8.dp, progressColor, trackColor,
    animate: Boolean = false, contentDescription: String? = null, content: @Composable () -> Unit = {})
```

**Feedback, settings, pro-gate, onboarding**
```kotlin
AsgardBanner(title: String, modifier, description: String? = null, icon: ImageVector? = null,
    containerColor, action: (@Composable RowScope.() -> Unit)? = null)
AsgardEmptyState(text: String, modifier, icon: ImageVector? = null, description: String? = null, action: (@Composable () -> Unit)? = null)
AsgardLoadingState(modifier, text: String? = null)
AsgardShimmer(modifier, shape: Shape? = null, cornerRadius = 12.dp, animate: Boolean = true)
AsgardDialogScaffold(onDismissRequest, title: String, confirmText: String, onConfirm, modifier,
    text: String? = null, dismissText: String? = null, icon: ImageVector? = null)
AsgardSectionCard(title: String, modifier, content: @Composable ColumnScope.() -> Unit)
AsgardSettingRow(title: String, modifier, subtitle: String? = null, value: String? = null,
    icon: ImageVector? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null)
AsgardSettingToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier, subtitle: String? = null)
AsgardProBadge(text: String = "PRO", modifier, icon: ImageVector? = null, onClick: (() -> Unit)? = null, outlined: Boolean = false)
AsgardUpgradeCard(title: String, description: String, cta: String, onUpgrade: () -> Unit, modifier, leading: (@Composable () -> Unit)? = null)
AsgardLockedOverlay(locked: Boolean, overlay: @Composable () -> Unit, modifier, blurRadius = 12.dp, content: @Composable () -> Unit)
AsgardOnboardingScaffold(title: String, modifier, subtitle: String? = null, actions: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit)
```

**Motion (Modifier extensions)**
```kotlin
Modifier.expressivePress(interactionSource: InteractionSource, scaleOnPress: Float = 0.95f)  // squish on press
Modifier.animateExpressiveResize()                                                            // spring resize
```

## 4. Charts — `com.valhalla.asgard.charts`

```kotlin
data class AsgardChartPoint(val x: Float, val y: Float, val label: String? = null)
data class AsgardLineSeries(val points: List<AsgardChartPoint>, val color: Color, val lineWidth: Dp = 3.dp,
    val smoothing: AsgardLineSmoothing = AsgardLineSmoothing.Cubic, val areaFill: Brush? = null,
    val fillArea: Boolean = true, val showEndMarker: Boolean = true)
AsgardLineChart(series: List<AsgardLineSeries>, modifier, yRange: ClosedFloatingPointRange<Float>? = null,
    gridLineCount: Int = 4, yValueFormatter: (Float) -> String = { it.toString() }, xLabelFormatter: (AsgardChartPoint) -> String? = { it.label },
    maxXLabels: Int = 5, emptyContent: @Composable () -> Unit = {})

data class AsgardBarStack(val values: List<Float>, val dimmed: Boolean = false)
AsgardStackedBarChart(bars: List<AsgardBarStack>, segmentColors: List<Color>, modifier, maxTotal: Float? = null,
    barWidthFraction: Float = 0.6f, gridLineCount: Int = 4, dimmedAlpha: Float = 0.45f)

data class AsgardTimelineSegment(val startMillis: Long, val endMillis: Long, val color: Color)
AsgardTimelineBar(segments: List<AsgardTimelineSegment>, windowStartMillis: Long, windowEndMillis: Long, modifier, height = 10.dp)

data class AsgardLegendEntry(val label: String, val color: Color)
AsgardChartLegend(entries: List<AsgardLegendEntry>, modifier, swatch: AsgardLegendSwatch = Square)  // or .Dot

AsgardPulseRing(modifier, color, ringSize = 32.dp, durationMillis = 1200, pulsing: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {})   // decorative attention pulse
```

To map app data: build one `AsgardLineSeries` per line, each point `AsgardChartPoint(index or x, yValue, xAxisLabel)`;
supply `yValueFormatter` for the y-axis. Colors come from `MaterialTheme.colorScheme`.

## 5. Recipes

Always call Asgard components with **named arguments** (as below) — `modifier` is the 2nd parameter,
so positional calls easily misalign.

**Nav scaffold** — `Scaffold(bottomBar = { AsgardNavigationBar(items = tabs, selectedIndex = i, onSelect = { i = it }) })`;
put `AsgardHeader(title = "Apps", icon = Icons.Rounded.Dashboard, actions = { ConnectedButtonGroup(/* … */) })` at the top of the body.

**Metric row** — a `Row` of `AsgardStatCard(label = "Battery", value = "82", unit = "%", icon = icon, modifier = Modifier.weight(1f))`,
or `AsgardStatTile(label = "Uptime", value = "6h", statusDotColor = color, secondaryValue = "today")` for compact tiles.

**Pro-gate** — `AsgardLockedOverlay(locked = !isPro, overlay = { AsgardUpgradeCard(/* … */) }) { PremiumContent() }`.

**Chart card** — `AsgardSectionCard(title = "Trend") { AsgardLineChart(series = series, modifier = Modifier.fillMaxWidth().height(220.dp)); AsgardChartLegend(entries = entries) }`.

## 6. Gotchas

- **2.0 rename:** `AsgardStatCard` uses `iconTint` (not `iconColor` — that was 1.x). See `MIGRATION.md`.
- **Shapes are theme-driven** (from `MaterialTheme.shapes`); pass a component's `shape` param to pin one.
- Provide icons yourself; add `material-icons-extended` if you want the Material icon set.
- For a metric-heavy screen prefer `AsgardStatTile`; for a single big number use `AsgardStatCard`.
- Verify the exact current signatures against the sources under
  `com/valhalla/asgard/` if a param is uncertain — this is a condensed reference.
