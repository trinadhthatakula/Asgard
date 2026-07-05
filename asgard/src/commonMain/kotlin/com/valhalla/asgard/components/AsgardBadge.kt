package com.valhalla.asgard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * @param shape the pill [Shape]; defaults to [AsgardDefaults.pillShape].
 * @param textStyle the [TextStyle] applied to the label.
 * @param onClickLabel accessibility label describing the click action (used when [onClick] is set).
 * @param enabled when `false`, disables the click interaction (only relevant when [onClick] is set).
 * @param iconContentDescription accessibility description for the leading [icon]; `null` treats it as decorative.
 * @param iconSize the size of the leading [icon].
 * @param contentPadding the inner padding around the pill content.
 * @param borderWidth the border thickness used when [outlined].
 * @param borderColor the border color used when [outlined]; defaults to [contentColor].
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
    shape: Shape = AsgardDefaults.pillShape,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    onClickLabel: String? = null,
    enabled: Boolean = true,
    iconContentDescription: String? = null,
    iconSize: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = contentColor,
) {
    var shell = modifier.clip(shape)
    if (onClick != null) {
        shell = shell.clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = Role.Button,
            onClick = onClick,
        )
    }
    shell = if (outlined) shell.border(BorderStroke(borderWidth, borderColor), shape)
    else shell.background(containerColor)
    Row(
        modifier = shell.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = textStyle,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
