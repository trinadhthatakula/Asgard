package com.valhalla.asgard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
    shape: Shape = RoundedCornerShape(20.dp),
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
) {
    Row(
        modifier = modifier
            // Border before clip: Modifier.border strokes on the shape boundary (half outside),
            // so clipping first would shave off its outer half and render it half-width.
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clip(shape)
            .background(containerColor)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            val resolvedTint = iconTint ?: valueColor
            if (iconContainerColor != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = resolvedTint,
                    )
                }
                Spacer(Modifier.width(16.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedTint,
                    modifier = Modifier.size(24.dp),
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusDotColor),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = label,
                        style = labelStyle ?: MaterialTheme.typography.labelMedium,
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
