package com.valhalla.asgard.components

import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A thin wrapper over M3 [FilledTonalIconButton] for a consistent tonal icon action. Colors come
 * from the ambient MaterialTheme.
 *
 * @param icon the icon to display.
 * @param contentDescription the accessibility description (null if purely decorative).
 * @param onClick invoked when tapped.
 * @param modifier the [Modifier] applied to the button.
 * @param enabled whether the button is interactive.
 */
@Composable
fun AsgardTonalIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
