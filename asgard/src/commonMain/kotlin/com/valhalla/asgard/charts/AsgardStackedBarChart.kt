package com.valhalla.asgard.charts

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One bar in an [AsgardStackedBarChart]: a list of segment [values] stacked bottom-to-top, plus a
 * [dimmed] flag for partial/incomplete bars (rendered at a reduced alpha).
 */
data class AsgardBarStack(val values: List<Float>, val dimmed: Boolean = false)

/**
 * A vertical stacked-bar chart drawn on a single [Canvas]: one bar per [AsgardBarStack], each split
 * into stacked segments colored by [segmentColors] (index-aligned to each stack's `values`), over a
 * set of horizontal grid lines. Bars scale to a shared [maxTotal] (derived from the data when null).
 *
 * ```kotlin
 * AsgardStackedBarChart(
 *     bars = days.map { AsgardBarStack(listOf(it.onHours, it.offHours), dimmed = it.isPartial) },
 *     segmentColors = listOf(colorScheme.primary, colorScheme.surfaceVariant),
 *     modifier = Modifier.fillMaxWidth().height(160.dp),
 * )
 * ```
 *
 * @param bars the bars, left to right.
 * @param segmentColors one color per stack index; a stack with more values than colors reuses the last.
 * @param maxTotal the value that maps to full bar height; `null` derives it from the tallest bar.
 * @param barWidthFraction fraction of each bar's slot occupied by the bar (the rest is gap).
 * @param gridLineCount number of interior horizontal grid divisions.
 * @param gridColor color of the grid lines.
 * @param dimmedAlpha alpha applied to segments of a [AsgardBarStack.dimmed] bar.
 * @param cornerRadius corner radius applied to the top of each bar.
 */
@Composable
fun AsgardStackedBarChart(
    bars: List<AsgardBarStack>,
    segmentColors: List<Color>,
    modifier: Modifier = Modifier,
    maxTotal: Float? = null,
    barWidthFraction: Float = 0.6f,
    gridLineCount: Int = 4,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    dimmedAlpha: Float = 0.45f,
    cornerRadius: Dp = 3.dp,
) {
    if (bars.isEmpty() || segmentColors.isEmpty()) {
        Canvas(modifier) {}
        return
    }
    val resolvedMax = asgardResolveMaxTotal(bars.map { it.values.sum() }, maxTotal)

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val radiusPx = cornerRadius.toPx()

        // Grid lines.
        val divisions = gridLineCount.coerceAtLeast(1)
        for (i in 0..divisions) {
            val y = h * i.toFloat() / divisions
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val slot = w / bars.size
        val barWidth = slot * barWidthFraction.coerceIn(0.05f, 1f)

        bars.forEachIndexed { index, bar ->
            val slotStart = slot * index
            val left = slotStart + (slot - barWidth) / 2f
            val alpha = if (bar.dimmed) dimmedAlpha else 1f
            var yCursor = h // stack upward from the baseline
            bar.values.forEachIndexed { segIndex, value ->
                if (value <= 0f) return@forEachIndexed
                val segHeight = (value / resolvedMax) * h
                val top = (yCursor - segHeight).coerceAtLeast(0f)
                val color = segmentColors[segIndex.coerceAtMost(segmentColors.lastIndex)].copy(alpha = alpha)
                // Only the topmost drawn segment gets rounded corners; others are square so the
                // stack reads as one bar. A simple approach: round the whole bar's top by rounding
                // the last (top) non-zero segment.
                val isTopSegment = segIndex == bar.values.indexOfLast { it > 0f }
                if (isTopSegment && radiusPx > 0f) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, yCursor - top),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
                    )
                } else {
                    drawRect(color = color, topLeft = Offset(left, top), size = Size(barWidth, yCursor - top))
                }
                yCursor = top
            }
        }
    }
}

/**
 * Resolves the value that maps to full bar height: the [explicit] max when given, else the tallest
 * bar total from [barSums]; a non-positive result is floored to `1f` to avoid divide-by-zero.
 * Extracted for unit testing.
 */
internal fun asgardResolveMaxTotal(barSums: List<Float>, explicit: Float?): Float {
    val max = explicit ?: (barSums.maxOrNull() ?: 0f)
    return if (max <= 0f) 1f else max
}
