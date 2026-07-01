package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

/**
 * A single-select Connected Button Group built on a weighted [Row] of [ToggleButton]s.
 *
 * Each button occupies an equal, tightly-bounded slot (`Modifier.weight(1f)`), so a button
 * can never be measured wider than its slot. This keeps the group crash-safe at any system
 * font/display scale (labels truncate with an ellipsis instead of overflowing). Shape logic
 * and single-select behaviour are handled internally; callers only describe *what* each button
 * shows via [ConnectedButtonGroupItem] and respond to selection changes.
 *
 * ---
 *
 * **Icon-only**:
 * ```kotlin
 * ConnectedButtonGroup(
 *     items = AppListType.entries.map { type ->
 *         ConnectedButtonGroupItem.Icon(
 *             icon = if (type == AppListType.USER) Icons.Default.Apps else Icons.Default.Android,
 *             contentDescription = type.name
 *         )
 *     },
 *     selectedIndex = AppListType.entries.indexOf(selectedType),
 *     onItemSelected = { onTypeChanged(AppListType.entries[it]) }
 * )
 * ```
 *
 * **Text labels**:
 * ```kotlin
 * ConnectedButtonGroup(
 *     items = ThemeMode.entries.map { ConnectedButtonGroupItem.Label(it.label()) },
 *     selectedIndex = ThemeMode.entries.indexOf(prefs.themeMode),
 *     onItemSelected = { viewModel.setThemeMode(ThemeMode.entries[it]) }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonGroup(
    items: List<ConnectedButtonGroupItem>,
    selectedIndex: Int,
    onItemSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.isNotEmpty()) { "ConnectedButtonGroup requires at least one item" }

    val lastIndex = items.lastIndex

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            ToggleButton(
                checked = index == selectedIndex,
                onCheckedChange = { checked -> if (checked) onItemSelected(index) },
                modifier = Modifier.weight(1f),
                shapes = connectedShapesFor(index, lastIndex),
            ) {
                ItemContent(item)
            }
        }
    }
}

// ─── Item descriptor ──────────────────────────────────────────────────────────

/**
 * Sealed hierarchy that describes the visual content of a single button.
 * Keeps the reusable composable generic without requiring caller-side lambdas.
 */
sealed interface ConnectedButtonGroupItem {

    /** Legacy accessibility/menu label. Retained for API compatibility (unused for rendering). */
    val menuLabel: String

    /** Legacy optional menu icon. Retained for API compatibility (unused for rendering). */
    val menuIcon: ImageVector? get() = null

    // ── Concrete variants ─────────────────────────────────────────────────────

    /** Button shows only an icon (e.g. User / System app-type switcher). */
    data class Icon(
        val icon: ImageVector,
        val contentDescription: String,
    ) : ConnectedButtonGroupItem {
        override val menuLabel: String get() = contentDescription
        override val menuIcon: ImageVector get() = icon
    }

    /** Button shows only a text label (e.g. ThemeMode picker, tab switcher). */
    data class Label(val text: String) : ConnectedButtonGroupItem {
        override val menuLabel: String get() = text
    }

    /** Button shows an icon followed by a text label. */
    data class IconWithLabel(
        val icon: ImageVector,
        val contentDescription: String,
        val text: String,
    ) : ConnectedButtonGroupItem {
        override val menuLabel: String get() = text
        override val menuIcon: ImageVector get() = icon
    }
}

// ─── Private helpers ──────────────────────────────────────────────────────────

/** Renders the correct inner content for each [ConnectedButtonGroupItem] variant. */
@Composable
private fun ItemContent(item: ConnectedButtonGroupItem) {
    when (item) {
        is ConnectedButtonGroupItem.Icon ->
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription
            )

        is ConnectedButtonGroupItem.Label ->
            Text(
                item.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        is ConnectedButtonGroupItem.IconWithLabel ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription
                )
                Text(
                    item.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
    }
}

/**
 * Maps a button's position within the group to the correct [ToggleButtonShapes],
 * following the Connected Button Group spec:
 *
 * - `index == 0`             → pill-left, small inner-right  *(leading)*
 * - `0 < index < lastIndex`  → small corners on all sides    *(middle)*
 * - `index == lastIndex`     → small inner-left, pill-right  *(trailing)*
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun connectedShapesFor(index: Int, lastIndex: Int): ToggleButtonShapes = when {
    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
    index == lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
}
