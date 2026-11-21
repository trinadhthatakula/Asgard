package com.valhalla.asgard

import androidx.compose.ui.unit.dp
import com.valhalla.asgard.charts.AsgardBarStack
import com.valhalla.asgard.charts.asgardStackScale
import com.valhalla.asgard.charts.timelineDistance
import com.valhalla.asgard.charts.asgardLabelSampleStep
import com.valhalla.asgard.charts.asgardLineChartYBounds
import com.valhalla.asgard.charts.asgardResolveMaxTotal
import com.valhalla.asgard.components.asgardClampProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the pure domain math extracted from the (otherwise Canvas-rendered) components.
 * Rendering/interaction behaviour is verified visually via the demo gallery.
 */
class AsgardDomainTest {

    // ── AsgardProgressRing: progress clamp ──────────────────────────────────────

    @Test
    fun clampProgress_coercesIntoUnitRange() {
        assertEquals(0f, asgardClampProgress(0f))
        assertEquals(1f, asgardClampProgress(1f))
        assertEquals(0.5f, asgardClampProgress(0.5f))
        assertEquals(1f, asgardClampProgress(2f))
        assertEquals(0f, asgardClampProgress(-1f))
    }

    @Test
    fun clampProgress_mapsNonFiniteToZero() {
        assertEquals(0f, asgardClampProgress(Float.NaN))
        assertEquals(0f, asgardClampProgress(Float.POSITIVE_INFINITY))
        assertEquals(0f, asgardClampProgress(Float.NEGATIVE_INFINITY))
    }

    // ── AsgardLineChart: Y auto-scaling ─────────────────────────────────────────

    @Test
    fun lineChartYBounds_padsSpanSymmetrically() {
        val (lo, hi) = asgardLineChartYBounds(0f, 10f, 0.15f)
        assertEquals(-1.5f, lo, 1e-4f)
        assertEquals(11.5f, hi, 1e-4f)
    }

    @Test
    fun lineChartYBounds_zeroSpanStillProducesRange() {
        val (lo, hi) = asgardLineChartYBounds(5f, 5f, 0.15f)
        assertEquals(4.85f, lo, 1e-4f)
        assertEquals(5.15f, hi, 1e-4f)
        assertTrue(hi > lo)
    }

    // ── AsgardLineChart: X label sampling ───────────────────────────────────────

    @Test
    fun labelSampleStep_samplesAtMostMaxLabels() {
        assertEquals(2, asgardLabelSampleStep(10, 5))
        assertEquals(1, asgardLabelSampleStep(3, 5))
        assertEquals(25, asgardLabelSampleStep(100, 4))
    }

    @Test
    fun labelSampleStep_neverZero() {
        assertEquals(1, asgardLabelSampleStep(0, 5))
        assertEquals(1, asgardLabelSampleStep(10, 0))
    }

    // ── AsgardStackedBarChart: max-total resolution ─────────────────────────────

    @Test
    fun resolveMaxTotal_prefersExplicit() {
        assertEquals(20f, asgardResolveMaxTotal(listOf(3f, 5f), explicit = 20f))
    }

    @Test
    fun resolveMaxTotal_derivesTallestBarWhenNull() {
        assertEquals(8f, asgardResolveMaxTotal(listOf(3f, 8f, 5f), explicit = null))
    }

    @Test
    fun resolveMaxTotal_flooredToOneToAvoidDivByZero() {
        assertEquals(1f, asgardResolveMaxTotal(emptyList(), explicit = null))
        assertEquals(1f, asgardResolveMaxTotal(listOf(0f, 0f), explicit = null))
        assertEquals(1f, asgardResolveMaxTotal(listOf(3f), explicit = -5f))
    }

    @Test
    fun stackedBarsUseOnlyDrawableValuesAndHandleLargeTotals() {
        val bars = listOf(AsgardBarStack(listOf(-9f, 2f, Float.NaN, Float.POSITIVE_INFINITY)))
        assertEquals(2.0, asgardStackScale(bars, explicit = null))
        val large = listOf(AsgardBarStack(listOf(Float.MAX_VALUE, Float.MAX_VALUE)))
        assertEquals(2.0 * Float.MAX_VALUE, asgardStackScale(large, explicit = null))
    }

    @Test
    fun chartBoundsHandleExtremeFiniteSamplesAndTimestamps() {
        assertEquals(-Float.MAX_VALUE to Float.MAX_VALUE,
            asgardLineChartYBounds(-Float.MAX_VALUE, Float.MAX_VALUE, 0.15f))
        assertEquals(1.0, timelineDistance(Long.MAX_VALUE - 1, Long.MAX_VALUE))
        assertEquals(ULong.MAX_VALUE.toDouble(), timelineDistance(Long.MIN_VALUE, Long.MAX_VALUE))
        assertEquals(2, asgardLabelSampleStep(Int.MAX_VALUE, Int.MAX_VALUE - 1))
    }

    // ── AsgardDefaults: spacing tokens ──────────────────────────────────────────

    @Test
    fun defaults_spacingConstants() {
        assertEquals(16.dp, AsgardDefaults.contentPadding)
        assertEquals(24.dp, AsgardDefaults.iconSize)
        assertEquals(48.dp, AsgardDefaults.iconChipSize)
        assertEquals(32.dp, AsgardDefaults.navContainerRadius)
    }
}
