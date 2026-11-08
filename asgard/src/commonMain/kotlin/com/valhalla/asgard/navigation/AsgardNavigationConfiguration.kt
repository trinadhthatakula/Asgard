package com.valhalla.asgard.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AsgardNavigationPlacement { Docked, Floating }
enum class AsgardNavigationAppearance { Expanding, Segmented, Standard }
enum class AsgardNavigationLabels { Selected, Always, Hidden, SelectedCaptionWhenNeeded }
enum class AsgardNavigationInsets { None, NavigationBars, SafeDrawing }

/** Placement, item geometry and labels are independent. None means the caller owns insets. */
data class AsgardNavigationConfiguration(
    val placement: AsgardNavigationPlacement = AsgardNavigationPlacement.Docked,
    val appearance: AsgardNavigationAppearance = AsgardNavigationAppearance.Expanding,
    val labels: AsgardNavigationLabels = AsgardNavigationLabels.SelectedCaptionWhenNeeded,
    val showIcons: Boolean = true,
    val containerShape: Shape? = null,
    val indicatorShape: Shape? = null,
    val maxWidth: Dp = 640.dp,
    val contentPadding: PaddingValues = PaddingValues(8.dp),
    val floatingMargin: Dp = 12.dp,
    val spacing: Dp = 4.dp,
    val elevation: Dp = 6.dp,
    val insets: AsgardNavigationInsets = AsgardNavigationInsets.None,
) {
    init { require(maxWidth > 0.dp && spacing >= 0.dp && floatingMargin >= 0.dp && elevation >= 0.dp) }
}

internal fun navigationNeedsCaption(available: Float, count: Int, labelWidth: Float): Boolean =
    count > 0 && available < count * 48f + labelWidth + 16f

private fun Modifier.navInsets(config: AsgardNavigationConfiguration): Modifier = when (config.insets) {
    AsgardNavigationInsets.None -> this
    AsgardNavigationInsets.NavigationBars -> navigationBarsPadding()
    AsgardNavigationInsets.SafeDrawing -> safeDrawingPadding()
}

/**
 * Configuration overload retaining the original bar entry point. Floating is a surface treatment;
 * place the bar in a Scaffold bottomBar or overlay at the call site. Route state remains caller-owned.
 * Too many destinations for 48dp targets produce horizontal scrolling rather than overlapping targets.
 */
@Composable
fun AsgardNavigationBar(
    items: List<AsgardNavItem>, selectedIndex: Int, onSelect: (Int) -> Unit,
    configuration: AsgardNavigationConfiguration,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    if (items.isEmpty()) return
    val margin = if (configuration.placement == AsgardNavigationPlacement.Floating) configuration.floatingMargin else 0.dp
    Box(modifier.fillMaxWidth().navInsets(configuration).padding(margin), contentAlignment = Alignment.Center) {
        Surface(
            Modifier.widthIn(max = configuration.maxWidth).fillMaxWidth(),
            shape = configuration.containerShape ?: if (configuration.placement == AsgardNavigationPlacement.Floating) MaterialTheme.shapes.extraLarge else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = containerColor, shadowElevation = if (configuration.placement == AsgardNavigationPlacement.Floating) configuration.elevation else 0.dp,
        ) {
            BoxWithConstraints(Modifier.padding(configuration.contentPadding)) {
                val measurer = rememberTextMeasurer()
                val density = androidx.compose.ui.platform.LocalDensity.current
                val selectedItem = items.getOrNull(selectedIndex)
                val labelWidth = selectedItem?.let { with(density) { measurer.measure(it.label, labelStyle).size.width.toDp().value } } ?: 0f
                val caption = configuration.labels == AsgardNavigationLabels.SelectedCaptionWhenNeeded &&
                    configuration.showIcons && navigationNeedsCaption(maxWidth.value - configuration.spacing.value * (items.size - 1), items.size, labelWidth)
                val stackLabels = configuration.showIcons && configuration.labels == AsgardNavigationLabels.Always &&
                    items.sumOf { with(density) { measurer.measure(it.label, labelStyle).size.width.toDp().value.toDouble() } + 48.0 } > maxWidth.value
                val minimum = 48.dp * items.size + configuration.spacing * (items.size - 1)
                val tooNarrow = maxWidth < minimum
                val rowWidth = if (tooNarrow) minimum.value else maxWidth.value
                val available = rowWidth - configuration.spacing.value * (items.size - 1)
                val maxExpansion = (available / 48f - (items.size - 1)).coerceAtLeast(1f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        Modifier.then(if (tooNarrow) Modifier.horizontalScroll(rememberScrollState()).width(minimum) else Modifier.fillMaxWidth()).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(configuration.spacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            val isSelected = index == selectedIndex
                            val showLabel = !configuration.showIcons || when (configuration.labels) {
                                AsgardNavigationLabels.Always -> true
                                AsgardNavigationLabels.Hidden -> false
                                AsgardNavigationLabels.Selected -> isSelected
                                AsgardNavigationLabels.SelectedCaptionWhenNeeded -> isSelected && !caption
                            }
                            val weight = if (configuration.appearance == AsgardNavigationAppearance.Expanding && isSelected && showLabel)
                                ((48f + labelWidth + 16f) / 48f).coerceAtMost(maxExpansion) else 1f
                            NavigationDestination(item, isSelected, { onSelect(index) },
                                configuration, index, items.lastIndex, showLabel, labelStyle,
                                selectedIndicatorColor, selectedContentColor, unselectedContentColor,
                                Modifier.weight(weight), stackLabels = stackLabels)
                        }
                    }
                    if (caption && selectedItem != null) Text(selectedItem.label,
                        Modifier.padding(top = 8.dp).clearAndSetSemantics {}, style = labelStyle,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun NavigationDestination(
    item: AsgardNavItem, selected: Boolean, onClick: () -> Unit,
    config: AsgardNavigationConfiguration, index: Int, last: Int, showLabel: Boolean,
    labelStyle: TextStyle, indicator: Color, selectedColor: Color, unselectedColor: Color,
    modifier: Modifier = Modifier,
    stackLabels: Boolean = false,
    vertical: Boolean = false,
) {
    val background by animateColorAsState(if (selected) indicator else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
    val foreground by animateColorAsState(if (selected) selectedColor else unselectedColor,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
    val shape = config.indicatorShape ?: if (config.appearance == AsgardNavigationAppearance.Segmented) {
        when {
            last == 0 -> RoundedCornerShape(24.dp)
            vertical && index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
            vertical && index == last -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            index == 0 -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp)
            index == last -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp)
            else -> RoundedCornerShape(4.dp)
        }
    } else MaterialTheme.shapes.extraLarge
    Box(modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).clip(shape).background(background)
        .semantics { contentDescription = listOfNotNull(item.contentDescription ?: item.label, item.badge?.takeIf { it.isNotEmpty() }).joinToString(", ") }
        .selectable(selected, role = Role.Tab, onClick = onClick).padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center) {
        val icon: @Composable () -> Unit = {
            if (config.showIcons) BadgedBox(badge = { item.badge?.let { if (it.isEmpty()) Badge() else Badge { Text(it) } } }) {
                Icon(if (selected) item.selectedIcon else item.icon, null, tint = foreground, modifier = Modifier.size(24.dp))
            }
        }
        if (config.appearance == AsgardNavigationAppearance.Standard || stackLabels) {
            Column(Modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                icon()
                if (showLabel) Text(item.label, color = foreground, style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        } else Row(Modifier.clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            if (showLabel) Text(item.label, Modifier.weight(1f, fill = false), color = foreground, style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Rail configuration overload; selection and naming use the same destination renderer as the bar. */
@Composable
fun AsgardNavigationRail(
    items: List<AsgardNavItem>, selectedIndex: Int, onSelect: (Int) -> Unit,
    configuration: AsgardNavigationConfiguration,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Surface(modifier.navInsets(configuration).padding(if (configuration.placement == AsgardNavigationPlacement.Floating) configuration.floatingMargin else 0.dp),
        shape = configuration.containerShape ?: MaterialTheme.shapes.extraLarge,
        color = containerColor, shadowElevation = if (configuration.placement == AsgardNavigationPlacement.Floating) configuration.elevation else 0.dp) {
        Column(Modifier.padding(configuration.contentPadding).selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(configuration.spacing)) {
            header?.invoke(this)
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                NavigationDestination(item, selected, { onSelect(index) }, configuration, index, items.lastIndex,
                    !configuration.showIcons || configuration.labels == AsgardNavigationLabels.Always ||
                        (configuration.labels != AsgardNavigationLabels.Hidden && selected),
                    labelStyle, selectedIndicatorColor, selectedContentColor, unselectedContentColor, vertical = true)
            }
            footer?.invoke(this)
        }
    }
}
