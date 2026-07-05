package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults

/**
 * A titled container that groups related settings rows on a rounded tonal surface.
 *
 * @param title the section heading.
 * @param modifier the [Modifier] applied to the card.
 * @param titleColor the heading color.
 * @param containerColor the card background color.
 * @param titleMaxLines the maximum number of lines for the [title].
 * @param titleOverflow how the [title] is truncated when it exceeds [titleMaxLines].
 * @param titleStyle the [TextStyle] for the [title]; when null the theme `titleSmall` is used.
 * @param shape the shape of the card surface.
 * @param contentPadding the padding applied around the title and rows inside the card.
 * @param titleTrailing optional content placed at the end of the title row (e.g. an action or
 *   status chip); when null the title renders on its own.
 * @param content the rows to lay out vertically inside the section.
 */
@Composable
fun AsgardSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    titleMaxLines: Int = 1,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    titleStyle: TextStyle? = null,
    shape: Shape = AsgardDefaults.sectionCardShape,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    titleTrailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            if (titleTrailing != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = titleStyle ?: MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = titleMaxLines,
                        overflow = titleOverflow,
                    )
                    titleTrailing()
                }
            } else {
                Text(
                    text = title,
                    style = titleStyle ?: MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = titleMaxLines,
                    overflow = titleOverflow,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
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
 * @param enabled whether the row is interactive; when false the row is dimmed and clicks are
 *   ignored.
 * @param onClick optional click handler.
 * @param valueMaxLines the maximum number of lines for the trailing [value] text.
 * @param valueOverflow how the [value] text is truncated when it exceeds [valueMaxLines].
 * @param valueStyle the [TextStyle] for the [value] text; when null the theme `bodyMedium` is used.
 * @param valueColor the color for the [value] text; when null the theme `onSurfaceVariant` is used.
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
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    valueMaxLines: Int = 1,
    valueOverflow: TextOverflow = TextOverflow.Ellipsis,
    valueStyle: TextStyle? = null,
    valueColor: Color? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    AsgardListRow(
        title = title,
        modifier = if (enabled) modifier else modifier.alpha(0.5f),
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        onClick = if (enabled) onClick else null,
        trailing = trailing ?: value?.let { v ->
            {
                Text(
                    text = v,
                    style = valueStyle ?: MaterialTheme.typography.bodyMedium,
                    color = valueColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = valueMaxLines,
                    overflow = valueOverflow,
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
 * @param enabled whether the row/switch is interactive; when false the row is dimmed and clicks are
 *   ignored.
 * @param subtitleMarquee when true, the [subtitle] is a single line that toggles a scrolling
 *   marquee on tap.
 * @param icon optional leading icon.
 * @param iconTint tint for [icon].
 * @param switchColors optional [SwitchColors] for the trailing [Switch]; when null the Material
 *   defaults are used.
 */
@Composable
fun AsgardSettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    subtitleMarquee: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    switchColors: SwitchColors? = null,
) {
    AsgardListRow(
        title = title,
        modifier = if (enabled) modifier else modifier.alpha(0.5f),
        subtitle = subtitle,
        icon = icon,
        iconTint = iconTint,
        onClick = if (enabled) {
            { onCheckedChange(!checked) }
        } else {
            null
        },
        subtitleMarquee = subtitleMarquee,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = switchColors ?: SwitchDefaults.colors(),
            )
        },
    )
}
