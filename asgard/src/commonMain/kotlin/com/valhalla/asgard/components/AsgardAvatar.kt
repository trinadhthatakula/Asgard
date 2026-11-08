package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Image-loader-independent avatar. Image content overlays the fallback, so asynchronous image
 * content can remain empty until loaded. Null [contentDescription] means decorative; an
 * interactive avatar requires a name. Children are decorative to avoid duplicate announcements.
 */
@Composable
fun AsgardAvatar(
    modifier: Modifier = Modifier,
    initials: String = "",
    fallbackIcon: ImageVector? = null,
    contentDescription: String? = null,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    image: (@Composable BoxScope.() -> Unit)? = null,
) {
    require(size > 0.dp)
    require(onClick == null || !contentDescription.isNullOrBlank()) { "Clickable avatars require an accessible name" }
    val named = if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier
    Box(
        modifier.size(size).clip(shape).background(containerColor).then(named)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.fillMaxSize().clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
            if (initials.isNotBlank()) Text(initials, color = contentColor, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            else fallbackIcon?.let { Icon(it, null, tint = contentColor, modifier = Modifier.size(size / 2)) }
            image?.invoke(this)
        }
    }
}
