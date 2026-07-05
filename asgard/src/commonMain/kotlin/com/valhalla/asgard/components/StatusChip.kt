package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults

/**
 * A compact, pill-shaped status label.
 *
 * Renders [text] clipped to [shape] with a solid [containerColor] background and a
 * bold, small-label typography. Colors default to the ambient [MaterialTheme] so the chip
 * stays theme-agnostic.
 *
 * @param text the label to display (single line).
 * @param modifier the [Modifier] applied to the chip.
 * @param containerColor the pill background color.
 * @param contentColor the text color; defaults to the matching on-color for [containerColor].
 * @param textStyle the typography applied to [text].
 * @param shape the shape the chip is clipped to.
 * @param contentPadding the inner padding around the chip content.
 * @param onClick optional click handler; when set the chip becomes clickable with [Role.Button].
 * @param leadingIcon optional composable rendered before [text].
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = contentColorFor(containerColor),
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    shape: Shape = AsgardDefaults.pillShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    onClick: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val containerModifier = modifier
        .clip(shape)
        .background(containerColor)
        .then(
            if (onClick != null) {
                Modifier.clickable(role = Role.Button, onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(contentPadding)

    if (leadingIcon != null) {
        Row(
            modifier = containerModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            leadingIcon()
            Text(
                text = text,
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Text(
            text = text,
            modifier = containerModifier,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
