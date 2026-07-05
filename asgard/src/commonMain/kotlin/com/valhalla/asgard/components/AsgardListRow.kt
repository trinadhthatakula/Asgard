package com.valhalla.asgard.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults

/**
 * A generic list item row: an optional leading [icon] (or custom [leading] slot), a [title]
 * with optional [subtitle]/[caption], and an optional [trailing] slot. Tapping invokes
 * [onClick] when provided.
 *
 * Colors and typography come from the ambient [MaterialTheme]. Wrap it in a Card/Surface at the
 * call site (or use [AsgardSectionCard]) for an elevated container.
 *
 * @param title the primary text.
 * @param modifier the [Modifier] applied to the row.
 * @param subtitle optional secondary line under the title.
 * @param caption optional tertiary line under the subtitle.
 * @param icon optional leading icon (ignored when [leading] is supplied).
 * @param iconTint tint for [icon].
 * @param onClick optional row click handler.
 * @param subtitleMarquee when true, the [subtitle] is a single line that toggles a scrolling
 *   marquee on tap (useful for long values that would otherwise be ellipsized).
 * @param leading optional custom leading content (avatar, checkbox, …).
 * @param trailing optional trailing content (value text, action, chevron, …).
 * @param enabled when false, the row click (if any) is disabled.
 * @param onClickLabel accessibility label describing the click action for [onClick].
 * @param titleStyle text style for the [title]. Defaults to the current `bodyLarge` style.
 * @param titleColor color for the [title]. Defaults to the current `onSurface` color.
 * @param titleMaxLines maximum number of lines for the [title].
 * @param titleOverflow how visual overflow of the [title] is handled.
 * @param subtitleStyle text style for the [subtitle]. Defaults to the current `bodyMedium` style.
 * @param subtitleColor color for the [subtitle]. Defaults to the current `onSurfaceVariant` color.
 * @param subtitleMaxLines maximum number of lines for the non-marquee [subtitle].
 * @param subtitleOverflow how visual overflow of the non-marquee [subtitle] is handled.
 * @param captionStyle text style for the [caption]. Defaults to the current `labelSmall` style.
 * @param captionColor color for the [caption]. Defaults to the current `onSurfaceVariant` color.
 * @param captionMaxLines maximum number of lines for the [caption].
 * @param captionOverflow how visual overflow of the [caption] is handled.
 * @param contentPadding padding applied around the row content.
 * @param iconContentDescription accessibility description for [icon] (null = decorative).
 * @param iconSize size of the leading [icon].
 * @param verticalAlignment vertical alignment of the row's children.
 */
@Composable
fun AsgardListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    caption: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    subtitleMarquee: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    titleMaxLines: Int = 1,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    subtitleMaxLines: Int = 2,
    subtitleOverflow: TextOverflow = TextOverflow.Ellipsis,
    captionStyle: TextStyle = MaterialTheme.typography.labelSmall,
    captionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    captionMaxLines: Int = 1,
    captionOverflow: TextOverflow = TextOverflow.Ellipsis,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    iconContentDescription: String? = null,
    iconSize: Dp = AsgardDefaults.iconSize,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    Row(
        modifier = (
            if (onClick != null) {
                modifier.clickable(
                    enabled = enabled,
                    onClickLabel = onClickLabel,
                    role = Role.Button,
                    onClick = onClick,
                )
            } else {
                modifier
            }
            ).padding(contentPadding),
        verticalAlignment = verticalAlignment,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(16.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = titleMaxLines,
                overflow = titleOverflow,
            )
            if (subtitle != null) {
                if (subtitleMarquee) {
                    var marqueeOn by remember(subtitle) { mutableStateOf(false) }
                    Text(
                        text = subtitle,
                        style = subtitleStyle,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = if (marqueeOn) TextOverflow.Clip else TextOverflow.Ellipsis,
                        modifier = (if (marqueeOn) Modifier.basicMarquee() else Modifier)
                            .clickable { marqueeOn = !marqueeOn },
                    )
                } else {
                    Text(
                        text = subtitle,
                        style = subtitleStyle,
                        color = subtitleColor,
                        maxLines = subtitleMaxLines,
                        overflow = subtitleOverflow,
                    )
                }
            }
            if (caption != null) {
                Text(
                    text = caption,
                    style = captionStyle,
                    color = captionColor,
                    maxLines = captionMaxLines,
                    overflow = captionOverflow,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}
