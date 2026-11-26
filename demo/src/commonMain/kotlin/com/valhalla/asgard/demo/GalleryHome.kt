package com.valhalla.asgard.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import asgard.demo.generated.resources.Res
import asgard.demo.generated.resources.vatnajokull
import com.valhalla.asgard.charts.AsgardBarStack
import com.valhalla.asgard.charts.AsgardStackedBarChart
import com.valhalla.asgard.components.AsgardProgressRing
import com.valhalla.asgard.components.AsgardSearchBar
import org.jetbrains.compose.resources.painterResource

private val wordBoundary = Regex("(?<=[a-z])(?=[A-Z])")
internal val ComponentEntry.galleryName: String
    get() = name.removePrefix("Asgard").replace(wordBoundary, " ")

private val sortedCatalog = asgardCatalog.sortedBy { it.galleryName.lowercase() }
private val categories = sortedCatalog.map { it.category }.distinct().sorted()

internal fun catalogMatches(query: String): List<ComponentEntry> {
    val term = query.trim()
    return sortedCatalog.filter { entry ->
        entry.name.contains(term, true) || entry.galleryName.contains(term, true) || entry.category.contains(term, true)
    }
}

@Composable
internal fun CatalogSearch(query: String, onChange: (String) -> Unit) {
    AsgardSearchBar(query, onChange, modifier = Modifier.fillMaxWidth(), placeholder = "Find component",
        leadingIcon = Icons.Rounded.Search, clearIcon = Icons.Rounded.Close, shape = MaterialTheme.shapes.large)
}

@Composable
internal fun GalleryHome(onSelect: (ComponentEntry) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val matches = remember(query, category) { catalogMatches(query).filter { category == null || it.category == category } }
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.widthIn(max = 1320.dp).fillMaxSize()) {
        val wide = maxWidth >= 800.dp && maxWidth / LocalDensity.current.fontScale >= 660.dp
        val pad = if (wide) 40.dp else 16.dp
        LazyVerticalGrid(
            columns = GridCells.Adaptive(244.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)),
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = pad, end = pad, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "intro", span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(top = if (wide) 36.dp else 18.dp, bottom = 8.dp)) {
                    Text("Component gallery", Modifier.semantics { heading() },
                        style = if (wide) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displaySmall)
                    Text("Explore Compose components and charts for Android and the web.",
                        style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }
            item(key = "featured", span = { GridItemSpan(maxLineSpan) }) {
                FeaturedExamples(wide, onSelect)
            }
            item(key = "index", span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(top = if (wide) 24.dp else 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                        Text("Component index", Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
                        Surface(color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer, shape = CircleShape) {
                            Text(matches.size.toString(), Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { CatalogSearch(query) { query = it } }
                            CategoryFilter(category) { category = it }
                        }
                    } else {
                        CatalogSearch(query) { query = it }
                        CategoryFilter(category) { category = it }
                    }
                }
            }
            items(matches, key = { it.id }) { entry ->
                OutlinedCard(onClick = { onSelect(entry) }, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large, border = BorderStroke(1.dp, colors.outlineVariant),
                    colors = CardDefaults.outlinedCardColors(containerColor = colors.surfaceContainerLowest)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = MaterialTheme.shapes.medium, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                            Icon(categoryIcon(entry.category), null, Modifier.padding(10.dp).size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(entry.galleryName, style = MaterialTheme.typography.titleMedium)
                            Text(entry.category, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
                    }
                }
            }
            if (matches.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No matching components", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { query = ""; category = null }) { Text("Clear filters") }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected ?: "All categories")
            Icon(Icons.Rounded.ArrowDropDown, null, Modifier.size(20.dp))
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            (listOf(null) + categories).forEach { category ->
                DropdownMenuItem(text = { Text(category ?: "All categories") },
                    trailingIcon = { if (category == selected) Icon(Icons.Rounded.Check, null) },
                    onClick = { onSelect(category); expanded = false })
            }
        }
    }
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Actions" -> Icons.Rounded.Bolt
    "Charts" -> Icons.Rounded.BarChart
    "Chips & badges" -> Icons.Rounded.Sell
    "Data display", "Display" -> Icons.Rounded.Widgets
    "Feedback" -> Icons.Rounded.Notifications
    "Input" -> Icons.Rounded.Tune
    "Lists" -> Icons.AutoMirrored.Rounded.List
    "Monetization" -> Icons.Rounded.Star
    "Navigation" -> Icons.Rounded.Explore
    "Onboarding" -> Icons.Rounded.Flag
    "Progress" -> Icons.Rounded.Timelapse
    "Settings" -> Icons.Rounded.Settings
    else -> Icons.Rounded.Dashboard
}

private val stackedBarsEntry = asgardCatalog.single { it.name == "AsgardStackedBarChart" }
private val progressRingEntry = asgardCatalog.single { it.name == "AsgardProgressRing" }
private val featuredBars = listOf(3f, 6f, 5f, 9f, 6f, 8f, 11f, 8f, 10f).mapIndexed { index, height ->
    AsgardBarStack(listOf(height, (index % 3 + 2).toFloat()))
}

@Composable
private fun FeaturedExamples(wide: Boolean, onSelect: (ComponentEntry) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val gap = Arrangement.spacedBy(12.dp)
    val titleStyle = if (wide) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
    @Composable fun Bars(modifier: Modifier) {
        SpecimenCard("Stacked bars", titleStyle, modifier, colors.secondaryContainer, colors.onSecondaryContainer, { onSelect(stackedBarsEntry) }) {
            AsgardStackedBarChart(featuredBars, listOf(colors.onSecondaryContainer, colors.secondary),
                Modifier.widthIn(max = 210.dp).fillMaxWidth().height(if (wide) 84.dp else 52.dp),
                gridColor = colors.onSecondaryContainer.copy(alpha = 0.15f))
        }
    }
    @Composable fun Ring(modifier: Modifier) {
        SpecimenCard("Progress ring", titleStyle, modifier, colors.tertiaryContainer, colors.onTertiaryContainer, { onSelect(progressRingEntry) }) {
            val ink = colors.onTertiaryContainer
            AsgardProgressRing(.76f, size = if (wide) 98.dp else 56.dp, strokeWidth = if (wide) 9.dp else 6.dp,
                progressColor = ink, trackColor = ink.copy(alpha = .16f)) {
                Text("76%", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    if (wide) {
        Row(Modifier.fillMaxWidth().heightIn(min = 216.dp).height(IntrinsicSize.Min), horizontalArrangement = gap) {
            GlacierCard(Modifier.weight(1.6f).fillMaxHeight(), onSelect)
            Bars(Modifier.weight(1f).fillMaxHeight())
            Ring(Modifier.weight(1f).fillMaxHeight())
        }
    } else {
        Column(verticalArrangement = gap) {
            GlacierCard(Modifier.fillMaxWidth(), onSelect)
            BoxWithConstraints {
                if (maxWidth / LocalDensity.current.fontScale >= 280.dp) {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = gap) {
                        Bars(Modifier.weight(1f).fillMaxHeight())
                        Ring(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(verticalArrangement = gap) { Bars(Modifier.fillMaxWidth()); Ring(Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
private fun GlacierCard(modifier: Modifier, onSelect: (ComponentEntry) -> Unit) {
    Surface(onClick = { onSelect(glassSurfaceEntry) }, modifier = modifier, shape = MaterialTheme.shapes.extraLarge,
        color = Color(0xFF19281F), contentColor = Color.White) {
        Box(Modifier.heightIn(min = 168.dp), contentAlignment = Alignment.BottomStart) {
            Image(painterResource(Res.drawable.vatnajokull), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Spacer(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .82f)))))
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Vatnajökull, Iceland", style = MaterialTheme.typography.labelMedium)
                FeatureLink("Glass surfaces", MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
private fun SpecimenCard(title: String, titleStyle: TextStyle, modifier: Modifier, background: Color, ink: Color, onClick: () -> Unit,
                         specimen: @Composable () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.extraLarge, color = background, contentColor = ink) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth().clearAndSetSemantics { }, contentAlignment = Alignment.Center) { specimen() }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(12.dp))
            FeatureLink(title, titleStyle)
        }
    }
}

@Composable
private fun FeatureLink(title: String, style: TextStyle) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, Modifier.weight(1f), style = style)
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
    }
}
