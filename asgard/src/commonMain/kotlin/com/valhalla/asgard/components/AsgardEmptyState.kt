package com.valhalla.asgard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
 * A centered empty-state placeholder: an optional [icon] above a [text] message and an optional
 * [action] (e.g. a button). Colors default to the ambient [MaterialTheme].
 *
 * @param text the message explaining the empty state.
 * @param modifier the [Modifier] applied to the container.
 * @param icon optional illustrative icon.
 * @param iconTint tint for [icon].
 * @param textColor the message color.
 * @param action optional content shown under the message (e.g. a call-to-action button).
 * @param description optional secondary line shown beneath [text] when non-null.
 * @param textStyle the [TextStyle] applied to [text] and [description].
 * @param maxLines the maximum number of lines for [text] and [description].
 * @param overflow how visual overflow of [text] and [description] is handled.
 * @param iconSize the size of [icon].
 * @param iconContentDescription accessibility description for [icon]; null marks it decorative.
 * @param contentPadding the padding applied inside the container.
 */
@Composable
fun AsgardEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    action: (@Composable () -> Unit)? = null,
    description: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    iconSize: Dp = 48.dp,
    iconContentDescription: String? = null,
    contentPadding: PaddingValues = PaddingValues(24.dp),
) {
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
            Spacer(Modifier.height(12.dp))
        }
        Text(
            text = text,
            style = textStyle,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            overflow = overflow,
        )
        if (description != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                overflow = overflow,
            )
        }
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/**
 * A centered loading state: a [CircularProgressIndicator] with an optional caption [text].
 *
 * @param modifier the [Modifier] applied to the container.
 * @param text optional caption shown under the spinner.
 * @param color the spinner color.
 * @param textColor the caption color.
 * @param textStyle the [TextStyle] applied to the caption.
 * @param indicatorSize optional override for the spinner size; null uses the Material default.
 * @param strokeWidth optional override for the spinner stroke width; null uses the Material default.
 * @param contentPadding the padding applied inside the container.
 */
@Composable
fun AsgardLoadingState(
    modifier: Modifier = Modifier,
    text: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    indicatorSize: Dp? = null,
    strokeWidth: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(24.dp),
) {
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = color,
            modifier = if (indicatorSize != null) Modifier.size(indicatorSize) else Modifier,
            strokeWidth = strokeWidth ?: ProgressIndicatorDefaults.CircularStrokeWidth,
        )
        if (text != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
