package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A small "PRO"/premium marker pill — a thin preset over [AsgardBadge] using the tertiary palette.
 *
 * @param modifier the [Modifier] applied to the badge.
 * @param text the marker label.
 * @param icon optional leading icon (e.g. a lock), supplied by the consumer.
 * @param containerColor the pill background color.
 * @param contentColor the pill content color.
 */
@Composable
fun AsgardProBadge(
    modifier: Modifier = Modifier,
    text: String = "PRO",
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
) {
    AsgardBadge(
        text = text,
        modifier = modifier,
        icon = icon,
        containerColor = containerColor,
        contentColor = contentColor,
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
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Spacer(Modifier.height(6.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onUpgrade) { Text(cta) }
        }
    }
}

/**
 * Gates [content]: when [locked] is `true`, the content is blurred and an [overlay] (e.g. an
 * [AsgardUpgradeCard]) is centered on top; when unlocked, [content] renders normally.
 *
 * Note: the caller is responsible for making [content] non-interactive while locked (the blur is
 * visual only). Blur is a no-op below Android 12.
 *
 * @param locked whether the gate is active.
 * @param overlay the composable shown centered while locked.
 * @param content the gated content.
 * @param modifier the [Modifier] applied to the gate container.
 * @param blurRadius the blur applied to [content] while locked.
 * @param scrimColor a translucent scrim drawn over the blurred content.
 */
@Composable
fun AsgardLockedOverlay(
    locked: Boolean,
    overlay: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 12.dp,
    scrimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
) {
    Box(modifier = modifier) {
        Box(modifier = if (locked) Modifier.blur(blurRadius) else Modifier) {
            content()
        }
        if (locked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(scrimColor),
                contentAlignment = Alignment.Center,
            ) {
                overlay()
            }
        }
    }
}
