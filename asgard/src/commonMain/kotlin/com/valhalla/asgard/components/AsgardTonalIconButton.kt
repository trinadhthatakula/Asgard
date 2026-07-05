package com.valhalla.asgard.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

/**
 * A thin wrapper over M3 [FilledTonalIconButton] for a consistent tonal icon action. Colors come
 * from the ambient MaterialTheme.
 *
 * @param icon the icon to display.
 * @param contentDescription the accessibility description (null if purely decorative).
 * @param onClick invoked when tapped.
 * @param modifier the [Modifier] applied to the button.
 * @param enabled whether the button is interactive.
 * @param colors the [IconButtonColors] for the button; when null the M3 tonal defaults are used.
 * @param shape the [Shape] of the button container; when null the M3 tonal default shape is used.
 * @param interactionSource the [MutableInteractionSource] for the button; when null a remembered
 *   instance is used.
 * @param iconTint the tint applied to the icon; when null the ambient content color is used.
 * @param iconSize an explicit size for the icon; when null the icon's intrinsic size is used.
 */
@Composable
fun AsgardTonalIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors? = null,
    shape: Shape? = null,
    interactionSource: MutableInteractionSource? = null,
    iconTint: Color? = null,
    iconSize: Dp? = null,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape ?: IconButtonDefaults.filledShape,
        colors = colors ?: IconButtonDefaults.filledTonalIconButtonColors(),
        interactionSource = interactionSource,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier,
            tint = iconTint ?: LocalContentColor.current,
        )
    }
}
