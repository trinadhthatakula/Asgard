package com.valhalla.asgard.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The swatch drawn beside a legend label. */
enum class AsgardLegendSwatch { Square, Dot }

/** One entry in an [AsgardChartLegend]: a [label] and the [color] it identifies. */
data class AsgardLegendEntry(val label: String, val color: Color)

/**
 * A wrapping legend for charts: a [FlowRow] of colored swatch + label pairs, so every chart renders
 * a consistent key. Pairs with [AsgardLineChart], [AsgardStackedBarChart] and [AsgardTimelineBar].
 *
 * @param entries the legend items.
 * @param swatch the swatch shape ([AsgardLegendSwatch.Square] or [AsgardLegendSwatch.Dot]).
 * @param swatchSize the swatch size.
 * @param itemSpacing horizontal (and wrapped-row vertical) spacing between items.
 * @param textStyle the label text style.
 * @param textColor the label text color.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AsgardChartLegend(
    entries: List<AsgardLegendEntry>,
    modifier: Modifier = Modifier,
    swatch: AsgardLegendSwatch = AsgardLegendSwatch.Square,
    swatchSize: Dp = 12.dp,
    itemSpacing: Dp = 16.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing / 2),
    ) {
        entries.forEach { entry ->
            AsgardLegendItem(
                label = entry.label,
                color = entry.color,
                swatch = swatch,
                swatchSize = swatchSize,
                textStyle = textStyle,
                textColor = textColor,
            )
        }
    }
}

/** A single legend swatch + label pair (also usable standalone). */
@Composable
fun AsgardLegendItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    swatch: AsgardLegendSwatch = AsgardLegendSwatch.Square,
    swatchSize: Dp = 12.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val shape = if (swatch == AsgardLegendSwatch.Dot) CircleShape else RoundedCornerShape(2.dp)
        androidx.compose.foundation.layout.Box(
            Modifier.size(swatchSize).background(color, shape),
        )
        Text(label, style = textStyle, color = textColor)
    }
}
