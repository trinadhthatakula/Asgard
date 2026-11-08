# Component expansion implementation and validation

Implemented on `feat/asgard-component-expansion`, starting from `97903e9`. VERSION_NAME remains
2.0.1; no Maven publication, release tag, app migration or pull request has been performed.
The initial audit reports remain historical evidence, rather than descriptions of the updated UI.

## Implemented

- ConnectedButtonGroup retains both original entry points and adds required shape-configuration
  overloads: expressive, fixed connected, uniform and per-position/per-state shapes. Singleton
  geometry is corrected; primary selection colors remain independent of shape behavior.
- AsgardNavigationBar/Rail retain their original APIs and add independent placement, appearance,
  label, icon, shape, spacing, elevation and inset configuration. Destinations remain named and
  selected in semantics. Measured widths select caption/stacked layouts; overflow target sets scroll.
- AsgardSheet/ActionSheet provide bounded scrolling content and wrapping footer actions. The
  dismiss helper hides before removal. AsgardPicker/PickerSheet keep filtering/history caller-owned
  and namespace repeated domain item keys by section.
- AsgardActionToolbar owns rendering and state styling; apps own availability/permissions.
  AsgardAvatar overlays caller image content on initials/icon fallback without image-loader dependencies.
- AsgardLazyScrollbar uses public Compose indicator/state APIs, a conflated request queue,
  keyboard/progress actions and list/grid adapters. Thor's GPL implementation was not relocated.
- AsgardCalendarHeatmap maps actual LocalDate/value data, sums duplicate dates, distinguishes
  missing/zero, pads complete weeks and validates range/value input. Cell/date labels and selection
  remain caller-configurable. The gallery uses dated sample entries that update when recorded.
- Disabled search Clear and settings switch interaction semantics are fixed; sliders expose their
  label and formatted value. Toggle-row subtitle marquees are noninteractive to retain one switch
  target; the old parameter is kept for compatibility and the KDoc describes that behavior.
- The gallery uses a compact detail pane below 840dp, searchable navigation, separate appearance
  controls, stable URL IDs, browser back handling, bounded selectable code and clipboard completion
  handling. Dark/light chart palettes use distinct theme roles; RTL and text-scale controls are available.
- The gallery identifies its source version/revision and distinguishes tagged release builds from
  development builds. New APIs in development builds are explicitly labeled as requiring source/SNAPSHOT use.
- SDK 37.2 satisfies the resolved Compose AAR metadata. PR CI now also builds the production wasm
  distribution and checks retained JVM entry points. CI/publication workflows install the compile SDK.

The [API guide](../../component-expansion.md) contains caller-owned boundaries and examples.
[Draft release notes](../../component-expansion-release-notes.md) cover the complete expansion.

## Automated checks

- Android library compilation and wasm library/demo compilation passed.
- Production wasm distribution passed. Webpack reports the existing class of large wasm/JS
  asset warnings; the build succeeds. These checks do not measure load performance.
- 28 host tests passed: 10 existing domain tests, 9 new date/key/scrollbar/layout math tests and
  9 native Compose interaction tests hosted by Robolectric at SDK 35.
- Native tests cover disabled Clear, one switch target/one callback, hidden navigation names and
  selected state, independent multiselect, duplicate picker items across sections, scrollbar progress
  actions and distinct navigation targets at 320dp with 2x text and RTL. Additional rail and long-label
  cases verify named selection and minimum target bounds. Segmented rail caps follow the vertical axis.
- 12 Python tooling tests passed.
- The retained JVM descriptor check passed for all 9 baseline entry points in 6 modified existing
  API classes. A commonTest consumer fixture also compiles old positional/default calls.

On this machine, Yarn discovers a pnpm packageManager in an ancestor directory. Production builds
use the supported per-command `SKIP_YARN_COREPACK_CHECK=1` override; the ancestor package file and
user's global package-manager settings were not changed.

## Browser evidence

The production gallery was exercised with ego-browser/Chromium. Screenshots are under
[implementation-screenshots](implementation-screenshots/).

- Navigation/detail layouts were captured at 320, 360, 390, 768 and 1440px. The 320px inline-label
  wrapping defect found during verification was corrected with measured stacking for always-visible labels.
- The searchable component browser filtered "Heatmap", selected its example and updated the URL.
- A 320x480 action-sheet check retained reachable Close/Continue footer buttons beside scrolling copy.
- Dragging the list scrollbar moved the visible range from item 1 to item 46; dragging the grid
  with variable spans moved from cell 1 into the cell 36 range.
- Adding a dated heatmap entry changed the selected date's value from 1.0 to 2.0 and its cell intensity.
- The toolbar's Copy action changed the displayed last action to Copy; tapping the named avatar
  incremented the profile counter from zero to one.
- Dark mode, RTL and 2x text scale were exercised via the gallery's appearance controls. Escape
  dismissed the appearance sheet. Native target bounds are also covered by the Robolectric test.

Some interaction screenshots precede the final source-metadata banner; that banner changes the
header text, rather than the component interaction contracts demonstrated in those captures.

## Remaining release validation

- Exercise an independently compiled published-2.0.1 consumer against the updated library. The
  JVM descriptor and source-call checks are useful evidence but do not replace that runtime fixture.
- Physical Android/iOS browsers, screen readers and native device smoke checks have not been run.
  `adb devices` returned no connected devices during this task; host tests use Robolectric.
- Clipboard rejection/success handling is implemented, but browser permission-denial behavior
  still needs dedicated verification. Header/keyboard focus and further content-change/reverse-grid
  cases should be included in the final release review.
- Keep the version bump/publication gate open until the remaining release checks and review are complete.
  All six components are implemented; these limits concern release verification and app adoption.


## Final workflow check

- `.github/workflows/publish.yml` triggers for gradle.properties changes pushed to main, or manual
  dispatch. Detection skips unchanged versions, SNAPSHOTs and existing tags. A fresh VERSION_NAME
  publishes Maven artifacts, then creates the annotated tag and GitHub release. Manual force
  bypasses the version-change comparison, while retaining the SNAPSHOT/existing-tag guards.
- `publishToMavenCentral --dry-run --no-configuration-cache` succeeded. Its task graph includes
  Android/wasm compilation and host tests before publication tasks. No signing/upload/tag/release
  action was executed by the dry run.
- `.github/workflows/deploy-demo.yml` deploys the production gallery on every main push or manual
  dispatch, independently of a Maven release. Source-build labeling prevents an untagged preview
  from claiming that its new APIs are already published.
- `.github/workflows/pr-ci.yml` validates the complete Android/wasm/test/production build and
  retained JVM entry points for PRs to main. PR and publication workflows install SDK 37.2.
- VERSION_NAME is still 2.0.1. This implementation branch does not trigger automatic publication;
  the eventual version bump on main is the release trigger.
