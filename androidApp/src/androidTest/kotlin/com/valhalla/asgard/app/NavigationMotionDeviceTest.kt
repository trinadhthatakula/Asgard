package com.valhalla.asgard.app

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valhalla.asgard.navigation.*
import com.valhalla.asgard.expressivePress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationMotionDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentTestActivity>()
    private val icon = ImageVector.Builder("Test", 24.dp, 24.dp, 24f, 24f).build()
    private val items = listOf("Home", "Applications", "Settings").map { AsgardNavItem(icon, it) }

    private fun expandingSelectionInterpolates(placement: AsgardNavigationPlacement) {
        var selected by mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                AsgardNavigationBar(items, selected, { selected = it },
                    configuration = AsgardNavigationConfiguration(placement = placement, labels = AsgardNavigationLabels.Selected),
                    modifier = Modifier.width(360.dp))
            }
        }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(1500)
        val target = compose.onNodeWithContentDescription("Applications")
        val before = target.fetchSemanticsNode().boundsInRoot.width
        compose.runOnIdle { selected = 1 }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeBy(80)
        val during = target.fetchSemanticsNode().boundsInRoot.width
        compose.mainClock.advanceTimeBy(1500)
        val after = target.fetchSemanticsNode().boundsInRoot.width
        assertTrue("Target should expand: $before → $after", after > before + 1f)
        assertTrue("Width should interpolate, not snap: $before → $during → $after", during > before + .5f && during < after - .5f)
        target.assertIsSelected()
    }

    @Test fun dockedTabsExpandOverMultipleFrames() = expandingSelectionInterpolates(AsgardNavigationPlacement.Docked)
    @Test fun floatingTabsExpandOverMultipleFrames() = expandingSelectionInterpolates(AsgardNavigationPlacement.Floating)

    @Test fun rapidSelectionChangesRetargetMotionWithoutLosingTheFinalDestination() {
        var selected by mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                AsgardNavigationBar(items, selected, { selected = it }, configuration = AsgardNavigationConfiguration(), modifier = Modifier.width(360.dp))
            }
        }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(1500)
        for (next in listOf(1, 2, 0, 2)) {
            compose.runOnIdle { selected = next }
            compose.mainClock.advanceTimeBy(64)
            compose.onNodeWithContentDescription(items[next].label).assertIsSelected()
        }
        compose.mainClock.advanceTimeBy(1500)
        val bounds = items.map { compose.onNodeWithContentDescription(it.label).fetchSemanticsNode().boundsInRoot }
        bounds.zipWithNext { left, right -> assertTrue("Settled targets must not overlap", left.right <= right.left + 1f) }
        compose.onNodeWithContentDescription("Settings").assertIsSelected()
    }

    @Test fun automaticCaptionDoesNotSwitchTheBarHeightBetweenShortAndLongLabels() {
        val names = listOf("Home", "A much longer destination label", "Settings").map { AsgardNavItem(icon, it) }
        var selected by mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                AsgardNavigationBar(names, selected, { selected = it }, configuration = AsgardNavigationConfiguration(), modifier = Modifier.width(260.dp).testTag("bar"))
            }
        }
        val before = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot.height
        compose.runOnIdle { selected = 1 }
        compose.waitForIdle()
        val after = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot.height
        assertEquals("Caption placement should be stable across tabs", before, after, .5f)
    }

    @Test fun automaticSegmentedLabelsUseACaptionWhenInlineTextCannotFit() {
        val names = listOf("Home", "Applications", "Freezer", "Settings").map { AsgardNavItem(icon, it) }
        var labels by mutableStateOf(AsgardNavigationLabels.SelectedCaptionWhenNeeded)
        compose.setContent {
            MaterialTheme {
                AsgardNavigationBar(names, 1, {}, configuration = AsgardNavigationConfiguration(
                    appearance = AsgardNavigationAppearance.Segmented, labels = labels,
                ), modifier = Modifier.width(320.dp).testTag("bar"))
            }
        }
        val captionHeight = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot.height
        compose.runOnIdle { labels = AsgardNavigationLabels.Hidden }
        val iconHeight = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot.height
        assertTrue("A narrow segmented bar should place a readable caption outside its equal-width targets", captionHeight > iconHeight + 1f)
    }

    @Test fun updatedPressScaleIsUsedWithoutReplacingTheInteractionSource() {
        val source = MutableInteractionSource()
        var scale by mutableFloatStateOf(.95f)
        lateinit var scope: CoroutineScope
        compose.setContent {
            MaterialTheme {
                scope = rememberCoroutineScope()
                Box(Modifier.size(100.dp).expressivePress(source, scaleOnPress = scale).testTag("press"))
            }
        }
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(1500)
        val before = compose.onNodeWithTag("press").fetchSemanticsNode().boundsInRoot.width
        compose.runOnIdle { scale = .8f }
        compose.mainClock.advanceTimeByFrame()
        val press = PressInteraction.Press(Offset.Zero)
        compose.runOnIdle { scope.launch { source.emit(press) } }
        compose.mainClock.advanceTimeBy(1500)
        val pressed = compose.onNodeWithTag("press").fetchSemanticsNode().boundsInRoot.width
        assertEquals("The updated parameter must drive the next press", .8f, pressed / before, .02f)
        compose.runOnIdle { scope.launch { source.emit(PressInteraction.Release(press)) } }
        compose.mainClock.advanceTimeBy(1500)
        assertEquals(before, compose.onNodeWithTag("press").fetchSemanticsNode().boundsInRoot.width, .5f)
    }
}
