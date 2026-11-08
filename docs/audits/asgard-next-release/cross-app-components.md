# Cross-app component opportunities

Date: 2026-11-08. Source inspection of local applications under
`/Users/trinadhthatakula/StudioProjects`, alongside Asgard at `97903e9`.

A broad Kotlin-file and UI-pattern inventory screened 49 application/prototype roots.
Focused inspection traced the candidates below to their implementations and actual callers.
This was a bounded source audit, not an exhaustive review of every app or a runtime test.
No app code was moved or changed.

**Scope update:** the user approved all six components and the navigation variations for the
bundled next release. The original first-four recommendation is superseded by the
[approved release scope](release-scope.md). Implementation is present on `feat/asgard-component-expansion`; see the
[validation record](implementation-validation.md).

## Recommended priorities

| Priority | Proposed component | Source apps | Recommendation |
|---|---|---|---|
| First | Sheet scaffold / action sheet | Tely, Adhan | Extract bounded content, header/footer slots and scrollable body with reachable actions. |
| First | Searchable picker / picker sheet | FluxRates, GPSNavigator2025, FuelCheckIndia | Generalize search, sections, selection and empty states. Compose the existing Asgard search/list primitives. |
| First | Action toolbar | FrameGallery, Kut, Thor | One shared renderer for selection actions and editor tools, with optional count/cancel slots. |
| First | Avatar | Tely, FrameGallery | Image-content slot with initials/icon fallback and clear accessibility semantics; image loading stays caller-owned. |
| This release | Draggable lazy scrollbar | Thor | Include list and grid support with portable input handling and a clear layout contract. |
| This release, new implementation | Calendar heatmap | MindLoom | Implement actual date/value mapping; the existing placeholder cannot be extracted as working analytics. |

Floating/segmented navigation is already part of the agreed customization work. Adhan,
iScan and Tely provide grounded implementation inputs; use them to extend Asgard's existing
navigation components. See [the navigation report](cross-app-navigation.md).

All six are now required for the bundled release. Implement sheets before the picker so both
reuse one modal layout contract. Sequence correctness and API customization first, then the
new components and their gallery examples. The scrollbar and heatmap need more implementation
work, but they are included in this release rather than left as optional follow-ups.

Names here describe proposed APIs, not components that already exist in Asgard.

## Source references

All paths below are relative to `StudioProjects`. Line numbers refer to the local sources
inspected during this audit and may move with subsequent app changes.

### Sheet scaffold and action sheet

- `Tely/app/src/main/java/com/trinadhthatakula/tely/phone/presentation/components/TelyBottomSheet.kt:32`
  implements a window-aware maximum width and bounded content. `TelyActionSheet` at line 56
  scrolls the body while keeping a wrapping footer action row outside that scroll. Real callers
  include contact dialogs, recording controls, blocklist prompts, filters and interface selection.
- `Adhan/app/src/main/java/com/trinadh/adhan/ui/common/AppBottomSheet.kt:17`
  adds a fixed header, bounded body and animated hide/dismiss handling.

**Library boundary:** expose sheet state, dismiss callback, width bounds, shape/colors,
insets and header/body/footer slots. Document one owner for insets and scroll constraints.
Reuse Material's modal sheet rather than copying its internals. Let callers provide localized
close labels/icons and actions. Remove app test tags and window breakpoints as fixed policy.

**Keep in the app:** permission prompts, purchase behavior, contact/recording models and app-lock
policy. In particular,
`MedFile/app/src/main/java/app/medfile/clinic/ui/common/MedFileModalBottomSheet.kt:237`
contains `LocalAppIsCovered` and dismissal/app-lock coordination; that wrapper should stay in
MedFile instead of moving wholesale.

### Searchable picker

- `FluxRates_Android/app/src/main/java/com/t3nadh/currencyexchange/fluxrates/currency/presentation/home/components/CurrencyList.kt:45`
  provides search/clear, stable lazy-list keys, scroll reset on query changes, local/recent/catalog
  sections, selected rows and an empty-state live region. Its companion
  `CurrencySelectorBottomSheet.kt:34` is used by `CurrencyHomeScreen`.
- `GPSNavigator2025/app/src/main/java/com/spectra/gps/navigator/address/offline/maps/presentation/currency/components/CurrencySelectorBottomSheet.kt:18`
  and FuelCheckIndia's state picker show the same need in other domains. The latter is in
  `FuelCheckIndia/app/src/main/java/com/trinadh/fuelcheck/india/ui/home/HomeScreen.kt:875`
  and is also used during onboarding.

**Library boundary:** caller-owned query and filtered items/sections, stable keys, labels,
row/leading-content slots, selected value and selection callback. Support both an inline list
and a sheet using the shared scaffold. The generic control should not choose a currency
catalog or persist recent selections.

**Keep in the app:** currency/region types, flag assets, locale lookup, filtering policy,
recent-history storage, exclusion rules and downstream actions. Selected-state semantics,
localized clear/empty labels and keyboard behavior belong in the reusable interaction contract.

### Action toolbar

- `FrameGallery/app/src/main/kotlin/com/trinadhthatakula/framegallery/manage/SelectionToolbar.kt:15`
  uses an elevated rounded surface and horizontally scrolling actions with enabled state and
  destructive emphasis. `gallery/GalleryScreen.kt:245` uses it for real media selections.
- `Kut/app/src/main/java/com/valhalla/videoeditor/cut/music/maker/ui/editor/components/EditorToolbar.kt:38`
  adds labeled tools and an active-tool state.
- `Thor/app/src/main/java/com/valhalla/thor/presentation/widgets/MultiSelectToolBox.kt:29`
  demonstrates the same need for app-management actions.

**Library boundary:** icon/content, localized label, enabled/selected state, emphasis and callback
per action; configurable surface shape/colors; an optional leading selection-count/cancel area.
Keep compact actions reachable at narrow widths and name icon-only actions. One
`AsgardActionToolbar` can cover these layouts without separate media and editor controls.

**Keep in the app:** action IDs/models, string-based media dispatch, editor tool enums,
privilege checks and app-action availability. Thor decides whether an operation is permitted;
Asgard renders the resulting action state.

### Avatar

- `Tely/app/src/main/java/com/trinadhthatakula/tely/phone/presentation/components/ContactAvatar.kt:51`
  renders initials/icon fallback under a photo, with decorative or named semantics and an
  optional action. It is used in contacts and call rows.
- `FrameGallery/app/src/main/kotlin/com/trinadhthatakula/framegallery/profile/ProfileAvatar.kt:42`
  has a custom shape and image fallback, used by `ProfileScreen.kt:163` and profile components.

**Library boundary:** caller image-content slot, initials/icon fallback, size, shape and colors.
Make decorative versus named usage explicit. Use an optional caller-owned click handler;
avoid obligating every consumer to install an image loader.

**Keep in the app:** Coil, contact URI permissions/cache invalidation, photo-version keys,
account matching, video-avatar playback and lifecycle coordination.

### Draggable lazy scrollbar

- `Thor/app/src/main/java/com/valhalla/thor/presentation/widgets/DraggableLazyScrollbar.kt:83`
  exposes a list overload; line 126 adds grid support, and line 194 starts the shared
  implementation. It includes thumb dragging, a range bubble, keyboard and semantics support.
- Production use appears in `Thor/app/src/main/java/com/valhalla/thor/presentation/widgets/AppList.kt:647`
  and `Thor/app/src/main/java/com/valhalla/thor/presentation/freezer/FreezerScreen.kt:450`
  for list and grid layouts.

**Library boundary:** list/grid state adapters, thumb/track geometry and colors, visibility
policy, caller-provided range text and accessibility labels. State whether the host must reserve
a gutter. Replace Android keycodes with portable Compose key handling and validate variable
item sizes, grid spans, RTL, keyboard and drag behavior on Android and wasm.

**Implementation boundary:** higher effort than the first four. The file explicitly carries
GPL-3.0-or-later while Asgard uses Apache-2.0. A direct source move needs file-specific rights
and attribution resolved. The approved scope calls for a new portable Asgard implementation
of the general list/grid interaction pattern; it does not change the Thor file's license.

### Calendar heatmap

- `MindLoom/composeApp/src/commonMain/kotlin/com/trinadhthatakula/mindloom/gratitude/presentation/journal/components/CalendarHeatmap.kt:19`
  has a real caller in `JournalScreen.kt:71`.
- Its `entries` argument is unused. Lines 50–60 fill an 18-week grid with a deterministic
  placeholder pattern, despite the header saying "Past 3 months". Fixed cell sizing can overflow.

**Recommendation:** implement a new `AsgardCalendarHeatmap` around actual date/value data,
an explicit range, week-start policy, intensity scale, locale labels, selection/details semantics
and constrained-width behavior. It could serve journal consistency, habits and activity views.
Use MindLoom as an adoption case, not as proof of a working chart implementation.

## Extend existing components or keep as recipes

| App pattern | Treatment |
|---|---|
| Adhan floating/segmented navigation; iScan measured-label fallback; Tely inline/text segments | Extend shared navigation placement, appearance and label policies. Preserve route/backstack ownership in each app. |
| MedFile floating standard navigation; lux expanding pill navigation | Migration consumers of the shared navigation options, rather than duplicate public components. |
| PowerMonitor `ChargeGauge.kt:32` | Compose `AsgardProgressRing` using its existing start/sweep angles and content slot. Keep wattage/temperature rules and formatting in PowerMonitor. |
| BatteryWatch `DetailScaffold.kt:37` | Potential future screen-scaffold recipe, with real use across detail screens. Its `AdaptiveContent.kt` is only a width constant, not a finished adaptive component. |
| CashNotebook FAB cradle | Distinct later bottom-app-bar/action-dock candidate. Needs measured FAB geometry, arbitrary destination counts, RTL and semantics work before reuse. |
| Thor `TermLogger.kt:134` | Later log-pane candidate: follow output, stop following on manual scroll, jump to latest and wrapping. Remove operation/status/resource coupling first. |
| Bolt result/partition cards; ClearCalc card variations; LedgerX standard navigation | Prefer existing Asgard card/badge/banner/navigation primitives or usage recipes. Domain-specific wrappers offer limited reusable behavior. |

## Implementation contract

Keep Asgard theme-agnostic and Android/wasm compatible. Consumers supply localized text,
icons and domain data; Asgard owns layout and interaction semantics. Do not transfer navigation
controllers, persistence, app-lock state, privileged-operation policy or image-loading machinery.

Before direct source extraction, check the actual files' license/provenance and preserve required
attribution. Local authorship is useful evidence but does not establish rights for every line.
The navigation report records additional iScan provenance distinctions.

Before shipping extracted components, exercise compact layouts, larger text, RTL, keyboard,
selection semantics, disabled actions and representative app integrations. These are proposed
implementation checks; no new component was built or runtime-verified during this source audit.
