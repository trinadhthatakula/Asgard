---
name: asgard-ui
description: >-
  Integrate the Asgard Compose component and chart library into an Android or Kotlin Multiplatform
  app. Use for com.trinadhthatakula:asgard setup, choosing components, navigation, inputs, sheets,
  pickers, scrollbars, charts, theming, accessibility, and adapting their APIs to an app.
metadata:
  asgard-version: "2.1.1"
---

# Asgard UI integration

Asgard supplies Compose components that use the host's Material theme. The app owns its routes,
selection, persistence, permissions, localized strings, icons, images and business rules. Use
`MaterialTheme` or `MaterialExpressiveTheme`, and pass theme colors and shapes when customizing.

## Version and setup

This skill is verified against **Asgard 2.1.1**. Respect the consuming project's pinned version;
check the corresponding library sources or release notes before using a newer API. Asgard 2.1.0
introduced the expanded components and configurable navigation; 2.1.1 includes motion and
rendering fixes.

For a new integration, add Maven Central and use the KMP umbrella coordinate:

```kotlin
// Android module dependencies, or KMP commonMain.dependencies:
implementation("com.trinadhthatakula:asgard:2.1.1")
```

The umbrella resolves `asgard-android` or `asgard-wasm-js` automatically. Version 2.1.1's Android
artifact requires **compile SDK 37.2**, supports `minSdk` 28, and targets JVM 21; align the host's
Android Gradle Plugin, SDK and Compose versions before changing them. The library targets Android
and wasmJs; do not assume it supplies iOS or desktop variants. A plugin hosted by an app that already
provides Asgard may need `compileOnly` and must match the host's version.

Compose UI, foundation, Material 3 and kotlinx-datetime types are exposed by the library. Supply
icons and image-loading content from the host. Public APIs can expose experimental Material types:
opt in at the call site when the types or theme used by that recipe require it; do not assume a
library-internal opt-in applies to consumer code. Use named arguments; `modifier` is not always in
the same position.

## Choose the right API

- **Navigation or connected choices:** read [navigation and selection](references/navigation.md).
  It covers the configuration overloads, floating placement, multiselect, state and shape choices.
- **Sheets, pickers, rows, actions, avatars, status or loading:** read the complete
  [component index and contracts](references/components.md).
- **Line, bar, timeline, heatmap or legend:** read [charts](references/charts.md).
- For complete Kotlin examples with imports, state and callbacks, read
  [navigation examples](examples/NavigationExamples.kt) or [component examples](examples/ComponentExamples.kt).
  These files are compiled as common code by the gallery's Android and wasm builds. Adapt text,
  state lifetime and accessibility labels to the host; the examples are starting points.

The repository's [component expansion guide](https://github.com/trinadhthatakula/Asgard/blob/v2.1.1/docs/component-expansion.md)
and [gallery](https://trinadhthatakula.github.io/Asgard/) give further examples. If a signature or
version is uncertain, inspect the source for the **version actually used by the app**, rather than
assuming the current main branch matches it.

## Integration checks

Use caller-owned, stable state and keys. Bound lazy content and reserve space for overlays and
scrollbars; apply each system inset once. Provide localized accessibility labels for interactive
icons and controls. Verify relevant small/large-screen and font-scale behavior, including selection
and dismissal. Run the consuming project's build and focused tests; for KMP changes check every
supported target used by the host.
