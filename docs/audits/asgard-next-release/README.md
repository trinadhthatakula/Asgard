# Asgard next-release audit and implementation plan

Date: 2026-11-08. Audited source: `97903e9` on `origin/main`, after PR #8.

Two independent agents audited the component APIs and the live gallery. The primary agent
checked the release state, reviewed the reports, verified the main source findings, and
inspected representative mobile and desktop screenshots. This directory retains the original analysis and approved scope. Implementation now exists
on `feat/asgard-component-expansion`; see the validation record for the current evidence.

The user chose to bundle customization, gallery fixes, all six cross-app component suggestions,
and navigation variations into the next release. All six APIs, navigation/shape configuration
and the gallery changes are implemented on the topic branch; release validation remains tracked. Recommend
**2.1.0**, subject to preserving existing public entry points and passing compatibility checks.
Maven Central and GitHub still list **2.0.1** as the latest release. The live gallery already
uses main's unreleased multiselect API, because gallery deployment follows every main push.

- [Component audit and customization matrix](components.md)
- [Gallery audit and browser evidence](gallery.md)
- [Cross-app component opportunities](cross-app-components.md)
- [Cross-app navigation implementations and extraction boundaries](cross-app-navigation.md)
- [Approved release scope and acceptance checklist](release-scope.md)
- [Implementation and validation record](implementation-validation.md)
- [Public API guide](../../component-expansion.md)
- [390px mobile evidence](screenshots/mobile-390-live-multiselect.png)
- [320px mobile evidence](screenshots/mobile-320-live-multiselect.png)
- [1440px desktop comparison](screenshots/desktop-live-header.png)

## Findings that should drive the release

| Area | Evidence | Proposed work |
|---|---|---|
| Connected button shapes | Both overloads force private Material shape selection. A single item receives only a leading end cap. | Add caller-controlled shapes with expressive, fixed-connected, uniform, and per-state configurations. Fix singleton geometry. |
| Navigation | Outer surface shape is configurable, but indicator geometry and placement are fixed. Collapsed default destinations can have no accessible name; selection semantics are absent. | Separate docked/floating placement from expanding/segmented item appearance. Share named, selectable destination behavior. |
| Input semantics | Search Clear does not receive the disabled flag. Settings switches have competing row/switch actions; sliders lack explicit label/value semantics. | Fix these interaction and accessibility contracts and add focused UI tests. |
| Mobile gallery | A fixed 240dp sidebar and 48dp detail gutters leave about 101dp at 390px and 31dp at 320px. The toolbar overlaps; tapping the visible switch thumb selected a color preset. | Use a compact, single-pane layout with searchable component navigation and separate appearance controls. |
| Gallery fidelity | Usage snippets differ from live previews; chart series share the same theme color. Copy success is reported before the clipboard promise completes. | Align previews and code, use distinct theme roles, and handle clipboard completion/failure. |
| Release confidence | Ten library tests cover math/tokens. PR CI compiles the demo; its production distribution is built only during deployment. | Add UI/semantics coverage, compatibility fixtures, production-bundle validation, and responsive browser checks before publishing. |

Source references and verification limits for each finding are in the two detailed reports.

## Component design decisions

### Connected buttons

Keep shape behavior independent of existing `colors` customization. Offer:

- **Expressive defaults:** current selected/pressed shape behavior, with proper standalone
  geometry for a single button.
- **Fixed connected shapes:** retain leading/middle/trailing end caps in every state; colors
  can still change on selection.
- **Uniform shape:** apply a caller's literal shape to every button and state.
- **Custom state shapes:** separate base, selected, and pressed shapes for each logical
  position, including the singleton case. Pressed takes precedence over selected.

Use the same contract for single and multiselect. Preserve current signatures as delegates;
add overloads requiring a distinct shapes configuration. Appending defaulted parameters alone
does not preserve JVM/Compose binary signatures. Preserve RTL start/end behavior and document
that arbitrary non-corner shapes may switch rather than morph.

### Navigation

Treat these as independent choices rather than a single `Default/Floating/Segmented` enum:

| Container placement | Item appearance | Label policy |
|---|---|---|
| Docked or floating | Expanding pill, segmented, or standard icon-over-label | Selected only, always, hidden, or selected-caption fallback |

This allows floating + segmented navigation. Expose container/indicator shapes, padding,
colors, width bounds, elevation, and explicit inset ownership through a coherent configuration.
Keep existing bar/rail entry points and defaults. Destination naming, selection, badges,
focus and activation should use shared behavior across appearances.

Segmented navigation also supports inline icon/text and text-only destination content. Keep
destination selection semantics distinct from connected buttons used for form-value choices.
Use measured available width and labels to trigger the selected-caption fallback, rather than
assuming four destinations. The navigation report records source inputs from Adhan, iScan and
Tely and the boundaries that remain app-owned.

## Gallery design

- At compact widths, show one full-width detail pane with 16dp gutters. At 320px this leaves
  288px for the preview. Use a Components button opening searchable navigation; close it after
  selection and move focus to the detail title.
- Put appearance settings in a separate panel/sheet. Keep the desktop sidebar only when the
  remaining content width is sufficient; start by evaluating an approximately 840dp breakpoint.
- Keep Preview, Controls and Code easy to reach. Use one main vertical scroll on phones and a
  bounded, selectable, horizontally scrollable code pane with a reachable Copy action.
- Give all catalog entries (42 at the audit baseline, plus the new components and variations)
  stable IDs, shareable URLs, browser-back support, and a defined
  scroll/reset policy.
- Bind controls to the demonstrated configuration. Generate matching code where practical;
  otherwise label a static minimal example clearly and make its initial state accurate.
- Show the demonstrated release/commit and matching install coordinate. Identify development
  examples that require unpublished APIs.
- Name switches, mark headings and selected navigation, preserve keyboard focus visibility,
  and use distinct theme color roles for chart series.

## Implementation and release sequence

1. Implement component correctness fixes and focused UI regression coverage.
2. Add shape customization and navigation options with default-appearance and compatibility
   fixtures. Compile current consumers and exercise an Android consumer compiled against 2.0.1.
3. Implement the sheet/action-sheet scaffold, then the searchable picker that composes it.
   Add the action toolbar and avatar with caller-owned data, actions, icons and image content.
4. Implement the portable list/grid scrollbar and a real date/value calendar heatmap. Both are
   required for this release, rather than deferred follow-ups. Define their data/layout contracts
   and cover the meaningful math and interaction cases in the scope checklist.
5. Rework the responsive gallery and its examples using the actual library APIs. Every new
   component and navigation/shape variation must have a working example and matching usage code.
6. Run existing Android/wasm compilation, host tests, and Python tooling tests. Add production
   wasm distribution creation and browser smoke checks to PR validation.
7. Verify representative components at 320/360/390/768/1440px, light/dark, larger text, RTL,
   keyboard and touch. Confirm no overlapping hit regions or inaccessible destinations.
   Verify Android Chrome and iOS Safari on physical devices before claiming that coverage.
8. Update API documentation, gallery version context, agent integration reference, and release
   notes. Prepare the 2.1.0 version bump only after the bundled changes pass their gates.
9. Merge the release change to main. The existing publication workflow detects a new
   `VERSION_NAME`, publishes Maven artifacts, and creates a tag/GitHub release. Verify the
   published artifacts before updating Thor's pinned dependency.

## New components grounded in our apps

A follow-up source scan screened 49 application/prototype roots under StudioProjects and
traced promising controls to implementations and callers. The approved additions are:

- Sheet/action-sheet scaffold (Tely, Adhan).
- Searchable picker, inline and in a sheet (FluxRates and region pickers).
- Action/selection toolbar (FrameGallery, Kut, Thor).
- Avatar (Tely, FrameGallery).
- Draggable lazy-list and lazy-grid scrollbar (Thor).
- Calendar heatmap with actual date/value mapping (MindLoom adoption case).

All six are required for the bundled release. No application code has been moved.

Implement the sheet scaffold before the picker, and compose existing Asgard primitives where
possible. The scrollbar requires a portable Asgard implementation. MindLoom's calendar
heatmap is a useful new-chart idea, but its current implementation ignores input dates and draws
a placeholder pattern, so it needs a fresh implementation within this release. Navigation variants belong
to the planned work; Adhan, iScan and Tely provide concrete inputs rather than separate duplicate
components. Details and app-owned boundaries are in the cross-app reports linked above.

## Follow-ups and limits

The expanded scope includes the six named additions and navigation variations. Stat cards,
tiles, banners and dialogs already expose substantial customization. The detailed component audit lists narrower
future options for wrappers, action-chip shape, chart finite-input handling and chart accessibility.
Keyboard focus beneath `AsgardLockedOverlay` requires targeted verification; it is currently a
source-level concern, not a reproduced bypass.

The gallery was exercised in Chromium at 390x844, 320x740 and 1440x900. Physical devices,
Safari, screen readers and increased text size were not tested during this audit. The component
agent performed source analysis, not runtime testing. PR #8's green checks and successful gallery
deployment establish the current build baseline; they do not validate the proposed changes.
