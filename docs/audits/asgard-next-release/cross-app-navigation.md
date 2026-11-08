# Cross-app navigation candidates for Asgard

Source-only, read-only inspection under `/Users/trinadhthatakula/StudioProjects`. No builds, runtime checks, migrations, commits, branch changes or app-source edits were performed. The applicable user-level instructions and iScan `AGENTS.md` were read; no additional `AGENTS.md` was found in the seven app trees. Existing dirty files were preserved.

Inspected snapshots: Tely `2ec04d9`, Adhan `a2a4e61`, MedFile `d23362d`, iScan `d7f160e`, LedgerX `d9d3126`, CashNotebook `91f7bcb`, lux `25b927e`. References below describe these local sources, not necessarily published app versions.

## Recommendation

Use **Adhan's navigation treatments plus iScan's layout/semantics design as inputs to the planned Asgard variants**. These already solve the requested floating/segmented cases and have real app callers. Tely confirms demand for text-only segments and independent dock/floating placement. MedFile and lux are good migration consumers, not reasons to add nearly identical components. CashNotebook's FAB cutout is a distinct later bottom-app-bar candidate. LedgerX contributes a usage recipe rather than reusable rendering logic.

Keep the 2.1 candidate focused: container placement/treatment, destination appearance, label layout, shared semantics. Do not move NavController/NavBackStack mutations, route enums, preferences, brand glyph systems, clinic/scanner/dialer actions, ad state or app dependency injection into Asgard.

## Source key

All paths are relative to `/Users/trinadhthatakula/StudioProjects`.

| Key | Source file |
|---|---|
| Tely nav | `Tely/app/src/main/java/com/trinadhthatakula/tely/phone/presentation/styles/InterfaceNavigation.kt` |
| Tely styles | `Tely/app/src/main/java/com/trinadhthatakula/tely/phone/presentation/styles/StyleComponents.kt` |
| Tely host | `Tely/app/src/main/java/com/trinadhthatakula/tely/phone/presentation/styles/InterfaceScaffold.kt` |
| Adhan nav | `Adhan/app/src/main/java/com/trinadh/adhan/navigation/HorizonBottomNavigation.kt` |
| Adhan host | `Adhan/app/src/main/java/com/trinadh/adhan/navigation/AdhanNavGraph.kt` |
| iScan nav | `iScan/app/src/main/java/com/valhalla/iscan/qr/ui/navigation/NavigationAppearance.kt` |
| iScan host | `iScan/app/src/main/java/com/valhalla/iscan/qr/ui/navigation/IscanNavigation.kt` |
| MedFile nav | `MedFile/app/src/main/java/app/medfile/clinic/ui/components/MedFileNavBar.kt` |
| MedFile host | `MedFile/app/src/main/java/app/medfile/clinic/ui/MainContainer.kt` |
| Cash nav | `CashNotebook/app/src/main/java/com/rms/expensetrackeroffline/mymoneymanager/daily/budget/planner/tracksavings/ui/widgets/CustomBottomBar.kt` |
| Cash shape | `CashNotebook/app/src/main/java/com/rms/expensetrackeroffline/mymoneymanager/daily/budget/planner/tracksavings/ui/widgets/CustomBottomBarShape.kt` |
| Cash host | `CashNotebook/app/src/main/java/com/rms/expensetrackeroffline/mymoneymanager/daily/budget/planner/tracksavings/ui/screens/HomeScreen.kt` |
| LedgerX nav | `LedgerX/shared/src/commonMain/kotlin/com/trinadhthatakula/ledgerx/ui/navigation/BottomNavBar.kt` |
| LedgerX host | `LedgerX/shared/src/commonMain/kotlin/com/trinadhthatakula/ledgerx/ui/navigation/AppNavigation.kt` |
| lux nav | `lux/app/src/main/kotlin/com/valhalla/lux/presentation/common/NavigationBar.kt` |
| lux host | `lux/app/src/main/kotlin/com/valhalla/lux/presentation/app/App.kt` |

## Ranked candidates and actual use

### 1. Adhan floating/segmented navigation — include as Asgard variants

**Reusable pattern:** `HorizonBottomNavigation` chooses a bounded rounded dock versus a bordered/elevated floating container (Adhan nav:59-80). Its destinations use animated weights: selected floating item 1.9, others 1; segmented items stay equal width (86-104). Floating uses icon plus selected label, segmented uses icon over always-visible label (105-143). Colors come from MaterialTheme, with independent color animation. This closely matches the requested additions.

**Production evidence:** Adhan host:73-92 reads the preference flow, defaults to `SEGMENTED`, and calls this renderer from the real Scaffold bottom bar. `Adhan/app/src/main/java/com/trinadh/adhan/ui/settings/BottomNavigationStyleSetting.kt:42-57` presents both options. It is not just a preview component.

**Extract:** the two container treatments, item arrangements, bounded width, and consistent selection semantics into Asgard's shared navigation implementation. Keep floating/docked independent of segmented/expanding so consumers can combine them. Use icon vectors or icon slots and localized labels supplied by consumers instead of Android `vectorResource`/`stringResource`, `BottomNavItem.all` and `BottomNavigationStyle` (Adhan nav:49,54-56,83-85,111,134).

**Useful correctness precedent:** `selectableGroup`, `Role.Tab`, explicit accessible names, and decorative child semantics (79,102-103,119,137) are stronger than current Asgard's plain clickable destinations. Adopt those semantics in every Asgard style.

**Caveats:** app code ties "floating" to item expansion and "segmented" to a particular dock; don't preserve that coupling. It owns horizontal/bottom safeDrawing insets and paints a full-width outer background (61-65), so this "floating" surface is still in a bottom-bar region rather than an overlay over app content. Expose inset/outer-container ownership. Fixed weights and ellipsized labels need large-font/translated-label tests; weighted layout alone does not prove every label fits. RTL reveal alignment needs checking even though Row and padding use logical placement. No runtime behavior verified.

### 2. iScan measured-label fallback and shared destination wrapper — include as behavior/options

**Reusable pattern:** it measures destination text at the user's current font scale and moves the selected label into a separate caption when in-row text would crowd the targets (iScan nav:67-79,94-116). Segmented items have logical leading/trailing corner treatment (117-122). NavigationTarget separates layout parent-data from TooltipBox, provides explicit naming and selectable semantics, and treats previews as noninteractive (157-175). The rail reuses the same target renderer (183-200).

**Production evidence:** iScan host:81-101 uses the bar for root destinations below 600.dp, hides it for IME visibility, and uses the rail for wider windows. `iScan/app/src/main/java/com/valhalla/iscan/qr/settings/SettingsScreen.kt:139` uses the same renderer for appearance choices. `iScan/app/src/androidTest/java/com/valhalla/iscan/qr/ui/NavigationAppearanceTest.kt:29-62` contains assertions for 320.dp, light/dark and 1/1.5/2 font scales, 48.dp bounds, names, nonoverlap and selected state. These tests were read, not executed; their presence is not evidence they currently pass.

**Extract:** a layout policy such as `SelectedCaptionWhenNeeded`, a shared destination semantics/focus/tooltip wrapper, and an explicit passive-preview mode. These should enhance Asgard nav, not become a separate "iScan nav" component. Its style named `ASGARD` is a local implementation: the file imports Compose and app types, not Asgard navigation.

**Remove coupling:** `MainDestination` enum, resource IDs, route strings, `SignalIcon`/`SignalSymbol`, stored preference IDs, and special-casing SCAN (34-52,171-172). SCAN launches a workflow and correctly uses Role.Button rather than pretending to select a permanent tab; retain that distinction in the app adapter or an explicitly modeled action slot, not a hardcoded generic-nav special case.

**Generalization work:** the fit formula assumes four items (`/4`, `48.dp * 3`, index 3 at 78-79,120-121); derive from item count and support singleton/empty-list contracts. It limits the row to 480.dp even for an 840.dp outer dock. Extremely narrow parents may still defeat `sizeIn(minWidth=48.dp)` when incoming constraints are tighter; do not promote the comments into an unconditional fit guarantee. Insets are owned by the host wrapper (iScan host:89-99), except the rail owns its own safeDrawing insets (iScan nav:186). Preserve this clear boundary in a documented Asgard contract. Test RTL and labels longer than the fixed English test fixture.

### 3. Tely pill segments and navigation/action separation — add a segment/label option, retain app scaffold

**Reusable pattern:** LUNAR navigation renders equal-weight inline icon+label pills; other styles use icon-over-label tabs (Tely nav:25-65). `ReachSegments` provides two text-only pill segments (111-122). These support an Asgard segmented item appearance that works with text-only or icon+text content, independently of where it sits. `LedgerNavigation` contains an underlined top-tab treatment (98-107); ordinary Material tabs or a later Asgard tab recipe already suit it.

**Production evidence:** Tely host:136-166 puts PORTRAIT/MOSAIC in Scaffold.bottomBar and LUNAR in the centered floating-action area alongside a separate contextual action. ReachSegments/ReachDialDock are used at Tely host:206 and `Tely/.../presentation/styles/RecommendedContacts.kt:177`; LedgerNavigation is used at `RecommendedContacts.kt:41` and Tely host:195. The source paths with `...` in this paragraph expand to the same `app/src/main/java/com/trinadhthatakula/tely/phone` prefix as Tely nav.

**Extract:** text-only and always-visible inline label arrangements, state-aware shape/color independence, and examples that keep a primary action outside the destination set. A form-value choice can use ConnectedButtonGroup; destination segments should keep navigation/tab semantics. Avoid shipping two indistinguishable pill components just because one app calls its control "ReachSegments".

**Keep in Tely:** UiStyle, TelyRoute, InterfaceActions, phone labels, glyph families, brand headers, dialer/contact actions, and the LUNAR-specific floating scaffold. Existing tabs have explicit role/selected semantics in most styles (52,118), but LedgerNavigation lacks selected semantics (100-103) and containers lack a selectable group. Small typography and hardcoded English need consumer-controlled style/text. `floating=true` means the host owns spacing/insets; do not add an unconditional internal navigationBarsPadding during migration (Tely nav:30-35; Tely host:145-147).

**StyleComponents layout note:** `StyleCallRow` deliberately gives details and call actions separate named targets (Tely styles:99-123). This is a good example for AsgardListRow's primary/trailing action contract, not a reusable telephony component. StyleSearch/Card/ProviderState and date/SIM/call formatting are outside this navigation audit; domain models and Java-time formatting should not move with a layout extraction.

### 4. MedFile floating standard Material navigation — migration target / recipe

**Pattern and use:** a rounded elevated Surface wraps standard Material `NavigationBar` and zeroes the inner bar's insets (MedFile nav:75-115), paired with standard `NavigationRail` (119-147). MedFile host:202-240 chooses rail at 600.dp and floating bar otherwise. This is actual production layout.

**Recommendation:** make it a consumer of Asgard's floating container plus standard icon-over-label destination appearance, or document the small M3 composition as a recipe. Do not copy it into a second floating-nav component. It demonstrates why outer Surface style and destination presentation should be separate.

**Coupling/caveats:** `NavTabItem` contains MedFileRoute and app-owned selected/unselected icons and hardcoded titles (40-71); adapters should map these to generic labels/icons/callbacks. Keep the clinical save guard and `backStack.clear()` policy in MainContainer (175-182). Outer modifier owns navigationBarsPadding while inner M3 insets are zero (84,93); preserve a single inset owner. Icon descriptions duplicate visible label text in source (103,108); verify announcement merging on migration. The component named AdaptiveNavigationRail does not choose the breakpoint—the host does. No novel adaptive routing primitive is warranted.

### 5. CashNotebook FAB cutout — distinct later bottom-app-bar candidate

**Reusable idea:** a bottom surface with a center action cradle and destination groups on both sides (Cash nav:39-82), supported by generic Compose Path geometry in DynamicFabBottomBarShape (Cash shape:178-245). That is distinct from the requested floating/segmented navigation variants and may justify a future `AsgardBottomAppBar`/action-dock abstraction with a FAB slot if a second app needs it.

**Production evidence:** Cash host:397-412 renders CustomBottomBar below the content. The actual centered FAB is owned separately by Scaffold (289-318), with a 27.dp bottom offset and context-sensitive transaction/filter/account behavior. Cash shape also contains alternatives/previews; only DynamicFabBottomBarShape is instantiated by the production bar (Cash nav:41). Do not describe every shape experiment as a shipped component.

**Why not copy now:** the bar assumes exactly four items and directly indexes 0–3 (58-79), leaves a fixed 40.dp spacer, fixes height to 70.dp, and uses `onPrimary` as its container (40,50,68-71). Only the icon is clickable; label and selected semantics are separate/missing (98-118). Its 40.dp visual icon bounds do not prove a 40.dp touch target because Compose may expand hit testing. Geometry needs edge/radius constraints: the custom position percentage is unvalidated and interpreted in physical x coordinates, ignoring LayoutDirection (Cash shape:179,186-202); the path can exceed usable space near edges or in narrow hosts. A generic extraction must coordinate measured FAB size/position and cutout geometry, use logical start/end or an explicitly physical positioning contract, and define insets. Domain BottomItem uses Android drawable/string IDs (Cash host:562), so use consumer icons/labels and callbacks instead. Do not move finance actions or ConstraintLayout screen code.

### 6. lux expanding pill navigation — consolidate into existing Asgard bar

**Pattern and use:** lux nav:46-75 and 79-150 are structurally close to current Asgard's expanding-label bar: motion-scheme colors, selected icon, press motion, animated resize and selected label. lux host:74-88 calls it for top-level destinations and manages backstack selection. It is not a new pattern worth extracting as another public component.

**Migration value:** use it as a second adoption fixture for existing Asgard navigation, passing primary/onPrimary colors and outer/indicator shapes. Its container and indicator use MaterialTheme.shapes.extraLarge (lux nav:55,108), whereas Asgard currently exposes only outer shape and hardcodes indicator geometry; this reinforces the existing indicator-shape API request. Keep NavKey/model/route logic and LuxTheme in the app (`lux/.../presentation/app/NavBarItems.kt:15-19`). Preserve selected icons, accessible names and callback behavior; fix plain-clickable selection semantics during migration (lux nav:111-115). Its unweighted SpaceEvenly row and internal navigationBarsPadding have the same bounded-layout/inset concerns as existing Asgard (61-64). Similarity is observed in source; history direction/copy origin was not established.

### 7. LedgerX standard NavigationBar — recipe, no rendering extraction

LedgerX nav:16-43 is four standard Material NavigationBarItems. LedgerX host:34-41 calls it from Scaffold for root destinations. The distinctive code is its app backstack helper (LedgerX nav:46-65), which pops/adds AppNavKey roots. Keep that policy in LedgerX and simply map root keys to generic Asgard items if it adopts the library. AndroidX Material already supplies the underlying reusable component. Its commonMain location is a useful KMP adoption fixture, not evidence of a new visual component.

## Provenance: distinguish app patterns from third-party implementation

Local git history attributes the named implementations to the user's author identities: Tely `ef17172`/`8695a50`, Adhan `7615b69`, MedFile `9618b64`, iScan `47c85a1`, LedgerX `d54e7a3`, CashNotebook `dea322a`/`5fb3b13`, lux `66d2abc`. No copied-from/third-party copyright header was found in these named navigation files. This supports describing them as **local app-layer patterns**, but author metadata and absent headers do not prove every line's original provenance.

The M3 NavigationBar/NavigationRail/Surface/Tooltip and Compose layout/animation primitives they call are third-party AndroidX/Compose library APIs, not app code to relocate. Use the dependencies normally instead of copying their internals.

iScan supplies explicit distinctions: `iScan/README.md:375` says chamo_qr is functional inspiration and no Flutter architecture/source/assets were copied; line 376 attributes theme/typography adaptations to Thor under GPL-3.0-or-later; line 377 covers OFL fonts; line 381 says there is no repository-wide root license declaration. The recommendation is to generalize the navigation geometry and behavior into neutral Asgard APIs, not move iScan's theme, fonts, glyph assets or unrelated borrowed material wholesale. Any direct source migration should preserve applicable attribution and establish the rights for those actual files. No third-party copied navigation component was positively identified by this bounded inspection.

## Release integration and verification contract

- Add navigation **options/variants** to the shared Asgard implementation; retain its current public entry points as delegates for Android/Compose binary compatibility. New required configuration overloads avoid ambiguities with existing defaulted calls. Merely appending default arguments does not preserve existing compiled signatures.
- Model container treatment (docked/floating, outer shape/elevation/insets), item appearance (expanding/segmented/standard), and label behavior (selected/always/hidden/caption fallback) independently. Only ship the subset with real adopters and reviewed defaults. A cutout action bar remains a separate follow-up.
- Consumers own route identity, backstack policy, preference persistence, workflow actions and localized strings. Asgard owns rendering/selection contracts. Avoid Android resource IDs or app icon enums in commonMain APIs.
- Transfer iScan's meaningful test ideas: constrained widths and translated text at 1/1.5/2 font scales, actual semantic bounds/nonoverlap, names, selected state, keyboard/tooltip behavior, and RTL. Derive tests from arbitrary item counts instead of copying its four-item assumptions. Include one-inset-owner fixtures for bottomBar, floating overlay, and rail; a visually floating bottom bar and an overlay need different layout examples.
- Distinguish visual bounds, semantic bounds and expanded touch bounds. No conclusion about exact touch-target size or runtime screen-reader output was established from this source-only audit. Keep Android and Wasm compilation/interaction checks plus old-ABI consumer fixtures in the implementation phase.
