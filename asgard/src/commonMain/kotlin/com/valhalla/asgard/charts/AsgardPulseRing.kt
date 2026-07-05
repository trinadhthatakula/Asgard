package com.valhalla.asgard.charts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A decorative "attention pulse": an expanding, fading ring drawn behind centered [content]. Use it
 * to draw the eye to a premium/new affordance (a crown, a badge, a CTA).
 *
 * ```kotlin
 * AsgardPulseRing(color = colorScheme.tertiary) {
 *     Icon(Icons.Rounded.WorkspacePremium, contentDescription = "Go Pro")
 * }
 * ```
 *
 * @param color the ring color (pulses from opaque to transparent).
 * @param ringSize the base diameter the ring expands from.
 * @param durationMillis the period of one pulse.
 * @param maxScale the peak scale the ring expands to (1f = the ring reaches [ringSize]).
 * @param pulsing when false the animation is disabled (reduced-motion escape hatch); only [content] shows.
 * @param content centered over the pulse (e.g. an icon or badge).
 */
@Composable
fun AsgardPulseRing(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    ringSize: Dp = 32.dp,
    durationMillis: Int = 1200,
    maxScale: Float = 1f,
    pulsing: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (pulsing) {
            val transition = rememberInfiniteTransition(label = "asgard-pulse")
            val scale by transition.animateFloat(
                initialValue = 0f,
                targetValue = maxScale,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "pulse-scale",
            )
            val alpha by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "pulse-alpha",
            )
            Canvas(Modifier.size(ringSize).clipToBounds()) {
                drawCircle(
                    color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = (size.minDimension / 2f) * scale,
                )
            }
        }
        content()
    }
}
