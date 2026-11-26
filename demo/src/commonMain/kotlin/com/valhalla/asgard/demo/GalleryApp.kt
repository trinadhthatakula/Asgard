package com.valhalla.asgard.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import asgard.demo.generated.resources.Res
import asgard.demo.generated.resources.bifrost
import com.valhalla.asgard.components.*
import org.jetbrains.compose.resources.painterResource

private val entriesById = asgardCatalog.associateBy { it.id }

@Composable
fun GalleryApp(initialEntry: String? = null) {
    val platform = rememberGalleryPlatform()
    val systemDark = isSystemInDarkTheme()
    val systemRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var darkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var rtlOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val dark = darkOverride ?: systemDark
    val rtl = rtlOverride ?: systemRtl
    var textScale by rememberSaveable { mutableFloatStateOf(1f) }
    var paletteName by rememberSaveable { mutableStateOf(GalleryPalette.Asgard.name) }
    val palette = GalleryPalette.valueOf(paletteName)
    var selected by remember(platform) { mutableStateOf(entriesById[initialEntry ?: platform.readEntry()]) }
    var browsing by rememberSaveable { mutableStateOf(false) }
    var appearance by rememberSaveable { mutableStateOf(false) }
    val pageState = rememberSaveableStateHolder()
    fun navigate(entry: ComponentEntry?) {
        selected = entry
        platform.selectEntry(entry?.id)
    }
    DisposableEffect(platform) {
        val stop = platform.observeEntry { id -> selected = entriesById[id] }
        onDispose { stop() }
    }
    LaunchedEffect(initialEntry, platform) { initialEntry?.let { navigate(entriesById[it]) } }
    SideEffect { platform.onThemeChanged(dark) }

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * textScale),
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        DemoTheme(dark, palette) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    GalleryBar(onHome = { navigate(null) }, onBrowse = { browsing = true }, onTheme = { appearance = true })
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        val entry = selected
                        if (entry == null) {
                            pageState.SaveableStateProvider("gallery") { GalleryHome(onSelect = ::navigate) }
                        } else {
                            val wide = maxWidth >= 840.dp && maxWidth / LocalDensity.current.fontScale >= 700.dp
                            Row(Modifier.widthIn(max = 1440.dp).fillMaxSize()) {
                                if (wide) {
                                    GallerySidebar(entry, ::navigate)
                                    VerticalDivider()
                                }
                                pageState.SaveableStateProvider(entry.id) {
                                    DetailPane(entry, platform, compact = !wide, onHome = { navigate(null) })
                                }
                            }
                        }
                    }
                }
            }
            if (browsing) {
                val sheet = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
                val dismiss = rememberAsgardSheetDismiss(sheet) { browsing = false }
                var query by rememberSaveable { mutableStateOf("") }
                val sections = remember(query) {
                    catalogMatches(query).groupBy { it.category }.entries.sortedBy { it.key }.map { (category, entries) ->
                        AsgardPickerSection(category, category, entries)
                    }
                }
                AsgardPickerSheet(onDismissRequest = { browsing = false }, title = "Components", sheetState = sheet) {
                    AsgardPicker(sections, query, { query = it }, itemKey = { it.id }, itemLabel = { it.name },
                        isSelected = { it == selected }, onSelect = { navigate(it); dismiss() },
                        modifier = Modifier.fillMaxWidth().height(440.dp), searchPlaceholder = "Find component",
                        searchIcon = Icons.Rounded.Search, clearIcon = Icons.Rounded.Close)
                }
            }
            if (appearance) {
                AsgardSheet(onDismissRequest = { appearance = false }, header = {
                    Text("Appearance", style = MaterialTheme.typography.headlineMedium)
                }) {
                    Text("Accent palette", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GalleryPalette.entries.forEach { option ->
                            FilterChip(selected = palette == option, onClick = { paletteName = option.name },
                                leadingIcon = { Spacer(Modifier.size(16.dp).background(option.accent, CircleShape)) },
                                label = { Text(option.label) })
                        }
                    }
                    AsgardSettingToggleRow("Dark theme", dark, { darkOverride = it }, modifier = Modifier.fillMaxWidth())
                    AsgardSettingToggleRow("Right-to-left layout", rtl, { rtlOverride = it }, modifier = Modifier.fillMaxWidth())
                    AsgardLabeledSlider("Text scale", textScale, { textScale = it }, valueRange = 1f..2f, steps = 1, valueLabel = "${textScale}×")
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Text("Gallery ${GalleryBuildInfo.galleryVersionName} · ${GalleryBuildInfo.revision}",
                        style = MaterialTheme.typography.labelMedium)
                    Text(if (GalleryBuildInfo.released) "Released library · com.trinadhthatakula:asgard:${GalleryBuildInfo.libraryVersion}"
                        else "Library ${GalleryBuildInfo.libraryVersion} development source. New APIs require this build or a matching SNAPSHOT.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun GalleryBar(onHome: () -> Unit, onBrowse: () -> Unit, onTheme: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val small = maxWidth / LocalDensity.current.fontScale < 360.dp
        Row(Modifier.widthIn(max = 1320.dp).fillMaxWidth().padding(horizontal = if (maxWidth >= 840.dp) 36.dp else 12.dp,
            vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clickable(role = Role.Button, onClickLabel = "Open gallery", onClick = onHome)
                    .heightIn(min = 48.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = colors.primaryContainer, contentColor = colors.onPrimaryContainer, shape = MaterialTheme.shapes.small) {
                        Icon(painterResource(Res.drawable.bifrost), null, Modifier.padding(5.dp).size(23.dp))
                    }
                    Text("Asgard", style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            BarAction("Browse", Icons.Rounded.Search, small, onBrowse)
            BarAction("Theme", Icons.Rounded.Tune, small, onTheme)
        }
    }
    HorizontalDivider(color = colors.outlineVariant)
}

@Composable
private fun BarAction(label: String, icon: ImageVector, compact: Boolean, onClick: () -> Unit) {
    if (compact) {
        IconButton(onClick) { Icon(icon, label) }
    } else {
        OutlinedButton(onClick, contentPadding = PaddingValues(horizontal = 12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Icon(icon, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label)
        }
    }
}

@Composable
private fun GallerySidebar(selected: ComponentEntry, onSelect: (ComponentEntry) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf("") }
    val groups = remember(query) { catalogMatches(query).groupBy { it.category }.entries.sortedBy { it.key } }
    Column(Modifier.width(272.dp).fillMaxHeight().padding(16.dp)) {
        CatalogSearch(query) { query = it }
        LazyColumn {
            groups.forEach { (category, entries) ->
                item(key = category) {
                    Text(category, Modifier.padding(start = 12.dp, top = 20.dp, bottom = 6.dp).semantics { heading() },
                        style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                items(entries, key = { it.id }) { entry ->
                    val active = selected == entry
                    Surface(shape = MaterialTheme.shapes.medium,
                        color = if (active) colors.secondaryContainer else colors.surface,
                        contentColor = if (active) colors.onSecondaryContainer else colors.onSurface) {
                        Text(entry.galleryName, Modifier.fillMaxWidth().selectable(active, role = Role.Tab, onClick = { onSelect(entry) })
                            .padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (groups.isEmpty()) item { Text("No matching components", Modifier.padding(12.dp)) }
        }
    }
}
