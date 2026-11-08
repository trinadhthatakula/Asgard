package com.valhalla.asgard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Bounded modal layout with fixed header/footer and an optionally scrolling body. The sheet
 * owns [contentWindowInsets]; hosts that already apply insets should supply zero insets.
 * For lazy content set [scrollBody] to false to avoid nesting vertical scroll containers.
 * Callers own whether the sheet is present and all permission, purchase and app-lock policy.
 */
@Composable
fun AsgardSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    sheetMaxWidth: Dp = 640.dp,
    maxContentHeight: Dp = 640.dp,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable RowScope.() -> Unit)? = null,
    scrollBody: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    require(sheetMaxWidth > 0.dp && maxContentHeight > 0.dp)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth, shape = shape, containerColor = containerColor,
        contentColor = contentColor, contentWindowInsets = contentWindowInsets,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().heightIn(max = minOf(maxHeight, maxContentHeight))) {
                header?.let { Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) { it() } }
                Column(
                    Modifier.weight(1f, fill = false).fillMaxWidth()
                        .then(if (scrollBody) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        .padding(horizontal = 24.dp),
                    content = content,
                )
                footer?.let {
                    FlowRow(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp), content = it,
                    )
                }
            }
        }
    }
}

/** Hide before removing a sheet from composition. A cancelled hide does not dismiss it. */
@Composable
fun rememberAsgardSheetDismiss(sheetState: SheetState, onDismissed: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val dismissed by rememberUpdatedState(onDismissed)
    var hiding by remember(sheetState) { mutableStateOf(false) }
    return remember(sheetState, scope) {
        {
            if (!hiding) {
                hiding = true
                scope.launch {
                    try {
                        sheetState.hide()
                        if (!sheetState.isVisible) dismissed()
                    } finally { hiding = false }
                }
            }
        }
    }
}

/** Action sheet with a scrollable message and a wrapping action footer. */
@Composable
fun AsgardActionSheet(
    onDismissRequest: () -> Unit,
    title: String,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
    content: @Composable ColumnScope.() -> Unit,
) {
    AsgardSheet(
        onDismissRequest, modifier, sheetState,
        header = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        footer = actions, content = content,
    )
}
