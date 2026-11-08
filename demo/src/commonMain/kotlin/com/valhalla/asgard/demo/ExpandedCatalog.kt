package com.valhalla.asgard.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.components.*
import com.valhalla.asgard.navigation.*
import com.valhalla.asgard.charts.*
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

private val destinations = listOf(
    AsgardNavItem(Icons.Rounded.Star, "Home"),
    AsgardNavItem(Icons.Rounded.Search, "Search"),
    AsgardNavItem(Icons.Rounded.Settings, "Settings", badge = "2"),
)

internal val expandedCatalog = listOf(
    ComponentEntry("Navigation variations", "Navigation",
        "Combine container placement, destination appearance, labels and shapes. Routes stay in the app.",
        "AsgardNavigationBar(items, selectedIndex, onSelect,\n    configuration = AsgardNavigationConfiguration(\n        placement = AsgardNavigationPlacement.Floating,\n        appearance = AsgardNavigationAppearance.Segmented,\n        labels = AsgardNavigationLabels.Always,\n    ),\n)") {
        var selected by remember { mutableIntStateOf(0) }
        var placement by remember { mutableIntStateOf(1) }
        var appearance by remember { mutableIntStateOf(1) }
        var labels by remember { mutableIntStateOf(1) }
        var icons by remember { mutableStateOf(true) }
        var rail by remember { mutableStateOf(false) }
        var square by remember { mutableStateOf(false) }
        val config = AsgardNavigationConfiguration(
            placement = AsgardNavigationPlacement.entries[placement],
            appearance = AsgardNavigationAppearance.entries[appearance],
            labels = AsgardNavigationLabels.entries[labels], showIcons = icons,
            indicatorShape = if (square) MaterialTheme.shapes.small else null,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (rail) AsgardNavigationRail(destinations, selected, { selected = it }, configuration = config)
            else AsgardNavigationBar(destinations, selected, { selected = it }, configuration = config)
            Choice("Placement", listOf("Docked", "Floating"), placement) { placement = it }
            Choice("Appearance", listOf("Expand", "Segments", "Standard"), appearance) { appearance = it }
            Choice("Labels", listOf("Select", "All", "Hide", "Auto"), labels) { labels = it }
            AsgardSettingToggleRow("Show icons", icons, { icons = it }, modifier = Modifier.fillMaxWidth())
            AsgardSettingToggleRow("Small indicator shape", square, { square = it }, modifier = Modifier.fillMaxWidth())
            AsgardSettingToggleRow("Show rail", rail, { rail = it }, modifier = Modifier.fillMaxWidth())
        }
    },
    ComponentEntry("Connected button shapes", "Input",
        "Shape changes are independent of primary selection colors, for single and multiple selections.",
        "ConnectedButtonGroup(items, selectedIndices, onSelectionChanged,\n    shapes = ConnectedButtonGroupShapes.FixedConnected,\n)") {
        var mode by remember { mutableIntStateOf(1) }
        var selected by remember { mutableStateOf(setOf(0, 1)) }
        var singleton by remember { mutableStateOf(false) }
        val shapes = when (mode) {
            0 -> ConnectedButtonGroupShapes.Expressive
            1 -> ConnectedButtonGroupShapes.FixedConnected
            2 -> ConnectedButtonGroupShapes.Uniform(MaterialTheme.shapes.small)
            else -> ToggleButtonShapes(MaterialTheme.shapes.small, MaterialTheme.shapes.extraSmall, MaterialTheme.shapes.large).let {
                ConnectedButtonGroupShapes.Custom(it, it, it, it)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ConnectedButtonGroup(listOf(ConnectedButtonGroupItem.IconWithLabel(Icons.Rounded.Person, "User apps", "User"),
                ConnectedButtonGroupItem.IconWithLabel(Icons.Rounded.Android, "System apps", "System")).take(if (singleton) 1 else 2),
                selected, { selected = it }, shapes = shapes, modifier = Modifier.fillMaxWidth())
            Choice("Shape behavior", listOf("Motion", "Fixed", "Same", "Custom"), mode) { mode = it }
            AsgardSettingToggleRow("Single button", singleton, { singleton = it }, modifier = Modifier.fillMaxWidth())
        }
    },
    ComponentEntry("AsgardActionSheet", "Structure", "A scrollable message with fixed, wrapping footer actions.",
        "AsgardActionSheet(onDismissRequest, title = \"Action sheet\",\n    actions = { TextButton(onClick = dismiss) { Text(\"Close\") } },\n) { Text(message) }") {
        var open by remember { mutableStateOf(false) }
        TextButton(onClick = { open = true }) { Text("Open action sheet") }
        if (open) {
            val state = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
            val dismiss = rememberAsgardSheetDismiss(state) { open = false }
            AsgardActionSheet({ open = false }, "Action sheet", actions = {
                TextButton(onClick = dismiss) { Text("Close") }
                Button(onClick = dismiss) { Text("Continue") }
            }, sheetState = state) {
                Text("The message scrolls while actions remain reachable. Resize the window to try a short layout.\n\n".repeat(12))
            }
        }
    },
    ComponentEntry("AsgardPicker", "Input", "Controlled query and sections, with domain filtering and recent-history storage left to the app.",
        "AsgardPicker(sections, query, onQueryChange,\n    itemKey = { it.id }, itemLabel = { it.label },\n    isSelected = { it.id == selectedId }, onSelect = onSelect,\n    modifier = Modifier.height(320.dp),\n)") {
        var query by remember { mutableStateOf("") }
        var selected by remember { mutableStateOf("India") }
        var sheet by remember { mutableStateOf(false) }
        val countries = listOf("India", "Australia", "Brazil", "Canada", "Germany", "Japan", "United Kingdom", "United States")
        val sections = listOf(AsgardPickerSection("recent", "Recent", listOf("India", "Japan").filter { it.contains(query, true) }),
            AsgardPickerSection("all", "All countries", countries.filter { it.contains(query, true) }))
        val picker: @Composable () -> Unit = {
            AsgardPicker(sections, query, { query = it }, { it }, { it }, { it == selected }, { selected = it },
                Modifier.fillMaxWidth().height(320.dp), searchIcon = Icons.Rounded.Search, clearIcon = Icons.Rounded.Close)
        }
        Column {
            Text("Selected: $selected")
            TextButton(onClick = { sheet = true }) { Text("Open picker sheet") }
            picker()
        }
        if (sheet) AsgardPickerSheet({ sheet = false }, "Choose country") { picker() }
    },
    ComponentEntry("AsgardActionToolbar", "Actions", "A scrolling selection/editor toolbar with enabled and selected actions.",
        "AsgardActionToolbar(actions = actions, showLabels = true,\n    leading = { Text(\"3 selected\") },\n)") {
        var enabled by remember { mutableStateOf(true) }
        var labels by remember { mutableStateOf(true) }
        var last by remember { mutableStateOf("None") }
        var count by remember { mutableIntStateOf(3) }
        val actions = listOf("Copy" to Icons.Rounded.ContentCopy, "Share" to Icons.Rounded.Share,
            "Hide" to Icons.Rounded.VisibilityOff, "Delete" to Icons.Rounded.Delete).map { (label, icon) ->
            AsgardToolbarAction(label, label, icon, { last = label }, selected = last == label, destructive = label == "Delete")
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsgardActionToolbar(actions, Modifier.fillMaxWidth(), enabled = enabled, showLabels = labels,
                leading = { IconButton(onClick = { count = if (count == 0) 3 else 0 }) { Icon(Icons.Rounded.Close, "Clear or restore selection") } })
            Text("$count selected · Last action: $last")
            AsgardSettingToggleRow("Enabled", enabled, { enabled = it }, modifier = Modifier.fillMaxWidth())
            AsgardSettingToggleRow("Action labels", labels, { labels = it }, modifier = Modifier.fillMaxWidth())
        }
    },
    ComponentEntry("AsgardAvatar", "Display", "Image content, initials and icon fallback without requiring an image loader.",
        "AsgardAvatar(initials = \"TT\", contentDescription = \"Profile\",\n    shape = MaterialTheme.shapes.large,\n    image = { /* caller image loader content */ },\n)") {
        var image by remember { mutableStateOf(false) }
        var round by remember { mutableStateOf(true) }
        var taps by remember { mutableIntStateOf(0) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AsgardAvatar(initials = "TT", contentDescription = "Open profile", onClick = { taps++ },
                    shape = if (round) androidx.compose.foundation.shape.CircleShape else MaterialTheme.shapes.large,
                    image = if (image) { { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Favorite, null, tint = MaterialTheme.colorScheme.onTertiary) } } } else null)
                AsgardAvatar(fallbackIcon = Icons.Rounded.Person)
            }
            Text("Profile taps: $taps")
            AsgardSettingToggleRow("Caller image content", image, { image = it }, modifier = Modifier.fillMaxWidth())
            AsgardSettingToggleRow("Circular shape", round, { round = it }, modifier = Modifier.fillMaxWidth())
        }
    },
    ComponentEntry("AsgardLazyScrollbar", "Structure", "Portable list/grid thumb dragging. The host reserves a 48dp end gutter.",
        "val state = rememberLazyListState()\nBox(Modifier.height(320.dp)) {\n    LazyColumn(state = state, modifier = Modifier.padding(end = 48.dp)) { /* items */ }\n    AsgardLazyScrollbar(rememberAsgardScrollbarState(state),\n        Modifier.align(Alignment.CenterEnd))\n}") {
        var grid by remember { mutableStateOf(false) }
        var reverse by remember { mutableStateOf(false) }
        var count by remember { mutableFloatStateOf(80f) }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().height(320.dp)) {
                if (grid) {
                    val state = rememberLazyGridState()
                    LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize().padding(end = 48.dp), state = state, reverseLayout = reverse) {
                        items(count.roundToInt(), span = { if (it % 10 == 0) GridItemSpan(maxLineSpan) else GridItemSpan(1) }) { index ->
                            Text("Cell ${index + 1}", Modifier.height(if (index % 3 == 0) 80.dp else 48.dp).padding(8.dp))
                        }
                    }
                    AsgardLazyScrollbar(rememberAsgardScrollbarState(state), Modifier.align(Alignment.CenterEnd))
                } else {
                    val state = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize().padding(end = 48.dp), state = state, reverseLayout = reverse) {
                        items((0 until count.roundToInt()).toList(), key = { it }) { index ->
                            Text("Item ${index + 1}", Modifier.fillMaxWidth().height(if (index % 3 == 0) 72.dp else 48.dp).padding(12.dp))
                        }
                    }
                    AsgardLazyScrollbar(rememberAsgardScrollbarState(state), Modifier.align(Alignment.CenterEnd))
                }
            }
            AsgardLabeledSlider("Items", count, { count = it }, valueRange = 0f..100f, steps = 99, valueLabel = "${count.roundToInt()}")
            AsgardSettingToggleRow("Grid with variable spans", grid, { grid = it }, modifier = Modifier.fillMaxWidth())
            AsgardSettingToggleRow("Reverse layout", reverse, { reverse = it }, modifier = Modifier.fillMaxWidth())
        }
    },
    ComponentEntry("AsgardCalendarHeatmap", "Charts", "Sample dated entries, missing days and recorded zero. Select a date and add entries to see actual values change.",
        "AsgardCalendarHeatmap(entries, start, endInclusive,\n    selectedDate = selected, onDateSelected = { selected = it },\n    weekStart = DayOfWeek.MONDAY,\n)") {
        val end = LocalDate(2026, 11, 8)
        var days by remember { mutableFloatStateOf(28f) }
        var sunday by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf(end) }
        var entries by remember { mutableStateOf(listOf(AsgardHeatmapEntry(end, 1.0), AsgardHeatmapEntry(LocalDate(2026, 11, 7), 0.0), AsgardHeatmapEntry(LocalDate(2026, 11, 4), 3.0))) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsgardCalendarHeatmap(entries, LocalDate.fromEpochDays(end.toEpochDays() - days.roundToInt() + 1), end,
                Modifier.fillMaxWidth(), selectedDate = selected, onDateSelected = { selected = it },
                weekStart = if (sunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY,
                weekLabel = { "${it.day}/${it.month.ordinal + 1}" },
                dayLabel = { it.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() } })
            Text("Selected: $selected · Value: ${entries.filter { it.date == selected }.sumOf { it.value }}")
            TextButton(onClick = { entries = entries + AsgardHeatmapEntry(selected, 1.0) }) { Text("Add an entry for selected date") }
            AsgardLabeledSlider("Range in days", days, { days = it }, valueRange = 7f..84f, steps = 76, valueLabel = "${days.roundToInt()}")
            AsgardSettingToggleRow("Week starts Sunday", sunday, { sunday = it }, modifier = Modifier.fillMaxWidth())
        }
    },
)

@Composable
private fun Choice(label: String, values: List<String>, selected: Int, change: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        ConnectedButtonGroup(values.map { ConnectedButtonGroupItem.Label(it) }, selected, change,
            shapes = ConnectedButtonGroupShapes.FixedConnected, modifier = Modifier.fillMaxWidth())
    }
}
