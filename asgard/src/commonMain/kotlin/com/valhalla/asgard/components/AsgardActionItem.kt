package com.valhalla.asgard.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults
import com.valhalla.asgard.expressivePress

/**
 * A vertical, tappable action tile: an icon "chip" above a short label.
 *
 * Lays out as a ~72.dp-wide [Column] with a 48.dp rounded icon container and a bold,
 * two-line caption. Pressing the tile triggers Asgard's [expressivePress] squish feedback.
 * All colors default to the ambient [MaterialTheme] so the item stays theme-agnostic.
 *
 * @param icon the [ImageVector] shown inside the chip.
 * @param label the caption beneath the icon (up to two lines).
 * @param onClick invoked when the tile is tapped.
 * @param modifier the [Modifier] applied to the tile.
 * @param enabled whether the tile is interactive; when `false` the content is dimmed.
 * @param containerColor the background color of the icon chip.
 * @param iconTint the tint applied to [icon].
 * @param iconContentDescription the accessibility description for [icon]; defaults to `null`
 * because [label] is already an on-screen, readable caption (avoids duplicate announcements).
 * @param labelMaxLines the maximum number of lines for the label before truncation.
 * @param labelOverflow how the label is truncated when it exceeds [labelMaxLines].
 * @param labelStyle the [TextStyle] applied to the label.
 * @param labelColor the color of the label text; dimmed by [disabledAlpha] when not [enabled].
 * @param width the fixed width of the tile.
 * @param shape the [Shape] used to clip the tile and its ripple.
 * @param iconChipSize the size of the rounded icon container.
 * @param iconSize the size of [icon] inside the chip.
 * @param disabledAlpha the alpha applied to the label when [enabled] is `false`.
 * @param onLongClick invoked on a long press; when non-`null` the tile uses a combined
 * click gesture. Defaults to `null` (long-press disabled).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AsgardActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconContentDescription: String? = null,
    labelMaxLines: Int = 2,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    width: Dp = 72.dp,
    shape: Shape = AsgardDefaults.actionItemShape,
    iconChipSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    disabledAlpha: Float = 0.38f,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(width)
            .expressivePress(interactionSource)
            .clip(shape)
            .let { base ->
                if (onLongClick != null) {
                    base.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                } else {
                    base.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                    )
                }
            }
            .padding(vertical = 8.dp),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides LocalContentColor.current.copy(
                alpha = if (enabled) 1f else disabledAlpha,
            ),
        ) {
            Box(
                modifier = Modifier
                    .size(iconChipSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = labelColor.copy(
                    alpha = if (enabled) labelColor.alpha else disabledAlpha,
                ),
                style = labelStyle,
                fontWeight = FontWeight.Bold,
                maxLines = labelMaxLines,
                overflow = labelOverflow,
                textAlign = TextAlign.Center,
            )
        }
    }
}
