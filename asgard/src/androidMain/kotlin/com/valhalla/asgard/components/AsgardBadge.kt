package com.valhalla.asgard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A small, neutral label pill with an optional leading [icon] — for static tags, category
 * labels, or "PRO"/"NEW" markers. Distinct from [StatusChip], which conveys semantic state.
 *
 * Renders filled (tonal) by default, or as an [outlined] pill with a [contentColor] border.
 * Colors default to the ambient [MaterialTheme] so the badge inherits the host palette.
 *
 * @param text the label (single line).
 * @param modifier the [Modifier] applied to the pill.
 * @param icon optional leading icon.
 * @param outlined when `true`, renders a transparent pill with a border instead of a filled one.
 * @param onClick optional click handler (usable as a filter/tag chip).
 * @param containerColor the filled background color.
 * @param contentColor the icon/text color (and border color when [outlined]).
 */
@Composable
fun AsgardBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    outlined: Boolean = false,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    var shell = modifier.clip(CircleShape)
    if (onClick != null) shell = shell.clickable(onClick = onClick)
    shell = if (outlined) shell.border(BorderStroke(1.dp, contentColor), CircleShape)
    else shell.background(containerColor)
    Row(
        modifier = shell.padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
        )
    }
}
