package com.valhalla.asgard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction

/**
 * A single-line search text field with an optional leading [leadingIcon] and an auto-managed
 * clear button (shown when [query] is non-empty and [clearIcon] is provided). Styling comes from
 * the ambient MaterialTheme; icons are supplied by the consumer. Pass [shape]/[colors] to match a
 * host's field styling.
 *
 * @param query the current search text.
 * @param onQueryChange invoked as the text changes.
 * @param modifier the [Modifier] applied to the field; full width is always applied first so a
 *   passed modifier no longer silently drops the default full-width sizing.
 * @param placeholder the hint shown when [query] is empty.
 * @param leadingIcon optional leading (search) icon.
 * @param clearIcon optional trailing clear icon; shown only when [query] is non-empty.
 * @param onSearch optional IME "search" action handler.
 * @param shape the field shape.
 * @param colors the field colors.
 * @param enabled whether the field is enabled and interactive.
 * @param isError whether the field is in an error state.
 * @param supportingText optional supporting text shown below the field.
 * @param keyboardOptions the software keyboard options; defaults to an IME "search" action.
 * @param focusRequester optional [FocusRequester] wired to the field when set.
 * @param leadingContentDescription accessibility description for [leadingIcon].
 * @param clearContentDescription accessibility description for the clear button.
 * @param placeholderStyle optional [TextStyle] for the [placeholder]; falls back to the ambient
 *   text style when null.
 */
@Composable
fun AsgardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    leadingIcon: ImageVector? = null,
    clearIcon: ImageVector? = null,
    onSearch: (() -> Unit)? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    focusRequester: FocusRequester? = null,
    leadingContentDescription: String? = null,
    clearContentDescription: String = "Clear",
    placeholderStyle: TextStyle? = null,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier,
            )
            .then(modifier),
        enabled = enabled,
        isError = isError,
        placeholder = {
            Text(placeholder, style = placeholderStyle ?: LocalTextStyle.current)
        },
        supportingText = supportingText,
        singleLine = true,
        shape = shape,
        colors = colors,
        leadingIcon = leadingIcon?.let { vector ->
            { Icon(imageVector = vector, contentDescription = leadingContentDescription) }
        },
        trailingIcon = if (query.isNotEmpty() && clearIcon != null) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = clearIcon, contentDescription = clearContentDescription)
                }
            }
        } else {
            null
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
    )
}
