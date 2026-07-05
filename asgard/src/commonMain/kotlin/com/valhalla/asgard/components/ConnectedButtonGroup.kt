package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex

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
 *
 * @param items list of [ConnectedButtonGroupItem] descriptors — one per button (must be non-empty).
 * @param selectedIndex index of the currently-selected button. Values outside `items.indices`
 *   are ignored (rendered as "nothing selected") and never crash.
 * @param onItemSelected invoked with the tapped button's index when a new button is selected.
 * @param modifier applied to the root [Row].
 * @param enabled group-wide enabled flag. When `false`, every button is disabled regardless of
 *   the per-item [ConnectedButtonGroupItem.enabled] flag.
 * @param colors optional [ToggleButtonColors] applied to every button. When `null`, the Material
 *   default is used but the **selected** button is emphasized with `primary` container / `onPrimary`
 *   content so its label and icon stay legible in both light and dark themes.
 * @param spacing horizontal spacing between buttons. Defaults to the connected-group overlap
 *   ([ButtonGroupDefaults.ConnectedSpaceBetween]).
 * @param contentDescription optional accessibility description applied to the whole group.
 * @param labelMaxLines maximum number of lines for text labels rendered inside buttons
 *   ([ConnectedButtonGroupItem.Label] / [ConnectedButtonGroupItem.IconWithLabel]). Defaults to `1`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonGroup(
    items: List<ConnectedButtonGroupItem>,
    selectedIndex: Int,
    onItemSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors? = null,
    spacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    contentDescription: String? = null,
    labelMaxLines: Int = 1,
) {
    require(items.isNotEmpty()) { "ConnectedButtonGroup requires at least one item" }

    val lastIndex = items.lastIndex
    // The selected button is emphasized with primary/onPrimary so its label + icon stay
    // high-contrast in both light and dark themes (the bare M3 default is low-contrast on dark).
    val resolvedColors = colors ?: ToggleButtonDefaults.toggleButtonColors(
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    )
    val groupDescription = contentDescription

    Row(
        modifier = modifier.then(
            if (groupDescription != null) {
                Modifier.semantics { this.contentDescription = groupDescription }
            } else {
                Modifier
            }
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            ToggleButton(
                checked = index == selectedIndex,
                onCheckedChange = { checked -> if (checked) onItemSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    // Draw the selected button on top so its borders aren't clipped by
                    // the overlapping neighbours produced by the negative connected spacing.
                    .zIndex(if (index == selectedIndex) 1f else 0f),
                enabled = enabled && item.enabled,
                colors = resolvedColors,
                shapes = connectedShapesFor(index, lastIndex),
            ) {
                ItemContent(item, labelMaxLines)
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

    /**
     * Whether this individual button is interactive. Combined with the group-wide
     * `enabled` flag on [ConnectedButtonGroup]; a button is enabled only when both are `true`.
     * Defaults to `true`.
     */
    val enabled: Boolean get() = true

    /** Legacy accessibility/menu label. Retained for API compatibility (unused for rendering). */
    @Deprecated("Unused; scheduled for removal in 2.0")
    val menuLabel: String

    /** Legacy optional menu icon. Retained for API compatibility (unused for rendering). */
    @Deprecated("Unused; scheduled for removal in 2.0")
    val menuIcon: ImageVector? get() = null

    // ── Concrete variants ─────────────────────────────────────────────────────

    /** Button shows only an icon (e.g. User / System app-type switcher). */
    data class Icon(
        val icon: ImageVector,
        val contentDescription: String,
        override val enabled: Boolean = true,
    ) : ConnectedButtonGroupItem {
        @Deprecated("Unused; scheduled for removal in 2.0")
        override val menuLabel: String get() = contentDescription
        @Deprecated("Unused; scheduled for removal in 2.0")
        override val menuIcon: ImageVector get() = icon
    }

    /** Button shows only a text label (e.g. ThemeMode picker, tab switcher). */
    data class Label(
        val text: String,
        override val enabled: Boolean = true,
    ) : ConnectedButtonGroupItem {
        @Deprecated("Unused; scheduled for removal in 2.0")
        override val menuLabel: String get() = text
    }

    /** Button shows an icon followed by a text label. */
    data class IconWithLabel(
        val icon: ImageVector,
        val contentDescription: String,
        val text: String,
        override val enabled: Boolean = true,
    ) : ConnectedButtonGroupItem {
        @Deprecated("Unused; scheduled for removal in 2.0")
        override val menuLabel: String get() = text
        @Deprecated("Unused; scheduled for removal in 2.0")
        override val menuIcon: ImageVector get() = icon
    }
}

// ─── Private helpers ──────────────────────────────────────────────────────────

/** Renders the correct inner content for each [ConnectedButtonGroupItem] variant. */
@Composable
private fun ItemContent(item: ConnectedButtonGroupItem, labelMaxLines: Int = 1) {
    when (item) {
        is ConnectedButtonGroupItem.Icon ->
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription
            )

        is ConnectedButtonGroupItem.Label ->
            Text(
                item.text,
                maxLines = labelMaxLines,
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
                    maxLines = labelMaxLines,
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
