package com.valhalla.asgard.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valhalla.asgard.charts.*
import com.valhalla.asgard.compatibility.PublishedConsumer
import com.valhalla.asgard.components.*
import com.valhalla.asgard.navigation.*
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentTestActivity>()
    private val icon = ImageVector.Builder("Test", 24.dp, 24.dp, 24f, 24f).build()
    private fun content(block: @Composable () -> Unit) { compose.setContent { MaterialTheme { block() } } }

    @Test fun published201BytecodeRunsAgainstCurrentLibrary() {
        var page by mutableIntStateOf(0)
        content { key(page) { PublishedConsumer(page) } }
        compose.onNodeWithText("Legacy system").performClick().assertIsOn()
        for (next in 1..6) {
            compose.runOnIdle { page = next }
            when (next) {
                1, 2 -> compose.onNodeWithContentDescription("Legacy search").performClick().assertIsSelected()
                3 -> compose.onNodeWithContentDescription("Clear").performClick()
                4 -> compose.onNodeWithText("Legacy row").assertExists()
                5 -> compose.onNodeWithText("Legacy toggle").performClick().assertIsOn()
                6 -> compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(.8f)) }
            }
        }
    }

    @Test fun connectedSelectionsRemainIndependent() {
        var selected by mutableStateOf(setOf(0))
        content { ConnectedButtonGroup(listOf(ConnectedButtonGroupItem.Label("User"), ConnectedButtonGroupItem.Label("System")), selected, { selected = it }, shapes = ConnectedButtonGroupShapes.FixedConnected) }
        compose.onNodeWithText("System").performClick().assertIsOn()
        compose.onNodeWithText("User").assertIsOn().performClick().assertIsOff()
        compose.onNodeWithText("System").assertIsOn()
        compose.runOnIdle { assertEquals(setOf(1), selected) }
    }

    @Test fun disabledSearchDoesNotClear() {
        var query by mutableStateOf("Keep this")
        content { AsgardSearchBar(query, { query = it }, clearIcon = icon, enabled = false) }
        compose.onNodeWithContentDescription("Clear").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals("Keep this", query) }
    }

    @Test fun settingsHasOneAccessibleSwitchAndOneCallback() {
        var checked by mutableStateOf(false)
        var calls = 0
        content { AsgardSettingToggleRow("Setting", checked, { checked = it; calls++ }) }
        compose.onAllNodes(isToggleable()).assertCountEquals(1)
        compose.onNodeWithText("Setting").performClick().assertIsOn()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun navigationHasNamedDistinct48dpTargetsInRtlAtDoubleTextSize() {
        content {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f), LocalLayoutDirection provides LayoutDirection.Rtl) {
                AsgardNavigationBar(listOf(AsgardNavItem(icon, "Home"), AsgardNavItem(icon, "Search"), AsgardNavItem(icon, "Settings")), 0, {},
                    configuration = AsgardNavigationConfiguration(appearance = AsgardNavigationAppearance.Segmented, labels = AsgardNavigationLabels.Always), modifier = Modifier.width(320.dp))
            }
        }
        val bounds = compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).fetchSemanticsNodes().map { it.boundsInRoot }.sortedBy { it.left }
        assertEquals(3, bounds.size)
        val minimum = with(compose.density) { 48.dp.toPx() }
        bounds.forEach { assertTrue(it.width >= minimum - 1 && it.height >= minimum - 1) }
        bounds.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left + 1) }
        compose.onNodeWithContentDescription("Home").assertIsSelected()
    }

    @Test fun sheetFooterStaysReachableAndDismissesAfterHide() {
        var open by mutableStateOf(true)
        content {
            if (open) {
                val state = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
                val dismiss = rememberAsgardSheetDismiss(state) { open = false }
                AsgardActionSheet({ open = false }, "Sheet", actions = { TextButton(onClick = dismiss) { Text("Close sheet") } }, sheetState = state) { Text("Long content\n".repeat(150)) }
            } else Text("Dismissed")
        }
        compose.onNodeWithText("Close sheet").assertIsDisplayed().performClick()
        compose.onNodeWithText("Dismissed").assertIsDisplayed()
        compose.onNodeWithText("Close sheet").assertDoesNotExist()
    }

    @Test fun pickerSelectsRepeatedItemThenClearsAndShowsEmptyResults() {
        var query by mutableStateOf("")
        var selected by mutableStateOf("India")
        content {
            val countries = listOf("India", "Japan").filter { it.contains(query, true) }
            AsgardPicker(listOf(AsgardPickerSection("recent", "Recent", countries.take(1)), AsgardPickerSection("all", "All", countries)), query, { query = it }, { it }, { it }, { it == selected }, { selected = it }, Modifier.height(360.dp), clearIcon = icon)
        }
        compose.onNodeWithContentDescription("Japan").performClick().assertIsSelected()
        compose.onNode(hasSetTextAction()).performTextInput("No matching country")
        compose.onNodeWithText("No results").assertIsDisplayed()
        compose.onNodeWithContentDescription("Clear").performClick()
        compose.onNodeWithContentDescription("Japan").assertIsSelected()
    }

    @Test fun sheetFooterRemainsReachableWithNativeKeyboardVisible() {
        var open by mutableStateOf(true)
        var query by mutableStateOf("")
        content {
            if (open) {
                val state = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
                val dismiss = rememberAsgardSheetDismiss(state) { open = false }
                AsgardActionSheet({ open = false }, "Keyboard sheet", actions = { TextButton(onClick = dismiss) { Text("Done editing") } }, sheetState = state) {
                    TextField(query, { query = it }, label = { Text("Sheet input") })
                    Text("Scrollable content\n".repeat(100))
                }
            } else Text("Editing dismissed")
        }
        compose.onNode(hasSetTextAction()).performClick().performTextInput("Test input")
        compose.waitUntil(5000) { compose.activity.window.decorView.rootWindowInsets?.isVisible(android.view.WindowInsets.Type.ime()) == true }
        compose.onNodeWithText("Done editing").assertIsDisplayed().performClick()
        compose.onNodeWithText("Editing dismissed").assertIsDisplayed()
    }

    @Test fun toolbarDisabledActionDoesNotInvokeCallback() {
        var calls = 0
        content { AsgardActionToolbar(listOf(AsgardToolbarAction("copy", "Copy action", icon, { calls++ }, enabled = false), AsgardToolbarAction("share", "Share action", icon, { calls++ }))) }
        compose.onNodeWithContentDescription("Copy action").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, calls) }
        compose.onNodeWithContentDescription("Share action").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun avatarHasOneNamedClickTarget() {
        var calls = 0
        content { AsgardAvatar(initials = "TT", contentDescription = "Profile", onClick = { calls++ }) }
        compose.onAllNodes(hasClickAction()).assertCountEquals(1)
        compose.onNodeWithContentDescription("Profile").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun heatmapDistinguishesMissingAndZeroAndSelectsDate() {
        val first = LocalDate(2026, 11, 2)
        val second = LocalDate(2026, 11, 3)
        var selected by mutableStateOf(first)
        content { AsgardCalendarHeatmap(listOf(AsgardHeatmapEntry(first, 0.0)), first, second, selectedDate = selected, onDateSelected = { selected = it }) }
        compose.onNodeWithContentDescription("2026-11-02: 0.0").assertIsSelected()
        compose.onNodeWithContentDescription("2026-11-03: No data").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(second, selected) }
    }

    @Test fun listScrollbarReachesEndAndHomeViaKeyboard() {
        val focus = FocusRequester()
        var last = 0
        var first = 0
        content {
            val state = rememberLazyListState()
            LaunchedEffect(state) {
                snapshotFlow { state.firstVisibleItemIndex to (state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: 0) }
                    .collect { (visibleFirst, visibleLast) -> first = visibleFirst; last = visibleLast }
            }
            Box(Modifier.height(240.dp)) {
                LazyColumn(state = state, modifier = Modifier.padding(end = 48.dp)) { items(80) { Text("Item $it", Modifier.height(48.dp)) } }
                AsgardLazyScrollbar(rememberAsgardScrollbarState(state), Modifier.focusRequester(focus))
            }
        }
        compose.runOnIdle { focus.requestFocus() }
        compose.onNodeWithContentDescription("Scroll").performKeyInput { pressKey(Key.MoveEnd) }
        compose.waitUntil(5000) { last == 79 }
        compose.onNodeWithContentDescription("Scroll").performKeyInput { pressKey(Key.MoveHome) }
        compose.waitUntil(5000) { first == 0 }
    }

    @Test fun reverseListScrollbarMovesThroughVariableRows() = checkReverseScrollbar(grid = false)
    @Test fun reverseGridScrollbarMovesThroughVariableSpans() = checkReverseScrollbar(grid = true)
    private fun checkReverseScrollbar(grid: Boolean) {
        var last = 0
        var first = 0
        content {
            Box(Modifier.height(240.dp)) {
                val adapter = if (grid) {
                    val state = rememberLazyGridState()
                    LazyVerticalGrid(GridCells.Fixed(2), state = state, reverseLayout = true, modifier = Modifier.padding(end = 48.dp)) {
                        items(80, span = { if (it % 10 == 0) GridItemSpan(maxLineSpan) else GridItemSpan(1) }) { Text("Cell $it", Modifier.height(if (it % 3 == 0) 80.dp else 48.dp)) }
                    }
                    rememberAsgardScrollbarState(state)
                } else {
                    val state = rememberLazyListState()
                    LazyColumn(state = state, reverseLayout = true, modifier = Modifier.padding(end = 48.dp)) { items(80) { Text("Item $it", Modifier.height(if (it % 3 == 0) 80.dp else 48.dp)) } }
                    rememberAsgardScrollbarState(state)
                }
                LaunchedEffect(adapter) {
                    snapshotFlow { adapter.firstVisibleIndex to adapter.lastVisibleIndex }
                        .collect { (visibleFirst, visibleLast) -> first = visibleFirst; last = visibleLast }
                }
                AsgardLazyScrollbar(adapter)
            }
        }
        compose.onNodeWithContentDescription("Scroll").performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(0f)) }
        compose.waitUntil(5000) { last == 79 }
        compose.onNodeWithContentDescription("Scroll").performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(1f)) }
        compose.waitUntil(5000) { first == 0 }
    }

    @Test fun scrollbarSurvivesContentShrinkingToEmptyAndGrowingAgain() {
        var count by mutableIntStateOf(80)
        var first = 0
        content {
            val state = rememberLazyListState()
            LaunchedEffect(state) { snapshotFlow { state.firstVisibleItemIndex }.collect { first = it } }
            Box(Modifier.height(240.dp)) {
                LazyColumn(state = state, modifier = Modifier.padding(end = 48.dp)) { items(count) { Text("Item $it", Modifier.height(48.dp)) } }
                AsgardLazyScrollbar(rememberAsgardScrollbarState(state))
            }
        }
        compose.onNodeWithContentDescription("Scroll").performSemanticsAction(SemanticsActions.SetProgress) { it(.5f) }
        compose.waitUntil(5000) { first > 0 }
        compose.runOnIdle { count = 0 }
        compose.onNodeWithContentDescription("Scroll").assertDoesNotExist()
        compose.runOnIdle { count = 80 }
        compose.onNodeWithContentDescription("Scroll").performSemanticsAction(SemanticsActions.SetProgress) { it(.5f) }
        compose.waitUntil(5000) { first > 0 }
    }

    @Test fun pickerQueryChangeResetsListToStart() {
        var query by mutableStateOf("")
        var first = 0
        content {
            val state = rememberLazyListState(initialFirstVisibleItemIndex = 40)
            LaunchedEffect(state) { snapshotFlow { state.firstVisibleItemIndex }.collect { first = it } }
            AsgardPicker(listOf(AsgardPickerSection("all", null, (0..79).toList())), query, { query = it }, { it.toString() }, { "Item $it" }, { false }, {}, Modifier.height(320.dp), listState = state)
        }
        // The initial query also resets the list. Scroll after that effect has completed.
        compose.onNodeWithContentDescription("Item 0").assertExists()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex)).performScrollToIndex(40)
        compose.waitUntil(5000) { first >= 40 }
        compose.runOnIdle { query = "new query" }
        compose.waitUntil(5000) { first == 0 }
    }

    @Test fun zeroTravelScrollbarDragDoesNotIssueScrollRequest() {
        var calls = 0
        val state = object : AsgardScrollbarState {
            override val metrics = AsgardScrollbarMetrics(0, 1000, 100)
            override val firstVisibleIndex = 0
            override val lastVisibleIndex = 1
            override val isScrolling = false
            override suspend fun scrollToFraction(fraction: Float) { calls++ }
        }
        content { Box(Modifier.height(24.dp)) { AsgardLazyScrollbar(state, minThumbHeight = 48.dp) } }
        compose.onNodeWithContentDescription("Scroll").performTouchInput { swipeUp() }
        compose.runOnIdle { assertEquals(0, calls) }
    }
}
