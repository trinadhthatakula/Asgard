package com.valhalla.asgard.compatibility

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.components.*
import com.valhalla.asgard.navigation.*

/** Compiled only against Maven Central 2.0.1; executes against the test app's updated library. */
@Composable
fun PublishedConsumer(page: Int) {
    val icon = remember { ImageVector.Builder("Fixture", 24.dp, 24.dp, 24f, 24f).build() }
    val items = remember { listOf(AsgardNavItem(icon, "Legacy home"), AsgardNavItem(icon, "Legacy search")) }
    val buttons = remember { listOf(ConnectedButtonGroupItem.Label("Legacy user"), ConnectedButtonGroupItem.Label("Legacy system")) }
    var selected by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("Legacy query") }
    var checked by remember { mutableStateOf(false) }
    var value by remember { mutableFloatStateOf(.5f) }
    MaterialTheme {
        Column(Modifier.width(320.dp)) {
            when (page) {
                0 -> ConnectedButtonGroup(buttons, selected, { selected = it })
                1 -> AsgardNavigationBar(items, selected, { selected = it })
                2 -> AsgardNavigationRail(items, selected, { selected = it })
                3 -> AsgardSearchBar(query, { query = it }, clearIcon = icon)
                4 -> AsgardSectionCard("Legacy section") { AsgardSettingRow("Legacy row", value = "Ready") }
                5 -> AsgardSettingToggleRow("Legacy toggle", checked, { checked = it })
                6 -> AsgardLabeledSlider("Legacy slider", value, { value = it })
            }
        }
    }
}
