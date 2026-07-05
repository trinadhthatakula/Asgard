package com.valhalla.asgard.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One destination in an [AsgardNavigationBar] / [AsgardNavigationRail].
 *
 * @param icon The icon shown for this destination when it is not selected.
 * @param label The text label shown for this destination.
 * @param selectedIcon The icon shown when this destination is selected; defaults to [icon].
 * @param contentDescription Accessibility description for the icon; `null` if not needed.
 * @param badge Optional notification/count string rendered as a badge on the item;
 *   `null` shows no badge.
 */
data class AsgardNavItem(
    val icon: ImageVector,
    val label: String,
    val selectedIcon: ImageVector = icon,
    val contentDescription: String? = null,
    val badge: String? = null,
)
