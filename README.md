# Asgard

A small, **theme-agnostic** Jetpack Compose component library built on **Expressive Material 3**.

Asgard ships the *structure and motion* of a polished UI — a signature header, expressive
navigation bar/rail, connected segmented toggles, status chips, action items, and tactile press
animations — while reading **all** colors, typography, and shapes from the **host app's
`MaterialTheme`**. The same component renders as "Thor" in one app and "Loki" in another; the
library never imposes a palette or font.

> Born from the [Thor](https://github.com/trinadhthatakula/Thor) app's design system, extracted so
> every Valhalla app — and anyone else — can share one component layer.

## Install

```kotlin
// settings.gradle.kts has mavenCentral() in dependencyResolutionManagement

dependencies {
    implementation("com.trinadhthatakula:asgard:1.0.0")
}
```

**Building a Thor extension?** If the host app (Thor) already bundles Asgard, depend on it as
`compileOnly` so it isn't duplicated in your extension APK — Thor provides it at runtime:

```kotlin
compileOnly("com.trinadhthatakula:asgard:1.0.0")
```

> Asgard shares the `com.trinadhthatakula` namespace with the Thor extension contract,
> [`com.trinadhthatakula:thor-extension-api`](https://github.com/trinadhthatakula/Thor-extension-api)
> — both are provided by the Thor host at runtime, so extensions depend on them as `compileOnly`.

## What's inside (v1)

| Component | What it is |
|---|---|
| `Modifier.expressivePress()` / `Modifier.animateExpressiveResize()` | The tactile squish-on-press and spring resize, sourced from `MaterialTheme.motionScheme`. |
| `AsgardHeader` | The signature screen header: optional brand icon / back button + a heavy, tightly-tracked title in `primary`, with a trailing actions slot. No `TopAppBar`. |
| `AsgardNavigationBar` + `AsgardNavigationRail` | Expressive bottom bar / side rail with a `primaryContainer` selected pill, animated label, and press feedback. Driven by `AsgardNavItem`. |
| `ConnectedButtonGroup` | Single-select connected segmented control (Expressive `ButtonGroup` + `ToggleButton`), described declaratively via `ConnectedButtonGroupItem`. |
| `StatusChip` | A pill status label (`labelSmall`, on a `CircleShape` background). |
| `AsgardActionItem` | An icon-chip + label action cell for action rows / toolbars. |

All icons are passed as `ImageVector` — Asgard bundles no icon pack.

## Usage

```kotlin
val tabs = listOf(
    AsgardNavItem(icon = Icons.Default.Apps, label = "Apps"),
    AsgardNavItem(icon = Icons.Default.AcUnit, label = "Freezer"),
)
var selected by remember { mutableIntStateOf(0) }

Scaffold(
    bottomBar = {
        AsgardNavigationBar(items = tabs, selectedIndex = selected, onSelect = { selected = it })
    }
) { padding ->
    Column(Modifier.padding(padding)) {
        AsgardHeader(
            title = "Apps",
            icon = Icons.Default.Dashboard,
            actions = {
                ConnectedButtonGroup(
                    items = listOf(
                        ConnectedButtonGroupItem.Icon(Icons.Default.GridView, "Grid"),
                        ConnectedButtonGroupItem.Icon(Icons.AutoMirrored.Filled.List, "List"),
                    ),
                    selectedIndex = 0,
                    onItemSelected = { /* … */ },
                )
            },
        )
        // …
    }
}
```

Wrap your app in `MaterialExpressiveTheme { … }` (or any `MaterialTheme`) — Asgard inherits whatever
you provide. The Expressive theme gives the richest motion.

## Requirements

- `minSdk` 28, JDK 21
- Jetpack Compose with **Material 3 `1.5.0-alpha22`+** (Expressive APIs)

## Publishing (maintainer)

Released from `gradle.properties` `VERSION_NAME` to Maven Central via the Vanniktech plugin (group
`com.trinadhthatakula`). Credentials (`mavenCentralUsername`/`Password`, `signingInMemoryKey`/`Password`)
live in `~/.gradle/gradle.properties`. Publish on **JDK 21**:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew publishToMavenCentral
```

Local dry-run without a GPG key: `./gradlew publishToMavenLocal -PVERSION_NAME=1.0.0-SNAPSHOT`.

## License

[Apache License 2.0](LICENSE).
