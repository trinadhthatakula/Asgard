package com.valhalla.asgard.skill.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.valhalla.asgard.components.ConnectedButtonGroup
import com.valhalla.asgard.components.ConnectedButtonGroupItem
import com.valhalla.asgard.components.ConnectedButtonGroupShapes
import com.valhalla.asgard.navigation.AsgardNavItem
import com.valhalla.asgard.navigation.AsgardNavigationAppearance
import com.valhalla.asgard.navigation.AsgardNavigationBar
import com.valhalla.asgard.navigation.AsgardNavigationConfiguration
import com.valhalla.asgard.navigation.AsgardNavigationInsets
import com.valhalla.asgard.navigation.AsgardNavigationLabels
import com.valhalla.asgard.navigation.AsgardNavigationPlacement

/** The host owns the set and may reject an empty selection. Localize labels in a real app. */
@Composable
fun ConnectedChoicesExample() {
    var selected by remember { mutableStateOf(setOf(0)) }
    ConnectedButtonGroup(
        items = listOf(ConnectedButtonGroupItem.Label("User"), ConnectedButtonGroupItem.Label("System")),
        selectedIndices = selected,
        onSelectionChanged = { next -> if (next.isNotEmpty()) selected = next },
        shapes = ConnectedButtonGroupShapes.FixedConnected,
        modifier = Modifier.fillMaxWidth(),
        contentDescription = "App sources",
    )
}

/** The bar floats because the host overlays it. Its measured height keeps the list tail reachable. */
@Composable
fun FloatingNavigationExample(
    destinations: List<AsgardNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    rows: List<String>,
) {
    var barHeightPx by remember { mutableIntStateOf(0) }
    val bottomClearance = with(LocalDensity.current) { barHeightPx.toDp() }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomClearance),
        ) { items(rows) { row -> Text(row) } }
        AsgardNavigationBar(
            items = destinations,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            configuration = AsgardNavigationConfiguration(
                placement = AsgardNavigationPlacement.Floating,
                appearance = AsgardNavigationAppearance.Segmented,
                labels = AsgardNavigationLabels.Selected,
                insets = AsgardNavigationInsets.NavigationBars,
            ),
            modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { barHeightPx = it.height },
        )
    }
}
