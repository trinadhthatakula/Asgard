package com.valhalla.asgard.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

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
 * @param enabled whether the slider is enabled and responds to input.
 * @param colors optional [SliderColors] for the [Slider]; when null the M3 defaults are used.
 * @param labelStyle the [TextStyle] for the [label] text.
 * @param labelColor the color for the [label] text.
 * @param valueStyle the [TextStyle] for the [valueLabel] text.
 * @param valueColor the color for the [valueLabel] text.
 * @param labelMaxLines the maximum number of lines for the [label] text.
 * @param labelOverflow how visual overflow of the [label] text is handled.
 * @param interactionSource optional [MutableInteractionSource] for the [Slider]; when null one is remembered internally.
 */
@Composable
fun AsgardLabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    valueLabel: String? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: SliderColors? = null,
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    valueStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    labelMaxLines: Int = 1,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    Column(modifier = Modifier.fillMaxWidth().then(modifier)) {
        Row {
            Text(
                text = label,
                style = labelStyle,
                color = labelColor,
                maxLines = labelMaxLines,
                overflow = labelOverflow,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = valueLabel ?: value.toString(),
                style = valueStyle,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            enabled = enabled,
            colors = colors ?: SliderDefaults.colors(),
            interactionSource = resolvedInteractionSource,
        )
    }
}
