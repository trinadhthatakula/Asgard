# Asgard gallery audit — 2026-11-08

Read-only audit of `https://trinadhthatakula.github.io/Asgard/` and checkout `/Users/trinadhthatakula/StudioProjects/Asgard`, branch `chore/asgard-component-gallery-audit`, HEAD `97903e9`. The parent verified that deployment corresponds to this commit. No source edits, builds, commits, branch changes, or deployments were made. Existing untracked `handover.md` remained untouched.

Used the ego-browser skill. Resumed the previous audit's single taskspace (ID 1), then finished it exactly once. Visually inspected screenshots using the image tool. Live Chromium viewport checks: 390×844 and 320×740 with mobile/touch emulation, 1440×900 desktop. These are browser emulation results, not physical-device or Safari validation.

## Findings and ownership

**P1 — The gallery shell makes the mobile preview unusable (observed; gallery defect).** At 390 px the fixed 240 dp navigation pane, divider, and 48 dp detail padding leave approximately 101 dp for all preview content. At 320 px only approximately 31 dp remains. The title wraps into fragments; AsgardHeader's preview shows only its icon; the multiselect labels become blank / `S...`; usage code falls to one or two characters per line. The browser document itself correctly measures 390×844 / 320×740, with no oversized document width. This is Compose layout behavior, not a missing HTML viewport tag. Source: `GalleryApp.kt:76-108` unconditional two-pane Row and 240 dp sidebar; `:118` 24 dp gutters. `index.html:5` already supplies viewport metadata; `Main.kt:9-10` mounts ComposeViewport. Catalog examples usually pass fillMaxWidth (`AsgardCatalog.kt:100,112,141,158`), accepting the wrapper's tiny constraints. These screenshots alone do not establish defects in the reusable components.

**P1 — Theme controls collide and misdirect touch input (observed; gallery defect).** On both mobile widths Light becomes a vertical stack of characters and the Rose/Amber region overlaps the switch. At 390 px a touch at the visible switch thumb (365,65) selected Rose while Light stayed active. Source: `GalleryApp.kt:53-74`, one nonwrapping Row containing title, spacer, four chips, label and switch. Desktop dark-mode switching worked normally.

**P1 — Navigation/discovery consumes the screen (observed + source; gallery defect).** The catalog contains 42 entries, with no shell search, category picker, or compact navigation. Charts require scrolling down the sidebar. Selection keeps the URL at `/Asgard/`; reload returns to the first entry. Source: `GalleryApp.kt:44-48,76-108` in-memory selection and category list; no route or search state. At mobile size the page has two independent vertical scrolling areas: touch scrolling the narrow right strip moved the detail while sidebar scroll moved the list. Scrolling itself worked. Source: `:77` LazyColumn and `:118` verticalScroll. Detail scroll is not keyed/reset by entry, although preview state is keyed (`:118,132-134`); retained detail position is a source-level risk, not a separately confirmed browser defect.

**P2 — Code and controls need a usable presentation and an explicit fidelity contract (observed + source; gallery defect).** Copy displays `Copied!`, but it wraps into three fragments on mobile. Code is plain Text with normal wrapping, without horizontal scrolling or a selection container (`GalleryApp.kt:146-165`). The copied text is always the static entry.code. It does not track the preview controls: line chart starts with two series and a percentage y-axis, while its snippet shows one series and an unscaled formatter (`AsgardCatalog.kt:597-638`); the single-select snippet lists Day/Week while the live preview also contains Month (`:130-141`). Turning chart area fill off changed the preview successfully, but not the snippet. This is gallery example fidelity, not a missing library capability. Source-only additional defect: `Clipboard.wasmJs.kt:4-9` starts clipboard.writeText without awaiting its promise, while `GalleryApp.kt:157` immediately declares success. Clipboard contents and blocked-permission behavior were not inspected.

**P2 — Accessibility semantics are incomplete (observed + source; gallery defect).** Basic Tab→Enter navigation from the selected header successfully activated AsgardNavigationBar. Browser accessibility data exposed the theme switch and three chart switches with empty accessible names, and zero heading roles. Source: detached Text/Switch in `GalleryApp.kt:72-73` and `AsgardCatalog.kt:741-744`; visual headings lack heading semantics (`GalleryApp.kt:80-88,120`). Navigation uses clickable Text with only a color change for current selection (`:91-103`), rather than selected/current semantics. The browser's Compose accessibility snapshot sometimes lagged visual state, so checked values were not treated as reliable state verification. Screen-reader behavior, full focus order, focus visibility, keyboard trapping and large-text reflow remain untested.

**P2 — Theme presets obscure chart distinctions (observed + source; gallery defect).** Series A/B and their legend swatches both render Rose after selecting that preset. `DemoTheme.kt:24-26` assigns the same seed to primary, secondary and tertiary; the chart deliberately uses primary and secondary for separate series (`AsgardCatalog.kt:606-618,628-632`). Supply coordinated, distinct role palettes and matching on-colors. Do not describe this as a chart-library inability to accept colors.

**P2 — PR validation omits the shipped browser bundle (source-confirmed).** `.github/workflows/pr-ci.yml:34-41` compiles wasm/Android and runs Android host tests; it does not build the production distribution or exercise browser layouts. `.github/workflows/deploy-demo.yml:3-6,34-41` builds `:demo:wasmJsBrowserDistribution` only on main push/manual deployment. A successful current deployment does not validate mobile usability. Add production bundle creation and browser smoke/screenshot checks to the next gallery PR; this audit did not execute builds or CI.

## Concrete release design

- Below approximately 840 dp, use a single full-width detail view with a compact top bar, Components button opening searchable navigation, and a separate Appearance sheet/menu. Close navigation after selection and announce/focus the detail title. Keep all theme controls reachable without overlap.
- At wider widths, retain a searchable sidebar. Bound the main content width (approximately 960–1120 dp), and let previews choose fit-content/full-width or explicit 320/390/768 dp widths without forcing the entire page wider.
- Mobile detail order: title/category/description, preview, labeled Controls and Code sections. Use 16 dp side gutters: at 320 px the usable preview must be at least 288 px. Keep one main vertical scroll on mobile; navigation scroll exists only while its sheet/drawer is open.
- Give every entry a stable ID/URL, browser back support, and an intentional selection/scroll policy. Add catalog search across component name, category and useful aliases.
- Show code in a dedicated bounded pane with horizontal scrolling, selectable text and a fixed reachable Copy action. Either generate code from current controls or clearly label a static minimal example and keep its initial example accurate. Announce success only after clipboard completion; show a recoverable failure state.
- Make customization controls demonstrate supported library APIs, with current values/reset, and distinct theme roles. API expansion belongs to the component audit; gallery controls should reflect the actual supported contract.

## Acceptance gates still to run after implementation

At 320/360/390/768/1440 CSS px, light/dark modes, and increased text size: no shell overflow, overlaps or character-per-line headings; current component and preview are readily visible; all 42 entries searchable/reachable; selected component survives share/reload/back as designed. Verify at least header, single/multiple/icon button groups, settings toggle, long-content card, line chart and code pane. Touch targets should be at least 44–48 px with no overlapping hit regions. Verify touch selection, slider drag, toggle, code scroll/copy, drawer dismissal and predictable main scroll. Test keyboard-only operation, named switches, headings, selected navigation state and screen-reader announcements. Run production wasm distribution and browser smoke checks before merge, then Android Chrome and iOS Safari on actual devices, including browser chrome resizing and safe areas.

## Inspected screenshots

Most useful comparison:
- `screenshots/mobile-390-live-header.png` — collapsed preview and overlapping toolbar.
- `screenshots/mobile-320-live-multiselect.png` — almost no detail width.
- `screenshots/desktop-live-header.png` — same header rendered normally at 1440 px.

Supporting evidence:
- `mobile-390-live-multiselect.png` — unreadable toggle labels and code.
- `mobile-390-detail-scroll.png` — independent right-pane touch scrolling.
- `mobile-320-preview-scrolled.png` — one-character code column.
- `mobile-390-touch-controls.png` — first option toggled off, Rose selected by overlapped switch touch, Copy confirmation wrapping.
- `desktop-keyboard-navigation.png` — Tab→Enter selected navigation example.
- `desktop-navigation-bottom.png` — catalog discovery and stretched desktop controls.
- `desktop-chart-controls.png` — two same-color series, controls and clipped-at-fold code.
- `desktop-chart-dark-controls.png` — dark theme and area-fill toggle visibly work.

All supporting paths are under `screenshots/`. Older screenshots from the resumed attempt remain there; the `live` images and chart/control images above were captured and inspected in this completed audit.

Release-context addition from the parent's independent verification: gallery deploys every main push and already shows merged multiselect, while the latest published Maven artifact remains 2.0.1. The gallery should display the demonstrated version/commit, identify release versus development examples, and provide the matching installation coordinate plus source/release links. This prevents apparently usable snippets from silently requiring unpublished code. No additional browser verification was performed for the artifact-version claim.
