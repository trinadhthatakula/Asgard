package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults

/**
 * A full-width tonal callout / alert card: an optional leading [icon], a [title], an optional
 * [description], and an optional trailing [action] slot.
 *
 * Defaults to an "error" palette from the ambient [MaterialTheme]; pass a different
 * [containerColor] (e.g. `MaterialTheme.colorScheme.tertiaryContainer`) for tip/info/success
 * variants, or a [containerBrush] for a gradient background. [contentColor] derives from
 * [containerColor] by default so it stays legible.
 *
 * @param title the bold headline.
 * @param modifier the [Modifier] applied to the banner.
 * @param description optional supporting body text.
 * @param icon optional leading icon.
 * @param containerColor the banner background color (used when [containerBrush] is null).
 * @param containerBrush optional gradient/brush background; overrides [containerColor] when set.
 * @param contentColor the icon/text color. Defaults to a legible color derived from
 *   [containerColor]; when a [containerBrush] is used, or when no on-color can be derived, it
 *   falls back to `MaterialTheme.colorScheme.onSurface`.
 * @param titleStyle the [title] text style.
 * @param descriptionStyle the [description] text style.
 * @param titleMaxLines the maximum number of lines for [title].
 * @param titleOverflow how visual overflow of [title] is handled.
 * @param descriptionMaxLines the maximum number of lines for [description].
 * @param descriptionOverflow how visual overflow of [description] is handled.
 * @param shape the banner background/clip shape.
 * @param contentPadding the padding around the banner content.
 * @param iconContentDescription accessibility description for [icon] (null = decorative).
 * @param iconSize the size of the leading [icon].
 * @param iconTint the tint applied to the leading [icon]. Defaults to [contentColor].
 * @param verticalAlignment the vertical alignment of the banner's row content.
 * @param onClick optional click handler; when set the banner becomes clickable with
 *   [Role.Button] semantics.
 * @param action optional trailing content (e.g. a dismiss or retry button) in a [RowScope].
 */
@Composable
fun AsgardBanner(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer,
    containerBrush: Brush? = null,
    contentColor: Color = if (containerBrush != null) {
        MaterialTheme.colorScheme.onSurface
    } else {
        contentColorFor(containerColor).takeIf { it != Color.Unspecified }
            ?: MaterialTheme.colorScheme.onSurface
    },
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    descriptionStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    titleMaxLines: Int = Int.MAX_VALUE,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    descriptionMaxLines: Int = Int.MAX_VALUE,
    descriptionOverflow: TextOverflow = TextOverflow.Ellipsis,
    shape: Shape = AsgardDefaults.bannerShape,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    iconContentDescription: String? = null,
    iconSize: Dp = 24.dp,
    iconTint: Color = contentColor,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: (() -> Unit)? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .then(
                if (containerBrush != null) {
                    Modifier.background(containerBrush)
                } else {
                    Modifier.background(containerColor)
                },
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) { onClick() }
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        verticalAlignment = verticalAlignment,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = titleMaxLines,
                overflow = titleOverflow,
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = descriptionStyle,
                    color = contentColor,
                    maxLines = descriptionMaxLines,
                    overflow = descriptionOverflow,
                )
            }
        }
        if (action != null) {
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}
