package com.valhalla.asgard.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.components.AsgardActionItem
import com.valhalla.asgard.components.AsgardHeader
import com.valhalla.asgard.components.ConnectedButtonGroup
import com.valhalla.asgard.components.ConnectedButtonGroupItem
import com.valhalla.asgard.navigation.AsgardNavItem
import com.valhalla.asgard.navigation.AsgardNavigationBar
import com.valhalla.asgard.navigation.AsgardNavigationRail
import com.valhalla.asgard.components.AsgardAnimatedNumeral
import com.valhalla.asgard.components.AsgardBadge
import com.valhalla.asgard.components.AsgardBanner
import com.valhalla.asgard.components.AsgardDialogScaffold
import com.valhalla.asgard.components.AsgardEmptyState
import com.valhalla.asgard.components.AsgardFeatureRow
import com.valhalla.asgard.components.AsgardLabeledSlider
import com.valhalla.asgard.components.AsgardListRow
import com.valhalla.asgard.components.AsgardLoadingState
import com.valhalla.asgard.components.AsgardLockedOverlay
import com.valhalla.asgard.components.AsgardOnboardingScaffold
import com.valhalla.asgard.components.AsgardProBadge
import com.valhalla.asgard.components.AsgardProgressRing
import com.valhalla.asgard.components.AsgardSearchBar
import com.valhalla.asgard.components.AsgardSectionCard
import com.valhalla.asgard.components.AsgardSettingRow
import com.valhalla.asgard.components.AsgardSettingToggleRow
import com.valhalla.asgard.components.AsgardShimmer
import com.valhalla.asgard.components.AsgardStatCard
import com.valhalla.asgard.components.AsgardStatTile
import com.valhalla.asgard.components.AsgardStepperRow
import com.valhalla.asgard.components.AsgardTonalIconButton
import com.valhalla.asgard.components.AsgardUpgradeCard
import com.valhalla.asgard.components.StatusChip
import com.valhalla.asgard.charts.AsgardBarStack
import com.valhalla.asgard.charts.AsgardChartLegend
import com.valhalla.asgard.charts.AsgardChartPoint
import com.valhalla.asgard.charts.AsgardLegendEntry
import com.valhalla.asgard.charts.AsgardLegendSwatch
import com.valhalla.asgard.charts.AsgardLineChart
import com.valhalla.asgard.charts.AsgardLineSeries
import com.valhalla.asgard.charts.AsgardLineSmoothing
import com.valhalla.asgard.charts.AsgardPulseRing
import com.valhalla.asgard.charts.AsgardStackedBarChart
import com.valhalla.asgard.charts.AsgardTimelineBar
import com.valhalla.asgard.charts.AsgardTimelineSegment
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val demoNavItems = listOf(
    AsgardNavItem(Icons.Rounded.Star, "Home"),
    AsgardNavItem(Icons.Rounded.Search, "Search"),
    AsgardNavItem(Icons.Rounded.Settings, "Settings"),
)

/** Single source of truth for the gallery — one entry per public Asgard component. */
val asgardCatalog: List<ComponentEntry> = listOf(
    ComponentEntry(
        "AsgardHeader", "Structure",
        "The signature screen header: a bold primary-tinted title, optional leading icon, trailing actions.",
        "AsgardHeader(title = \"Clusters\", icon = Icons.Rounded.Star)",
    ) { AsgardHeader(title = "Clusters", icon = Icons.Rounded.Star, modifier = Modifier.fillMaxWidth()) },

    ComponentEntry(
        "AsgardNavigationBar", "Navigation",
        "An expressive bottom navigation bar — the selected item expands to reveal its label.",
        "AsgardNavigationBar(items, selectedIndex, onSelect = { … })",
    ) {
        var sel by remember { mutableStateOf(0) }
        AsgardNavigationBar(
            items = demoNavItems,
            selectedIndex = sel,
            onSelect = { sel = it },
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardNavigationRail", "Navigation",
        "An expressive vertical navigation rail (the side-nav counterpart to the bar).",
        "AsgardNavigationRail(items, selectedIndex, onSelect = { … })",
    ) {
        var sel by remember { mutableStateOf(0) }
        Box(Modifier.height(300.dp)) {
            AsgardNavigationRail(items = demoNavItems, selectedIndex = sel, onSelect = { sel = it })
        }
    },

    ComponentEntry(
        "ConnectedButtonGroup", "Input",
        "A single-select connected toggle-button group; crash-safe at any system font scale.",
        "ConnectedButtonGroup(items = listOf(Label(\"Day\"), Label(\"Week\")), selectedIndex, onItemSelected)",
    ) {
        var sel by remember { mutableStateOf(0) }
        ConnectedButtonGroup(
            items = listOf(
                ConnectedButtonGroupItem.Label("Day"),
                ConnectedButtonGroupItem.Label("Week"),
                ConnectedButtonGroupItem.Label("Month"),
            ),
            selectedIndex = sel,
            onItemSelected = { sel = it },
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "ConnectedButtonGroup · icons", "Input",
        "The same group using the Icon (icon-only) and IconWithLabel item variants instead of plain labels.",
        "ConnectedButtonGroup(items = listOf(\n    ConnectedButtonGroupItem.Icon(Icons.Rounded.Star, \"Home\"),\n    ConnectedButtonGroupItem.IconWithLabel(Icons.Rounded.Bolt, \"Recent\", \"Recent\"),\n), selectedIndex, onItemSelected)",
    ) {
        var iconSel by remember { mutableStateOf(0) }
        var labelSel by remember { mutableStateOf(1) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ConnectedButtonGroup(
                items = listOf(
                    ConnectedButtonGroupItem.Icon(Icons.Rounded.Star, "Home"),
                    ConnectedButtonGroupItem.Icon(Icons.Rounded.Search, "Search"),
                    ConnectedButtonGroupItem.Icon(Icons.Rounded.Settings, "Settings"),
                ),
                selectedIndex = iconSel,
                onItemSelected = { iconSel = it },
                modifier = Modifier.fillMaxWidth(),
            )
            ConnectedButtonGroup(
                items = listOf(
                    ConnectedButtonGroupItem.IconWithLabel(Icons.Rounded.Star, "Starred", "Starred"),
                    ConnectedButtonGroupItem.IconWithLabel(Icons.Rounded.Bolt, "Recent", "Recent"),
                ),
                selectedIndex = labelSel,
                onItemSelected = { labelSel = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    },

    ComponentEntry(
        "StatusChip", "Chips & badges",
        "A compact, pill-shaped status label that inherits the theme.",
        "StatusChip(text = \"Frozen\")",
    ) { StatusChip(text = "Frozen") },

    ComponentEntry(
        "AsgardBadge", "Chips & badges",
        "A neutral label pill with an optional icon; filled or outlined.",
        "AsgardBadge(text = \"NEW\", icon = Icons.Rounded.Star)",
    ) { AsgardBadge(text = "NEW", icon = Icons.Rounded.Star) },

    ComponentEntry(
        "AsgardProBadge", "Monetization",
        "A PRO/premium marker pill preset over AsgardBadge.",
        "AsgardProBadge(icon = Icons.Rounded.Lock)",
    ) { AsgardProBadge(icon = Icons.Rounded.Lock) },

    ComponentEntry(
        "AsgardStatTile", "Data display",
        "Compact metric tile: a label over an emphasized value, optional icon.",
        "AsgardStatTile(label = \"Uptime\", value = \"12h 30m\", icon = Icons.Rounded.Bolt)",
    ) { AsgardStatTile(label = "Uptime", value = "12h 30m", icon = Icons.Rounded.Bolt) },

    ComponentEntry(
        "AsgardStatTile · status dot", "Data display",
        "A stat tile with a colored status dot beside the label — for live health/state read-outs.",
        "AsgardStatTile(label = \"Server\", value = \"Online\",\n    statusDotColor = Color(0xFF22C55E))",
    ) {
        AsgardStatTile(
            label = "Server",
            value = "Online",
            statusDotColor = Color(0xFF22C55E),
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardStatTile · icon badge", "Data display",
        "A stat tile whose icon sits inside a filled circular badge instead of a bare glyph.",
        "AsgardStatTile(label = \"Downloads\", value = \"1,204\",\n    icon = Icons.Rounded.Bolt, iconTint = Color.White,\n    iconContainerColor = Color(0xFF6750A4))",
    ) {
        AsgardStatTile(
            label = "Downloads",
            value = "1,204",
            icon = Icons.Rounded.Bolt,
            iconTint = Color.White,
            iconContainerColor = Color(0xFF6750A4),
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardStatTile · animated", "Data display",
        "A value-first tile that counts up on change, with an inline secondary label and a border; tap to add.",
        "var n by remember { mutableStateOf(0) }\nAsgardStatTile(label = \"Steps today\", value = n.toString(),\n    secondaryValue = \"steps\", animateValue = true, valueFirst = true,\n    onClick = { n += 250 },\n    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))",
    ) {
        var n by remember { mutableStateOf(0) }
        AsgardStatTile(
            label = "Steps today",
            value = n.toString(),
            secondaryValue = "steps",
            animateValue = true,
            valueFirst = true,
            onClick = { n += 250 },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardStatCard", "Data display",
        "Vertical metric card: an icon + label header over a large value with an optional unit suffix.",
        "AsgardStatCard(label = \"Voltage\", value = \"5.00\", unit = \"V\",\n    icon = Icons.Rounded.Bolt)",
    ) {
        AsgardStatCard(
            label = "Voltage",
            value = "5.00",
            unit = "V",
            icon = Icons.Rounded.Bolt,
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardStatCard · bento", "Data display",
        "A compact bento cell: the icon sits inline with the label, a tight unit suffix, clickable with a border.",
        "AsgardStatCard(label = \"Battery\", value = \"95\", unit = \"%\",\n    unitSeparator = \"\", icon = Icons.Rounded.Bolt,\n    iconInlineWithLabel = true, onClick = {},\n    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))",
    ) {
        AsgardStatCard(
            label = "Battery",
            value = "95",
            unit = "%",
            unitSeparator = "",
            icon = Icons.Rounded.Bolt,
            iconInlineWithLabel = true,
            onClick = {},
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardBanner", "Feedback",
        "A tonal callout card: icon + title + description + optional action.",
        "AsgardBanner(title = \"No connection\", description = \"Check your network.\",\n    icon = Icons.Rounded.Warning) { TextButton(onClick = {}) { Text(\"Retry\") } }",
    ) {
        AsgardBanner(
            title = "No connection",
            description = "Check your network and try again.",
            icon = Icons.Rounded.Warning,
            modifier = Modifier.fillMaxWidth(),
        ) { TextButton(onClick = {}) { Text("Retry") } }
    },

    ComponentEntry(
        "AsgardBanner · gradient", "Feedback",
        "A banner with a horizontal-gradient background and a description clamped to two lines.",
        "AsgardBanner(title = \"You're on Pro\", description = \"…\",\n    descriptionMaxLines = 2, icon = Icons.Rounded.Star,\n    containerBrush = Brush.horizontalGradient(\n        listOf(Color(0xFF6750A4), Color(0xFF9A82DB))))",
    ) {
        AsgardBanner(
            title = "You're on Pro",
            description = "Enjoy unlimited exports, priority sync, and every premium theme — " +
                "all included with your plan for as long as your subscription stays active.",
            descriptionMaxLines = 2,
            icon = Icons.Rounded.Star,
            containerBrush = Brush.horizontalGradient(
                listOf(Color(0xFF6750A4), Color(0xFF9A82DB)),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardListRow", "Lists",
        "A generic list item: leading icon, title/subtitle, optional trailing slot.",
        "AsgardListRow(title = \"Wi-Fi\", subtitle = \"Home network\",\n    icon = Icons.Rounded.Wifi, onClick = {})",
    ) {
        AsgardListRow(
            title = "Wi-Fi",
            subtitle = "Home network",
            icon = Icons.Rounded.Wifi,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardSectionCard", "Settings",
        "A titled tonal container that groups related rows.",
        "AsgardSectionCard(title = \"General\") { /* rows */ }",
    ) {
        AsgardSectionCard(title = "General", modifier = Modifier.fillMaxWidth()) {
            AsgardSettingRow(title = "Language", value = "English", icon = Icons.Rounded.Settings)
            AsgardSettingRow(title = "Notifications", value = "On", icon = Icons.Rounded.Notifications)
        }
    },

    ComponentEntry(
        "AsgardSettingRow", "Settings",
        "A labeled row with an optional trailing value or custom slot.",
        "AsgardSettingRow(title = \"Version\", value = \"1.90.3\")",
    ) {
        AsgardSettingRow(title = "Version", value = "1.90.3", modifier = Modifier.fillMaxWidth())
    },

    ComponentEntry(
        "AsgardSettingToggleRow", "Settings",
        "A settings row with a trailing switch (tapping the row toggles it).",
        "var on by remember { mutableStateOf(true) }\nAsgardSettingToggleRow(\"Wi-Fi only\", on) { on = it }",
    ) {
        var on by remember { mutableStateOf(true) }
        AsgardSettingToggleRow(
            title = "Wi-Fi only downloads",
            checked = on,
            onCheckedChange = { on = it },
            icon = Icons.Rounded.Wifi,
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardUpgradeCard", "Monetization",
        "A promotional card with a title, description, and CTA button.",
        "AsgardUpgradeCard(title = \"Go Pro\", description = \"…\", cta = \"Upgrade\") {}",
    ) {
        AsgardUpgradeCard(
            title = "Go Pro",
            description = "Unlock every feature and remove ads.",
            cta = "Upgrade",
            onUpgrade = {},
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardLockedOverlay", "Monetization",
        "Gates content behind a blurred overlay while locked.",
        "AsgardLockedOverlay(locked, overlay = { AsgardProBadge() }) { /* content */ }",
    ) {
        var locked by remember { mutableStateOf(true) }
        AsgardLockedOverlay(
            locked = locked,
            overlay = { TextButton(onClick = { locked = false }) { Text("Unlock") } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsgardStatTile(label = "Revenue", value = "$4,210", modifier = Modifier.fillMaxWidth())
        }
    },

    ComponentEntry(
        "AsgardProgressRing", "Progress",
        "A circular progress/gauge ring with a centered content slot.",
        "AsgardProgressRing(progress = 0.66f) { Text(\"66%\") }",
    ) {
        AsgardProgressRing(progress = 0.66f, size = 140.dp) {
            Text("66%", style = MaterialTheme.typography.titleLarge)
        }
    },

    ComponentEntry(
        "AsgardEmptyState", "Feedback",
        "A centered placeholder for empty screens.",
        "AsgardEmptyState(text = \"No items yet\", icon = Icons.Rounded.Inbox)",
    ) { AsgardEmptyState(text = "No items yet", icon = Icons.Rounded.Inbox) },

    ComponentEntry(
        "AsgardLoadingState", "Feedback",
        "A centered spinner with an optional caption.",
        "AsgardLoadingState(text = \"Loading…\")",
    ) { AsgardLoadingState(text = "Loading…") },

    ComponentEntry(
        "AsgardFeatureRow", "Onboarding",
        "An icon badge beside a title and description — for benefit lists.",
        "AsgardFeatureRow(icon = Icons.Rounded.Bolt, title = \"Fast\", description = \"…\")",
    ) {
        AsgardFeatureRow(
            icon = Icons.Rounded.Bolt,
            title = "Blazing fast",
            description = "Optimized for instant results.",
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardOnboardingScaffold", "Onboarding",
        "A page scaffold: title, subtitle, body, and a bottom actions row.",
        "AsgardOnboardingScaffold(title = \"Welcome\", subtitle = \"…\") { /* body */ }",
    ) {
        AsgardOnboardingScaffold(
            title = "Welcome",
            subtitle = "Everything you need in one place.",
            actions = { TextButton(onClick = {}) { Text("Next") } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsgardFeatureRow(icon = Icons.Rounded.Favorite, title = "Made for you")
        }
    },

    ComponentEntry(
        "AsgardAnimatedNumeral", "Data display",
        "Numeric text that slides on change (odometer style).",
        "var n by remember { mutableStateOf(0) }\nAsgardAnimatedNumeral(n.toString())",
    ) {
        var n by remember { mutableStateOf(42) }
        Box {
            TextButton(onClick = { n += 7 }) {
                AsgardAnimatedNumeral(n.toString())
            }
        }
    },

    ComponentEntry(
        "AsgardDialogScaffold", "Feedback",
        "A thin confirm/dismiss dialog wrapper over M3 AlertDialog.",
        "AsgardDialogScaffold(onDismiss, title = \"Delete?\", confirmText = \"Delete\", onConfirm)",
    ) {
        var show by remember { mutableStateOf(false) }
        TextButton(onClick = { show = true }) { Text("Show dialog") }
        if (show) {
            AsgardDialogScaffold(
                onDismissRequest = { show = false },
                title = "Delete item?",
                text = "This action can't be undone.",
                confirmText = "Delete",
                onConfirm = { show = false },
                dismissText = "Cancel",
                icon = Icons.Rounded.Warning,
            )
        }
    },

    ComponentEntry(
        "AsgardStepperRow", "Settings",
        "A labeled numeric stepper with min/max bounds.",
        "AsgardStepperRow(\"Servings\", value, Icons.Rounded.Remove, Icons.Rounded.Add, …)",
    ) {
        var n by remember { mutableStateOf(2) }
        AsgardStepperRow(
            label = "Servings",
            value = n.toString(),
            decrementIcon = Icons.Rounded.Remove,
            incrementIcon = Icons.Rounded.Add,
            onDecrement = { n-- },
            onIncrement = { n++ },
            canDecrement = n > 1,
            canIncrement = n < 12,
            modifier = Modifier.fillMaxWidth(),
        )
    },

    ComponentEntry(
        "AsgardSearchBar", "Input",
        "A single-line search field with an auto clear button.",
        "AsgardSearchBar(query, onQueryChange, leadingIcon = Icons.Rounded.Search)",
    ) {
        var q by remember { mutableStateOf("") }
        AsgardSearchBar(
            query = q,
            onQueryChange = { q = it },
            leadingIcon = Icons.Rounded.Search,
            clearIcon = Icons.Rounded.Close,
            placeholder = "Search apps",
        )
    },

    ComponentEntry(
        "AsgardLabeledSlider", "Input",
        "A labeled slider with a value read-out.",
        "AsgardLabeledSlider(\"Volume\", value, onValueChange)",
    ) {
        var v by remember { mutableStateOf(0.4f) }
        AsgardLabeledSlider(
            label = "Volume",
            value = v,
            onValueChange = { v = it },
            valueLabel = "${(v * 100).toInt()}%",
        )
    },

    ComponentEntry(
        "AsgardShimmer", "Feedback",
        "A skeleton/shimmer placeholder shown while content loads.",
        "AsgardShimmer(Modifier.fillMaxWidth().height(24.dp))",
    ) {
        AsgardShimmer(Modifier.fillMaxWidth().height(24.dp))
    },

    ComponentEntry(
        "AsgardTonalIconButton", "Input",
        "A thin tonal icon button wrapper.",
        "AsgardTonalIconButton(Icons.Rounded.Favorite, \"Like\", onClick)",
    ) { AsgardTonalIconButton(icon = Icons.Rounded.Favorite, contentDescription = "Like", onClick = {}) },

    ComponentEntry(
        "AsgardActionItem", "Actions",
        "A vertical tappable action tile: icon chip over a caption.",
        "AsgardActionItem(icon = Icons.Rounded.Info, label = \"Info\", onClick = {})",
    ) { AsgardActionItem(icon = Icons.Rounded.Info, label = "Info", onClick = {}) },

    ComponentEntry(
        "AsgardActionItem · one line", "Actions",
        "An action tile capped at a single label line (labelMaxLines = 1) so long captions truncate cleanly.",
        "AsgardActionItem(icon = Icons.Rounded.Notifications,\n    label = \"Notification settings\", labelMaxLines = 1, onClick = {})",
    ) {
        AsgardActionItem(
            icon = Icons.Rounded.Notifications,
            label = "Notification settings",
            labelMaxLines = 1,
            onClick = {},
        )
    },

    // ─── Charts (interactive — drag the knobs to see live changes) ───────────────

    ComponentEntry(
        "AsgardLineChart", "Charts",
        "A multi-series smoothed line + area chart. Adjust the knobs below to see it react live.",
        "AsgardLineChart(\n    series = listOf(AsgardLineSeries(points, color = colorScheme.primary)),\n    yValueFormatter = { it.roundToInt().toString() },\n    modifier = Modifier.fillMaxWidth().height(200.dp),\n)",
    ) {
        var count by remember { mutableStateOf(9f) }
        var amp by remember { mutableStateOf(0.6f) }
        var smooth by remember { mutableStateOf(true) }
        var fill by remember { mutableStateOf(true) }
        var twoSeries by remember { mutableStateOf(true) }
        val n = count.roundToInt()
        val mode = if (smooth) AsgardLineSmoothing.Cubic else AsgardLineSmoothing.None
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        val series = buildList {
            add(
                AsgardLineSeries(
                    points = (0 until n).map { i -> AsgardChartPoint(i.toFloat(), 0.5f + amp * sin(i * 0.8f), "D${i + 1}") },
                    color = primary, smoothing = mode, fillArea = fill,
                ),
            )
            if (twoSeries) add(
                AsgardLineSeries(
                    points = (0 until n).map { i -> AsgardChartPoint(i.toFloat(), 0.5f + amp * 0.6f * cos(i * 0.7f)) },
                    color = secondary, smoothing = mode, fillArea = fill,
                ),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsgardLineChart(
                series = series,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                yValueFormatter = { (it * 100).roundToInt().toString() },
            )
            AsgardChartLegend(
                entries = buildList {
                    add(AsgardLegendEntry("Series A", primary))
                    if (twoSeries) add(AsgardLegendEntry("Series B", secondary))
                },
            )
            AsgardLabeledSlider("Points", count, { count = it }, valueRange = 3f..16f, steps = 12, valueLabel = "$n")
            AsgardLabeledSlider("Amplitude", amp, { amp = it }, valueRange = 0.1f..0.9f, valueLabel = "${(amp * 100).roundToInt()}%")
            ControlToggle("Smooth (cubic)", smooth) { smooth = it }
            ControlToggle("Area fill", fill) { fill = it }
            ControlToggle("Second series", twoSeries) { twoSeries = it }
        }
    },

    ComponentEntry(
        "AsgardStackedBarChart", "Charts",
        "N-segment stacked bars with a shared scale. Change the bar count and dim the last (partial) bar.",
        "AsgardStackedBarChart(\n    bars = days.map { AsgardBarStack(listOf(it.active, it.idle), dimmed = it.partial) },\n    segmentColors = listOf(colorScheme.primary, colorScheme.tertiary),\n)",
    ) {
        var barCount by remember { mutableStateOf(7f) }
        var dimLast by remember { mutableStateOf(true) }
        val n = barCount.roundToInt()
        val c1 = MaterialTheme.colorScheme.primary
        val c2 = MaterialTheme.colorScheme.tertiary
        val bars = (0 until n).map { i ->
            AsgardBarStack(
                values = listOf(1f + (i % 4), 1f + ((i + 2) % 3)),
                dimmed = dimLast && i == n - 1,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsgardStackedBarChart(
                bars = bars,
                segmentColors = listOf(c1, c2),
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            AsgardChartLegend(entries = listOf(AsgardLegendEntry("Active", c1), AsgardLegendEntry("Idle", c2)))
            AsgardLabeledSlider("Bars", barCount, { barCount = it }, valueRange = 3f..12f, steps = 8, valueLabel = "$n")
            ControlToggle("Dim last bar (partial)", dimLast) { dimLast = it }
        }
    },

    ComponentEntry(
        "AsgardTimelineBar", "Charts",
        "Colored interval spans across a time window (screen states, sessions, schedules). Drag to move the split.",
        "AsgardTimelineBar(segments, windowStartMillis = 0, windowEndMillis = 100)",
    ) {
        var split by remember { mutableStateOf(50f) }
        val on = MaterialTheme.colorScheme.primary
        val doze = MaterialTheme.colorScheme.tertiary
        val s = split.roundToInt().toLong()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsgardTimelineBar(
                segments = listOf(
                    AsgardTimelineSegment(0, s, on),
                    AsgardTimelineSegment(s + 5, 100, doze),
                ),
                windowStartMillis = 0, windowEndMillis = 100,
                modifier = Modifier.fillMaxWidth(),
                height = 14.dp,
            )
            AsgardChartLegend(
                entries = listOf(AsgardLegendEntry("On", on), AsgardLegendEntry("Doze", doze)),
                swatch = AsgardLegendSwatch.Dot,
            )
            AsgardLabeledSlider("Split", split, { split = it }, valueRange = 10f..80f, valueLabel = "$s")
        }
    },

    ComponentEntry(
        "AsgardChartLegend", "Charts",
        "The wrapping swatch + label key shared by every Asgard chart. Toggle square/dot swatches.",
        "AsgardChartLegend(entries = listOf(AsgardLegendEntry(\"Petrol\", primary), AsgardLegendEntry(\"Diesel\", secondary)))",
    ) {
        var dot by remember { mutableStateOf(false) }
        val cs = MaterialTheme.colorScheme
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsgardChartLegend(
                entries = listOf(
                    AsgardLegendEntry("Petrol", cs.primary),
                    AsgardLegendEntry("Diesel", cs.secondary),
                    AsgardLegendEntry("CNG", cs.tertiary),
                ),
                swatch = if (dot) AsgardLegendSwatch.Dot else AsgardLegendSwatch.Square,
            )
            ControlToggle("Dot swatches", dot) { dot = it }
        }
    },

    ComponentEntry(
        "AsgardPulseRing", "Charts",
        "A decorative attention pulse behind any content. Change the speed or stop it.",
        "AsgardPulseRing(color = colorScheme.tertiary) { Icon(Icons.Rounded.Star, null) }",
    ) {
        var speed by remember { mutableStateOf(1200f) }
        var pulsing by remember { mutableStateOf(true) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsgardPulseRing(
                color = MaterialTheme.colorScheme.tertiary,
                ringSize = 40.dp,
                durationMillis = speed.roundToInt(),
                pulsing = pulsing,
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            }
            AsgardLabeledSlider("Speed (ms)", speed, { speed = it }, valueRange = 400f..2400f, valueLabel = "${speed.roundToInt()}")
            ControlToggle("Pulsing", pulsing) { pulsing = it }
        }
    },
)

/** A label + trailing Switch row used by the interactive chart demos. */
@Composable
private fun ControlToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
