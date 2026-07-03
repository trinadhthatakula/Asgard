package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
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
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor,
            modifier = Modifier.weight(1f),
        )
        FilledTonalIconButton(onClick = onDecrement, enabled = canDecrement) {
            Icon(imageVector = decrementIcon, contentDescription = "Decrement $label")
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 48.dp)
                .padding(horizontal = 8.dp),
        )
        FilledTonalIconButton(onClick = onIncrement, enabled = canIncrement) {
            Icon(imageVector = incrementIcon, contentDescription = "Increment $label")
        }
    }
}
