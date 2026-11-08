package com.valhalla.asgard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/** Section IDs must be unique; an item may appear in several sections without key collisions. */
data class AsgardPickerSection<T>(val id: String, val title: String?, val items: List<T>)

internal fun pickerItemKey(section: String, item: String): String = "${section.length}:$section$item"

/**
 * Controlled, searchable selection list. Consumers filter [sections], persist recent values
 * and decide whether selection dismisses a sheet. Give this component bounded height.
 * Keys are namespaced by section; keys within a section must be unique.
 */
@Composable
fun <T> AsgardPicker(
    sections: List<AsgardPickerSection<T>>,
    query: String,
    onQueryChange: (String) -> Unit,
    itemKey: (T) -> String,
    itemLabel: (T) -> String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Search",
    emptyLabel: String = "No results",
    clearLabel: String = "Clear",
    searchIcon: ImageVector? = null,
    clearIcon: ImageVector? = null,
    enabled: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
    leading: (@Composable (T) -> Unit)? = null,
    rowContent: (@Composable (T) -> Unit)? = null,
) {
    require(sections.map { it.id }.distinct().size == sections.size) { "Picker section IDs must be unique" }
    sections.forEach { section ->
        require(section.items.map(itemKey).distinct().size == section.items.size) { "Picker item keys must be unique within a section" }
    }
    LaunchedEffect(query) { listState.scrollToItem(0) }
    Column(modifier) {
        AsgardSearchBar(
            query, onQueryChange, modifier = Modifier.semantics { contentDescription = searchPlaceholder },
            placeholder = searchPlaceholder, leadingIcon = searchIcon,
            clearIcon = clearIcon, clearContentDescription = clearLabel, enabled = enabled,
        )
        if (query.isNotEmpty() && clearIcon == null) {
            TextButton(onClick = { onQueryChange("") }, enabled = enabled) { Text(clearLabel) }
        }
        if (sections.all { it.items.isEmpty() }) {
            Text(emptyLabel, Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite })
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState) {
                sections.forEach { section ->
                    if (section.items.isNotEmpty()) {
                        section.title?.let { title ->
                            item(key = "header:${section.id}", contentType = "header") {
                                Text(title, Modifier.padding(vertical = 12.dp).semantics { heading() },
                                    style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        items(section.items, key = { "item:" + pickerItemKey(section.id, itemKey(it)) }) { value ->
                            Row(
                                Modifier.fillMaxWidth().selectable(
                                    selected = isSelected(value), enabled = enabled, role = Role.RadioButton,
                                    onClick = { onSelect(value) },
                                ).padding(vertical = 12.dp).semantics(mergeDescendants = true) { contentDescription = itemLabel(value) },
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                leading?.invoke(value)
                                Box(Modifier.weight(1f)) { rowContent?.invoke(value) ?: Text(itemLabel(value)) }
                                RadioButton(isSelected(value), onClick = null, enabled = enabled)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Sheet host for an existing picker composition; uses the same inset and state contract. */
@Composable
fun AsgardPickerSheet(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    content: @Composable ColumnScope.() -> Unit,
) {
    AsgardSheet(onDismissRequest, modifier, sheetState, scrollBody = false,
        header = { Text(title, style = MaterialTheme.typography.headlineSmall) }, content = content)
}
