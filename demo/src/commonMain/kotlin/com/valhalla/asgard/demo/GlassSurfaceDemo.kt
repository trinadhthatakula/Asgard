package com.valhalla.asgard.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.North
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.South
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import asgard.demo.generated.resources.Res
import asgard.demo.generated.resources.vatnajokull
import com.valhalla.asgard.components.AsgardActionToolbar
import com.valhalla.asgard.components.AsgardSettingToggleRow
import com.valhalla.asgard.components.AsgardToolbarAction
import com.valhalla.asgard.glass.AsgardGlassSurface
import com.valhalla.asgard.glass.asgardGlassSource
import com.valhalla.asgard.glass.rememberAsgardGlassSource
import com.valhalla.asgard.navigation.AsgardNavItem
import com.valhalla.asgard.navigation.AsgardNavigationBar
import com.valhalla.asgard.navigation.AsgardNavigationConfiguration
import com.valhalla.asgard.navigation.AsgardNavigationPlacement
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

internal val glassSurfaceEntry = ComponentEntry(
    "Glass surfaces", "Display",
    "Scroll the glacier beneath the controls. Preview glass on the region label or try it on either bar.",
    """
    val source = rememberAsgardGlassSource(enabled = !reduceTransparency)
    Box {
        Feed(Modifier.fillMaxSize().asgardGlassSource(source))
        AsgardGlassSurface(source, enabled = glassLabel) { Text(region) }
        AsgardActionToolbar(actions, container = source.frosted(glassToolbar))
        AsgardNavigationBar(
            tabs, selected, onSelect, floatingConfiguration,
            container = source.frosted(glassNavigation),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    """.trimIndent(),
    glassComponent = "AsgardGlassSurface",
) { GlassSurfaceDemo() }

private val glacierRegions = listOf(
    AsgardNavItem(Icons.Rounded.North, "North"),
    AsgardNavItem(Icons.Rounded.MyLocation, "Center"),
    AsgardNavItem(Icons.Rounded.South, "South"),
)

@Composable
private fun GlassSurfaceDemo() {
    val labelGlass = LocalPreviewGlass.current
    var toolbarGlass by remember { mutableStateOf(false) }
    var navigationGlass by remember { mutableStateOf(false) }
    var reduceTransparency by remember { mutableStateOf(false) }
    var zoom by remember { mutableIntStateOf(1) }
    val source = rememberAsgardGlassSource(enabled = !reduceTransparency && (labelGlass || toolbarGlass || navigationGlass))
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val region by remember {
        derivedStateOf { (scroll.value.toFloat() / scroll.maxValue.coerceAtLeast(1) * glacierRegions.lastIndex).roundToInt() }
    }

    Column {
        Box(Modifier.fillMaxWidth().height(300.dp).clip(MaterialTheme.shapes.large)) {
            Column(Modifier.fillMaxSize().asgardGlassSource(source).verticalScroll(scroll)) {
                Image(
                    painterResource(Res.drawable.vatnajokull), "False-color Landsat image of Vatnajökull, Iceland",
                    Modifier.fillMaxWidth().height((490 + 130 * zoom).dp),
                    contentScale = ContentScale.Crop,
                )
            }
            AsgardGlassSurface(source, Modifier.align(Alignment.Center), enabled = labelGlass) {
                Text(glacierRegions[region].label, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            AsgardActionToolbar(
                actions = listOf(
                    AsgardToolbarAction("out", "Zoom out", Icons.Rounded.Remove, { zoom-- }, enabled = zoom > 0),
                    AsgardToolbarAction("in", "Zoom in", Icons.Rounded.Add, { zoom++ }, enabled = zoom < 3),
                ),
                container = source.frosted(toolbarGlass),
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp).widthIn(max = 300.dp),
                leading = { Text("Vatnajökull", Modifier.padding(horizontal = 10.dp), style = MaterialTheme.typography.titleMedium) },
            )
            AsgardNavigationBar(
                glacierRegions, region,
                onSelect = { index -> scope.launch { scroll.animateScrollTo(scroll.maxValue / glacierRegions.lastIndex * index) } },
                configuration = AsgardNavigationConfiguration(placement = AsgardNavigationPlacement.Floating),
                container = source.frosted(navigationGlass),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        Text("Landsat 7 · USGS EROS", Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
        AsgardSettingToggleRow("Frosted toolbar", toolbarGlass, { toolbarGlass = it })
        AsgardSettingToggleRow("Frosted navigation", navigationGlass, { navigationGlass = it })
        AsgardSettingToggleRow("Reduce transparency", reduceTransparency, { reduceTransparency = it })
    }
}
