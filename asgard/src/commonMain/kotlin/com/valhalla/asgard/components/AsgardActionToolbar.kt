package com.valhalla.asgard.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Consumers decide availability and permissions; labels are localized at the call site. */
data class AsgardToolbarAction(
    val id: String, val label: String, val icon: ImageVector, val onClick: () -> Unit,
    val enabled: Boolean = true, val selected: Boolean = false, val destructive: Boolean = false,
)

/** A compact scrolling toolbar, with an optional fixed selection-count/cancel slot. */
@Composable
fun AsgardActionToolbar(
    actions: List<AsgardToolbarAction>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showLabels: Boolean = false,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 6.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    require(actions.map { it.id }.distinct().size == actions.size) { "Toolbar action IDs must be unique" }
    Surface(modifier, shape = shape, color = containerColor, contentColor = contentColor, shadowElevation = elevation) {
        Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            leading?.invoke()
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                actions.forEach { action ->
                    val color = when {
                        action.destructive -> MaterialTheme.colorScheme.error
                        action.selected -> MaterialTheme.colorScheme.primary
                        else -> contentColor
                    }
                    val semantics = Modifier.semantics {
                        contentDescription = action.label
                        selected = action.selected
                    }
                    if (showLabels) {
                        TextButton(onClick = action.onClick, enabled = enabled && action.enabled,
                            modifier = semantics, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
                            Icon(action.icon, null)
                            Spacer(Modifier.width(8.dp))
                            Text(action.label)
                        }
                    } else {
                        IconButton(onClick = action.onClick, enabled = enabled && action.enabled,
                            modifier = semantics) { Icon(action.icon, null, tint = if (enabled && action.enabled) color else color.copy(alpha = .38f)) }
                    }
                }
            }
        }
    }
}
