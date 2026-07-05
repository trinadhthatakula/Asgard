package com.valhalla.asgard

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Central design tokens shared across Asgard components.
 *
 * Each token's default value equals the radius/size the corresponding component was previously
 * hard-coding, so routing a component through [AsgardDefaults] changes no pixels — it only
 * centralises the values and makes them overridable per call site. A fuller migration that sources
 * these from the host [MaterialTheme] (which would intentionally shift some radii) is planned for a
 * later, non-1.2.0 release.
 */
object AsgardDefaults {

    // ---- Shapes -----------------------------------------------------------------------------

    /** Shape for large metric cards (e.g. [com.valhalla.asgard.components.AsgardStatCard]). */
    val cardShape: Shape
        @Composable get() = MaterialTheme.shapes.large

    /** Shape for compact metric tiles (e.g. [com.valhalla.asgard.components.AsgardStatTile]). */
    val tileShape: Shape
        @Composable get() = RoundedCornerShape(20.dp)

    /** Shape for titled section cards (e.g. AsgardSectionCard). */
    val sectionCardShape: Shape
        @Composable get() = RoundedCornerShape(20.dp)

    /** Shape for full-width callout banners (e.g. [com.valhalla.asgard.components.AsgardBanner]). */
    val bannerShape: Shape
        @Composable get() = RoundedCornerShape(16.dp)

    /** Shape for vertical action tiles (e.g. [com.valhalla.asgard.components.AsgardActionItem]). */
    val actionItemShape: Shape
        @Composable get() = RoundedCornerShape(24.dp)

    /** Shape for promotional upgrade cards (e.g. AsgardUpgradeCard). */
    val upgradeCardShape: Shape
        @Composable get() = RoundedCornerShape(24.dp)

    /** Pill shape for badges and status chips. */
    val pillShape: Shape
        get() = CircleShape

    // ---- Spacing / sizing -------------------------------------------------------------------

    /** Default internal content padding for card-like surfaces. */
    val contentPadding: Dp = 16.dp

    /** Default icon size for row/list leading icons. */
    val iconSize: Dp = 24.dp

    /** Default size for circular icon "chip" badges. */
    val iconChipSize: Dp = 48.dp

    /** Corner radius used by the expressive navigation surfaces. */
    val navContainerRadius: Dp = 32.dp
}
