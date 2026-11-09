package com.valhalla.asgard.app

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valhalla.asgard.components.AsgardProgressRing
import com.valhalla.asgard.components.AsgardShimmer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class RenderingMotionDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentTestActivity>()

    @Test fun bouncyProgressRemainsInRangeThroughoutBothDirectionsAndRetargeting() {
        var progress by mutableFloatStateOf(0f)
        compose.setContent { MaterialTheme {
            AsgardProgressRing(progress, size = 100.dp, animate = true,
                animationSpec = spring(dampingRatio = .12f, stiffness = 180f),
                contentDescription = "Animated progress")
        } }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeByFrame()
        for (target in listOf(1f, 0f, 1f)) {
            compose.runOnIdle { progress = target }
            repeat(45) {
                compose.mainClock.advanceTimeBy(16)
                val sample = compose.onNodeWithContentDescription("Animated progress")
                    .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
                assertTrue("Animated sample must remain normalized: $sample", sample.isFinite() && sample in 0f..1f)
            }
        }
    }

    @Test fun disablingProgressAnimationUsesTheNewTargetImmediately() {
        var progress by mutableFloatStateOf(0f)
        var animated by mutableStateOf(true)
        compose.setContent { MaterialTheme {
            AsgardProgressRing(progress, animate = animated, contentDescription = "Progress")
        } }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { progress = 1f }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { animated = false; progress = .25f }
        compose.mainClock.advanceTimeByFrame()
        val sample = compose.onNodeWithContentDescription("Progress").fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo].current
        assertEquals(.25f, sample, 0f)
    }

    @Test fun shimmerReducedMotionAndColorChangesRenderAStaticSurface() {
        var animated by mutableStateOf(true)
        var color by mutableStateOf(Color.Red)
        compose.setContent { MaterialTheme {
            AsgardShimmer(Modifier.size(100.dp).testTag("shimmer"), baseColor = color,
                highlightColor = Color.Green, shape = RectangleShape, animate = animated)
        } }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(400)
        compose.runOnIdle { animated = false; color = Color.Blue }
        compose.mainClock.advanceTimeByFrame()
        val pixels = compose.onNodeWithTag("shimmer").captureToImage().toPixelMap()
        for (x in listOf(pixels.width / 4, pixels.width / 2, 3 * pixels.width / 4)) {
            assertEquals(Color.Blue, pixels[x, pixels.height / 2])
        }
    }

    @Test fun cachedShimmerMatchesMovingGradientAfterResizeAndColorChanges() {
        var width by mutableStateOf(100.dp)
        var base by mutableStateOf(Color.Red)
        var highlight by mutableStateOf(Color.Green)
        compose.setContent { MaterialTheme {
            val phase by rememberInfiniteTransition().animateFloat(0f, 1f,
                infiniteRepeatable(tween(800), RepeatMode.Restart))
            Column {
                AsgardShimmer(Modifier.size(width, 40.dp).testTag("cached"), baseColor = base,
                    highlightColor = highlight, shape = RectangleShape, durationMillis = 800)
                Box(Modifier.size(width, 40.dp).testTag("reference").drawBehind {
                    val x = phase * size.width * 2f - size.width
                    drawRect(Brush.linearGradient(listOf(base, highlight, base),
                        Offset(x, 0f), Offset(x + size.width, 0f)))
                })
            }
        } }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeByFrame()
        repeat(3) { configuration ->
            if (configuration == 1) compose.runOnIdle { width = 160.dp; base = Color.Blue; highlight = Color.Yellow }
            if (configuration == 2) {
                compose.runOnIdle { width = 0.dp }
                compose.mainClock.advanceTimeByFrame()
                compose.runOnIdle { width = 80.dp }
            }
            repeat(4) {
                compose.mainClock.advanceTimeBy(100)
                val actual = compose.onNodeWithTag("cached").captureToImage().toPixelMap()
                val expected = compose.onNodeWithTag("reference").captureToImage().toPixelMap()
                assertEquals(expected.width, actual.width)
                for (x in 1..9) {
                    val px = x * actual.width / 10
                    val y = actual.height / 2
                    assertEquals(expected[px, y].red, actual[px, y].red, .01f)
                    assertEquals(expected[px, y].green, actual[px, y].green, .01f)
                    assertEquals(expected[px, y].blue, actual[px, y].blue, .01f)
                }
            }
        }
    }

    @Test fun progressDrawingCacheRefreshesSizeStrokeColorsAndBrush() {
        var configuration by mutableIntStateOf(0)
        compose.setContent { MaterialTheme {
            val diameter = listOf(100.dp, 160.dp, 80.dp)[configuration]
            val stroke = listOf(8.dp, 12.dp, 4.dp)[configuration]
            val trackStroke = listOf(8.dp, 16.dp, 20.dp)[configuration]
            AsgardProgressRing(.25f, Modifier.testTag("cached ring"), size = diameter,
                strokeWidth = stroke, trackStrokeWidth = trackStroke,
                progressColor = if (configuration == 0) Color.Blue else Color.Cyan,
                trackColor = listOf(Color.Red, Color.Yellow, Color.Magenta)[configuration],
                progressBrush = if (configuration == 1) Brush.linearGradient(listOf(Color.Green, Color.Green)) else null)
        } }
        for (index in 0..2) {
            compose.runOnIdle { configuration = index }
            val pixels = compose.onNodeWithTag("cached ring").captureToImage().toPixelMap()
            val diameter = listOf(100f, 160f, 80f)[index]
            val maxStroke = listOf(8f, 16f, 20f)[index]
            val inset = (maxStroke / 2f * pixels.width / diameter).roundToInt()
            assertEquals(pixels.width, pixels.height)
            assertEquals(listOf(Color.Blue, Color.Green, Color.Cyan)[index], pixels[pixels.width / 2, inset])
            assertEquals(listOf(Color.Red, Color.Yellow, Color.Magenta)[index],
                pixels[pixels.width / 2, pixels.height - inset - 1])
        }
    }
}
