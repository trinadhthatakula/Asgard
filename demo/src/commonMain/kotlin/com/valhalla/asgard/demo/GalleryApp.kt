package com.valhalla.asgard.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.valhalla.asgard.components.*
import kotlinx.coroutines.delay

/** Responsive shell; the same catalog drives sidebar, searchable navigation and URL state. */
@Composable
fun GalleryApp() {
    var dark by remember { mutableStateOf(false) }
    var rtl by remember { mutableStateOf(false) }
    var textScale by remember { mutableFloatStateOf(1f) }
    var seed by remember { mutableStateOf(demoSeeds.first().second) }
    var selected by remember { mutableStateOf(asgardCatalog.find { it.id == readGalleryEntry() } ?: asgardCatalog.first()) }
    var browsing by remember { mutableStateOf(false) }
    var appearance by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    DisposableEffect(Unit) {
        val stop = observeGalleryEntry { id -> selected = asgardCatalog.find { it.id == id } ?: asgardCatalog.first() }
        onDispose { stop() }
    }
    fun choose(entry: ComponentEntry) { selected = entry; browsing = false; selectGalleryEntry(entry.id) }
    DemoTheme(dark = dark, seed = seed) {
        GalleryEnvironment(rtl, textScale) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val compact = maxWidth < 840.dp
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Asgard", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        TextButton(onClick = { browsing = true }) { Text("Browse") }
                        TextButton(onClick = { appearance = true }) { Text("Theme") }
                    }
                    Text("${if (GalleryBuildInfo.released) "Release" else "Development source"} · ${GalleryBuildInfo.version} · ${GalleryBuildInfo.revision}",
                        Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (GalleryBuildInfo.released) "com.trinadhthatakula:asgard:${GalleryBuildInfo.version}" else "New APIs require this source build or a matching SNAPSHOT.",
                        Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                    HorizontalDivider()
                    Row(Modifier.weight(1f)) {
                        if (!compact) {
                            Column(Modifier.width(240.dp).fillMaxHeight().padding(8.dp)) {
                                AsgardSearchBar(query, { query = it }, placeholder = "Find component",
                                    leadingIcon = Icons.Rounded.Search, clearIcon = Icons.Rounded.Close)
                                LazyColumn(Modifier.weight(1f)) {
                                    asgardCatalog.filter { it.name.contains(query, true) || it.category.contains(query, true) }
                                        .groupBy { it.category }.forEach { (category, entries) ->
                                            item(key = "category:$category") {
                                                Text(category, Modifier.fillMaxWidth().padding(12.dp).semantics { heading() },
                                                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                            }
                                            items(entries, key = { it.id }) { entry ->
                                                Text(entry.name, Modifier.fillMaxWidth().selectable(
                                                    entry == selected, role = Role.Tab, onClick = { choose(entry) },
                                                ).background(if (entry == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                                    .padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                }
                            }
                            VerticalDivider()
                        }
                        key(selected.id) { DetailPane(selected, Modifier.weight(1f), compact) }
                    }
                }
                if (browsing) {
                    val sheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
                    val dismiss = rememberAsgardSheetDismiss(sheetState) { browsing = false }
                    AsgardPickerSheet(onDismissRequest = { browsing = false }, title = "Components", sheetState = sheetState) {
                        AsgardPicker(
                            asgardCatalog.filter { it.name.contains(query, true) || it.category.contains(query, true) }
                                .groupBy { it.category }.map { (category, entries) -> AsgardPickerSection(category, category, entries) },
                            query, { query = it }, itemKey = { it.id }, itemLabel = { it.name },
                            isSelected = { it == selected }, onSelect = { entry ->
                                selected = entry; selectGalleryEntry(entry.id); dismiss()
                            }, modifier = Modifier.fillMaxWidth().height(420.dp),
                            searchPlaceholder = "Find component", searchIcon = Icons.Rounded.Search, clearIcon = Icons.Rounded.Close,
                        )
                    }
                }
                if (appearance) {
                    AsgardSheet(onDismissRequest = { appearance = false }, header = { Text("Appearance", style = MaterialTheme.typography.headlineSmall) }) {
                        AsgardSettingToggleRow("Dark theme", dark, { dark = it }, modifier = Modifier.fillMaxWidth())
                        AsgardSettingToggleRow("Right-to-left layout", rtl, { rtl = it }, modifier = Modifier.fillMaxWidth())
                        AsgardLabeledSlider("Text scale", textScale, { textScale = it }, valueRange = 1f..2f, steps = 1, valueLabel = "${textScale}×")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            demoSeeds.forEach { (label, color) -> FilterChip(seed == color, { seed = color }, label = { Text(label) }) }
                        }
                    }
                }
            }
        }
    }
}

}

@Composable
private fun GalleryEnvironment(rtl: Boolean, textScale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, textScale),
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        content = content,
    )
}

@Composable
private fun DetailPane(entry: ComponentEntry, modifier: Modifier, compact: Boolean) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(entry.id) { focus.requestFocus() }
    Column(modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(if (compact) 16.dp else 24.dp)) {
        Text(entry.name, Modifier.focusRequester(focus).focusable().semantics { heading() },
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(entry.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(entry.description, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.fillMaxWidth().padding(vertical = 24.dp)) { entry.content() }
        var copyState by remember(entry.id) { mutableStateOf("Copy") }
        LaunchedEffect(copyState) { if (copyState != "Copy" && copyState != "Copying…") { delay(2000); copyState = "Copy" } }
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Minimal example", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                    TextButton(enabled = copyState != "Copying…", onClick = {
                        copyState = "Copying…"
                        copyToClipboard(entry.code) { success -> copyState = if (success) "Copied" else "Copy failed · retry" }
                    }) { Text(copyState) }
                }
                Text("Preview controls are independent of this example.", style = MaterialTheme.typography.labelSmall)
                SelectionContainer {
                    Box(Modifier.fillMaxWidth().heightIn(max = 280.dp).verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
                        Text(entry.code, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
