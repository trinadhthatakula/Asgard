package com.valhalla.asgard.demo

import androidx.compose.foundation.layout.Box
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
        AsgardShimmer(Modifier.fillMaxWidth().size(width = 320.dp, height = 24.dp))
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
)
