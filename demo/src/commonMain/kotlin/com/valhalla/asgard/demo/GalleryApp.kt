package com.valhalla.asgard.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The demo shell: a theming toolbar, a component nav list, and a live detail pane. */
@Composable
fun GalleryApp() {
    var dark by remember { mutableStateOf(false) }
    var seed by remember { mutableStateOf(demoSeeds.first().second) }
    var selected by remember { mutableStateOf(asgardCatalog.first()) }
    // Group the sidebar by category (preserves first-seen category + entry order).
    val grouped = remember { asgardCatalog.groupBy { it.category } }

    DemoTheme(dark = dark, seed = seed) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Asgard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Box(Modifier.weight(1f))
                    demoSeeds.forEach { (label, color) ->
                        FilterChip(
                            selected = seed == color,
                            onClick = { seed = color },
                            label = { Text(label) },
                        )
                    }
                    Text(if (dark) "Dark" else "Light", style = MaterialTheme.typography.labelLarge)
                    Switch(checked = dark, onCheckedChange = { dark = it })
                }
                HorizontalDivider()
                Row(Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.width(240.dp).fillMaxHeight()) {
                        grouped.forEach { (category, entries) ->
                            item(key = category) {
                                Text(
                                    category,
                                    Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            items(entries, key = { it.name }) { entry ->
                                val isSel = entry == selected
                                Text(
                                    entry.name,
                                    Modifier.fillMaxWidth()
                                        .clickable { selected = entry }
                                        .background(
                                            if (isSel) MaterialTheme.colorScheme.secondaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                    VerticalDivider()
                    DetailPane(selected)
                }
            }
        }
    }
}

@Composable
private fun DetailPane(entry: ComponentEntry) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
    ) {
        Text(entry.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            entry.category,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            entry.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        // key() by name so each preview's own remember { } state is isolated and reset when
        // switching entries (previews hold state of different types — Boolean/Int/Float/String).
        Box(Modifier.padding(vertical = 32.dp)) { key(entry.name) { entry.content() } }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                entry.code,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
