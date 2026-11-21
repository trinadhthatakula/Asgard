# Glass UI in Asgard 2.2.0

Add `implementation("com.trinadhthatakula:asgard-glass:2.2.0")` from Maven Central to your Android or KMP common dependencies. The add-on brings in matching Asgard core. Core-only apps retain their existing Material controls. Within a screen, remember one `AsgardGlassSource`, mark the background subtree with `Modifier.asgardGlassSource(source)`, and draw the chosen controls as siblings above it. Keep overlays outside the captured subtree.

```kotlin
import com.valhalla.asgard.glass.asgardGlass
import com.valhalla.asgard.glass.asgardGlassSource
import com.valhalla.asgard.glass.rememberAsgardGlassSource

val source = rememberAsgardGlassSource(enabled = glassTitle && !reduceTransparency)
Box {
    PhotoFeed(Modifier.fillMaxSize().asgardGlassSource(source))
    Text(
        text = title,
        modifier = Modifier.asgardGlass(source, enabled = glassTitle, shape = MaterialTheme.shapes.medium).padding(12.dp),
        color = MaterialTheme.colorScheme.onSurface,
    )
}
```

`Modifier.asgardGlass` supplies a shaped background to any modifier-taking composable. Put layout margins before it and inner padding after it. Passing `enabled = false` to the modifier preserves the original modifier chain. Pass a disabled **source** to retain a solid themed fill for reduced transparency. For a filled Material button or card, give its own container a transparent color while glass is selected and an explicit matching content color. Keep checked, disabled, pressed and error states distinguishable. For content without a modifier slot, use `AsgardGlassSurface`; an opaque child still needs a transparent fill or a background slot. Fully opaque imagery can use a glass caption or frame.

A filled Asgard card already supplies theme colors for its text. Clear just its fill when selected. Here `Photo` is caller-provided image content.

```kotlin
val shape = MaterialTheme.shapes.large
val fill = MaterialTheme.colorScheme.surfaceContainer
val source = rememberAsgardGlassSource(enabled = glassCard && !reduceTransparency)
Box {
    Photo(Modifier.matchParentSize().asgardGlassSource(source))
    AsgardStatCard(
        label = "Voltage", value = "5.00", unit = "V",
        modifier = Modifier.asgardGlass(source, enabled = glassCard, shape = shape, containerColor = fill),
        shape = shape,
        containerColor = if (glassCard) Color.Transparent else fill,
        onClick = openVoltageDetails,
    )
}
```

Use `source.frosted()` with the toolbar or configured navigation bar for their exact inner bounds. Android hardware-accelerated windows on API 33+ and browser Wasm support frost. Older Android versions use the solid fallback. Wasm also follows the browser's reduced-transparency preference.

## Compatibility ratings

These are Asgard's design and integration ratings for individual instances. **5** is a natural container with a direct integration. **4** is a good fit with explicit color, shape or state handling. **3** is situational because of density, imagery or interaction. **2** favors a surrounding surface or an in-window content slot. **1** reserves glass for a supporting surface because the component itself communicates through a visual effect. Ratings are independent of device performance. In the Android test app or browser gallery, choose **Browse → component → Preview glass**. Each entry starts with glass off and remembers its choice when you visit another entry and return. For modal components, turn on the switch and then open the sheet or dialog.

| Component | Compatibility | Integration guidance |
| --- | --- | --- |
| `AsgardActionItem` | 4/5 | Frost the action tile and keep its icon and caption legible through the press animation. |
| `AsgardActionSheet` | 2/5 | Put the source and frosted message inside the sheet window. Keep footer actions readable. |
| `AsgardActionToolbar` | 5/5 | Use the container overload to share one backdrop across the toolbar actions. |
| `AsgardAnimatedNumeral` | 3/5 | Draw a glass plate behind the numeral so its changing digits stay sharp. |
| `AsgardAvatar` | 3/5 | Frost an initials or icon fill. An opaque photograph can use a glass frame. |
| `AsgardBadge` | 4/5 | Match the pill shape, clear the badge fill and preserve the outline and text color. |
| `AsgardBanner` | 4/5 | Use the banner shape and semantic tint. A custom gradient must leave the backdrop visible. |
| `AsgardCalendarHeatmap` | 2/5 | Use one plate under the grid and retain distinct colors for missing, zero and selected dates. |
| `AsgardChartLegend` | 4/5 | Frost the legend container once and keep all series swatches opaque. |
| `AsgardDialogScaffold` | 2/5 | Use an in-window source for the dialog body and preserve the modal scrim. |
| `AsgardEmptyState` | 4/5 | Use a bounded surface behind the empty-state icon and message. |
| `AsgardFeatureRow` | 4/5 | Apply frost beneath the row and keep the feature icon badge distinct. |
| `AsgardGlassSurface` | 5/5 | Pass the shared source and place custom content inside this shaped surface. |
| `AsgardHeader` | 5/5 | Frost a pinned header over scrolling content and retain the host's title and action colors. |
| `AsgardLabeledSlider` | 3/5 | Frost the group behind the slider while retaining the track, thumb and value colors. |
| `AsgardLazyScrollbar` | 2/5 | Use a plate for the scroll area and preserve the thumb contrast and drag target. |
| `AsgardLegendItem` | 4/5 | Place the label and swatch together on one small plate. |
| `AsgardLineChart` | 2/5 | Use a single chart plate and keep plotted lines, axes and data labels sharp. |
| `AsgardListRow` | 3/5 | Select individual rows or one shared list container. Measure fast scrolling with repeated effects. |
| `AsgardLoadingState` | 3/5 | Frost the caption and spinner container, leaving the progress indicator distinct. |
| `AsgardLockedOverlay` | 1/5 | Preserve the lock scrim and input interception. Put optional frost on the supporting content. |
| `AsgardNavigationBar` | 5/5 | Use the configured bar's container slot so the floating margin stays clear. |
| `AsgardNavigationRail` | 4/5 | Match the rail bounds and clear its fill. Retain the selected destination indicator. |
| `AsgardOnboardingScaffold` | 3/5 | Keep the onboarding surface bounded and check long text over the brightest background. |
| `AsgardPicker` | 3/5 | Place one surface behind the picker and keep its search, item labels and selection readable. |
| `AsgardPickerSheet` | 2/5 | Place the backdrop and picker together inside the sheet body. |
| `AsgardProBadge` | 4/5 | Retain the premium tint and its matching foreground on a pill-shaped surface. |
| `AsgardProgressRing` | 2/5 | Keep the ring stroke and progress label sharp over a supporting plate. |
| `AsgardPulseRing` | 2/5 | Leave the animated pulse in the foreground and use frost on its supporting surface. |
| `AsgardSearchBar` | 4/5 | Keep the field outline, cursor, placeholder and error state legible over the fill. |
| `AsgardSectionCard` | 5/5 | Frost the card once, clear its internal fill and share the background across its rows. |
| `AsgardSettingRow` | 4/5 | Use a bounded row surface and retain contrast for both the label and trailing value. |
| `AsgardSettingToggleRow` | 4/5 | Keep the switch track, thumb and checked state independent of the row background. |
| `AsgardSheet` | 2/5 | Put a bounded source inside the sheet and frost the selected content slot. |
| `AsgardShimmer` | 1/5 | Preserve the skeleton animation. Use glass only for its supporting surface. |
| `AsgardStackedBarChart` | 2/5 | Draw one plate beneath the plot and preserve segment colors and axis labels. |
| `AsgardStatCard` | 5/5 | Match the card shape, make the card fill transparent and retain label and value colors. |
| `AsgardStatTile` | 5/5 | Frost an individual tile while keeping the number, icon and status dot sharp. |
| `AsgardStepperRow` | 4/5 | Use one surface behind the value and controls and retain their disabled states. |
| `AsgardTimelineBar` | 2/5 | Use a supporting plate and preserve the segment boundaries and time encoding. |
| `AsgardTonalIconButton` | 4/5 | Match the button shape and clear its active fill while keeping a contrasting icon. |
| `AsgardUpgradeCard` | 4/5 | Keep the call-to-action colors explicit when replacing the promotional card fill. |
| `ConnectedButtonGroup` | 3/5 | Frost the group once and retain opaque checked indicators and distinct disabled segments. |
| `StatusChip` | 4/5 | Use a pill-shaped background and retain the status label and foreground color. |
