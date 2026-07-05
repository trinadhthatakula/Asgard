package com.valhalla.asgard.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A single colored span on an [AsgardTimelineBar], in the same time unit as the window bounds. */
data class AsgardTimelineSegment(val startMillis: Long, val endMillis: Long, val color: Color)

/**
 * A compact horizontal timeline strip: colored [segments] laid out across a
 * `[windowStartMillis, windowEndMillis]` window over a background [trackColor] (which shows through
 * any uncovered gaps). A generic Gantt/interval band for sessions, states, schedules, etc.
 *
 * ```kotlin
 * AsgardTimelineBar(
 *     segments = states.map { AsgardTimelineSegment(it.start, it.end, it.color) },
 *     windowStartMillis = windowStart,
 *     windowEndMillis = windowEnd,
 *     modifier = Modifier.fillMaxWidth(),
 * )
 * ```
 *
 * @param segments the spans to draw; each is clamped to the window and skipped if fully outside it.
 * @param windowStartMillis the value mapped to the left edge.
 * @param windowEndMillis the value mapped to the right edge (must be > start; otherwise nothing draws).
 * @param height the strip height.
 * @param trackColor the background track color shown behind/between segments.
 * @param cornerRadius corner radius clipping the whole strip (rounds the ends).
 */
@Composable
fun AsgardTimelineBar(
    segments: List<AsgardTimelineSegment>,
    windowStartMillis: Long,
    windowEndMillis: Long,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    cornerRadius: Dp = 5.dp,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        drawRect(trackColor)

        val span = (windowEndMillis - windowStartMillis).toFloat()
        if (span <= 0f) return@Canvas

        segments.forEach { seg ->
            val start = seg.startMillis.coerceIn(windowStartMillis, windowEndMillis)
            val end = seg.endMillis.coerceIn(windowStartMillis, windowEndMillis)
            if (end <= start) return@forEach
            val left = (start - windowStartMillis) / span * size.width
            val right = (end - windowStartMillis) / span * size.width
            drawRect(
                color = seg.color,
                topLeft = Offset(left, 0f),
                size = Size(right - left, size.height),
            )
        }
    }
}
