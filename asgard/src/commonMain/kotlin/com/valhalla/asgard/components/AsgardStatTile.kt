package com.valhalla.asgard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.valhalla.asgard.expressivePress

/**
 * A compact metric tile: a small [label] paired with a large, emphasized [value] on a rounded
 * tonal surface, with an optional leading [icon] and an optional [statusDotColor] indicator.
 *
 * All colors and the value text style default to the ambient [MaterialTheme] so the tile stays
 * theme-agnostic. See [AsgardStatCard] for the vertical, icon-above-the-number companion.
 *
 * @param label the caption paired with the value (single line).
 * @param value the emphasized value text (single line).
 * @param modifier the [Modifier] applied to the tile.
 * @param icon optional leading icon shown before the text stack.
 * @param iconTint tint for [icon]; defaults to [valueColor].
 * @param iconContainerColor when non-null, [icon] is drawn inside a circular badge filled with
 *   this color (48dp) instead of as a bare icon.
 * @param statusDotColor when non-null, draws a small filled dot next to the label.
 * @param onClick optional click handler; when non-null the whole tile becomes clickable.
 * @param animateValue when true, [value] is rendered via [AsgardAnimatedNumeral] with a count-up
 *   animation (for numeric values).
 * @param valueFirst when true, [value] is placed above [label] (large-number-first layout).
 * @param secondaryValue optional smaller value shown inline after [value] (e.g. a delta or unit).
 * @param shape the tile shape.
 * @param border optional border stroke drawn around the tile.
 * @param contentPadding padding inside the tile.
 * @param containerColor the tile background color.
 * @param labelColor the [label] text color.
 * @param labelStyle the [label] text style; defaults to `labelMedium`.
 * @param valueColor the [value] text color.
 * @param valueStyle the [value] text style.
 * @param valueFontWeight overrides the [value] font weight; when null the weight from [valueStyle]
 *   is used (falling back to bold). Resolved identically for the animated and static value.
 * @param secondaryValueColor the [secondaryValue] text color.
 * @param secondaryValueStyle the [secondaryValue] text style.
 * @param labelMaxLines the maximum number of lines for [label] before truncation.
 * @param labelOverflow how [label] is truncated when it exceeds [labelMaxLines].
 * @param valueMaxLines the maximum number of lines for the static [value] before truncation.
 * @param valueOverflow how the static [value] is truncated when it exceeds [valueMaxLines].
 * @param iconContentDescription the accessibility description for [icon]; defaults to `null`
 *   because [label]/[value] are already on-screen, readable text (avoids duplicate announcements).
 * @param iconBadgeSize the size of the circular icon badge drawn when [iconContainerColor] is set.
 * @param iconSize the size of [icon].
 * @param statusDotSize the diameter of the [statusDotColor] indicator dot.
 */
@Composable
fun AsgardStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    iconContainerColor: Color? = null,
    statusDotColor: Color? = null,
    onClick: (() -> Unit)? = null,
    animateValue: Boolean = false,
    valueFirst: Boolean = false,
    secondaryValue: String? = null,
    shape: Shape = AsgardDefaults.tileShape,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelStyle: TextStyle? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    valueFontWeight: FontWeight? = null,
    secondaryValueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    secondaryValueStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    labelMaxLines: Int = 1,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    valueMaxLines: Int = 1,
    valueOverflow: TextOverflow = TextOverflow.Ellipsis,
    iconContentDescription: String? = null,
    iconBadgeSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    statusDotSize: Dp = 8.dp,
) {
    // Shared source so the expressivePress squish reacts to the clickable's press interactions
    // (indication = null, so the squish is the only feedback) — matching AsgardActionItem.
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            // Border before clip: Modifier.border strokes on the shape boundary (half outside),
            // so clipping first would shave off its outer half and render it half-width.
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(if (onClick != null) Modifier.expressivePress(interactionSource) else Modifier)
            .clip(shape)
            .background(containerColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            val resolvedTint = iconTint ?: valueColor
            if (iconContainerColor != null) {
                Box(
                    modifier = Modifier
                        .size(iconBadgeSize)
                        .clip(CircleShape)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        tint = resolvedTint,
                        modifier = Modifier.size(iconSize),
                    )
                }
                Spacer(Modifier.width(16.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = resolvedTint,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(Modifier.width(12.dp))
            }
        }
        Column {
            val labelContent: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (statusDotColor != null) {
                        Spacer(
                            Modifier
                                .size(statusDotSize)
                                .clip(CircleShape)
                                .background(statusDotColor),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = label,
                        style = labelStyle ?: MaterialTheme.typography.labelMedium,
                        color = labelColor,
                        maxLines = labelMaxLines,
                        overflow = labelOverflow,
                    )
                }
            }
            // One resolution for both paths: explicit override wins, else the style's weight,
            // else bold. Keeps the animated and static value visually identical.
            val resolvedValueWeight = valueFontWeight ?: valueStyle.fontWeight ?: FontWeight.Bold
            val valueText: @Composable () -> Unit = {
                if (animateValue) {
                    AsgardAnimatedNumeral(
                        value = value,
                        style = valueStyle,
                        color = valueColor,
                        fontWeight = resolvedValueWeight,
                        countUp = true,
                    )
                } else {
                    Text(
                        text = value,
                        style = valueStyle,
                        fontWeight = resolvedValueWeight,
                        color = valueColor,
                        maxLines = valueMaxLines,
                        overflow = valueOverflow,
                    )
                }
            }
            val valueContent: @Composable () -> Unit = {
                if (secondaryValue != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        valueText()
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = secondaryValue,
                            style = secondaryValueStyle,
                            color = secondaryValueColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else {
                    valueText()
                }
            }
            if (valueFirst) {
                valueContent()
                labelContent()
            } else {
                labelContent()
                valueContent()
            }
        }
    }
}
