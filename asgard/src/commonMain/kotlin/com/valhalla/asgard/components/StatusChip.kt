package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A compact, pill-shaped status label.
 *
 * Renders [text] clipped to a [CircleShape] with a solid [containerColor] background and a
 * bold, small-label typography. Colors default to the ambient [MaterialTheme] so the chip
 * stays theme-agnostic.
 *
 * @param text the label to display (single line).
 * @param modifier the [Modifier] applied to the chip.
 * @param containerColor the pill background color.
 * @param contentColor the text color; defaults to the matching on-color for [containerColor].
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = contentColorFor(containerColor),
) {
    Text(
        text = text,
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        maxLines = 1,
    )
}
