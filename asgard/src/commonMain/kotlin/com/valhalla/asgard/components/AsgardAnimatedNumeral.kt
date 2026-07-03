package com.valhalla.asgard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Numeric (or short) text that animates on change: the outgoing value slides out while the
 * incoming value slides in, giving an "odometer"-style transition. Styling comes from the
 * ambient [MaterialTheme] by default.
 *
 * @param value the current text to display (drives the transition when it changes).
 * @param modifier the [Modifier] applied to the text.
 * @param style the text style.
 * @param color the text color.
 * @param fontWeight optional font weight.
 * @param durationMillis the slide duration.
 */
@Composable
fun AsgardAnimatedNumeral(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = FontWeight.Bold,
    durationMillis: Int = 300,
) {
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        transitionSpec = {
            // Prefer a numeric comparison (correct for decimals / negatives); fall back to a
            // length-then-lexicographic heuristic for non-numeric text.
            val targetNum = targetState.toDoubleOrNull()
            val initialNum = initialState.toDoubleOrNull()
            val goingUp = if (targetNum != null && initialNum != null) {
                targetNum > initialNum
            } else {
                targetState.length > initialState.length ||
                    (targetState.length == initialState.length && targetState > initialState)
            }
            slideInVertically(tween(durationMillis)) { h -> if (goingUp) h else -h }
                .togetherWith(slideOutVertically(tween(durationMillis)) { h -> if (goingUp) -h else h })
                .using(SizeTransform(clip = false))
        },
        label = "AsgardAnimatedNumeral",
    ) { target ->
        Text(text = target, style = style, color = color, fontWeight = fontWeight, maxLines = 1)
    }
}
