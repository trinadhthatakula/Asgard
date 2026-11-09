# Asgard motion audit

Source audit of `/Users/trinadhthatakula/StudioProjects/Asgard` on `fix/navigation-selection-motion`, 2026-11-09. The source audit was read-only; the focused fixes described below were subsequently checked with builds and native tests. Line numbers refer to the source inspected during the audit.

The shared navigation implementation and press feedback are the highest-priority correctness work. The other strongest candidates are the progress ring, scrollbar and shimmer. Their code exposes avoidable work, but this audit does not establish that any of them currently drops frames on a device.

Audit coverage: source-wide motion/state/effect/layout/lazy/drawing triage of commonMain library components, navigation, charts and `Motion.kt`; focused review of the animated/interactive paths identified by that scan; the common gallery shell/catalog, Android gallery platform persistence, and Android harness build configuration. Reviewed applicable Compose-performance references for state, layout, lazy layouts, animation, effects, graphics and modifier nodes. No blocking I/O, layout-to-composition size feedback, per-row external Flow subscriptions or additional proven navigation-style weight snap was found in the scanned library paths. This is a source audit, not exhaustive validation of every runtime combination or platform backend.

Recommended next batch:

1. Fix the progress ring's custom-spring range edge case, defer its animation sample to drawing/semantics, and add an intermediate-frame native regression.
2. Add gallery motion stress presets plus saveable Android controls so navigation, press, ring and scrollbar cases can be reproduced consistently.
3. Establish a nondebuggable, optimized physical-device benchmark baseline. Use the measurements to choose whether scrollbar phase deferral, shimmer gradient caching and press-modifier modernization are worth implementing next.

## Concrete correctness findings

### P1: Navigation caption fitting must match each appearance — fixed in the local patch

Evidence: `asgard/src/commonMain/kotlin/com/valhalla/asgard/navigation/AsgardNavigationConfiguration.kt:96–105,123–136` and `navigationNeedsCaption` in that file.

The original automatic-caption threshold assumed the selected destination can take extra width. Expanding navigation does that, while Segmented and Standard destinations remain equal-width. With four destinations, 308 dp available and a widest label of 55 dp, the expanding threshold is 263 dp, so it chooses inline labels. Segmented destinations get only 77 dp each; an inline icon, gap, padding and label need roughly 103 dp. The label can therefore be clipped even though the caption mode exists to avoid that.

The local patch changes the fit calculation to use expanding row geometry, segmented equal-width inline geometry and standard equal-width stacked geometry, with dedicated tests. Keep the decision based on the widest destination so tapping between short and long names does not repeatedly change the whole label layout.

Meaningful regression: render four/five mixed-length labels at the fit boundary for every appearance; select each destination and check caption mode stays stable and the visible label fits. Include narrow width, RTL and large font scale. A unit test of the calculation is useful, but cannot replace the rendered fit assertion.

### P2: Shared press feedback captured an outdated scale — fixed in current working tree

Evidence: `asgard/src/commonMain/kotlin/com/valhalla/asgard/Motion.kt:48–77`. The current working tree now includes `rememberUpdatedState(scaleOnPress)` at line 55 and reads it when handling Press.

Previously the collector's `LaunchedEffect` keys contained the source/specs but not `scaleOnPress`. Changing the scale from .95 to .80 with the same source and animation specs left future presses using .95. Because cards, tiles and navigation share this modifier, the defect was library-wide even though most default callers never changed the parameter.

Retaining the collector and reading the latest parameter is the appropriate narrow fix. Restarting the collector on every scale change would add cancellation overhead and can interfere with an in-flight press.

Meaningful regression: keep the same interaction source/specs, change only `scaleOnPress`, emit a new Press, advance the controlled clock and check the rendered scale. Release/Cancel must restore 1.0. Also test rapid Press/Release/Press interruption. Source replacement while pressed is a further robustness case to test; this audit did not establish a supported multi-press scenario and does not count it as another confirmed defect.

### P2: Progress-ring animation can exceed its normalized range with a custom spring

Evidence: `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/AsgardProgressRing.kt:62,66–77,104,114`.

The target is clamped to 0..1, but the animated sample is used unchanged for the arc and progress semantics. The public `animationSpec` accepts an underdamped spring. Such a spring can briefly produce a negative sample when moving toward zero or exceed one near completion. That can draw a reverse/excess sweep and publish out-of-range progress. The default `spring()` is nonbouncy, so this does not affect its ordinary default transition.

Proposed fix: clamp the animated sample at its draw and semantics consumption points. Keep the animation state's internal trajectory intact so retargeting remains continuous. This combines naturally with deferring its state read described below.

Meaningful regression: use a deliberately underdamped spring, animate 1→0 and 0→1 with a controlled clock, and assert the sampled rendered sweep/progress semantics stay inside 0..1 at intermediate times as well as completion. Include rapid target reversal.

## Performance candidates requiring release measurements

### P2: Progress ring observes every animation sample in composition

Evidence: `AsgardProgressRing.kt:66–71,83–122`.

Calling `.value` immediately on `animateFloatAsState` makes the component observe animation frames during composition; its draw callback receives a captured primitive. Only the arc needs that sample. Retain the animation State and read it in Canvas/draw scope, and read a clamped sample in the semantics lambda if animated semantics are required. This can avoid composition work while the ring moves. Caller content is not guaranteed to recompose each frame because child skipping may apply; do not overstate the effect.

Validate with multiple rings and nontrivial center content in a release benchmark. Compare frame timing and CPU work before/after. Recomposition counts alone are insufficient evidence of smoother output.

### P2: Scrollbar movement and metrics are observed in composition

Evidence: `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/AsgardLazyScrollbar.kt:45–49,69–73,119–130,167–169`.

The component reads continuously changing scroll metrics/drag fraction in composition and moves its thumb/label with the value-based `offset(y = travel * fraction)`. Its `BoxWithConstraints` subcomposition is also in the hot component. Thumb movement belongs in placement or drawing. Consider a single custom layout for track, thumb and drag label, obtaining current geometry in layout rather than pushing pixel changes through composition. Keep conditional paint/focus and accessible range text isolated from thumb placement.

The pointer handler already reads updated travel geometry via `rememberUpdatedState` at line 129. No stale drag-geometry bug was found. The conflated request channel also avoids launching an independent scrolling coroutine per pointer event. Preserve these protections.

Validate long variable-height lists and grids, mixed spans, forward/reverse layout, drag while content changes, and flings on both 60 Hz and a high-refresh physical device. Measure thumb and list frame timing together. This source inspection does not prove one-frame latency or visible frame drops.

### P3: Shimmer allocates its gradient during every draw

Evidence: `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/AsgardShimmer.kt:74–85`.

Animation sampling is already deferred to drawing, which is correct. However, each draw builds a color list and linear-gradient brush. Consider caching a stationary gradient keyed by dimensions/colors and translating it through drawing while retaining full placeholder coverage and the intended edge colors. Do not put the per-frame progress read in a `drawWithCache` cache-building block or that cache will invalidate each frame.

Validate many visible skeleton placeholders, resize, theme changes and disabled animation. Use release allocation/frame traces to decide whether the change pays off; a single placeholder may be too cheap to matter.

### P3: Modernize the shared press modifier after measuring

Evidence: `Motion.kt:53–82`.

The shared helper uses `Modifier.composed`. Its graphics-layer scale reads are already deferred correctly. A persistent `Modifier.Node` can preserve an Animatable and interaction collector while allowing source/spec/scale updates without the legacy modifier subcomposition overhead. This is a broad change to all tappable surfaces and should follow the focused correctness fix, backed by interaction/device tests and a measured baseline.

### P3: Picker validation scales with the entire dataset

Evidence: `asgard/src/commonMain/kotlin/com/valhalla/asgard/components/AsgardPicker.kt:43–45,64,69`.

Duplicate-key checks allocate mapped/distinct lists and traverse every item during each component recomposition. This is reasonable validation for small pickers; large consumer datasets or frequent filtering need measurement. Avoid blindly caching against mutable list identity, which could hide invalid keys. A prepared picker model or explicit validated input contract is a possible later design if profiling shows material cost.

The header has a content type and rows use the default null type. Add an explicit row type for clarity; do not claim every item is un-reusable now. Compose can reuse compatible null-type rows, and typed headers are already separated. Gallery sidebar headers/rows are both untyped at `GalleryApp.kt:86,90`; differentiating those shapes is the more relevant reuse improvement.

## Motion consistency improvements, not confirmed regressions

- `AsgardActionToolbar.kt:42–62` replaces TextButton with IconButton when labels change. That recreates the button subtree and abruptly changes geometry. Consider a stable keyed action renderer with animated label reveal/bounds, preserving focus and interaction identity across live customization. Animation is not currently promised by this API.
- `AsgardNavigationRail.kt:214–223` in the original overload changes label style/weight immediately with selection and adds/removes labels without visibility transitions. Outer resize and colors are animated; consider bringing the label reveal/style treatment into the same documented policy as configured navigation after rendered comparison.
- `AsgardProGate.kt:180–210` adds/removes blur and overlay abruptly. A visual transition is possible, but input and accessibility gating must take effect immediately when locked; never delay protection until an entry animation finishes.
- AnimatedNumeral uses duration-based transitions (`AsgardAnimatedNumeral.kt:49,55,92–94`), while ProgressRing defaults to `spring()` and navigation reads the host MotionScheme. Decide whether count-up/odometer motion should expose theme defaults consistently. Changing glyphs legitimately needs composition; count-up recomposition is not a generic phase-deferral bug.
- Avatar, badge, banner and ordinary list rows use conventional click/ripple feedback, while cards/tiles/navigation use squish. Document an intentional feedback policy before changing every component to one effect.

ConnectedButtonGroup is not another navigation-weight regression: its cells stay equal-weight and M3 ToggleButton supplies selection shape/color motion. The custom-shape API documents that non-corner shapes may switch rather than morph (`ConnectedButtonGroup.kt:365`). Static charts do not promise animated data transitions; absence of an animation is not itself a bug.

## Gallery changes to make regressions visible

1. Add motion stress presets to navigation variations. The current demo only has Home/Search/Settings (`demo/src/commonMain/kotlin/com/valhalla/asgard/demo/ExpandedCatalog.kt:23–27`). Include 3/4/5 destinations, mixed long labels, text-only tabs, badges, narrow/wide preview bounds and rapid selection. Show every appearance/label policy, including Thor's automatic selected-caption behavior.
2. Add an appearance motion selector and animation-off control next to RTL/text scale (`GalleryApp.kt:119–126`). Shimmer and pulse already expose local opt-outs; the gallery should exercise both host-level duration scaling and local flags.
3. Preserve Android test configuration across recreation. Theme/RTL/text scale/seed use ordinary remember (`GalleryApp.kt:43–46`); navigation settings use remember (`ExpandedCatalog.kt:33–39`). Android persists the chosen component in preferences (`demo/src/androidMain/kotlin/com/valhalla/asgard/demo/GalleryPlatform.android.kt:22–26,56–59`), but not its controls or navigation history. Rotating therefore reconstructs defaults, making before/after comparisons unreliable. Use saveable primitive settings/per-entry control state, plus saved history where appropriate. This audit did not run a rotation test.
4. Add a reproducible motion page with progress rings, digit-boundary changes, press surfaces, toolbar label switching and list/grid scrollbars. Display current settings and a short replay action so future apps can reproduce problems without building another harness.

## Native validation and release gate

The existing Android app module is a good correctness harness. Its build file currently does not configure R8/minification, profileability, or a benchmark variant (`androidApp/build.gradle.kts:8–28`). Existing debug instrumentation tests establish rendered correctness, not production smoothness. Do not benchmark a debuggable APK.

Recommended next layer: add a nondebuggable, R8-enabled, profileable benchmark target and Macrobenchmark journeys for navigation selection, scrollbar fling/drag, press interruption and a group of animated placeholders/rings. Run warmed repeated samples on the physical device, report its refresh rate and frame overrun distribution, and retain a baseline for comparison. Baseline profiles can then cover repeated critical journeys once the implementation is stable; a profile is not a substitute for fixing layout discontinuities.

| Area | Correctness assertion | Motion/performance validation |
| --- | --- | --- |
| Navigation | Stable label mode, final selection and unobscured text for each appearance/policy | Intermediate geometry changes continuously; rapid retargeting does not snap; 60/high-refresh frame timing |
| Press modifier | Latest scale parameter; Release/Cancel restores 1; source replacement handled | Rapid interruption preserves trajectory; no freeze while host theme/spec changes |
| Progress ring | Clamped intermediate semantics and sweep with bouncy specs | Multiple animated rings avoid unnecessary composition; rapid target reversals remain continuous |
| Numeral | 9↔10, 99↔100, negative/decimal/localized fallback reaches correct final text | No clipped digits or parent width jumps; rapid updates and animation-off behavior |
| Connected buttons | Single/multi selections and all shape policies stay correct | Rapid selection, custom shape changes, RTL and disabled controls |
| Scrollbar | Ends, reverse layout, variable sizes/spans, resizing and drag cancel stay correct | Thumb tracks drag/fling without new frame overruns |
| Sheets/toolbars | Hide-before-dismiss, cancellation and latest callback; label toggle preserves focus if redesigned | IME/rotation/large text; any geometry transition remains continuous |
| Shimmer/pulse | Local opt-out stops animation; theme and resize render correctly | Repeated placeholders have bounded draw allocations and frame cost |
| Gallery | Chosen component/control settings survive recreation | Replays expose the same scenario on wasm and Android |

For motion correctness tests, use a controlled clock and intermediate-frame assertions, not only an idle screenshot or final selected label. For performance, use release frame measurements rather than emulator/debug impressions. Apply narrow changes first and keep the source-only candidates separate from verified device failures.

## Verified patch and integration

On 2026-11-09, the local `fix/navigation-selection-motion` branch restores configurable navigation
item bounds animation, label fade/size transitions and press feedback. Caption fitting now uses the
widest label and the selected appearance's geometry. Dynamic press-scale updates retain the
interaction collector and use the latest scale.

Android and WebAssembly compilation, all 31 Android host tests, the gallery WebAssembly distribution
and Android harness/test assembly passed. Debug/release lint passed with no errors; each variant
reported one `ObsoleteLintCustomCheck` warning for the AndroidX Lifecycle registry, so
`UnsafeLifecycleWhenUsage` and `RepeatOnLifecycleWrongUsage` were skipped by that lint toolchain.
All 27 native component/gallery/motion checks passed on the Android 17 emulator with system
animations disabled as in CI, including intermediate widths, rapid retargeting, stable caption
placement, segmented text fitting and updated press scales. Device animation settings were restored.
The 12 dependency-updater tests passed, and all nine retained JVM entry points passed compatibility
verification.
Thor built with the local Asgard source override, passed its nine navigation editor checks, and its
patched Foss debug APK was installed on the connected physical device. These are correctness checks;
physical-device frame timing and WebAssembly runtime smoothness have not been benchmarked.

The navigation motion patch was merged in PR #10 without a version bump. The 2.1.1 release now
bundles that patch with normalized progress-ring drawing/semantics, deferred ring animation
sampling and cached shimmer drawing. The Android harness's Lifecycle warning is resolved with
2.11.0, and both previously skipped detectors were verified using temporary faulty examples.
The new optimized benchmark variant and its results are documented in
[performance-2.1.1.md](performance-2.1.1.md), including 32 passing native tests and the emulator-only
measurement limits. The broader unmeasured candidates above remain follow-ups.

The publication workflow runs when the fresh non-SNAPSHOT `VERSION_NAME=2.1.1` is merged into
`main`. Thor still needs the published artifact and a dependency-pin update before ordinary
Maven Central builds receive the navigation motion fix.
