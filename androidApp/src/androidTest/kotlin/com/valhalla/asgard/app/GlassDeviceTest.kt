package com.valhalla.asgard.app

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valhalla.asgard.components.AsgardActionToolbar
import com.valhalla.asgard.components.AsgardToolbarAction
import com.valhalla.asgard.glass.AsgardGlassSurface
import com.valhalla.asgard.glass.asgardGlass
import com.valhalla.asgard.glass.asgardGlassSource
import com.valhalla.asgard.glass.rememberAsgardGlassSource
import com.valhalla.asgard.navigation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlassDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentTestActivity>()

    @Test fun defaultNavigationLabelKeepsContrastAgainstExtremeBackdrops() {
        var dark by mutableStateOf(true)
        val darkScheme = darkColorScheme()
        val lightScheme = lightColorScheme()
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkScheme else lightScheme) {
                val source = rememberAsgardGlassSource()
                Box(Modifier.size(120.dp)) {
                    Box(Modifier.fillMaxSize().asgardGlassSource(source).background(if (dark) Color.White else Color.Black))
                    AsgardGlassSurface(source, Modifier.fillMaxSize().testTag("frost")) {}
                }
            }
        }
        for (scheme in listOf(darkScheme, lightScheme)) {
            compose.runOnIdle { dark = scheme === darkScheme }
            val pixels = compose.onNodeWithTag("frost").captureToImage().toPixelMap()
            val background = pixels[pixels.width / 2, pixels.height / 2].luminance()
            val label = scheme.onSurfaceVariant.luminance()
            val contrast = (maxOf(label, background) + .05f) / (minOf(label, background) + .05f)
            assertTrue("Expected at least 4.5:1 against the backdrop, measured $contrast", contrast >= 4.5f)
        }
    }

    @Test fun reducedTransparencyUsesAnOpaqueBaseOnlyForTheOptedInSurface() {
        var selected by mutableStateOf(true)
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(surface = Color.White)) {
                val source = rememberAsgardGlassSource(enabled = false)
                Box(Modifier.size(80.dp).background(Color.Red).testTag("scene")) {
                    AsgardGlassSurface(source, Modifier.fillMaxSize(), enabled = selected,
                        containerColor = Color.Transparent) {}
                }
            }
        }
        fun center(): Color {
            val pixels = compose.onNodeWithTag("scene").captureToImage().toPixelMap()
            return pixels[pixels.width / 2, pixels.height / 2]
        }
        assertEquals(Color.White, center())
        compose.runOnIdle { selected = false }
        assertEquals(Color.Red, center())
    }

    @Test fun genericModifierFrostsOnlyTheSelectedButtonAndPreservesItsShapeAndClick() {
        var selected by mutableStateOf(false)
        var allowFrost by mutableStateOf(true)
        var clicks = 0
        compose.setContent {
            MaterialTheme {
                val source = rememberAsgardGlassSource(allowFrost)
                Box(Modifier.size(160.dp, 64.dp).testTag("scene")) {
                    Box(Modifier.fillMaxSize().asgardGlassSource(source).background(Color.Green))
                    Button(
                        onClick = { clicks++ },
                        modifier = Modifier.fillMaxSize().asgardGlass(source, selected, CircleShape, Color.White),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color.Transparent else Color.Blue),
                        shape = CircleShape,
                    ) {}
                }
            }
        }
        val scene = compose.onNodeWithTag("scene")
        fun center(): Color {
            val pixels = scene.captureToImage().toPixelMap()
            return pixels[pixels.width / 2, pixels.height / 2]
        }
        assertEquals(Color.Blue, center())
        compose.runOnIdle { selected = true }
        if (Build.VERSION.SDK_INT >= 33) {
            compose.waitUntil(3_000) { center().let { it.green > .98f && it.red in 0.65f..0.95f } }
        } else {
            assertEquals(Color.White, center())
        }
        assertEquals(Color.Green, scene.captureToImage().toPixelMap()[1, 1])
        compose.runOnIdle { allowFrost = false }
        assertEquals(Color.White, center())
        compose.runOnIdle { selected = false }
        assertEquals(Color.Blue, center())
        compose.runOnIdle { selected = true; allowFrost = true }
        compose.onNode(hasClickAction()).performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test fun frostSamplesOnlySelectedContainersAndClearsRemovedSources() {
        var toolbar by mutableStateOf(true)
        var navigation by mutableStateOf(false)
        var enabled by mutableStateOf(true)
        var attached by mutableStateOf(true)
        var green by mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            MaterialTheme {
                val source = rememberAsgardGlassSource(enabled)
                val icon = remember { ImageVector.Builder("Test", 24.dp, 24.dp, 24f, 24f).build() }
                Box(Modifier.size(240.dp, 200.dp)) {
                    Canvas(Modifier.fillMaxSize().then(if (attached) Modifier.asgardGlassSource(source) else Modifier)) {
                        drawRect(if (green) Color.Green else Color.Red)
                        if (!green) {
                            // Two-pixel stripes expose a tint-only implementation as well as stale captures.
                            for (x in 0 until size.width.toInt() step 4) {
                                drawRect(Color.Blue, Offset(x.toFloat(), 0f), Size(2f, size.height))
                            }
                        }
                    }
                    Column {
                        AsgardActionToolbar(
                            listOf(AsgardToolbarAction("sort", "Sort", icon, { clicks++ })),
                            source.frosted(toolbar), modifier = Modifier.testTag("toolbar"),
                            shape = RectangleShape, containerColor = Color.White, elevation = 0.dp,
                        )
                        AsgardNavigationBar(
                            listOf(AsgardNavItem(icon, "Home"), AsgardNavItem(icon, "Saved")), 0, { clicks++ },
                            AsgardNavigationConfiguration(placement = AsgardNavigationPlacement.Floating,
                                containerShape = RectangleShape, maxWidth = 180.dp, floatingMargin = 0.dp, elevation = 0.dp),
                            source.frosted(navigation), modifier = Modifier.testTag("nav"), containerColor = Color.White,
                        )
                        AsgardGlassSurface(source, Modifier.size(80.dp, 32.dp).testTag("custom"),
                            shape = RectangleShape, containerColor = Color.White) {}
                    }
                }
            }
        }

        fun samples(tag: String, outside: Boolean = false): List<Color> {
            val pixels = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
            val x = if (outside) 1 else pixels.width * 2 / 3
            val y = if (tag == "nav") 4 else pixels.height / 2
            return List(8) { pixels[x + it, y] }
        }
        fun solid(tag: String) = assertTrue("Expected white $tag", samples(tag).all { it == Color.White })
        fun frost(tag: String) {
            compose.waitUntil(timeoutMillis = 3_000) {
                samples(tag).all { it.green < .9f && it.red > it.green + .06f && it.blue > it.green + .06f }
            }
        }
        compose.onNodeWithContentDescription("Sort").performClick()
        compose.onNodeWithContentDescription("Saved").performClick()
        compose.runOnIdle { assertEquals(2, clicks) }
        solid("nav")
        if (Build.VERSION.SDK_INT < 33) {
            solid("toolbar")
            solid("custom")
            return
        }
        frost("toolbar")
        frost("custom")
        compose.runOnIdle { toolbar = false; navigation = true }
        solid("toolbar")
        frost("nav")
        assertTrue("Floating margins must show the sharp source",
            samples("nav", outside = true).all { it == Color.Red || it == Color.Blue })
        compose.runOnIdle { green = true }
        compose.waitUntil(timeoutMillis = 3_000) { samples("nav").all { it.green > it.red + .15f } }
        compose.runOnIdle { attached = false }
        solid("nav")
        solid("custom")
        compose.runOnIdle { attached = true; enabled = false }
        solid("nav")
        solid("custom")
    }
}
