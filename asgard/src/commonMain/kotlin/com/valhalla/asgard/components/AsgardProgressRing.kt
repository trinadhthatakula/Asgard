package com.valhalla.asgard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A circular progress / gauge ring drawn on a [Canvas]: a faint full-sweep track with a
 * rounded-cap progress arc on top, and a centered [content] slot for a value/label.
 *
 * @param progress the fraction filled, in `0f..1f` (coerced into range).
 * @param modifier the [Modifier] applied to the ring container.
 * @param size the overall diameter.
 * @param strokeWidth the arc thickness.
 * @param progressColor the color of the filled arc.
 * @param trackColor the color of the background track.
 * @param startAngle the angle (degrees, clockwise from 3 o'clock) where the sweep begins; `-90f` is the top.
 * @param sweepAngle the total angular span of the track/progress, in degrees.
 * @param content centered content (e.g. a percentage or countdown).
 */
@Composable
fun AsgardProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 8.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    startAngle: Float = -90f,
    sweepAngle: Float = 360f,
    content: @Composable () -> Unit = {},
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val sw = strokeWidth.toPx()
            val stroke = Stroke(width = sw, cap = StrokeCap.Round)
            val topLeft = Offset(sw / 2f, sw / 2f)
            val arcSize = Size(this.size.width - sw, this.size.height - sw)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = progressColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        content()
    }
}
