package com.valhalla.asgard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction

/**
 * A single-line search text field with an optional leading [leadingIcon] and an auto-managed
 * clear button (shown when [query] is non-empty and [clearIcon] is provided). Styling comes from
 * the ambient MaterialTheme; icons are supplied by the consumer.
 *
 * @param query the current search text.
 * @param onQueryChange invoked as the text changes.
 * @param modifier the [Modifier] applied to the field.
 * @param placeholder the hint shown when [query] is empty.
 * @param leadingIcon optional leading (search) icon.
 * @param clearIcon optional trailing clear icon; shown only when [query] is non-empty.
 * @param onSearch optional IME "search" action handler.
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
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
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
