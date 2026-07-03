package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A titled container that groups related settings rows on a rounded tonal surface.
 *
 * @param title the section heading.
 * @param modifier the [Modifier] applied to the card.
 * @param titleColor the heading color.
 * @param containerColor the card background color.
 * @param content the rows to lay out vertically inside the section.
 */
@Composable
fun AsgardSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            content()
        }
    }
}

/**
 * A settings/preference row: an optional leading [icon], a [title] with optional [subtitle], and
 * either a trailing [value] string or a custom [trailing] slot. Tapping invokes [onClick].
 *
 * Delegates to [AsgardListRow]; all colors come from the ambient [MaterialTheme].
 *
 * @param title the setting label.
 * @param modifier the [Modifier] applied to the row.
 * @param subtitle optional description under the title.
 * @param value optional trailing value text (ignored when [trailing] is supplied).
 * @param icon optional leading icon.
 * @param iconTint tint for [icon].
 * @param onClick optional click handler.
 * @param trailing optional custom trailing content.
 */
@Composable
fun AsgardSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    AsgardListRow(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        onClick = onClick,
        trailing = trailing ?: value?.let { v ->
            {
                Text(
                    text = v,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

/**
 * A settings row with a trailing on/off [Switch]. Tapping the row toggles it.
 *
 * @param title the setting label.
 * @param checked the current toggle state.
 * @param onCheckedChange invoked when the switch is toggled.
 * @param modifier the [Modifier] applied to the row.
 * @param subtitle optional description under the title.
 * @param enabled whether the row/switch is interactive.
 * @param icon optional leading icon.
 * @param iconTint tint for [icon].
 */
@Composable
fun AsgardSettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    AsgardListRow(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        onClick = if (enabled) {
            { onCheckedChange(!checked) }
        } else {
            null
        },
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        },
    )
}
