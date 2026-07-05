package com.valhalla.asgard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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
 * @param pressSpec animation spec used when animating down to [scaleOnPress] on press.
 *   When `null` (the default) the ambient [MaterialTheme.motionScheme]'s
 *   `fastSpatialSpec` is used, matching the previous behavior.
 * @param releaseSpec animation spec used when animating back to `1f` on release/cancel.
 *   When `null` (the default) the ambient [MaterialTheme.motionScheme]'s
 *   `defaultSpatialSpec` is used, matching the previous behavior.
 */
fun Modifier.expressivePress(
    interactionSource: InteractionSource,
    scaleOnPress: Float = 0.95f,
    pressSpec: AnimationSpec<Float>? = null,
    releaseSpec: AnimationSpec<Float>? = null
): Modifier = composed {
    val animatable = remember { Animatable(1f) }
    val motionScheme = MaterialTheme.motionScheme
    val resolvedPressSpec = pressSpec ?: motionScheme.fastSpatialSpec()
    val resolvedReleaseSpec = releaseSpec ?: motionScheme.defaultSpatialSpec()

    LaunchedEffect(interactionSource, resolvedPressSpec, resolvedReleaseSpec) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> launch {
                    animatable.animateTo(
                        targetValue = scaleOnPress,
                        animationSpec = resolvedPressSpec
                    )
                }

                is PressInteraction.Release, is PressInteraction.Cancel -> launch {
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = resolvedReleaseSpec
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
