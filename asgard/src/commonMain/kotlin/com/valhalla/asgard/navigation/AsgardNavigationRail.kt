package com.valhalla.asgard.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults
import com.valhalla.asgard.animateExpressiveResize
import com.valhalla.asgard.expressivePress

/**
 * A vertical navigation rail that renders [items] as a column of tappable destinations, typically
 * anchored to the leading edge on larger screens.
 *
 * Every [AsgardNavItem.badge] is rendered as a small dot/count badge over the item icon when it is
 * non-null. All color/shape/label parameters default to the values the rail was previously
 * hardcoded to, so adopting them changes no pixels.
 *
 * @param items The destinations to display, top-to-bottom.
 * @param selectedIndex The index of the currently selected destination; values outside
 *   `items.indices` simply leave nothing selected.
 * @param onSelect Invoked with the tapped destination's index.
 * @param modifier The [Modifier] applied to the rail's [Surface] container.
 * @param showLabel Whether to render each destination's text label beneath its icon.
 * @param containerColor The background color of the rail surface.
 * @param selectedIndicatorColor The pill background color drawn behind the selected destination.
 * @param selectedContentColor The icon/label color used for the selected destination.
 * @param unselectedContentColor The icon/label color used for unselected destinations.
 * @param unselectedAlpha The content alpha applied to unselected destinations.
 * @param shape The shape of the rail surface; when `null` the default rounded-trailing-edge shape
 *   (a [RoundedCornerShape] using [AsgardDefaults.navContainerRadius]) is used.
 * @param labelStyle The [TextStyle] for destination labels; when `null` the selected destination
 *   uses `labelMedium` and unselected destinations use `labelSmall`.
 * @param header Optional content rendered above the destinations inside the rail's [Column].
 * @param footer Optional content rendered below the destinations inside the rail's [Column].
 */
@Composable
fun AsgardNavigationRail(
    items: List<AsgardNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedAlpha: Float = 0.7f,
    shape: Shape? = null,
    labelStyle: TextStyle? = null,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val resolvedShape = shape ?: RoundedCornerShape(
        topEnd = AsgardDefaults.navContainerRadius,
        bottomEnd = AsgardDefaults.navContainerRadius
    )
    // Guard against an out-of-range selectedIndex so nothing is force-selected and no crash occurs.
    val hasValidSelection = selectedIndex in items.indices

    Surface(
        modifier = modifier.fillMaxHeight(),
        color = containerColor,
        shape = resolvedShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .safeDrawingPadding()
                .padding(vertical = 24.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            header?.invoke(this)

            items.forEachIndexed { index, item ->
                AsgardNavigationRailItem(
                    item = item,
                    selected = hasValidSelection && index == selectedIndex,
                    onClick = { onSelect(index) },
                    showLabel = showLabel,
                    selectedIndicatorColor = selectedIndicatorColor,
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor,
                    unselectedAlpha = unselectedAlpha,
                    labelStyle = labelStyle
                )
            }

            footer?.invoke(this)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AsgardNavigationRailItem(
    item: AsgardNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedAlpha: Float = 0.7f,
    labelStyle: TextStyle? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val contentColorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val alphaEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    val containerColor by animateColorAsState(
        targetValue = if (selected) selectedIndicatorColor else Color.Transparent,
        animationSpec = containerColorSpec,
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unselectedContentColor,
        animationSpec = contentColorSpec,
        label = "contentColor"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else unselectedAlpha,
        animationSpec = alphaEffectsSpec,
        label = "contentAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .expressivePress(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .animateExpressiveResize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val icon: @Composable () -> Unit = {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.icon,
                    contentDescription = item.contentDescription,
                    tint = contentColor
                )
            }

            if (item.badge != null) {
                BadgedBox(
                    badge = {
                        if (item.badge.isNotEmpty()) {
                            Badge { Text(item.badge) }
                        } else {
                            Badge()
                        }
                    }
                ) {
                    icon()
                }
            } else {
                icon()
            }

            if (showLabel) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    color = contentColor,
                    style = labelStyle
                        ?: if (selected) MaterialTheme.typography.labelMedium
                        else MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
