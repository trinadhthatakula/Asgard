package com.valhalla.asgard.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/** One destination in an [AsgardNavigationBar] / [AsgardNavigationRail]. */
data class AsgardNavItem(
    val icon: ImageVector,
    val label: String,
    val selectedIcon: ImageVector = icon,
    val contentDescription: String? = null,
)
