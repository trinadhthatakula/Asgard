package com.valhalla.asgard.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A skeleton/shimmer placeholder box: a rounded surface with a horizontally-sweeping highlight
 * gradient, shown while real content loads. Set its size via [modifier] at the call site. Colors
 * default to the ambient [MaterialTheme].
 *
 * @param modifier the [Modifier] applied to the box (give it a size).
 * @param cornerRadius the corner radius of the placeholder.
 * @param baseColor the base (dim) color.
 * @param highlightColor the sweeping highlight color.
 * @param durationMillis one full sweep duration.
 */
@Composable
fun AsgardShimmer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    baseColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    highlightColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    durationMillis: Int = 1200,
) {
    val transition = rememberInfiniteTransition(label = "AsgardShimmer")
    val x by transition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX",
    )
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(x, 0f),
        end = Offset(x + 400f, 0f),
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush),
    )
}
