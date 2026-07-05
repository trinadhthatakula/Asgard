package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardDefaults

/**
 * A small "PRO"/premium marker pill — a thin preset over [AsgardBadge] using the tertiary palette.
 *
 * @param text the marker label.
 * @param modifier the [Modifier] applied to the badge.
 * @param icon optional leading icon (e.g. a lock), supplied by the consumer.
 * @param containerColor the pill background color.
 * @param contentColor the pill content color.
 * @param onClick optional click handler, forwarded to [AsgardBadge] (usable as a filter/tag chip).
 * @param outlined when `true`, renders a transparent pill with a border instead of a filled one.
 * @param textStyle optional override for the label text style; when `null` the [AsgardBadge]
 * default (`MaterialTheme.typography.labelMedium`) is used.
 */
@Composable
fun AsgardProBadge(
    text: String = "PRO",
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    onClick: (() -> Unit)? = null,
    outlined: Boolean = false,
    textStyle: TextStyle? = null,
) {
    AsgardBadge(
        text = text,
        modifier = modifier,
        icon = icon,
        outlined = outlined,
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = textStyle ?: MaterialTheme.typography.labelMedium,
    )
}

/**
 * A promotional upgrade card: a [title], a [description], and a primary call-to-action button.
 *
 * @param title the headline.
 * @param description the supporting copy.
 * @param cta the button label.
 * @param onUpgrade invoked when the CTA is tapped.
 * @param modifier the [Modifier] applied to the card.
 * @param containerColor the card background color.
 * @param contentColor the title/description color.
 * @param titleMaxLines the maximum number of lines for the [title].
 * @param titleOverflow how the [title] is truncated when it exceeds [titleMaxLines].
 * @param descriptionMaxLines the maximum number of lines for the [description].
 * @param descriptionOverflow how the [description] is truncated when it exceeds
 * [descriptionMaxLines].
 * @param titleStyle the text style for the [title].
 * @param descriptionStyle the text style for the [description].
 * @param shape the card shape.
 * @param contentPadding the padding around the card content.
 * @param ctaColors optional override for the CTA button colors; when `null` the colors are derived
 * from [contentColor]/[containerColor] so the button contrasts against the card.
 * @param leading optional composable rendered above the [title] (e.g. an illustration or icon).
 */
@Composable
fun AsgardUpgradeCard(
    title: String,
    description: String,
    cta: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    titleMaxLines: Int = Int.MAX_VALUE,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    descriptionMaxLines: Int = Int.MAX_VALUE,
    descriptionOverflow: TextOverflow = TextOverflow.Ellipsis,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    descriptionStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    shape: Shape = AsgardDefaults.upgradeCardShape,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    ctaColors: ButtonColors? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            if (leading != null) {
                leading()
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = titleMaxLines,
                overflow = titleOverflow,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = descriptionStyle,
                color = contentColor,
                maxLines = descriptionMaxLines,
                overflow = descriptionOverflow,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onUpgrade,
                colors = ctaColors ?: ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = containerColor,
                ),
            ) { Text(cta) }
        }
    }
}

/**
 * Gates [content]: when [locked] is `true`, the content is blurred and an [overlay] (e.g. an
 * [AsgardUpgradeCard]) is shown on top; when unlocked, [content] renders normally.
 *
 * While locked, the scrim intercepts input so the gated [content] can't be interacted with, and
 * the gated content is hidden from accessibility services so screen readers don't read through the
 * blur. Blur is a no-op on platforms without a blur effect (e.g. Android < 12).
 *
 * @param locked whether the gate is active.
 * @param overlay the composable shown over the blurred content while locked.
 * @param content the gated content.
 * @param modifier the [Modifier] applied to the gate container.
 * @param blurRadius the blur applied to [content] while locked.
 * @param scrimColor a translucent scrim drawn over the blurred content.
 * @param contentAlignment how [overlay] is aligned within the scrim while locked.
 * @param overlayShape when non-null, clips the blurred content and scrim to this shape.
 */
@Composable
fun AsgardLockedOverlay(
    locked: Boolean,
    overlay: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 12.dp,
    scrimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
    contentAlignment: Alignment = Alignment.Center,
    overlayShape: Shape? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = if (locked) {
                Modifier
                    .then(if (overlayShape != null) Modifier.clip(overlayShape) else Modifier)
                    .blur(blurRadius)
                    // Hide the blurred content from screen readers while locked.
                    .clearAndSetSemantics {}
            } else {
                Modifier
            },
        ) {
            content()
        }
        if (locked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(if (overlayShape != null) Modifier.clip(overlayShape) else Modifier)
                    .background(scrimColor)
                    // Swallow input so the blurred content underneath can't be interacted with.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                contentAlignment = contentAlignment,
            ) {
                overlay()
            }
        }
    }
}
