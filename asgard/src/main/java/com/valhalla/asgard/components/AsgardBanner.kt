package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A full-width tonal callout / alert card: an optional leading [icon], a [title], an optional
 * [description], and an optional trailing [action] slot.
 *
 * Defaults to an "error" palette from the ambient [MaterialTheme]; pass a different
 * [containerColor] (e.g. `MaterialTheme.colorScheme.tertiaryContainer`) for tip/info/success
 * variants. [contentColor] derives from [containerColor] by default so it stays legible.
 *
 * @param title the bold headline.
 * @param modifier the [Modifier] applied to the banner.
 * @param description optional supporting body text.
 * @param icon optional leading icon.
 * @param containerColor the banner background color.
 * @param contentColor the icon/text color.
 * @param titleStyle the [title] text style.
 * @param descriptionStyle the [description] text style.
 * @param action optional trailing content (e.g. a dismiss or retry button) in a [RowScope].
 */
@Composable
fun AsgardBanner(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer,
    contentColor: Color = contentColorFor(containerColor),
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    descriptionStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = description, style = descriptionStyle, color = contentColor)
            }
        }
        if (action != null) {
            Spacer(Modifier.width(12.dp))
            action()
        }
    }
}
