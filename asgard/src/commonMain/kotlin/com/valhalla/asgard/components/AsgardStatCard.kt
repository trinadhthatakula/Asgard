package com.valhalla.asgard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valhalla.asgard.expressivePress

/**
 * A vertical metric card: an [icon] + [label] header at the top and a large, emphasized [value]
 * (with an optional smaller [unit] suffix) pinned to the bottom, on a rounded [Card] surface.
 *
 * This is the icon-above-the-number companion to the horizontal [AsgardStatTile]. All colors,
 * shape and the value style default to the ambient [MaterialTheme] so the card stays theme-agnostic.
 *
 * The header adapts to two common layouts via [iconInlineWithLabel]:
 * - `false` (default): the icon sits **above** the label (dashboard-style metric card).
 * - `true`: the icon sits **beside** the label on one row (compact "bento" cell).
 *
 * Pass a fixed height (e.g. `Modifier.height(100.dp)`) or `Modifier.fillMaxWidth()` via [modifier];
 * the card itself only wraps its content. When [onClick] is non-null the whole card becomes
 * clickable with the shared Expressive press "squish".
 *
 * @param label the caption describing the metric (single line).
 * @param value the emphasized value text (single line).
 * @param modifier the [Modifier] applied to the card (size/weight/height live here).
 * @param icon optional leading icon.
 * @param iconColor tint for [icon]. Deprecated in favor of [iconTint]; retained for source
 *   compatibility. If [iconTint] is not set it defaults to this value.
 * @param iconSize the size of [icon].
 * @param iconInlineWithLabel when true, [icon] is placed beside [label] instead of above it.
 * @param unit optional smaller suffix appended after [value] (e.g. a "mAh"/"%" superscript).
 * @param onClick optional click handler; when non-null the whole card becomes clickable.
 * @param onClickLabel accessibility label announced for the click action.
 * @param shape the card shape.
 * @param containerColor the card background color.
 * @param border optional border stroke.
 * @param contentPadding padding inside the card.
 * @param labelColor the [label] text color.
 * @param valueColor the [value] text color.
 * @param valueStyle the [value] text style.
 * @param valueFontWeight the [value] font weight.
 * @param unitFontSize the font size of the [unit] suffix.
 * @param labelStyle the [label] text style.
 * @param labelMaxLines maximum number of lines for [label]. Defaults to `1`.
 * @param labelOverflow how visual overflow of [label] is handled. Defaults to [TextOverflow.Ellipsis].
 * @param valueMaxLines maximum number of lines for [value]. Defaults to `1`.
 * @param valueOverflow how visual overflow of [value] is handled. Defaults to [TextOverflow.Ellipsis].
 * @param textAlign optional [TextAlign] applied to both [label] and [value]. `null` keeps the default start alignment.
 * @param iconTint tint for [icon]; defaults to [iconColor]. Preferred over the deprecated [iconColor].
 * @param unitSeparator the string placed between [value] and [unit]. Defaults to a single space; pass `""` for a tight suffix (e.g. `95%`).
 */
@Composable
fun AsgardStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 20.dp,
    iconInlineWithLabel: Boolean = false,
    unit: String? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    valueFontWeight: FontWeight = FontWeight.Medium,
    unitFontSize: TextUnit = 14.sp,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
    labelMaxLines: Int = 1,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    valueMaxLines: Int = 1,
    valueOverflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    iconTint: Color = iconColor,
    unitSeparator: String = " ",
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier
                    .expressivePress(interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = onClickLabel,
                        onClick = onClick,
                    )
            } else {
                Modifier
            }
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header: icon + label, either stacked (icon above) or inline (icon beside).
            if (iconInlineWithLabel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(iconSize),
                        )
                    }
                    Text(
                        text = label,
                        style = labelStyle,
                        color = labelColor,
                        textAlign = textAlign,
                        maxLines = labelMaxLines,
                        overflow = labelOverflow,
                    )
                }
            } else {
                Column {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(iconSize),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = label,
                        style = labelStyle,
                        color = labelColor,
                        textAlign = textAlign,
                        maxLines = labelMaxLines,
                        overflow = labelOverflow,
                    )
                }
            }

            // Value, with an optional smaller unit suffix.
            Text(
                text = buildAnnotatedString {
                    append(value)
                    if (!unit.isNullOrEmpty()) {
                        withStyle(SpanStyle(fontSize = unitFontSize)) {
                            append("$unitSeparator$unit")
                        }
                    }
                },
                style = valueStyle,
                fontWeight = valueFontWeight,
                color = valueColor,
                textAlign = textAlign,
                maxLines = valueMaxLines,
                overflow = valueOverflow,
            )
        }
    }
}
