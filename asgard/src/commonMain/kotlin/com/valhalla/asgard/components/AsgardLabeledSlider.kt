package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * A labeled slider: a [label] and a formatted [valueLabel] on one line, with an M3 [Slider]
 * beneath. Colors come from the ambient [MaterialTheme].
 *
 * @param label the descriptive label.
 * @param value the current slider value.
 * @param onValueChange invoked as the slider moves.
 * @param modifier the [Modifier] applied to the container.
 * @param valueRange the min..max range.
 * @param steps discrete steps between endpoints (0 = continuous).
 * @param valueLabel optional formatted value shown next to the label (defaults to the raw value).
 * @param onValueChangeFinished invoked when the user stops dragging.
 */
@Composable
fun AsgardLabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    valueLabel: String? = null,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueLabel ?: value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}
