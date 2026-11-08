package com.valhalla.asgard

import androidx.compose.runtime.Composable
import com.valhalla.asgard.charts.*
import com.valhalla.asgard.components.*
import com.valhalla.asgard.navigation.*
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.*

class ExpandedComponentsTest {
    @Test fun heatmapPadsPartialWeeksAndIncludesLeapDay() {
        val cells = asgardHeatmapCells(emptyList(), LocalDate(2024, 2, 28), LocalDate(2024, 3, 2))
        assertEquals(7, cells.size)
        assertEquals(LocalDate(2024, 2, 26), cells.first().date)
        assertEquals(LocalDate(2024, 3, 3), cells.last().date)
        assertEquals(4, cells.count { it.inRange })
        assertTrue(cells.any { it.date == LocalDate(2024, 2, 29) && it.inRange })
        assertFalse(cells.first().inRange)
    }
    @Test fun heatmapSumsDuplicateDatesButDistinguishesMissingFromZero() {
        val day = LocalDate(2026, 1, 1)
        val zero = LocalDate(2026, 1, 2)
        val missing = LocalDate(2026, 1, 3)
        val cells = asgardHeatmapCells(listOf(AsgardHeatmapEntry(day, 2.0), AsgardHeatmapEntry(day, 3.0), AsgardHeatmapEntry(zero, 0.0)), day, missing)
        assertEquals(5.0, cells.single { it.date == day }.value)
        assertEquals(0.0, cells.single { it.date == zero }.value)
        assertNull(cells.single { it.date == missing }.value)
    }
    @Test fun heatmapSupportsSundayStartAndYearBoundary() {
        val cells = asgardHeatmapCells(emptyList(), LocalDate(2025, 12, 31), LocalDate(2026, 1, 4), DayOfWeek.SUNDAY)
        assertEquals(LocalDate(2025, 12, 28), cells.first().date)
        assertEquals(14, cells.size)
        assertEquals(5, cells.count { it.inRange })
    }
    @Test fun heatmapRejectsInvalidValuesAndOverflow() {
        val day = LocalDate(2026, 1, 1)
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, -1.0)) {
            assertFailsWith<IllegalArgumentException> { asgardHeatmapCells(listOf(AsgardHeatmapEntry(day, value)), day, day) }
        }
        assertFailsWith<IllegalArgumentException> { asgardHeatmapCells(listOf(AsgardHeatmapEntry(day, Double.MAX_VALUE), AsgardHeatmapEntry(day, Double.MAX_VALUE)), day, day) }
    }
    @Test fun heatmapRejectsInvertedAndExcessiveRanges() {
        assertFailsWith<IllegalArgumentException> { asgardHeatmapCells(emptyList(), LocalDate(2026, 1, 2), LocalDate(2026, 1, 1)) }
        assertFailsWith<IllegalArgumentException> { asgardHeatmapCells(emptyList(), LocalDate(2000, 1, 1), LocalDate(2026, 1, 1)) }
    }
    @Test fun pickerSectionKeysCannotCollideWithRepeatedItemsOrDelimiters() {
        assertNotEquals(pickerItemKey("recent", "India"), pickerItemKey("all", "India"))
        assertNotEquals(pickerItemKey("a:b", "c"), pickerItemKey("a", "b:c"))
        assertNotEquals(pickerItemKey("", "abc"), pickerItemKey("a", "bc"))
    }
    @Test fun scrollbarEmptyAndShortContentHasNoTravelOrNaN() {
        for (metrics in listOf(AsgardScrollbarMetrics(0, 0, 0), AsgardScrollbarMetrics(0, 80, 100))) {
            assertEquals(0f, metrics.range)
            assertEquals(0f, metrics.fraction)
            assertTrue(metrics.viewportFraction.isFinite())
        }
    }
    @Test fun scrollbarClampsOffsetsAndSupportsLargeContent() {
        assertEquals(0f, AsgardScrollbarMetrics(-100, 1000, 100).fraction)
        assertEquals(1f, AsgardScrollbarMetrics(1200, 1000, 100).fraction)
        assertEquals(.5f, AsgardScrollbarMetrics(450, 1000, 100).fraction)
        assertTrue(AsgardScrollbarMetrics(Int.MAX_VALUE, Int.MAX_VALUE, 100).range > 0)
    }
    @Test fun navigationCaptionUsesActualCountAndMeasuredLabelWidth() {
        assertFalse(navigationNeedsCaption(320f, 3, 100f))
        assertTrue(navigationNeedsCaption(320f, 5, 100f))
        assertTrue(navigationNeedsCaption(320f, 3, 220f))
        assertFalse(navigationNeedsCaption(0f, 0, 100f))
    }
}

/** Compile fixture: old positional/default calls must remain unambiguous. */
@Composable
private fun ExistingConsumer(items: List<AsgardNavItem>, buttons: List<ConnectedButtonGroupItem>) {
    AsgardNavigationBar(items, 0, {})
    AsgardNavigationRail(items, 0, {})
    ConnectedButtonGroup(buttons, 0, {})
    ConnectedButtonGroup(buttons, setOf(0), {})
}
