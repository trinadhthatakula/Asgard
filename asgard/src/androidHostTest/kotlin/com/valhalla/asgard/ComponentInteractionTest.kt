package com.valhalla.asgard

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import org.robolectric.android.controller.ActivityController
import org.junit.After
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.components.*
import com.valhalla.asgard.navigation.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.PAUSED)
class ComponentInteractionTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val icon = ImageVector.Builder("Test", 24.dp, 24.dp, 24f, 24f).build()
    private var controller: ActivityController<ComponentActivity>? = null
    @After fun disposeActivity() { controller?.pause()?.stop()?.destroy() }
    private fun content(block: @Composable () -> Unit) {
        controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller!!.get()
        val view = ComposeView(activity)
        activity.setContentView(view)
        view.setContent { MaterialTheme { block() } }
    }

    @Test fun disabledSearchCannotClearQuery() {
        var query by mutableStateOf("existing")
        content { AsgardSearchBar(query, { query = it }, clearIcon = icon, enabled = false) }
        compose.onNodeWithContentDescription("Clear").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals("existing", query) }
    }

    @Test fun settingToggleHasOneSwitchTargetAndTogglesOnce() {
        var checked by mutableStateOf(false)
        var calls = 0
        content { AsgardSettingToggleRow("Dark theme", checked, { checked = it; calls++ }) }
        compose.onAllNodes(isToggleable()).assertCountEquals(1)
        compose.onNodeWithText("Dark theme").assertIsOff().performClick().assertIsOn()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun hiddenNavigationLabelsRemainNamedAndSelected() {
        var selected by mutableIntStateOf(0)
        content { AsgardNavigationBar(listOf(AsgardNavItem(icon, "Home"), AsgardNavItem(icon, "Search")), selected, { selected = it },
            configuration = AsgardNavigationConfiguration(labels = AsgardNavigationLabels.Hidden)) }
        compose.onNodeWithContentDescription("Home").assertIsSelected()
        compose.onNodeWithContentDescription("Search").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Home").assertIsNotSelected()
    }

    @Test fun narrowRtlNavigationAtLargeFontHasDistinctNamedTargets() {
        content {
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(1f, 2f),
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl,
            ) {
                AsgardNavigationBar(listOf(AsgardNavItem(icon, "Home"), AsgardNavItem(icon, "Search"), AsgardNavItem(icon, "Settings")), 0, {},
                    configuration = AsgardNavigationConfiguration(appearance = AsgardNavigationAppearance.Segmented, labels = AsgardNavigationLabels.Always),
                    modifier = Modifier.width(320.dp))
            }
        }
        val nodes = compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).fetchSemanticsNodes()
        assertEquals(3, nodes.size)
        val bounds = nodes.map { it.boundsInRoot }.sortedBy { it.left }
        bounds.forEach { assertTrue(it.width >= 48f && it.height >= 48f) }
        bounds.zipWithNext().forEach { (left, right) -> assertTrue(left.right <= right.left) }
    }

    @Test fun segmentedRailHasNamedSelectableTargetsWithMinimumBounds() {
        var selected by mutableIntStateOf(-1)
        content {
            AsgardNavigationRail(listOf(AsgardNavItem(icon, "Home"), AsgardNavItem(icon, "Search"), AsgardNavItem(icon, "Settings")), selected, { selected = it },
                configuration = AsgardNavigationConfiguration(appearance = AsgardNavigationAppearance.Segmented, labels = AsgardNavigationLabels.Hidden))
        }
        compose.onNodeWithContentDescription("Settings").assertIsNotSelected().performClick().assertIsSelected()
        val bounds = compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).fetchSemanticsNodes().map { it.boundsInRoot }.sortedBy { it.top }
        assertEquals(3, bounds.size)
        bounds.forEach { assertTrue(it.width >= 48f && it.height >= 48f) }
        bounds.zipWithNext().forEach { (top, bottom) -> assertTrue(top.bottom <= bottom.top) }
    }

    @Test fun longSelectedLabelCannotShrinkNeighborTargetsBelow48dp() {
        content {
            AsgardNavigationBar(listOf(AsgardNavItem(icon, "A very long selected destination name"), AsgardNavItem(icon, "Search"), AsgardNavItem(icon, "Settings")), 0, {},
                configuration = AsgardNavigationConfiguration(labels = AsgardNavigationLabels.Selected), modifier = Modifier.width(320.dp))
        }
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).fetchSemanticsNodes().forEach {
            assertTrue(it.boundsInRoot.width >= 48f && it.boundsInRoot.height >= 48f)
        }
    }

    @Test fun multiselectDoesNotUncheckOtherButtons() {
        var selected by mutableStateOf(setOf(0))
        content { ConnectedButtonGroup(listOf(ConnectedButtonGroupItem.Label("User"), ConnectedButtonGroupItem.Label("System")), selected, { selected = it },
            shapes = ConnectedButtonGroupShapes.FixedConnected) }
        compose.onNodeWithText("System").performClick().assertIsOn()
        compose.onNodeWithText("User").assertIsOn()
        compose.runOnIdle { assertEquals(setOf(0, 1), selected) }
    }

    @Test fun repeatedPickerItemsAcrossSectionsDoNotCollide() {
        var selected by mutableStateOf("India")
        content { AsgardPicker(listOf(AsgardPickerSection("recent", "Recent", listOf("India")), AsgardPickerSection("all", "All", listOf("India", "Japan"))),
            "", {}, { it }, { it }, { it == selected }, { selected = it }, Modifier.height(400.dp)) }
        compose.onNodeWithContentDescription("Japan").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals("Japan", selected) }
    }

    @Test fun scrollbarSemanticsMovesLazyList() {
        var firstVisible = 0
        content {
            val list = rememberLazyListState()
            LaunchedEffect(list) { snapshotFlow { list.firstVisibleItemIndex }.collect { firstVisible = it } }
            Box(Modifier.height(240.dp).width(320.dp)) {
                LazyColumn(state = list, modifier = Modifier.padding(end = 48.dp)) {
                    items(100) { Text("Item $it", Modifier.height(48.dp)) }
                }
                AsgardLazyScrollbar(rememberAsgardScrollbarState(list), Modifier.align(androidx.compose.ui.Alignment.CenterEnd))
            }
        }
        compose.onNodeWithContentDescription("Scroll").performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(.5f)) }
        compose.waitUntil(5000) { firstVisible > 0 }
    }
}
