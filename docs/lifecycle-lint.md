# Android Lifecycle lint compatibility

The Android gallery previously resolved Lifecycle 2.9.4 through Compose and Activity. Its bundled
lint registry referenced `KtLightModifierList.getKotlinOrigin()`, which the current AGP/Kotlin lint
toolchain no longer supplies. Lint therefore reported `ObsoleteLintCustomCheck` and skipped both
`UnsafeLifecycleWhenUsage` and `RepeatOnLifecycleWrongUsage`.

The Android harness now constrains Lifecycle runtime to 2.11.0. Lifecycle's atomic dependency
group aligns its related Android artifacts at the same version. The constraint applies to the
Android app, without adding a Lifecycle dependency to the shared library or WebAssembly output.
The harness treats lint warnings as errors so an incompatible registry cannot silently pass CI.

Verified on 2026-11-09:

- Dependency insight changed `androidx.lifecycle:lifecycle-runtime` from 2.9.4 to 2.11.0.
- A temporary Activity called `repeatOnLifecycle` from `onStart`, and performed an unguarded
  `@UiThread` operation in the `finally` block of `Lifecycle.whenStarted`. Lint failed with both
  `RepeatOnLifecycleWrongUsage` and `UnsafeLifecycleWhenUsage`, confirming the detectors ran.
- The temporary source was removed. `:androidApp:lintDebug :androidApp:lintRelease` then passed
  with zero errors and warnings; neither detector is suppressed or disabled.

Normal verification:

```sh
./gradlew :androidApp:lintDebug :androidApp:lintRelease
./gradlew :androidApp:dependencyInsight --dependency lifecycle-runtime --configuration debugRuntimeClasspath
```
