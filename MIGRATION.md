# Migrating to Asgard 2.0

Asgard 2.0 is a **mostly additive** release. Every 1.x component still exists with the same
behavior; 2.0 adds a large set of customization parameters, a new charts package, and a handful of
bug fixes. There are **four small breaking changes**, all of which most apps are unaffected by —
if you use named arguments (the norm in Compose) you'll likely change **0–2 lines**.

Bump the version and let the compiler flag anything:

```kotlin
implementation("com.trinadhthatakula:asgard:2.0.0")
```

---

## Breaking changes

### 1. `AsgardStatCard`: `iconColor` → `iconTint`

The parameter was renamed to match every other component (`AsgardStatTile`, `AsgardListRow`,
`AsgardActionItem`, … all use `iconTint`).

```kotlin
// 1.x
AsgardStatCard(label = "Charge", value = "82%", icon = Icons.Rounded.Bolt, iconColor = primary)

// 2.0
AsgardStatCard(label = "Charge", value = "82%", icon = Icons.Rounded.Bolt, iconTint = primary)
```

### 2. `ConnectedButtonGroupItem`: `menuLabel` / `menuIcon` removed

These were vestigial members never used for rendering. If you referenced them, drop them — the item
already carries `contentDescription` (Icon/IconWithLabel) and `text` (Label/IconWithLabel).

```kotlin
// 1.x — .menuLabel / .menuIcon existed but did nothing
val label = item.menuLabel

// 2.0 — use the real fields
val label = when (item) {
    is ConnectedButtonGroupItem.Label -> item.text
    is ConnectedButtonGroupItem.IconWithLabel -> item.text
    is ConnectedButtonGroupItem.Icon -> item.contentDescription
}
```

*Construction is unchanged* — `ConnectedButtonGroupItem.Label("Day")`, `.Icon(icon, "Grid")`,
`.IconWithLabel(icon, "Recent", "Recent")` all work exactly as before.

### 3. `AsgardProBadge`: parameters reordered (content-first)

`text` now comes before `modifier`, matching the Compose convention. **Named calls are unaffected.**
Only positional calls (rare for this component) need updating.

```kotlin
// 1.x positional
AsgardProBadge(Modifier.padding(4.dp), "PRO")

// 2.0 positional
AsgardProBadge("PRO", Modifier.padding(4.dp))

// Named (both versions) — no change needed
AsgardProBadge(text = "PRO", modifier = Modifier.padding(4.dp))
```

### 4. `AsgardLockedOverlay`: `overlay` moved up (so `content` is the trailing lambda)

`overlay` is now a regular parameter and `content` is the single trailing lambda, so the idiomatic
call reads naturally. **Named calls are unaffected.**

```kotlin
// 2.0 — the recommended shape
AsgardLockedOverlay(
    locked = isLocked,
    overlay = { AsgardUpgradeCard(/* … */) },
) {
    PremiumContent()
}
```

---

## Behavior & visual changes (non-breaking)

- **Shapes are now theme-driven.** `AsgardDefaults` shape tokens read `MaterialTheme.shapes`
  (`large`/`medium`) instead of hardcoded radii, so Asgard surfaces follow your app's shape scheme.
  Some default corner radii shift slightly vs 1.x. To pin a specific radius, pass the component's
  `shape` param, or set `MaterialTheme.shapes` on your theme.

- **`ConnectedButtonGroup` selected emphasis.** The selected button now defaults to
  `primary` container / `onPrimary` content so it stays legible in dark mode. Override via the
  `colors` param if you want the previous look.

- **No more `material-icons-extended` (informational — no action needed).** Asgard dropped that
  dependency in 2.0 (its one built-in glyph is now a self-contained vector). Because it was always
  `implementation`-scoped — in 1.x too — it was **never** exposed to your app transitively, so its
  removal changes nothing for consumers. This only matters if your own code accidentally relied on
  it being on the classpath *via Asgard*; if so, depend on it directly:
  `implementation(compose.materialIconsExtended)`.

---

## New in 2.0 (all opt-in)

- **Customization params on every component** — `maxLines` / `overflow` / `softWrap`,
  `TextStyle` / color / `Shape` overrides, `contentPadding`, and accessibility hooks
  (`Role.Button`, overridable `contentDescription`). All default to the prior behavior.
- **Charts** — a new `com.valhalla.asgard.charts` package: `AsgardLineChart` (multi-series smoothed
  line + area), `AsgardStackedBarChart`, `AsgardTimelineBar`, `AsgardChartLegend`, `AsgardPulseRing`.
  Pure `Canvas`, no extra dependencies.
- **Bug fixes** — non-finite progress guard (`AsgardProgressRing`), marquee state keying
  (`AsgardListRow`), legible banner content color, locked-overlay accessibility, nav-rail animation.

---

## Checklist

1. Bump to `2.0.0`.
2. Build — the compiler flags any of the four breaking changes above (almost always just
   `iconColor` → `iconTint`).
3. If your theme relies on specific corner radii, confirm the theme-driven shapes look right (or set
   `MaterialTheme.shapes`).
4. Explore the new customization params and charts — see the
   [live gallery](https://trinadhthatakula.github.io/Asgard/).
