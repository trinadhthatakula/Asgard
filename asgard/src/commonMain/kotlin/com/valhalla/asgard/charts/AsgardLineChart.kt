package com.valhalla.asgard.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

/** How a [AsgardLineSeries] connects its points. */
enum class AsgardLineSmoothing { None, Cubic }

/** A single (x, y) sample for a chart, with an optional x-axis [label]. */
data class AsgardChartPoint(val x: Float, val y: Float, val label: String? = null)

/**
 * One line in an [AsgardLineChart].
 *
 * @param points the samples, in draw order (x is used for horizontal placement).
 * @param color the stroke color (and the source of the default area gradient).
 * @param lineWidth the stroke width.
 * @param smoothing [AsgardLineSmoothing.Cubic] for a smooth curve, [AsgardLineSmoothing.None] for straight segments.
 * @param areaFill optional [Brush] filled beneath the line; when `null` and [fillArea] is true a
 *   vertical gradient from `color` (25% alpha) to transparent is used.
 * @param fillArea whether to fill the area beneath the line.
 * @param showEndMarker whether to draw a white-cored ring at the last point.
 */
data class AsgardLineSeries(
    val points: List<AsgardChartPoint>,
    val color: Color,
    val lineWidth: Dp = 3.dp,
    val smoothing: AsgardLineSmoothing = AsgardLineSmoothing.Cubic,
    val areaFill: Brush? = null,
    val fillArea: Boolean = true,
    val showEndMarker: Boolean = true,
)

/**
 * A multi-series line/area chart drawn on a single [Canvas]: smooth (cubic) or straight lines with
 * an optional gradient area fill, horizontal grid lines, right-formatted Y-axis value labels,
 * evenly-spaced X-axis labels, and a trailing end-point marker per series.
 *
 * Everything is theme-agnostic: colors default to the ambient [MaterialTheme]. The Y domain
 * auto-scales from the combined min/max of all series (with [yPaddingFraction] headroom) unless an
 * explicit [yRange] is supplied.
 *
 * ```kotlin
 * AsgardLineChart(
 *     series = listOf(
 *         AsgardLineSeries(petrol.mapIndexed { i, p -> AsgardChartPoint(i.toFloat(), p.price, p.date) },
 *             color = MaterialTheme.colorScheme.primary),
 *         AsgardLineSeries(diesel.mapIndexed { i, p -> AsgardChartPoint(i.toFloat(), p.price) },
 *             color = MaterialTheme.colorScheme.secondary),
 *     ),
 *     yValueFormatter = { "₹${it.roundToInt()}" },
 *     modifier = Modifier.fillMaxWidth().height(220.dp),
 * )
 * ```
 *
 * @param series the lines to draw. Empty series are skipped; if none have points, [emptyContent] shows.
 * @param yRange fixes the Y domain; `null` auto-scales from the data.
 * @param yPaddingFraction fractional headroom added above/below the data range when auto-scaling.
 * @param gridLineCount number of interior horizontal grid divisions.
 * @param gridColor color of the grid lines.
 * @param axisLabelColor color of the axis value/label text.
 * @param axisLabelStyle base [TextStyle] for axis text (its color is overridden by [axisLabelColor]).
 * @param yValueFormatter formats a Y value into its axis label.
 * @param xLabelFormatter maps a point to its X-axis label (`null`/blank = no label for that point).
 * @param maxXLabels the maximum number of X labels to render (they are sampled evenly).
 * @param contentPadding reserves space for the axis labels around the plot area.
 * @param emptyContent shown (centered) when there is no data to plot.
 */
@Composable
fun AsgardLineChart(
    series: List<AsgardLineSeries>,
    modifier: Modifier = Modifier,
    yRange: ClosedFloatingPointRange<Float>? = null,
    yPaddingFraction: Float = 0.15f,
    gridLineCount: Int = 4,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    axisLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    axisLabelStyle: TextStyle = MaterialTheme.typography.labelSmall,
    yValueFormatter: (Float) -> String = { it.roundToInt().toString() },
    xLabelFormatter: (AsgardChartPoint) -> String? = { it.label },
    maxXLabels: Int = 5,
    contentPadding: PaddingValues = PaddingValues(start = 44.dp, top = 12.dp, end = 12.dp, bottom = 24.dp),
    emptyContent: @Composable () -> Unit = {},
) {
    val nonEmpty = series.filter { it.points.isNotEmpty() }
    if (nonEmpty.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) { emptyContent() }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val layoutDirection = LocalLayoutDirection.current
    val resolvedAxisStyle = axisLabelStyle.copy(color = axisLabelColor)

    val allPoints = nonEmpty.flatMap { it.points }
    val xMin = allPoints.minOf { it.x }
    val xMax = allPoints.maxOf { it.x }
    val yMin: Float
    val yMax: Float
    if (yRange != null) {
        yMin = yRange.start
        yMax = yRange.endInclusive
    } else {
        val (lo, hi) = asgardLineChartYBounds(allPoints.minOf { it.y }, allPoints.maxOf { it.y }, yPaddingFraction)
        yMin = lo
        yMax = hi
    }

    // Pre-measure axis labels in composition — measuring inside the DrawScope would re-run every frame.
    val divisions = gridLineCount.coerceAtLeast(1)
    val yLabelResults = (0..divisions).map { i ->
        val value = yMax - (i.toFloat() / divisions) * (yMax - yMin)
        textMeasurer.measure(yValueFormatter(value), resolvedAxisStyle)
    }
    val labelPoints = nonEmpty.first().points
    val xLabelResults: List<Pair<AsgardChartPoint, TextLayoutResult>> =
        if (maxXLabels > 0 && labelPoints.isNotEmpty()) {
            val step = asgardLabelSampleStep(labelPoints.size, maxXLabels)
            labelPoints.filterIndexed { idx, _ -> idx % step == 0 }
                .mapNotNull { p ->
                    val t = xLabelFormatter(p)
                    if (t.isNullOrEmpty()) null else p to textMeasurer.measure(t, resolvedAxisStyle)
                }
        } else {
            emptyList()
        }

    Canvas(modifier) {
        val startPad = contentPadding.calculateStartPadding(layoutDirection).toPx()
        val endPad = contentPadding.calculateEndPadding(layoutDirection).toPx()
        val topPad = contentPadding.calculateTopPadding().toPx()
        val bottomPad = contentPadding.calculateBottomPadding().toPx()

        val plotLeft = startPad
        val plotTop = topPad
        val plotWidth = (size.width - startPad - endPad).coerceAtLeast(1f)
        val plotHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
        val plotBottom = plotTop + plotHeight

        fun xToPx(x: Float): Float {
            val t = if (xMax == xMin) 0.5f else (x - xMin) / (xMax - xMin)
            return plotLeft + t * plotWidth
        }
        fun yToPx(y: Float): Float {
            val t = if (yMax == yMin) 0.5f else (y - yMin) / (yMax - yMin)
            return plotBottom - t * plotHeight
        }

        // Grid + pre-measured Y-axis value labels.
        for (i in 0..divisions) {
            val yPx = plotTop + (i.toFloat() / divisions) * plotHeight
            drawLine(gridColor, Offset(plotLeft, yPx), Offset(plotLeft + plotWidth, yPx), strokeWidth = 1f)
            val measured = yLabelResults[i]
            drawText(
                measured,
                topLeft = Offset(
                    x = (plotLeft - measured.size.width - 6f).coerceAtLeast(0f),
                    y = yPx - measured.size.height / 2f,
                ),
            )
        }

        // Pre-measured X-axis labels.
        xLabelResults.forEach { (p, measured) ->
            val cx = xToPx(p.x)
            drawText(
                measured,
                topLeft = Offset(
                    x = (cx - measured.size.width / 2f)
                        .coerceIn(plotLeft, (plotLeft + plotWidth - measured.size.width).coerceAtLeast(plotLeft)),
                    y = plotBottom + 4f,
                ),
            )
        }

        // Series.
        nonEmpty.forEach { s ->
            val pts = s.points.map { Offset(xToPx(it.x), yToPx(it.y)) }
            val strokePx = s.lineWidth.toPx()

            if (pts.size == 1) {
                if (s.showEndMarker) {
                    drawCircle(Color.White, radius = strokePx + 1.5f, center = pts[0])
                    drawCircle(s.color, radius = strokePx + 1.5f, center = pts[0], style = Stroke(width = 2f))
                }
                return@forEach
            }

            val linePath = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                if (s.smoothing == AsgardLineSmoothing.Cubic) {
                    for (i in 1 until pts.size) {
                        val p0 = pts[i - 1]
                        val p1 = pts[i]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                } else {
                    for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
                }
            }

            if (s.fillArea) {
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(pts.last().x, plotBottom)
                    lineTo(pts.first().x, plotBottom)
                    close()
                }
                val brush = s.areaFill ?: Brush.verticalGradient(
                    colors = listOf(s.color.copy(alpha = 0.25f), Color.Transparent),
                    startY = plotTop,
                    endY = plotBottom,
                )
                drawPath(fillPath, brush)
            }

            drawPath(
                linePath,
                color = s.color,
                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            if (s.showEndMarker) {
                val last = pts.last()
                drawCircle(Color.White, radius = strokePx + 1.5f, center = last)
                drawCircle(s.color, radius = strokePx + 1.5f, center = last, style = Stroke(width = 2f))
            }
        }
    }
}

/**
 * Computes the padded `[min, max]` Y bounds for auto-scaling: adds [paddingFraction] of the data
 * span above and below, treating a zero span as `1f`. Extracted for unit testing.
 */
internal fun asgardLineChartYBounds(dataMin: Float, dataMax: Float, paddingFraction: Float): Pair<Float, Float> {
    val span = (dataMax - dataMin).let { if (it == 0f) 1f else it }
    return (dataMin - span * paddingFraction) to (dataMax + span * paddingFraction)
}

/**
 * The stride used to sample at most [maxLabels] evenly-spaced X labels from [count] points
 * (`>= 1`, never zero). Extracted for unit testing.
 */
internal fun asgardLabelSampleStep(count: Int, maxLabels: Int): Int =
    if (count <= 0 || maxLabels <= 0) 1 else max(1, (count + maxLabels - 1) / maxLabels)
