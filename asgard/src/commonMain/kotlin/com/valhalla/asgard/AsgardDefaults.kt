package com.valhalla.asgard

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Central design tokens shared across Asgard components.
 *
 * The shape tokens are **sourced from the host [MaterialTheme.shapes]**, so Asgard's surfaces follow
 * the app's own shape scheme — override `MaterialTheme.shapes` (large / medium / …) and every Asgard
 * card, tile, banner and action item reshapes with it. Any component still accepts an explicit
 * `shape` param to override per call site.
 *
 * Spacing/sizing tokens are plain [Dp] constants (Material 3 has no theme spacing scale).
 */
object AsgardDefaults {

    // ---- Shapes (theme-driven) ---------------------------------------------------------------

    /** Shape for large metric cards (e.g. [com.valhalla.asgard.components.AsgardStatCard]). */
    val cardShape: Shape
        @Composable get() = MaterialTheme.shapes.large

    /** Shape for compact metric tiles (e.g. [com.valhalla.asgard.components.AsgardStatTile]). */
    val tileShape: Shape
        @Composable get() = MaterialTheme.shapes.large

    /** Shape for titled section cards (e.g. AsgardSectionCard). */
    val sectionCardShape: Shape
        @Composable get() = MaterialTheme.shapes.large

    /** Shape for full-width callout banners (e.g. [com.valhalla.asgard.components.AsgardBanner]). */
    val bannerShape: Shape
        @Composable get() = MaterialTheme.shapes.medium

    /** Shape for vertical action tiles (e.g. [com.valhalla.asgard.components.AsgardActionItem]). */
    val actionItemShape: Shape
        @Composable get() = MaterialTheme.shapes.large

    /** Shape for promotional upgrade cards (e.g. AsgardUpgradeCard). */
    val upgradeCardShape: Shape
        @Composable get() = MaterialTheme.shapes.large

    /** Pill shape for badges and status chips. */
    val pillShape: Shape
        get() = CircleShape

    // ---- Spacing / sizing --------------------------------------------------------------------

    /** Default internal content padding for card-like surfaces. */
    val contentPadding: Dp = 16.dp

    /** Default icon size for row/list leading icons. */
    val iconSize: Dp = 24.dp

    /** Default size for circular icon "chip" badges. */
    val iconChipSize: Dp = 48.dp

    /** Corner radius used by the expressive navigation surfaces. */
    val navContainerRadius: Dp = 32.dp
}
