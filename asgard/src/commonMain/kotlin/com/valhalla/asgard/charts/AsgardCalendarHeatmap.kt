package com.valhalla.asgard.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class AsgardHeatmapEntry(val date: LocalDate, val value: Double)
data class AsgardHeatmapCell(val date: LocalDate, val value: Double?, val inRange: Boolean)

/**
 * Inclusive Gregorian range padded to complete weeks. Duplicate dates sum; missing dates stay
 * null, distinct from recorded zero. Non-finite/negative values and overflowing sums are rejected.
 * At most ten years per chart keeps allocation and semantics bounded. Out-of-range entries
 * do not render but their values still must satisfy the input contract.
 */
fun asgardHeatmapCells(
    entries: List<AsgardHeatmapEntry>, start: LocalDate, endInclusive: LocalDate,
    weekStart: DayOfWeek = DayOfWeek.MONDAY,
): List<AsgardHeatmapCell> {
    val first = start.toEpochDays()
    val last = endInclusive.toEpochDays()
    require(last >= first && last - first < 3660) { "Heatmap range must contain 1 to 3660 days" }
    val values = mutableMapOf<LocalDate, Double>()
    entries.forEach {
        require(it.value.isFinite() && it.value >= 0.0) { "Heatmap values must be finite and nonnegative" }
        val sum = (values[it.date] ?: 0.0) + it.value
        require(sum.isFinite()) { "Heatmap date sum overflowed" }
        values[it.date] = sum
    }
    val padding = (start.dayOfWeek.ordinal - weekStart.ordinal + 7) % 7
    val count = last - first + 1 + padding
    val paddedCount = ((count + 6) / 7) * 7
    return List(paddedCount.toInt()) { index ->
        val date = LocalDate.fromEpochDays(first - padding + index)
        val active = date >= start && date <= endInclusive
        AsgardHeatmapCell(date, if (active) values[date] else null, active)
    }
}

/** Scrollable calendar grid. Interactive cells reserve at least 48dp; static cells stay compact. */
@Composable
fun AsgardCalendarHeatmap(
    entries: List<AsgardHeatmapEntry>,
    start: LocalDate,
    endInclusive: LocalDate,
    modifier: Modifier = Modifier,
    weekStart: DayOfWeek = DayOfWeek.MONDAY,
    selectedDate: LocalDate? = null,
    onDateSelected: ((LocalDate) -> Unit)? = null,
    maxValue: Double? = null,
    intensityLevels: Int = 4,
    cellSize: Dp = 20.dp,
    spacing: Dp = 4.dp,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    missingColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    zeroColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    weekLabel: ((LocalDate) -> String)? = null,
    dayLabel: ((DayOfWeek) -> String)? = null,
    cellLabel: (LocalDate, Double?) -> String = { date, value -> "$date: ${value ?: "No data"}" },
) {
    require(cellSize > 0.dp && spacing >= 0.dp && intensityLevels > 0)
    require(maxValue == null || (maxValue.isFinite() && maxValue > 0.0))
    val cells = remember(entries, start, endInclusive, weekStart) { asgardHeatmapCells(entries, start, endInclusive, weekStart) }
    val maximum = maxValue ?: (cells.maxOfOrNull { it.value ?: 0.0 } ?: 0.0).coerceAtLeast(1.0)
    val targetSize = if (onDateSelected != null) maxOf(cellSize, 48.dp) else cellSize
    val headingHeight = with(androidx.compose.ui.platform.LocalDensity.current) { MaterialTheme.typography.labelSmall.fontSize.toDp() * 1.5f }.coerceAtLeast(24.dp)
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(spacing)) {
        if (dayLabel != null) Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            if (weekLabel != null) Spacer(Modifier.height(headingHeight))
            repeat(7) { index -> Box(Modifier.height(targetSize), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
                Text(dayLabel(DayOfWeek.entries[(weekStart.ordinal + index) % 7]), style = MaterialTheme.typography.labelSmall)
            } }
        }
        cells.chunked(7).forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                if (weekLabel != null) Text(weekLabel(week.first().date), Modifier.width(targetSize).height(headingHeight),
                    style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                week.forEach { cell ->
                    if (!cell.inRange) Spacer(Modifier.size(targetSize))
                    else {
                        val color = when {
                            cell.value == null -> missingColor
                            cell.value == 0.0 -> zeroColor
                            else -> {
                                val level = kotlin.math.ceil((cell.value / maximum).coerceIn(0.0, 1.0) * intensityLevels).toInt().coerceIn(1, intensityLevels)
                                activeColor.copy(alpha = .25f + .75f * level / intensityLevels)
                            }
                        }
                        Box(
                            Modifier.size(targetSize).background(color, shape)
                                .then(if (selectedDate == cell.date) Modifier.border(2.dp, selectedColor, shape) else Modifier)
                                .semantics { contentDescription = cellLabel(cell.date, cell.value); selected = selectedDate == cell.date }
                                .then(if (onDateSelected != null) Modifier.clickable(role = Role.Button) { onDateSelected(cell.date) } else Modifier),
                        )
                    }
                }
            }
        }
    }
}
