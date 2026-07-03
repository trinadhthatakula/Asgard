package com.valhalla.asgard.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A thin wrapper over M3 [AlertDialog] for the common confirm/dismiss dialog: a [title], optional
 * body [text] (or a custom [content] slot), a confirm button, and an optional dismiss button.
 * Styling comes from the ambient MaterialTheme.
 *
 * @param onDismissRequest called when the dialog is dismissed (scrim tap / back).
 * @param title the dialog title.
 * @param confirmText the confirm button label.
 * @param onConfirm invoked when the confirm button is tapped.
 * @param modifier the [Modifier] applied to the dialog.
 * @param text optional body text (ignored when [content] is supplied).
 * @param dismissText optional dismiss button label; when null, no dismiss button is shown.
 * @param icon optional icon shown above the title.
 * @param content optional custom body content (replaces [text]).
 */
@Composable
fun AsgardDialogScaffold(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    dismissText: String? = null,
    icon: ImageVector? = null,
    content: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon?.let { vector -> { Icon(imageVector = vector, contentDescription = null) } },
        title = { Text(title) },
        text = content ?: text?.let { body -> { Text(body) } },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText) }
        },
        dismissButton = dismissText?.let { label ->
            { TextButton(onClick = onDismissRequest) { Text(label) } }
        },
    )
}
