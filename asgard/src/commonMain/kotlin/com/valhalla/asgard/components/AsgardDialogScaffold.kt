package com.valhalla.asgard.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

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
 * @param shape optional override for the dialog container shape; when null, the M3 default is used.
 * @param containerColor optional override for the dialog container color; when null, the M3 default is used.
 * @param titleContentColor optional override for the title content color; when null, the M3 default is used.
 * @param textContentColor optional override for the body text content color; when null, the M3 default is used.
 * @param iconContentColor optional override for the icon content color; when null, the M3 default is used.
 * @param tonalElevation optional override for the dialog tonal elevation; when null, the M3 default is used.
 * @param properties platform [DialogProperties] controlling dismiss/window behavior.
 * @param titleStyle optional [TextStyle] for the title; when null, the ambient dialog title style is used.
 * @param textStyle optional [TextStyle] for the body [text]; when null, the ambient dialog text style is used.
 * @param titleMaxLines maximum lines for the title text before truncation.
 * @param textMaxLines maximum lines for the body text before truncation.
 * @param confirmEnabled whether the confirm button is enabled.
 * @param confirmColors optional [ButtonColors] for the confirm button (e.g. destructive styling);
 *   when null, the M3 text-button colors are used.
 * @param iconContentDescription optional content description for the [icon]; when null, the icon is decorative.
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
    shape: Shape? = null,
    containerColor: Color? = null,
    titleContentColor: Color? = null,
    textContentColor: Color? = null,
    iconContentColor: Color? = null,
    tonalElevation: Dp? = null,
    properties: DialogProperties = DialogProperties(),
    titleStyle: TextStyle? = null,
    textStyle: TextStyle? = null,
    titleMaxLines: Int = Int.MAX_VALUE,
    textMaxLines: Int = Int.MAX_VALUE,
    confirmEnabled: Boolean = true,
    confirmColors: ButtonColors? = null,
    iconContentDescription: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = icon?.let { vector ->
            { Icon(imageVector = vector, contentDescription = iconContentDescription) }
        },
        title = {
            Text(
                text = title,
                style = titleStyle ?: LocalTextStyle.current,
                maxLines = titleMaxLines,
            )
        },
        text = content ?: text?.let { body ->
            {
                Text(
                    text = body,
                    style = textStyle ?: LocalTextStyle.current,
                    maxLines = textMaxLines,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = confirmColors ?: ButtonDefaults.textButtonColors(),
            ) { Text(confirmText) }
        },
        dismissButton = dismissText?.let { label ->
            { TextButton(onClick = onDismissRequest) { Text(label) } }
        },
        shape = shape ?: AlertDialogDefaults.shape,
        containerColor = containerColor ?: AlertDialogDefaults.containerColor,
        iconContentColor = iconContentColor ?: AlertDialogDefaults.iconContentColor,
        titleContentColor = titleContentColor ?: AlertDialogDefaults.titleContentColor,
        textContentColor = textContentColor ?: AlertDialogDefaults.textContentColor,
        tonalElevation = tonalElevation ?: AlertDialogDefaults.TonalElevation,
        properties = properties,
    )
}
