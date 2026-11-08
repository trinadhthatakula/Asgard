# Asgard 2.1.1 component performance audit

The confirmed correctness fix is a progress-ring animation sample that can escape `0..1` with a custom bouncy spring. The candidate clamps each rendered/accessibility sample, retains the internal spring trajectory, and defers animated state reads to drawing/semantics. It also caches ring stroke/geometry objects and reuses shimmer gradients by translating their drawing coordinates. Public signatures and default animation specifications remain unchanged.

An emulator-only A/B comparison exercises these changes and representative unchanged motion paths. It does not establish smoothness or frame-rate guarantees on a physical device, or performance on the WebAssembly backend.

## Correctness evidence

`RenderingMotionDeviceTest` fails against the baseline with progress `1.0113152`, then passes against the candidate. It samples both directions and rapid retargeting across intermediate controlled-clock frames. Further regressions cover switching progress animation off immediately, reduced-motion/static shimmer colors, and animated shimmer pixel equivalence to the original moving-gradient algorithm across color/size changes and zero-width recovery. A further ring regression checks that cached drawing refreshes after size, stroke-width, color and custom-brush changes.

The complete Android harness suite passes 32 tests on API 37; the library passes 31 host tests. Android debug/test/optimized benchmark assembly, debug/release/benchmark lint, Kotlin Android/wasm compilation, wasm production distribution and all nine retained JVM API entry points pass. Benchmark tool fixtures cover unsupported GPU metrics, missing deadlines, empty data and invalid labels. Lifecycle detector verification is described in [lifecycle-lint.md](lifecycle-lint.md).

For this workstation, a parent directory chooses pnpm and Yarn 1.22 searches that ancestor. `SKIP_YARN_COREPACK_CHECK=1` is a per-process override for local wasm validation; no global or repository package-manager settings were changed. Existing Kotlin slider deprecation and webpack bundle-size messages are unrelated to these component changes; Android lint has zero findings.

## Measurement method

Both APKs use the same benchmark-only activity, library dependencies and deterministic workloads. Measurement APKs were built before the release metadata bump with app version name `2.1.0`; the tested component changes are being shipped in `2.1.1`. The baseline includes the earlier navigation/press fixes but predates the progress/shimmer optimizations. The candidate differs in those two library component implementations. Both are nondebuggable, R8-minified, resource-shrunk, profileable and installed in the isolated `com.valhalla.asgard.app.benchmark` package. The harness uses debug signing for installation; its runtime build is optimized rather than a debug app.

Each installation is AOT-compiled with `cmd package compile -m speed -f`. Each scene warms for four seconds, then records twelve seconds. There are no concurrent Gradle builds during the timed workloads. Window `FrameMetrics` records draw/layout/GPU/total elapsed pipeline times and each frame's actual deadline. A dedicated handler thread records into preallocated arrays. First-draw frames are excluded. Whole-process CPU time is recorded separately with `Process.getElapsedCpuTime()`.

The sequence is baseline A and candidate A (all nine scenarios), then baseline B/candidate B and baseline C/candidate C (targeted progress/shimmer scenarios). Exact APK hashes, environment and per-run summaries are retained in [performance/2.1.1/results.json](performance/2.1.1/results.json). The timing order alternates builds to expose repeat-to-repeat variation. These small repeated samples support comparisons for these workloads, not statistical population guarantees.

The emulator is `sdk_gphone16k_arm64`, Android 17/API 37, 1080 x 2400 at approximately 60 Hz, hosted on an Apple M4/macOS arm64 machine. Its EGL/Vulkan drivers are `emulation`/`ranchu`. Driver and host scheduling substantially affect the pipeline. Actual per-frame deadlines can differ from the nominal 16.67 ms display period; the report compares total duration to the recorded positive deadline instead of assuming every frame has a fixed budget.

## Measured results

Three paired samples per targeted workload show consistently lower shimmer drawing time. Median per-run p50 decreases from 0.145 to 0.101 ms and p95 from 0.586 to 0.362 ms; candidate and baseline ranges do not overlap for these draw metrics. Shimmer CPU/frame is lower in each paired sample, but whole-process/driver and emulator variation prevent treating that as an isolated library CPU profile. Progress results overlap and do not establish a CPU/draw improvement. The progress change is retained for its confirmed range correction and avoiding frame-by-frame composition/cached-object rebuilding.

Values below are medians of the three **per-run** measurements, followed by the minimum–maximum run range. They are not percentiles pooled across runs. All targeted runs report zero lost metric callbacks.

| Workload / metric | Baseline median (range) | Candidate median (range) |
| --- | --- | --- |
| Progress / Draw p50 ms | 0.196 (0.177–0.197) | 0.189 (0.185–0.203) |
| Progress / Draw p95 ms | 0.487 (0.486–0.488) | 0.499 (0.455–0.592) |
| Progress / Process CPU ms/frame | 8.300 (8.046–8.666) | 8.059 (7.749–8.790) |
| Progress / Frames above actual deadline % | 34.429 (33.591–36.078) | 36.204 (32.314–39.558) |
| Shimmer / Draw p50 ms | 0.145 (0.138–0.164) | 0.101 (0.100–0.102) |
| Shimmer / Draw p95 ms | 0.586 (0.532–0.700) | 0.362 (0.297–0.376) |
| Shimmer / Process CPU ms/frame | 11.059 (10.904–11.319) | 10.211 (8.594–10.663) |
| Shimmer / Frames above actual deadline % | 46.707 (42.941–55.460) | 49.180 (47.879–50.205) |

**No overall frame-smoothness or deadline-overrun improvement is established.** Shimmer median deadline-overrun frequency increases from 46.707% to 49.180%; total p50 increases from 64.635 to 66.588 ms. Its local draw-time saving does not overcome the emulator graphics/scheduling cost. Progress median deadline-overrun frequency increases from 34.429% to 36.204%, while per-run ranges overlap. These metrics remain visible rather than being inferred from draw/CPU results.

The first all-nine-scenario pair is a descriptive coverage pass. The seven unchanged workloads serve as controls. A single pair cannot establish absence of regressions in those paths. For example, unchanged navigation CPU/frame varies by about 20% between builds, illustrating why whole-process timing cannot be attributed solely to the two edits. Arrow pairs below are baseline A → candidate A. Full quantiles/deadlines and raw-record hashes are in `results.json`.

| Workload | Process CPU ms/frame | Draw p95 ms | Above actual deadline % |
| --- | --- | --- | --- |
| Progress | 8.300 → 8.790 | 0.487 → 0.455 | 36.078 → 32.314 |
| Shimmer | 10.904 → 10.663 | 0.532 → 0.297 | 55.460 → 47.879 |
| Charts | 61.917 → 62.879 | 1.956 → 2.687 | 100.000 → 100.000 |
| Navigation | 18.270 → 14.684 | 11.323 → 10.099 | 48.354 → 50.520 |
| Scrollbar | 5.944 → 6.516 | 0.252 → 0.285 | 29.032 → 32.176 |
| Numerals | 8.331 → 7.663 | 1.032 → 1.002 | 39.607 → 44.575 |
| Press | 5.879 → 6.513 | 0.006 → 0.007 | 43.312 → 44.206 |
| Connected | 6.593 → 6.111 | 0.260 → 0.340 | 29.369 → 33.581 |
| Pulse | 6.552 → 5.838 | 0.266 → 0.294 | 33.333 → 29.369 |

## Audit coverage and deferred work

Source-wide triage covers all 39 commonMain Kotlin files (27 component files, six chart files, four navigation files, and Motion/defaults), with focused review of animated state, effects, lazy layouts, drawing/allocation, measurement and interaction paths. Runtime timing covers the nine workloads below. This is not exhaustive runtime validation of every component combination or backend.

| Workload | Exercise |
| --- | --- |
| Progress | 40 animated rings with center content, target reversal every 900 ms |
| Shimmer | 72 independently animated placeholders |
| Charts | Three charts, four 512-point lines each, unchanged data/bounds under host redraw |
| Navigation | All three configured bar appearances plus a rail, selection every 180 ms |
| Scrollbar | 500 stable-key variable-height rows with frame-driven bidirectional scrolling |
| Numerals | Ten numeral/stat-tile pairs alternating 9 and 100 |
| Press | 40 surfaces with automated Press/Release interaction cycles |
| Connected buttons | Eight connected groups alternating selection |
| Pulse | 40 independently animated rings |

- **Line chart:** Drawing recreates point offsets, paths, brushes and strokes. The heavy chart workload identifies a useful future profiling target, but emulator process CPU includes driver work and does not identify the allocation's CPU share. Caching must invalidate on size/range/theme/stroke/model changes and observed snapshot-list updates; ordinary `List` identity cannot be assumed immutable. A broad cache rewrite needs a clear mutable-input contract and dedicated rendered invalidation tests, so it is deferred.
- **Scrollbar:** Pixel metrics, thumb size/position and `BoxWithConstraints` enter composition while scrolling. Moving position reads into placement/drawing is a future targeted experiment. Preserve variable-item sizing, grids/spans, reverse layouts, drag cancellation, focus/keyboard and accessibility behavior before accepting it.
- **Shared press modifier:** A `Modifier.Node` migration could reduce legacy `composed` overhead, but measurements here do not isolate that cost. The confirmed latest-scale/retargeting correction remains in place; a broad modifier rewrite is deferred.
- **Large static datasets:** Picker key validation is O(N); the calendar heatmap composes all weeks in a horizontal row; stacked bars repeatedly find their last positive segment. These are source candidates for large-data workloads, not measured defects in this audit. Avoid identity-based memoization of mutable collections or blanket stability annotations.
- **Animated numerals/navigation/connected buttons:** Content/selection changes legitimately require composition. Existing controlled-clock motion tests protect transitions; no extra blanket animation or layout rewrite was justified here.
- **Gallery recreation:** Some preview controls use ordinary remembered state and can reset on recreation. Saveable stress presets and backend-specific journeys would improve future reproducibility; they are distinct from the two library rendering fixes.
- **Static wrapper components:** Buttons, settings, rows, sheets, dialogs and form wrappers received source triage. No additional blocking I/O or independent perpetual animation was found in those scanned paths. Their complete device interactions are not represented by nine timing scenarios.

## Reproduction and limits

Build the optimized target with `./gradlew :androidApp:assembleBenchmark :androidApp:lintBenchmark`. To reconstruct a baseline, use a temporary checkout of the candidate tree with the same harness/dependencies and replace only `AsgardProgressRing.kt` and `AsgardShimmer.kt` with their versions from commit `7b43196`. Build and save that APK before restoring the candidate component files. The recorded binary hashes identify the exact tested artifacts. Use identical APK/harness/dependency settings for each paired run:

```sh
python3 tools/run_component_benchmarks.py --serial emulator-5554 \
  --apk /tmp/asgard-performance-baseline.apk --label baseline-a \
  --out /tmp/asgard-emulator-ab --repeats 1 --seconds 12
python3 tools/run_component_benchmarks.py --serial emulator-5554 \
  --apk /tmp/asgard-performance-candidate.apk --label candidate-a \
  --out /tmp/asgard-emulator-ab --repeats 1 --seconds 12
# Repeat alternating builds with unique labels and --scenarios progress shimmer.
python3 tools/summarize_component_benchmarks.py /tmp/asgard-emulator-ab
python3 -m unittest -v tools/test_component_benchmarks.py
```

The twelve targeted raw frame CSVs are retained as deterministic gzip files alongside `results.json`; decompressed CSV SHA256 values let reviewers verify them. All 26 per-run summaries and APK hashes are retained. Full per-frame CSV/JSON and before/after device dumps remain at `/tmp/asgard-emulator-ab` on the validation workstation. APK binaries are not committed. The harness requires Android 12/API 31 or newer for GPU/deadline metrics. Unsupported metrics are reported as null; overrun percentages use only positive-deadline frames. `droppedMetricCallbacks` means recorder delivery loss, not dropped display frames. CPU time is whole-process CPU rather than stack samples, draw time is elapsed recording time rather than a CPU profile, and total/GPU timing includes the emulator graphics pipeline. No physical-device performance trace, allocation profile or wasm runtime benchmark was performed. Physical high-refresh validation and representative production-app journeys remain the next evidence needed before making broader smoothness claims.
