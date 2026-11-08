# Asgard component API audit

Read-only source audit of `/Users/trinadhthatakula/StudioProjects/Asgard`, branch `chore/asgard-component-gallery-audit`, commit `97903e92e8c787574b9938209f2fb2876d374c52`. Repository source and the locally cached Material 3 1.13.0-alpha02 source were inspected. No implementation, build, device, or browser verification was performed by this agent. The gallery agent owns browser findings. Existing untracked `handover.md` was preserved.

Paths below are relative to the repository. Short component filenames refer to `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/`; navigation filenames refer to its sibling `navigation/`, and chart filenames to `charts/`.

## Recommended next-release scope

Bundle the already-merged multiselect group with: (1) explicit fixed/custom button shape policies for both selection modes; (2) docked/floating navigation containers and expanding/segmented item appearances; (3) confirmed selection/name/disabled-state accessibility fixes; (4) regression coverage of those behaviors. Preserve existing entry points and default appearances except documented bug fixes. This is a focused component release, not a replacement routing framework or a redesign of every component.

The parent audit established Maven latest 2.0.1 and that multiselect source is not released yet; this agent did not independently query Maven. Do not describe current main APIs as available in 2.0.1. A minor release such as 2.1.0 is appropriate for additive public APIs if compatibility checks pass; choose the version against the final release state.

## Confirmed source defects and priorities

**P1 — Default collapsed navigation destinations can be unnamed, and navigation never exposes selection semantics.** `AsgardNavItem.kt:15-20` defaults `contentDescription` to null. The bar passes it to the icon (`AsgardNavigationBar.kt:181-184`) and renders text only for `selected && showLabel` (`188-205`). Therefore an ordinary unselected item with no badge or explicit description has neither an accessible name nor visible text. `showLabel=false` makes this affect every destination. Rail icon-only mode has the same name problem (`AsgardNavigationRail.kt:182-186,206-215`). Both item roots use plain `clickable` without a role or selected state (`Bar:155-164`, `Rail:163-172`); the containers have no selectable group semantics. Screen-reader users cannot reliably identify or distinguish the current destination. Fix on a shared destination implementation: stable name from `contentDescription ?: label`, selected state, `Role.Tab`, group semantics, one logical focus target, and a badge description policy. Avoid announcing the label twice when visible. Preserve explicit descriptions as overrides. Verify keyboard and screen-reader behavior separately on Android and Wasm.

**P1 — A disabled search field still has an enabled clear action.** `AsgardSearchBar.kt:77` sends `enabled` to `OutlinedTextField`, but the independently interactive trailing `IconButton` at `89-93` does not receive it. With nonempty query and `clearIcon`, pressing Clear still calls `onQueryChange("")`. The field's disabled flag is not a universal disable switch for arbitrary child controls. Pass the same `enabled` flag to the clear button and test pointer, keyboard, and semantic activation without firing callbacks when disabled.

**P2 — A one-item ConnectedButtonGroup has only a leading end cap.** Nonempty lists are accepted (`ConnectedButtonGroup.kt:157`), including size one. `connectedShapesFor` matches `index == 0` before `index == lastIndex` (`285-288`), so singleton groups receive the leading shape with inner corners on the other edge. Resolve `itemCount == 1` first and use a standalone shape. This is a visual logic defect; screenshots have not been captured by this agent.

**P2 — Single-select groups announce checkbox semantics and omit their exclusivity relationship.** Both overloads use the same `ToggleButton` path (`ConnectedButtonGroup.kt:88-98,129-140,168-193`). The cached Material 3 `ToggleButton.kt:188-192` explicitly sets `Role.Checkbox`; Asgard supplies no single-select/radio/group semantics. Use mutually exclusive selection semantics for the single-select API and independent checkbox semantics for multiselect. Do not add a second clickable handler on top of ToggleButton: retain one interaction/action owner and test the final merged semantics, selected state, and repeat-selection callback behavior.

**P2 — Settings wrappers discard correct disabled/combined-control semantics.** `AsgardSettingRow` dims and removes its callback instead of forwarding `enabled` (`AsgardSettings.kt:136-154`), although `AsgardListRow` already accepts and forwards it to `clickable` (`AsgardListRow.kt:82,103-109`). The disabled row therefore loses its disabled-action semantics. The toggle wrapper creates a clickable row with button semantics plus an independently interactive Switch (`AsgardSettings.kt:187-205`; `AsgardListRow.kt:107`). This presents two controls for one setting and separates the label from the switch's focus node. Make the row the toggle owner with `Role.Switch`, checked state, label and enabled state; use a passive visual Switch with `onCheckedChange=null`. Keep the base list row's arbitrary trailing slot interactive for consumers that intentionally provide separate actions. Also settle subtitle marquee behavior: its own unconditional clickable (`AsgardListRow.kt:138-147`) intercepts subtitle taps and remains active even for a disabled wrapper. This is not a double-toggle bug; it is a focus/action ownership problem.

**P2 — Labeled slider label/value are disconnected from the adjustable control.** `AsgardLabeledSlider.kt:63-88` renders label/value as siblings above an M3 Slider without assigning its label or formatted value semantics. The wrapper's visual label does not become the Slider's accessible name. Add inner slider semantics using `label` and, when provided, `valueLabel`, while preserving native range/actions and avoiding duplicate announcements. An outer modifier alone is not a robust way for consumers to label the inner control.

**P2, follow-up unless readily bounded — Non-finite chart input is not guarded.** `AsgardLineChart.kt:118-136,260-262` passes NaN/Infinity through bounds and into the default formatter (`102`: `roundToInt()`); a NaN Y sample can produce the Kotlin NaN rounding exception instead of an empty/error rendering. Input preconditions currently do not forbid it. `AsgardStackedBarChart.kt:57,79-83,109-111` also lets non-finite totals/segments reach drawing. Establish a finite-input policy, e.g. drop invalid points/segments and render `emptyContent` if no valid data remains; validate explicit domains before formatting. Make scaling consistent with rendered data: bar totals currently include negative values that drawing skips. This is independent of button/navigation work and can be a small robustness follow-up; no runtime chart crash was reproduced here.

## Required button shape API

Current public parameters expose `colors`, spacing, description and label lines, but no shape option in either overload (`ConnectedButtonGroup.kt:77-87,117-127`); shape selection is private and unconditional (`190,285-288`). Colors already resolve independently (`162-165`). `colors` must never imply a geometry policy, and `Modifier.clip` on the group cannot replace the individual button shapes.

Use an Asgard-owned immutable configuration containing per-position, per-state shapes. Illustrative API design, not compiled implementation:

```kotlin
@Immutable
class ConnectedButtonStateShapes(
    val shape: Shape,
    val selectedShape: Shape = shape,
    val pressedShape: Shape = shape,
)

@Immutable
class ConnectedButtonGroupShapes(
    val single: ConnectedButtonStateShapes,
    val leading: ConnectedButtonStateShapes,
    val middle: ConnectedButtonStateShapes,
    val trailing: ConnectedButtonStateShapes,
)

object ConnectedButtonGroupDefaults {
    @Composable fun shapes(): ConnectedButtonGroupShapes
    @Composable fun fixedConnectedShapes(): ConnectedButtonGroupShapes
    fun uniformShapes(shape: Shape): ConnectedButtonGroupShapes
    fun uniformShapes(states: ConnectedButtonStateShapes): ConnectedButtonGroupShapes
}
```

Expose new overloads with a **required** `shapes` parameter, while keeping the old signatures verbatim:

```kotlin
@Composable fun ConnectedButtonGroup(
    items: List<ConnectedButtonGroupItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    shapes: ConnectedButtonGroupShapes, // required; selects the new overload
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors? = null,
    spacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    contentDescription: String? = null,
    labelMaxLines: Int = 1,
)

@Composable fun ConnectedButtonGroup(
    items: List<ConnectedButtonGroupItem>,
    selectedIndices: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    shapes: ConnectedButtonGroupShapes, // identical shape contract in both modes
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors? = null,
    spacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    contentDescription: String? = null,
    labelMaxLines: Int = 1,
)
```

`shapes()` preserves current Material connected leading/middle/trailing base, selected and pressed shapes; singleton gets proper standalone ToggleButton defaults. `fixedConnectedShapes()` keeps those position-specific **base** outlines in every state. This is the desired "fixed shape, changing colors" option while retaining connected end caps. `uniformShapes(shape)` applies one literal shape at every position and in every state; it does not secretly restore connected corners. `uniformShapes(states)` applies the same state map at each position. Explicit per-position construction permits custom leading/middle/trailing/single state maps. Omitted selected/pressed values on an explicitly supplied state map inherit its base shape; the default factory explicitly supplies expressive state shapes.

Precedence is deterministic: choose `single` for one item, else logical leading/middle/trailing; then choose pressed > selected > base. Disabled suppresses new interactions but retains caller-controlled selection; no separate disabled-shape API is needed. Preserve color resolution, z-order, selection callbacks, enabled logic, and spacing independently. Do not share a single interaction source among all buttons: a press must not deform neighbors.

The current cached Material 3 implementation resolves pressed > checked > base (`ToggleButton.kt:1699-1719`), and morphs only compatible `CornerBasedShape` sets. Other arbitrary Shape implementations switch instead of morphing. Document this limitation rather than promising universal shape animation. A fixed shape also does not mean "disable all animation": color transitions/ripple remain independent. Existing negative overlap may suit connected shapes but not a consumer's separate circular buttons; `spacing` remains explicitly caller-controlled.

Logical start/end are required for RTL. Reuse M3 logical corner shapes where appropriate and add outline tests in both directions. Do not label the public shape positions left/right. Avoid adding a public arbitrary composable shape callback, new item subclasses or more Material experimental types for this release; the four-position config handles the actual need with predictable state behavior.

## Navigation architecture: placement and appearance are independent

Today the bar always fills width, owns `navigationBarsPadding`, uses 12.dp padding and a 64.dp row (`AsgardNavigationBar.kt:82-97`); the rail fills height and consumes `safeDrawingPadding` (`AsgardNavigationRail.kt:91-102`). `shape` changes only the outer surface. Item indicators are separately hardcoded to 32.dp and 20.dp corners (`Bar:157`, `Rail:165`). Thus an outer shape override cannot provide the requested indicator shapes or a genuine floating bar. `showLabel` also means selected-only in the bar and all destinations in the rail (`Bar:188-205`; `Rail:206-215`). These are feature/API gaps, not false claims that the current documented pill bar implements other styles.

Keep routing/selection controlled by the caller and use three internal layers:

1. **Container:** orientation/placement, width or height bounds, docked or floating treatment, outer shape/color, padding, elevation, insets. Existing bar and rail APIs remain orientation-specific facades. A floating container adds outer margins and max width and keeps system-bar inset space outside its colored surface; it should not merely be a docked Surface with extra rounding. Provide explicit inset ownership (`WindowInsets` or an equivalent documented policy), so consumers can pass zero insets when Scaffold already owns them.
2. **Destination layout and appearance:** expanding pill or segmented selection; label policy `SelectedOnly`, `Always`, `Hidden`; indicator shape, selected/unselected colors, item padding/minimum size. Segmented means equal bounded destination slots with a shared container and selected-segment indicator, independent of floating/docked placement. Floating + expanding and floating + segmented should both work. Preserve the existing expanding default and existing rail label defaults through wrappers.
3. **Shared destination behavior:** accessible name, selection semantics, enabled/focus behavior, badge, icon/selected icon, callback, one interaction source per item. Both appearances must share this implementation so visual variants cannot diverge on disabled or accessibility handling.

Add overloads requiring an `AsgardNavigationOptions` (or similarly scoped configuration), and keep existing `AsgardNavigationBar`/`Rail` signatures intact as delegates with legacy defaults. Put container options and item options into separate fields, e.g. `container = Docked/Floating(...)`, `itemStyle = ExpandingPill/Segmented(...)`, `labelVisibility = ...`. Keep item colors separate from indicator shape. Factories should keep new types manageable; do not collapse these orthogonal choices into an enum `Default/Floating/Segmented` that makes floating+segmented impossible.

Public slots for every internal element, drawers, automatic window-size routing, and a new destination identity model are outside this release. Avoid adding fields casually to the existing `AsgardNavItem` data class: even a defaulted property changes constructor and copy ABI. If optional enablement/custom badges are necessary now, add them through the new options/item-presentation API and retain the old model ABI.

## Component-family customization matrix

| Family | Current useful customization | Specific gap / next step |
|---|---|---|
| Connected groups | Colors, spacing, labels/icons, group and per-item enabled; source supports single and multi (`ConnectedButtonGroup.kt:77,117,204`) | Required shapes API and singleton/semantics fixes. Keep equal bounded weights; do not regress large-font safety. |
| Navigation | Outer shape, color palette, label style, badges; rail header/footer (`Bar:68-80`; `Rail:68-82`) | Separate container and indicator policies; floating/segmented; names/selection/focus and bounded layout. |
| Search / slider / stepper / icon button | Search shape/colors/error/support/IME/focus (`SearchBar:49-66`); slider colors/text/interaction (`LabeledSlider:43-60`); stepper text/colors/descriptions (`StepperRow:54-75`); icon button shape/colors/interaction (`TonalIconButton:34-44`) | Clear disabled and slider labeling fixes now. Search text style/readOnly/keyboard action slots and stepper padding/button shapes are later enhancements, not release blockers. |
| List / settings / section | Base list has text styles, overflow, padding, leading/trailing, enabled and click label (`ListRow:71-99`); section shape and content/title slots (`Settings:46-57`) | Settings wrappers fail to forward base-row capabilities and need semantic fixes. Later add a coherent row configuration instead of duplicating dozens of flat params. Test long section titles beside trailing actions (`Settings:73-81`: unweighted title). |
| Stat card / tile | Shapes, borders, padding, colors, label/value styles/overflow and icon sizing (`StatCard:84-110`; `StatTile:80-110`) | Already broadly customizable. No broad rewrite needed; consistent enabled/click-label options can follow actual consumer demand. |
| Badge / status / pro badge / action tile | Badge shape/colors/border/padding/enabled/click label (`Badge:56-72`); StatusChip shape/padding/icon slot (`StatusChip:43-52`); ProBadge preset (`ProGate:49-57`); action shape/size/colors/long click (`ActionItem:67-85`) | Wrappers differ on enabled, click label and shape forwarding. ActionItem's exposed shape clips outer Column while its visible icon-chip shape remains fixed 16.dp (`ActionItem:93,120`), so theme changes do not reshape that chip. Treat a dedicated chip shape option as an additive follow-up. Add explicit action role to ActionItem's clickable/combinedClickable path (`94-109`). |
| Banner / empty / loading / dialog | Broad text/colors/padding/shape; banners brush+action; dialog content/properties/confirm enablement (`Banner:69-95`; `EmptyState:45-59,113-121`; `DialogScaffold:51-74`) | Mostly sound customization coverage. Keep dialog custom content precedence documented; avoid speculative parameter expansion. |
| Onboarding / premium | Feature-row sizing/text/colors/shape/click (`Onboarding:61-81`); scaffold slots/alignment/padding (`154-170`); upgrade card shape/text/colors/leading/CTA colors (`ProGate:95-112`) | Upgrade CTA enabled/state/slot capability is a future consumer-driven addition. LockedOverlay keyboard/focus suppression needs verification before its input-blocking promise can be relied upon. |
| Progress / animated text / shimmer | Ring brushes/strokes/angles/animation/content; numeral text/format/animation; shimmer shape/colors/motion (`ProgressRing:49-64`; `AnimatedNumeral:43-56`; `Shimmer:43-50`) | Ring range semantics are opt-in only when description exists (`ProgressRing:72-80`), a documented limitation; consider an explicit semantic/decorative policy later. Do not claim all motion uses the theme: numeral uses tween and ring spring defaults. |
| Charts / legend | Series color/width/fill/smoothing, formatting, plot padding, bar/timeline geometry, wrapping legend (`LineChart:93-106`; `StackedBarChart:42-51`; `TimelineBar:42-49`; `ChartLegend:40-52`) | Finite-input handling and accessible summary/data representation. Canvas labels are not ordinary semantic Text nodes. Axis overflow, chronological RTL policy and gestures require dedicated follow-up scope. |

## Layout, accessibility and behavior verification still required

- **Narrow navigation / large text:** bar items are unweighted and measured sequentially with `SpaceEvenly`; selected labels can consume remaining width (`Bar:90-111,155-205`). Define minimum touch targets, maximum label width/ellipsis and behavior for too many destinations. Test 320.dp and smaller constrained hosts, 3–5 items, long German/Arabic labels and 1.0/1.5/2.0 font scales. Do not silently make a navigation destination inaccessible to preserve the selected label. Rail is not scrollable and a long label can widen the surface; test short-height windows/header/footer combinations.
- **Keyboard / visible focus:** bar and rail explicitly suppress indication (`Bar:162`, `Rail:170`), while their custom motion responds to press. Verify a visible keyboard-focus indicator, stable tab order, Enter/Space activation, and selection announcement. Do not assume platform touch target expansion equals a guaranteed 48.dp nonoverlapping hit target.
- **RTL:** logical navigation container corners and header's automirrored back icon already help (`Rail:84-86`; `Header:136-143`). Check shape leading/trailing, label reveal origin and icon order under RTL. Charts currently use a physical left-to-right plot and timeline; numeric/time axes need an explicit product policy rather than blindly mirroring everything.
- **LockedOverlay risk, not a reproduced exploit:** `AsgardProGate.kt:180-208` hides child semantics and intercepts pointer clicks with a scrim, but does not remove keyboard focusability, clear existing focus or block key input in underlying content. Test a TextField or focused Button before and after locking, tab traversal and key activation. If it is reachable, suppress underlying focus/input while locked and focus the actionable overlay. This UI component is not an authorization boundary.
- **Modifier order:** search and slider deliberately apply `fillMaxWidth()` before the consumer modifier (`SearchBar:71-76`; `LabeledSlider:63`), which can prevent a later `Modifier.width(...)` from shrinking them under bounded constraints. KDoc explicitly promises full-width behavior. Treat changing this as a documented behavior decision, not an unannounced cleanup in the shape release.
- **Localization:** Back/Search/Clear/stepper action defaults are English but caller-overridable (`Header:77`; `SearchBar:53,65`; `StepperRow:97,118`). Samples must pass localized accessible strings. Visible nav labels should be usable accessible-name fallbacks even when visual labels are hidden.

## Compatibility and meaningful tests

**Keep binary entry points.** Appending default parameters is source-compatible for many recompiled Kotlin calls but changes JVM and Compose-generated function signatures/default masks. Old precompiled Android consumers or Thor extensions may fail at runtime. Retain old function parameter lists/order/defaults and let their bodies delegate; new overloads require a distinct configuration argument so legacy calls cannot become ambiguous. Preserve trailing lambda conventions and keep old public data class constructors/copy signatures. New config types should have intentional evolution rules; defaulted constructors do not grant future binary compatibility. Prefer an Asgard-owned shapes wrapper over exporting more volatile Material experimental API. Do not change the current Kotlin/Compose toolchain in this release without a separately verified reason.

**Compatibility fixtures:** compile representative Android consumers against published 2.0.1 and link/run them with the new library without recompiling those fixtures, including defaulted and positional calls, then separately compile current-source consumers including multiselect/new APIs. Include a Kotlin function-reference fixture with explicit expected types and a Wasm consumer compile. Run an API/ABI signature comparison against the released Android artifact; source compilations alone do not prove binary compatibility.

**Focused UI tests:** (1) single/multi callbacks, immutable selection sets, invalid indices, group/item disabled states and correct final semantics; (2) fixed-connected/uniform/custom-state shapes across press/release/selection, custom colors independent of shapes, singleton and RTL outlines; (3) nav names, selected role/group, badges, keyboard focus/activation, bounded targets and inset ownership for docked/floating × expanding/segmented; (4) disabled search Clear cannot mutate the query; (5) one labeled settings-switch node and one action per gesture; (6) slider name/formatted value/range; (7) lock/unlock focus regression if the lock fix enters scope.

**Rendering assertions:** preserve screenshots for existing default single/multi groups and existing bar/rail, except reviewed bug fixes. Add only representative stress cases for new states/layouts, with controlled animation time. For non-corner shapes assert state resolution rather than claiming interpolated outlines. Use practical pointer and keyboard interaction tests, not tests that merely restate a resolver's implementation.

Current checked-in Kotlin coverage is ten domain/token tests (`asgard/src/commonTest/kotlin/com/valhalla/asgard/AsgardDomainTest.kt:20-94`), with no UI test dependency (`asgard/build.gradle.kts:51-55`). The parent reports twelve Python tooling tests; they do not cover component semantics. After implementing, retain `:asgard:compileAndroidMain`, `:asgard:compileKotlinWasmJs` and `:asgard:testAndroidHostTest` (already wired into publishing at `asgard/build.gradle.kts:62-64`), run the gallery/tooling checks owned by the other audit, and add a supported Android UI/semantics test harness for these regression cases. Manual browser/device checks supplement the suite; they are not implied by the existing green math tests. No build/test success is claimed by this report.
