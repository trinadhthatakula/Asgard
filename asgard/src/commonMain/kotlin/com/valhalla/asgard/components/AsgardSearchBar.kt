package com.valhalla.asgard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction

/**
 * A single-line search text field with an optional leading [leadingIcon] and an auto-managed
 * clear button (shown when [query] is non-empty and [clearIcon] is provided). Styling comes from
 * the ambient MaterialTheme; icons are supplied by the consumer. Pass [shape]/[colors] to match a
 * host's field styling.
 *
 * @param query the current search text.
 * @param onQueryChange invoked as the text changes.
 * @param modifier the [Modifier] applied to the field; defaults to full width (override to size it).
 * @param placeholder the hint shown when [query] is empty.
 * @param leadingIcon optional leading (search) icon.
 * @param clearIcon optional trailing clear icon; shown only when [query] is non-empty.
 * @param onSearch optional IME "search" action handler.
 * @param shape the field shape.
 * @param colors the field colors.
 */
@Composable
fun AsgardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String = "Search",
    leadingIcon: ImageVector? = null,
    clearIcon: ImageVector? = null,
    onSearch: (() -> Unit)? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = shape,
        colors = colors,
        leadingIcon = leadingIcon?.let { vector ->
            { Icon(imageVector = vector, contentDescription = null) }
        },
        trailingIcon = if (query.isNotEmpty() && clearIcon != null) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = clearIcon, contentDescription = "Clear")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Default,
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
    )
}
