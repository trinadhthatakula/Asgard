package com.valhalla.asgard.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A circular progress / gauge ring drawn on a [Canvas]: a faint full-sweep track with a
 * rounded-cap progress arc on top, and a centered [content] slot for a value/label.
 *
 * @param progress the fraction filled, in `0f..1f` (coerced into range; non-finite values render as `0f`).
 * @param modifier the [Modifier] applied to the ring container.
 * @param size the overall diameter.
 * @param strokeWidth the arc thickness.
 * @param progressColor the color of the filled arc.
 * @param trackColor the color of the background track.
 * @param startAngle the angle (degrees, clockwise from 3 o'clock) where the sweep begins; `-90f` is the top.
 * @param sweepAngle the total angular span of the track/progress, in degrees.
 * @param cap the [StrokeCap] used for the track and progress arcs.
 * @param trackStrokeWidth the thickness of the background track arc; defaults to [strokeWidth].
 * @param progressBrush when non-null, the [Brush] used to stroke the progress arc instead of [progressColor].
 * @param animate when `true`, the progress fraction is animated via [animateFloatAsState] using [animationSpec].
 * @param animationSpec the [AnimationSpec] driving the progress animation when [animate] is `true`.
 * @param contentDescription when non-null, an accessibility description applied to the root, along with
 *   [ProgressBarRangeInfo] reflecting the current progress.
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
    cap: StrokeCap = StrokeCap.Round,
    trackStrokeWidth: Dp = strokeWidth,
    progressBrush: Brush? = null,
    animate: Boolean = false,
    animationSpec: AnimationSpec<Float> = spring(),
    contentDescription: String? = null,
    content: @Composable () -> Unit = {},
) {
    val target = asgardClampProgress(progress)
    val p = if (animate) {
        animateFloatAsState(targetValue = target, animationSpec = animationSpec).value
    } else {
        target
    }
    val rootModifier = if (contentDescription != null) {
        modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
                progressBarRangeInfo = ProgressBarRangeInfo(p, 0f..1f)
            }
    } else {
        modifier.size(size)
    }
    Box(modifier = rootModifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val trackSw = trackStrokeWidth.toPx()
            val trackStroke = Stroke(width = trackSw, cap = cap)
            val progressStroke = Stroke(width = sw, cap = cap)
            val maxSw = maxOf(sw, trackSw)
            val topLeft = Offset(maxSw / 2f, maxSw / 2f)
            val arcSize = Size(this.size.width - maxSw, this.size.height - maxSw)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = trackStroke,
            )
            if (progressBrush != null) {
                drawArc(
                    brush = progressBrush,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * p,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = progressStroke,
                )
            } else {
                drawArc(
                    color = progressColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * p,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = progressStroke,
                )
            }
        }
        content()
    }
}

/**
 * Clamps a raw progress value into `0f..1f`, mapping non-finite input (`NaN`/±∞) to `0f`.
 * Extracted so the guard can be unit-tested without a rendering harness.
 */
internal fun asgardClampProgress(progress: Float): Float =
    (if (progress.isFinite()) progress else 0f).coerceIn(0f, 1f)
