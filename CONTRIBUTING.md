# Contributing to Asgard

Submit every change to `main` through a pull request, including documentation, dependency updates and maintainer changes. Open a branch or fork, describe the change and include the checks you ran. For a visible gallery change, include a screenshot of the affected preview.

A PR from anyone other than the repository owner needs an approving review from a reviewer with write access. The current reviewer is @trinadhthatakula. This includes Dependabot PRs. New reviewable changes dismiss stale approvals. For a PR authored by the owner, a base-branch workflow records the review exemption against the current commit. Every PR must pass the required checks before merge.

## Local checks

Use JDK 21 and Android SDK platform 37.2. Run the checks that exercise your change from the repository root.

```sh
./gradlew :asgard:testAndroidHostTest :asgard:compileKotlinWasmJs
./gradlew :androidApp:assembleDebug :androidApp:lintRelease
./gradlew :demo:wasmJsBrowserDistribution
python3 -m unittest discover
python3 tools/check_skill_api.py
python3 tools/update_gallery_reference.py --check
```

Run device-specific checks locally when changing Android behavior. The [Android gallery guide](docs/android-gallery.md) covers the physical-device and emulator commands.

## Dependencies

Dependabot groups Android gallery dependencies and Fastlane separately from the library and shared Kotlin, Compose and Gradle toolchain. The web gallery builds from the same Gradle catalog and has a separate group for its GitHub Pages actions. Keep the published Asgard version in `compatibilityConsumer` pinned for binary compatibility testing.

Use the root dependency updater for manual catalog updates. [Agent and tooling guide](AGENTS.md)
