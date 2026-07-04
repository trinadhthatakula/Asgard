package com.valhalla.asgard.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
) {
    Row(
        modifier = (if (onClick != null) modifier.clickable(onClick = onClick) else modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(16.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                if (subtitleMarquee) {
                    var marqueeOn by remember { mutableStateOf(false) }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = if (marqueeOn) TextOverflow.Clip else TextOverflow.Ellipsis,
                        modifier = (if (marqueeOn) Modifier.basicMarquee() else Modifier)
                            .clickable { marqueeOn = !marqueeOn },
                    )
                } else {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}
