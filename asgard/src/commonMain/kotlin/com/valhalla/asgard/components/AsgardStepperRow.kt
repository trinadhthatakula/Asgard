package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A labeled numeric stepper row: a [label] on the left, then a decrement button, the current
 * [value] text, and an increment button. Buttons disable at bounds via [canDecrement] /
 * [canIncrement]. Icons are supplied by the consumer (Asgard ships only core icons).
 *
 * @param label the row label.
 * @param value the formatted current value shown between the buttons.
 * @param decrementIcon the icon for the decrement button.
 * @param incrementIcon the icon for the increment button.
 * @param onDecrement invoked when the decrement button is tapped.
 * @param onIncrement invoked when the increment button is tapped.
 * @param modifier the [Modifier] applied to the row.
 * @param canDecrement whether the decrement button is enabled.
 * @param canIncrement whether the increment button is enabled.
 * @param labelColor the [label] color.
 * @param valueColor the [value] text color.
 * @param labelMaxLines the maximum number of lines for the [label].
 * @param valueMaxLines the maximum number of lines for the [value].
 * @param labelOverflow how visual overflow of the [label] is handled.
 * @param valueOverflow how visual overflow of the [value] is handled.
 * @param labelStyle the [TextStyle] applied to the [label].
 * @param valueStyle the [TextStyle] applied to the [value].
 * @param buttonColors optional [IconButtonColors] for both stepper buttons; when null the
 *   default filled-tonal icon-button colors are used.
 * @param decrementContentDescription the content description for the decrement button icon;
 *   when null it defaults to "Decrement <label>".
 * @param incrementContentDescription the content description for the increment button icon;
 *   when null it defaults to "Increment <label>".
 * @param valueMinWidth the minimum width reserved for the [value] text.
 */
@Composable
fun AsgardStepperRow(
    label: String,
    value: String,
    decrementIcon: ImageVector,
    incrementIcon: ImageVector,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    canDecrement: Boolean = true,
    canIncrement: Boolean = true,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelMaxLines: Int = 1,
    valueMaxLines: Int = 1,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    valueOverflow: TextOverflow = TextOverflow.Ellipsis,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    valueStyle: TextStyle = MaterialTheme.typography.titleMedium,
    buttonColors: IconButtonColors? = null,
    decrementContentDescription: String? = null,
    incrementContentDescription: String? = null,
    valueMinWidth: Dp = 48.dp,
) {
    val resolvedButtonColors = buttonColors ?: IconButtonDefaults.filledTonalIconButtonColors()
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            maxLines = labelMaxLines,
            overflow = labelOverflow,
            modifier = Modifier.weight(1f),
        )
        FilledTonalIconButton(
            onClick = onDecrement,
            enabled = canDecrement,
            colors = resolvedButtonColors,
        ) {
            Icon(
                imageVector = decrementIcon,
                contentDescription = decrementContentDescription ?: "Decrement $label",
            )
        }
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            maxLines = valueMaxLines,
            overflow = valueOverflow,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = valueMinWidth)
                .padding(horizontal = 8.dp),
        )
        FilledTonalIconButton(
            onClick = onIncrement,
            enabled = canIncrement,
            colors = resolvedButtonColors,
        ) {
            Icon(
                imageVector = incrementIcon,
                contentDescription = incrementContentDescription ?: "Increment $label",
            )
        }
    }
}
