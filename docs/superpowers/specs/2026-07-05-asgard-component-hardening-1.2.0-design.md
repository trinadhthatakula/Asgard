# Asgard Component Hardening — 1.2.0 Design Spec

**Date:** 2026-07-05
**Status:** Draft (awaiting user review)
**Sub-project:** 1 of 2 (Component Hardening → 1.2.0). Sub-project 2 = *Asgard Charts* (separate spec, follows this one.)

## 1. Summary

Harden every public Asgard component: fix the correctness bugs found in the audit, add
consistent customization params so consumers can control text truncation, typography, color,
shape, spacing and accessibility, and clear the non-breaking packaging/hygiene debt. Refresh the
wasmJs gallery so the new capabilities are visible. Ship as **1.2.0**.

**Guiding constraint — additive only.** No breaking changes. Thor (an existing consumer) must
compile against 1.2.0 with zero source changes. Every new parameter has a default that reproduces
the component's *current* behavior. Renames/removals are deferred to a future 2.0.

## 2. Goals

- Fix all identified correctness bugs (NaN progress, stale marquee state, invisible banner text,
  CTA color mismatch, a11y semantics leak, nav-rail animation jitter, inconsistent disabled dimming).
- Add customization knobs across ~27 public composables using **flat params** (matching the
  library's existing convention) plus a shared **`AsgardDefaults`** token object for shapes/spacing.
- Standardize text control: for every text slot expose `<slot>MaxLines`, `<slot>Overflow`,
  `<slot>Style`, `<slot>Color` (with behavior-preserving defaults).
- Close accessibility gaps: `Role.Button` + `onClickLabel` on clickable surfaces, overridable
  `contentDescription`, fix duplicate/leaking semantics.
- Non-breaking hygiene: drop `materialIconsExtended`, promote `compose.ui`/`compose.foundation`
  to `api`, KDoc the navigation package, deprecate dead API, fix README version, commit the wasm
  yarn lock.
- Gallery: fix the Shimmer preview bug, demonstrate the new params via multi-variant previews,
  group the nav list by category.

## 3. Non-goals (this sub-project)

- **No new chart components** — `AsgardLineChart`, `AsgardTimelineBar`, `AsgardChartLegend`,
  `AsgardStackedBarChart`, `AsgardPulseRing` are Sub-project 2.
- **No breaking renames/removals.** `iconColor` → `iconTint` unification, param reorders, and
  removing dead members happen via `@Deprecated` now, real removal in a future 2.0.
- **No sibling-app refactors** (BatteryWatch/FuelCheckIndia adopt Asgard on their own schedule).
- **No interactive gallery knobs / copy-to-clipboard** (deferred; "demonstrate + fix" scope only).
- **No new test infrastructure** beyond compile + visual verification (see §8). Flagged as a
  recommendation, not in-scope.

## 4. Design principles

1. **Behavior-preserving defaults.** A new param's default must render identically to today.
   Example: `AsgardActionItem` gains `labelMaxLines: Int = 2` — the value it is currently hardcoded to.
2. **Flat params for cosmetics.** Text/color knobs are added as flat parameters, consistent with
   `AsgardStatTile` (22 params) and `AsgardStatCard` (20 params). No per-component style-object type.
3. **Shapes/spacing via `AsgardDefaults`.** Radii/spacing/icon-sizes are sourced from a single
   theme-aware token object so the design language is consistent and overridable, replacing the
   current per-component hardcoded 16/20/24/32 dp drift. Callers may still override per-call.
4. **Standard text-slot param set.** Naming pattern `<slot>MaxLines`, `<slot>Overflow`,
   `<slot>Style`, `<slot>Color` (e.g. `titleMaxLines`, `titleOverflow`, `titleStyle`, `titleColor`).
5. **Naming drift handled additively.** Where a component's param name diverges from the library
   norm (`iconColor` vs `iconTint`), add the norm-named param and mark the old one `@Deprecated`
   (`ReplaceWith`), keeping both wired so nothing breaks.

## 5. `AsgardDefaults` token object

New file: `asgard/src/commonMain/kotlin/com/valhalla/asgard/AsgardDefaults.kt`.

```kotlin
object AsgardDefaults {
    // Shapes — @Composable getters so a host can theme them.
    val tileShape: Shape        @Composable get() = RoundedCornerShape(20.dp)
    val cardShape: Shape        @Composable get() = MaterialTheme.shapes.large
    val bannerShape: Shape      @Composable get() = RoundedCornerShape(16.dp)
    val sectionCardShape: Shape @Composable get() = RoundedCornerShape(20.dp)
    val actionItemShape: Shape  @Composable get() = RoundedCornerShape(24.dp)
    val pillShape: Shape        get() = CircleShape
    val navContainerRadius: Dp  get() = 32.dp

    // Spacing / sizing scale.
    val contentPadding: Dp = 16.dp
    val iconSize: Dp = 24.dp
    val iconChipSize: Dp = 48.dp
    // ... rounded out during implementation
}
```

**Visual-parity rule:** token *default values* are set to each component's **current** radius/size,
so adopting `AsgardDefaults` centralizes and exposes the values without changing any pixels. Fuller
migration to `MaterialTheme.shapes` (which would shift some radii) is explicitly a later follow-up,
not part of 1.2.0.

## 6. Per-component changes

Legend: **FIX** = bug/correctness; **ADD** = new additive param(s); **DEPRECATE** = kept-but-marked.

### Metrics & data display
- **AsgardProgressRing** — FIX: guard non-finite `progress` (`if (!isFinite) 0f`, then `coerceIn`)
  so `NaN` no longer draws garbage; drop the redundant inner `Canvas.size()`. ADD: `cap: StrokeCap`,
  `trackStrokeWidth`, `progressBrush: Brush?`, `animate: Boolean`/`animationSpec`, progress semantics
  + `contentDescription`.
- **AsgardStatCard** — ADD: `labelStyle`, `labelMaxLines`/`valueMaxLines` + `*Overflow`, `textAlign`,
  `iconTint` (DEPRECATE `iconColor` → `iconTint`), tight-unit option (param instead of hardcoded
  leading space).
- **AsgardStatTile** — ADD: `labelMaxLines`/`valueMaxLines` + `*Overflow`, `Role.Button`
  + `interactionSource` for `onClick` (parity with ActionItem), `iconContentDescription`, icon-size
  params.
- **AsgardAnimatedNumeral** — ADD: `maxLines`/`overflow`/`softWrap`, `textAlign`, `animationSpec`,
  and a `format: (Int) -> String` hook so count-up preserves the caller's number formatting.

### Rows & list items
- **AsgardListRow** — FIX: key marquee state to `subtitle` (`remember(subtitle)`) so recycled rows
  don't inherit stale marquee; add `Role.Button` + `onClickLabel` when clickable. ADD: `enabled`,
  `titleStyle`/`subtitleStyle`/`captionStyle` + colors, `title/subtitleMaxLines` + `*Overflow`,
  `contentPadding`, `iconContentDescription`, `iconSize`, `verticalAlignment`.
- **AsgardActionItem** *(user's headline example)* — FIX: stop duplicating the visible label as the
  icon `contentDescription`. ADD: `labelMaxLines: Int = 2`, `labelOverflow`, `labelStyle`,
  `labelColor`, `width`, `shape` (from `AsgardDefaults`), `iconChipSize`, `iconSize`, `disabledAlpha`,
  `onLongClick`.
- **AsgardStepperRow** — FIX: add `valueColor` (symmetry with existing `labelColor`). ADD:
  `label/valueMaxLines` + `*Overflow`, `labelStyle`/`valueStyle`, decrement/increment button colors,
  overridable button `contentDescription`, value min-width/padding params.
- **AsgardSettingRow / AsgardSettingToggleRow** — FIX: apply consistent disabled dimming to
  `AsgardSettingToggleRow` (currently only `AsgardSettingRow` dims). ADD: value `maxLines`/`overflow`
  + `valueStyle`/`valueColor`; `Switch` colors on the toggle row; surface title/subtitle style
  overrides.
- **AsgardSectionCard** — ADD: title `maxLines`/`overflow` + `titleStyle`, `shape`
  (from `AsgardDefaults`), `contentPadding`, optional title-trailing slot.
- **AsgardFeatureRow** — ADD: title/description `maxLines`/`overflow` + styles + colors, `badgeSize`,
  `iconSize`, `spacing`, `badgeShape`, `onClick`, `iconContentDescription`.

### Chips, badges, banners
- **AsgardBadge** — FIX: add `overflow = Ellipsis` (currently hard-clips). ADD: `shape`, `textStyle`,
  `Role.Button` + `onClickLabel`, `iconContentDescription`, `iconSize`, `contentPadding`, `enabled`,
  `borderWidth`/`borderColor`.
- **StatusChip** — FIX: add `overflow = Ellipsis`. ADD: `textStyle`, `shape`, `contentPadding`,
  `onClick`, `leadingIcon` slot.
- **AsgardProBadge** — ADD: forward `onClick`, `outlined`, `textStyle` through to `AsgardBadge`.
  (Param-order fix is breaking → deferred to 2.0.)
- **AsgardBanner** — FIX: guard `contentColor` when `containerBrush`/non-scheme `containerColor` is
  used (fall back to a legible on-color rather than `Color.Unspecified`). ADD: title/description
  `maxLines`/`overflow`, `shape`, `contentPadding`, `iconContentDescription`, `iconSize`, `iconTint`,
  `verticalAlignment`, `onClick`.

### Pro / upgrade
- **AsgardUpgradeCard** — FIX: propagate `contentColor` to the CTA button (derive `ButtonColors`) so
  card and button schemes match. ADD: title/description `maxLines`/`overflow` + styles, `shape`,
  `contentPadding`, `ctaColors`, optional leading icon/illustration slot.
- **AsgardLockedOverlay** — FIX: `clearAndSetSemantics {}` on gated content while `locked` so
  screen readers don't read through the blur. ADD: `contentAlignment`, `shape`/clip, optional
  lock/unlock transition flag. (Trailing-lambda slot reorder is breaking → deferred.)

### Dialogs, buttons, inputs
- **AsgardDialogScaffold** — ADD: `shape`, `containerColor`, `titleContentColor`, `textContentColor`,
  `iconContentColor`, `tonalElevation`, `properties: DialogProperties`, `titleStyle`/`textStyle`
  + `maxLines`, `confirmEnabled`, destructive/confirm `colors`, `iconContentDescription`.
- **AsgardTonalIconButton** — ADD: `colors: IconButtonColors`, `shape`, `interactionSource`,
  `iconTint`, `iconSize`.
- **ConnectedButtonGroup** — FIX: guard out-of-range `selectedIndex`. ADD: group `enabled` +
  per-item enablement, `colors`, `spacing`, group `contentDescription`, item-label `maxLines`.
  DEPRECATE: `ConnectedButtonGroupItem.menuLabel` / `menuIcon` (dead API).
- **AsgardSearchBar** — FIX: replace the `Modifier.fillMaxWidth()` *default* with `Modifier` +
  internal base `fillMaxWidth()` (removes the "modifier silently drops fillMaxWidth" footgun without
  changing the default full-width look). ADD: `enabled`, `isError`, `supportingText`,
  `KeyboardOptions` passthrough, `focusRequester`/request-focus hook, overridable
  `contentDescription`s, placeholder `textStyle`.
- **AsgardLabeledSlider** — FIX: same `Modifier` default correction. ADD: `enabled`,
  `colors: SliderColors`, `labelStyle`/`valueStyle` + colors, label `maxLines`/`overflow`,
  `interactionSource`.

### Headers, onboarding, states, shimmer
- **AsgardHeader** — ADD: `titleMaxLines` (default 1), `titleStyle`, `titleColor`, `contentPadding`,
  `backContentDescription`, `iconContentDescription`, optional leading-content slot.
- **AsgardOnboardingScaffold** — ADD: title/subtitle `maxLines`/`overflow` + styles + colors,
  `contentPadding`, `actionsArrangement`, `horizontalAlignment`.
- **AsgardEmptyState** — ADD: optional `description` (split from single `text`), `textStyle`,
  `maxLines`/`overflow`, `iconSize`, `iconContentDescription`, `contentPadding`.
- **AsgardLoadingState** — ADD: `textColor`, `textStyle`, spinner size/stroke, `contentPadding`.
- **AsgardShimmer** — ADD: `shape: Shape` (supersedes `cornerRadius`, which is kept `@Deprecated`),
  `animate: Boolean = true` (reduced-motion escape hatch).

### Navigation & motion
- **AsgardNavigationBar** — FIX: guard `selectedIndex`. ADD: `containerColor`,
  `selectedIndicatorColor`, `selectedContentColor`, `unselectedContentColor`, `shape`,
  `unselectedAlpha`, label `TextStyle`.
- **AsgardNavigationRail** — FIX: drop the outer `animateContentSize()` (fights the per-item
  `animateExpressiveResize()` → jitter) and align with the library's own motion helper; guard
  `selectedIndex`. ADD: same color/shape/alpha/label params as the bar; optional header/footer slot.
- **AsgardNavItem** — ADD KDoc; optional additive `badge` field (default null) for notification dots.
- **Motion.expressivePress** — FIX: migrate off the deprecated `Modifier.composed {}` to a
  `Modifier.Node` implementation (internal change, non-breaking). ADD: optional press/release
  `animationSpec` params.

## 7. Hygiene / packaging (non-breaking)

- **Drop** `implementation(compose.materialIconsExtended)` — `AsgardHeader`'s `ArrowBack` resolves
  from material-icons-**core** (transitive via material3). Verify no other component imports an
  extended icon. *(High impact: removes multi-MB bloat from every consumer.)*
- **Promote** `compose.ui` and `compose.foundation` from `implementation` to `api` — they saturate
  the public API surface; current success relies on material3's transitive re-export.
- **KDoc** `AsgardNavigationBar`, `AsgardNavigationRail`, expand `AsgardNavItem`.
- **Deprecate** `ConnectedButtonGroupItem.menuLabel`/`menuIcon`.
- **README**: bump the install-snippet version to 1.2.0 on release; add a release-checklist step so
  it doesn't drift again. Align coordinate references on `com.trinadhthatakula:asgard`.
- **Commit** `kotlin-js-store/wasm/yarn.lock` (reproducible wasm builds); remove from ignore if listed.
- **Version catalog**: remove unused `compose-uiToolingPreview` / `compose-componentsResources`
  entries (or wire them if previews are wanted).
- **Bump** `VERSION_NAME` → `1.2.0`.

## 8. Gallery ("demonstrate + fix")

- **FIX** the `AsgardShimmer` preview: `Modifier.fillMaxWidth().height(24.dp)` (the current
  `.size(320.dp)` overrides `.fillMaxWidth()` and contradicts the shown snippet).
- **Demonstrate new params** via multi-variant previews: `AsgardStatTile`
  (status-dot / icon-badge / secondaryValue / animateValue / border), `AsgardStatCard`
  (inline icon / clickable press / border), `ConnectedButtonGroup` (Icon + IconWithLabel variants),
  `AsgardActionItem` (custom `labelMaxLines`), `AsgardBanner` (overflow / brush), etc.
- **Group** the sidebar `LazyColumn` by `ComponentEntry.category` (currently dead metadata) with
  sticky headers.
- Ensure each entry's displayed code snippet matches its live preview.

## 9. Verification

No test source set exists today. For 1.2.0:

1. `./gradlew :asgard:compileKotlinWasmJs :asgard:assemble` and the demo's
   `:demo:compileKotlinWasmJs` compile clean for Android + wasmJs.
2. Run the wasmJs gallery in a browser and visually confirm the hardened components and new previews
   render correctly in light and dark.
3. Confirm `materialIconsExtended` removal still resolves `ArrowBack` (build succeeds).

**Recommendation (out of scope unless requested):** stand up a `commonTest` source set with a few
semantics/screenshot smoke tests for the interactive components (nav, ConnectedButtonGroup, ProGate
scrim, SearchBar clear) — the library currently ships untested.

## 10. Risks & mitigations

- **Radius unification shifting visuals** → tokens default to each component's *current* value; no
  pixels change in 1.2.0.
- **SearchBar / LabeledSlider modifier-default change** → keep default full-width by applying
  `fillMaxWidth()` internally; API-compatible, removes the footgun. Flag in release notes.
- **Large surface area (27 components)** → sequence component-by-component (per the chosen "per-
  component hardening" approach); each PR-sized batch keeps defaults behavior-preserving and is
  independently compilable.
- **`materialIconsExtended` removal** → verify with a clean build; the audit confirmed only
  `AsgardHeader` references an icon and `ArrowBack` is in core.

## 11. Rollout

- One **1.2.0** release at the end of Sub-project 1.
- Sub-project 2 (*Asgard Charts*) targets **1.3.0** with its own spec.

## 12. Open questions

- Should the `badge` field on `AsgardNavItem` (notification dots) land in 1.2.0 or defer to the
  charts milestone? (Additive either way; included here as low-risk.)
- Confirm the `AsgardEmptyState` `description` split is wanted vs. leaving it single-`text`.
