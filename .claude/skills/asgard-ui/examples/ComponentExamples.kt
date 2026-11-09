@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.valhalla.asgard.skill.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.charts.AsgardCalendarHeatmap
import com.valhalla.asgard.charts.AsgardHeatmapEntry
import com.valhalla.asgard.components.AsgardActionSheet
import com.valhalla.asgard.components.AsgardActionToolbar
import com.valhalla.asgard.components.AsgardAvatar
import com.valhalla.asgard.components.AsgardLazyScrollbar
import com.valhalla.asgard.components.AsgardPicker
import com.valhalla.asgard.components.AsgardPickerSection
import com.valhalla.asgard.components.AsgardPickerSheet
import com.valhalla.asgard.components.AsgardSheet
import com.valhalla.asgard.components.AsgardToolbarAction
import com.valhalla.asgard.components.rememberAsgardScrollbarState
import com.valhalla.asgard.components.rememberAsgardSheetDismiss
import kotlinx.datetime.LocalDate

/** Compose only while open. The host filters choices and decides what selection means. */
@Composable
fun PickerSheetExample(choices: List<String>, selected: String?, onSelected: (String) -> Unit, onDismissed: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(choices, query) { choices.distinct().filter { it.contains(query, ignoreCase = true) } }
    val sheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    val dismiss = rememberAsgardSheetDismiss(sheetState, onDismissed)
    AsgardPickerSheet(onDismissRequest = onDismissed, title = "Choose an item", sheetState = sheetState) {
        AsgardPicker(
            sections = listOf(AsgardPickerSection(id = "all", title = null, items = filtered)),
            query = query,
            onQueryChange = { query = it },
            itemKey = { it },
            itemLabel = { it },
            isSelected = { it == selected },
            onSelect = { onSelected(it); dismiss() },
            modifier = Modifier.fillMaxWidth().height(320.dp),
            searchPlaceholder = "Search items",
            emptyLabel = "No items",
            clearLabel = "Clear",
        )
    }
}

/** Caller removes the sheet after its animated hide. Localize all visible text in an app. */
@Composable
fun SheetExample(onDismissed: () -> Unit) {
    val sheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    val dismiss = rememberAsgardSheetDismiss(sheetState, onDismissed)
    AsgardSheet(
        onDismissRequest = onDismissed,
        sheetState = sheetState,
        header = { Text("Options") },
        footer = { TextButton(onClick = dismiss) { Text("Done") } },
    ) { Text("Sheet content") }
}

@Composable
fun ActionSheetExample(onDismissed: () -> Unit) {
    val sheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    val dismiss = rememberAsgardSheetDismiss(sheetState, onDismissed)
    AsgardActionSheet(
        onDismissRequest = onDismissed,
        title = "Confirm",
        sheetState = sheetState,
        actions = { TextButton(onClick = dismiss) { Text("Close") } },
    ) { Text("Review the action before continuing.") }
}

/** The scrollbar is an overlay; reserve its 48 dp target within the list host. */
@Composable
fun ScrollbarExample(rows: List<String>) {
    val listState = rememberLazyListState()
    Box(Modifier.fillMaxWidth().height(320.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(end = 48.dp), state = listState) {
            items(rows) { row -> Text(row) }
        }
        AsgardLazyScrollbar(
            state = rememberAsgardScrollbarState(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            label = "Scroll items",
            rangeLabel = { first, last -> "Items ${first + 1} to ${last + 1}" },
        )
    }
}

@Composable
fun ToolbarExample(icon: ImageVector, onShare: () -> Unit) {
    AsgardActionToolbar(
        actions = listOf(AsgardToolbarAction(id = "share", label = "Share", icon = icon, onClick = onShare)),
        leading = { Text("1 selected") },
    )
}

@Composable
fun AvatarExample(onOpenProfile: () -> Unit) {
    AsgardAvatar(initials = "TT", contentDescription = "Open profile", onClick = onOpenProfile)
}

@Composable
fun CalendarExample(entries: List<AsgardHeatmapEntry>, selected: LocalDate?, onSelected: (LocalDate) -> Unit) {
    AsgardCalendarHeatmap(
        entries = entries,
        start = LocalDate(2026, 11, 1),
        endInclusive = LocalDate(2026, 11, 30),
        selectedDate = selected,
        onDateSelected = onSelected,
        cellLabel = { date, value -> "$date: ${value ?: "No data"}" },
    )
}
