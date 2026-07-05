package com.valhalla.asgard.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults
import com.valhalla.asgard.animateExpressiveResize
import com.valhalla.asgard.expressivePress

/**
 * A pill-style bottom navigation bar. Each destination collapses to its icon when unselected
 * and expands to show its label when selected, with expressive motion between states.
 *
 * Any [AsgardNavItem.badge] that is non-null is rendered as a small badge over the item's icon.
 * If [selectedIndex] is out of range for [items], no destination is highlighted (no crash).
 *
 * @param items The destinations to display.
 * @param selectedIndex The index of the currently selected destination; out-of-range values
 *   simply leave every destination unselected.
 * @param onSelect Invoked with the tapped destination's index.
 * @param modifier The [Modifier] applied to the bar.
 * @param showLabel Whether the selected destination expands to show its text label.
 * @param containerColor The background color of the bar.
 * @param selectedIndicatorColor The pill/background color behind the selected destination.
 * @param selectedContentColor The icon/label color for the selected destination.
 * @param unselectedContentColor The icon color for unselected destinations.
 * @param unselectedAlpha The content alpha applied to unselected destinations.
 * @param shape The shape of the bar; `null` keeps the default top-rounded (32.dp) corners.
 * @param labelStyle The [TextStyle] for the selected destination's label; `null` uses
 *   `MaterialTheme.typography.labelLarge`.
 */
@Composable
fun AsgardNavigationBar(
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
    labelStyle: TextStyle? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = shape ?: RoundedCornerShape(
            topStart = AsgardDefaults.navContainerRadius,
            topEnd = AsgardDefaults.navContainerRadius,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                AsgardNavigationBarItem(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    showLabel = showLabel,
                    selectedIndicatorColor = selectedIndicatorColor,
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor,
                    unselectedAlpha = unselectedAlpha,
                    labelStyle = labelStyle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AsgardNavigationBarItem(
    item: AsgardNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedAlpha: Float = 0.7f,
    labelStyle: TextStyle? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val contentColorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val alphaEffectsSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.fastSpatialSpec<IntSize>()

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
            .clip(RoundedCornerShape(32.dp))
            .background(containerColor)
            .expressivePress(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .animateExpressiveResize()
            .padding(horizontal = if (selected) 20.dp else 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    item.badge?.let { badgeText ->
                        if (badgeText.isEmpty()) Badge() else Badge { Text(badgeText) }
                    }
                }
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.icon,
                    contentDescription = item.contentDescription,
                    tint = contentColor
                )
            }

            AnimatedVisibility(
                visible = selected && showLabel,
                enter = fadeIn(animationSpec = alphaEffectsSpec) +
                        expandHorizontally(
                            animationSpec = spatialSpec
                        ),
                exit = fadeOut(animationSpec = alphaEffectsSpec) +
                        shrinkHorizontally(
                            animationSpec = spatialSpec
                        )
            ) {
                Text(
                    text = item.label,
                    color = contentColor,
                    style = labelStyle ?: MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}
