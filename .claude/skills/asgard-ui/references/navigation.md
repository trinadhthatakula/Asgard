# Navigation and selection (2.1.1)

Use `com.valhalla.asgard.navigation` for `AsgardNavItem`, `AsgardNavigationBar` and
`AsgardNavigationRail`. Both retain the legacy simple overload and offer an overload accepting
`AsgardNavigationConfiguration`. The caller owns the selected index and navigation/back stack.

## Configuration

`AsgardNavigationConfiguration` independently selects `AsgardNavigationPlacement` (`Docked`,
`Floating`), `AsgardNavigationAppearance` (`Expanding`, `Segmented`, `Standard`),
`AsgardNavigationLabels` (`Selected`, `Always`, `Hidden`, `SelectedCaptionWhenNeeded`),
`showIcons`, container/indicator shapes, maximum width, padding, spacing, floating margin,
elevation and `AsgardNavigationInsets` (`None`, `NavigationBars`, `SafeDrawing`). Nonpositive
width and negative spacing, margin or elevation are rejected.

**Floating is a surface treatment, not an overlay.** The host chooses where to place the bar.
For a true overlay, place it above scrolling content and reserve its measured height for list tails,
actions and scrollbars; coordinate keyboard/system insets with the host rather than adding them
twice. For a docked bar, a `Scaffold` bottom slot is appropriate. The rail's configuration overload
also supports header and footer. Empty bars render nothing; invalid selected indices highlight no
item. Text-only navigation keeps names visible; narrow layouts can scroll or use captions.

See [NavigationExamples.kt](../examples/NavigationExamples.kt) for a measured floating overlay.

## Connected buttons

`ConnectedButtonGroup` accepts `ConnectedButtonGroupItem.Label`, `.Icon` and `.IconWithLabel`.
Items must be nonempty. `selectedIndex`/`onItemSelected` is single-select;
`selectedIndices`/`onSelectionChanged` is caller-controlled multiselect. An empty set is allowed;
reject it in the callback only if the product requires at least one choice. Out-of-range indices
are ignored; callbacks receive a new set. Item and group `enabled` flags combine.

The optional shape overload takes `ConnectedButtonGroupShapes`: `Expressive`, `FixedConnected`,
`Uniform(shape)` or `Custom(leading, middle, trailing, standalone)`. Shape behavior is separate
from colors; default checked colors are the theme's primary/onPrimary. `Custom` exposes Material
`ToggleButtonShapes`, which can require an experimental Material opt-in at the call site.
See the multiselect example in [NavigationExamples.kt](../examples/NavigationExamples.kt).
