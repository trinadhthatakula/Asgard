# Component expansion implementation and validation

Implemented on `feat/asgard-component-expansion`, starting from `97903e9`. VERSION_NAME is
2.1.0 for the release PR. Maven publication, release tagging and application migrations have
not been performed. Earlier checks below record the implementation before the version bump.
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
- 29 host tests passed: 10 existing domain tests, 10 new date/key/scrollbar/layout math tests and
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

- Physical Android devices and iOS/Safari browser checks remain unrun. This machine has no
  connected physical device or iOS simulator; the browser evidence uses ego-browser/Chromium.
- TalkBack keyboard focus/activation has a native smoke check. Spoken announcements, touch
  exploration and a complete screen-reader journey remain outside the recorded coverage.
- Browser clipboard denial/retry was exercised with injected rejected/resolved write promises.
  The native clipboard has an actual write/read test. Real browser permission-prompt behavior
  remains unverified; the promise tests do not imply that a browser permission was changed.
- API 35 emulator coverage runs in PR CI. Local device tests ran on API 37; review the hosted
  result as part of release approval.
- VERSION_NAME is now 2.1.0 for the requested release PR. These outstanding checks and review
  remain part of the release decision.


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
- The initial workflow check used VERSION_NAME 2.0.1. The release PR now bumps it to 2.1.0.
  Pushing this topic branch does not publish; merging the version bump to main is the release trigger.

## Follow-up: permanent Android app and remaining local checks

- Added `:androidApp`, namespace/application ID `com.valhalla.asgard.app`, sharing the complete
  `:demo` catalog. Native platform services supply clipboard writes, saved last selection and
  Android Back handling. Insets, system font scale, initial system theme/direction and bar icon
  appearance are handled on Android. The app/test host is documented in
  [Android gallery and tests](../../android-gallery.md).
- `:compatibilityConsumer` compiled against the strictly pinned, actual published
  `com.trinadhthatakula:asgard-android:2.0.1` AAR. Gradle dependencyInsight confirmed the published
  Android API variant on its compile classpath. Its compiled bytecode ran against the updated
  project library on the emulator, exercising all eight changed entry points present in 2.0.1.
  The merged multiselect overload is newer than published 2.0.1 and is covered separately.
- **21 native instrumentation tests passed** on `Tely_Magisk_API_37_2`, Android 17 / SDK 37,
  1080x2400 at density 420. These include compatibility, independent selection, disabled Clear,
  switch semantics, 320dp/RTL/2x-text navigation targets, animated sheet dismissal, native IME
  footer reachability, picker selection/empty results/query reset, toolbar and avatar callbacks,
  heatmap selection, scrollbar keyboard boundaries/reversed lists/reversed grids/content shrink,
  zero-travel dragging, native clipboard and gallery search/Back behavior.
- Gradle connected tests passed for the first 20 cases. The final 21-case suite was then run
  from the explicitly installed APKs with `python3 tools/run_android_tests.py --serial emulator-5554`,
  without invoking Gradle: `OK (21 tests)`, 43.635 seconds. Gradle removes its connected-test APKs;
  explicit install tasks preserve the binaries for future build-free reruns. Both APKs remain installed.
- Reversed scrollbar metrics now track physical top-to-bottom progress. Boundary requests use
  real item boundaries instead of relying on estimated pixel ranges. The extra host test covers
  reversed metrics; all **29 host tests**, **12 tooling tests** and **9 retained JVM entry points** pass.
- Android debug/release lint passed with **zero errors and one upstream warning** each:
  `ObsoleteLintCustomCheck` in the Lifecycle lint jar. `UnsafeLifecycleWhenUsage` and
  `RepeatOnLifecycleWrongUsage` are skipped by that jar on this Kotlin/lint toolchain. A fresh
  lint run with `--rerun-tasks --no-build-cache` confirmed the current manifests/resources.
- The Android and production wasm builds passed after the shared gallery/platform changes.
  Native screenshots under [android-screenshots](android-screenshots/) show portrait navigation,
  2x **system** font scale and an action sheet with both footer actions reachable in landscape
  at that scale. TalkBack enabled/touch-exploration enabled were confirmed; Tab moved the green
  accessibility focus to System and Enter unchecked it while User remained selected. Emulator
  font, rotation and enabled accessibility services were restored afterward. Android normalized
  `accessibility_enabled` to zero once no accessibility services remained enabled.
  A native track drag moved the variable-height list from item 1 to the item 58–64 range,
  independently of the instrumentation progress-action checks.
- Final Chromium promise-failure/retry screenshots and keyboard activation evidence are under
  [implementation-screenshots](implementation-screenshots/). The retry showed Copied only after
  completion; Tab/Enter activated the named avatar and changed the profile count from 0 to 1.
- PR CI now builds/lints both Android variants and the test APK, plus a separate API 35 emulator
  job. All three workflows install compile SDK 37.2. A final publication dry run passed and
  showed uploads only for `:asgard`; the Android gallery and compatibility fixture are not published.

## 2.1.0 release verification

- Re-ran the complete local PR build with VERSION_NAME 2.1.0: Android/wasm compilation,
  production wasm distribution, app/test APK builds, host tests and debug/release lint passed.
  The 29 host tests, 12 tooling tests and all 9 retained JVM entry points passed.
- Installed both 2.1.0 APKs and re-ran the build-free emulator suite: `OK (21 tests)`,
  41.09 seconds. Both APKs remain installed for future checks.
- The 2.1.0 Maven Central publication dry run passed without signing, uploading, tagging or
  creating a release. The four required publication secret names are configured; their values
  were not inspected or validated. Maven Central still reports 2.0.1 as the latest release.
- The validation limits above remain applicable. Hosted PR CI must be reviewed before merging.
