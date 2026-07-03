package com.valhalla.asgard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

/**
 * Applies the Expressive 'Spatial' spring to layout changes.
 * Use this instead of the standard [androidx.compose.animation.animateContentSize].
 *
 * Reads the spring spec from the ambient [MaterialTheme.motionScheme], so the feel
 * matches whatever motion scheme the host app provides.
 */
@Composable
fun Modifier.animateExpressiveResize(): Modifier {
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    return this.animateContentSize(animationSpec = spatialSpec)
}

/**
 * Adds a physical "squish" scale effect on press — the tactile feedback used across
 * Asgard's tappable surfaces (cards, tiles, nav items, custom buttons).
 *
 * Pair it with a [androidx.compose.foundation.clickable] that shares the same
 * [interactionSource] and uses `indication = null`.
 *
 * @param interactionSource the press source to observe (share it with `clickable`).
 * @param scaleOnPress the scale to animate to while pressed (default `0.95`).
 */
fun Modifier.expressivePress(
    interactionSource: InteractionSource,
    scaleOnPress: Float = 0.95f
): Modifier = composed {
    val animatable = remember { Animatable(1f) }
    val motionScheme = MaterialTheme.motionScheme

    LaunchedEffect(interactionSource, motionScheme) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> launch {
                    animatable.animateTo(
                        targetValue = scaleOnPress,
                        animationSpec = motionScheme.fastSpatialSpec()
                    )
                }

                is PressInteraction.Release, is PressInteraction.Cancel -> launch {
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = motionScheme.defaultSpatialSpec()
                    )
                }
            }
        }
    }
    this.graphicsLayer {
        scaleX = animatable.value
        scaleY = animatable.value
    }
}
