package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel

/** Pixel metrics use Compose's measured content-size estimate, including variable sizes/spans. */
data class AsgardScrollbarMetrics(val offset: Int, val content: Int, val viewport: Int) {
    val range: Float get() = (content.toLong() - viewport.coerceAtLeast(0)).coerceAtLeast(0).toFloat()
    val fraction: Float get() = if (range == 0f) 0f else (offset / range).coerceIn(0f, 1f)
    val viewportFraction: Float get() = if (content <= 0) 1f else (viewport.toFloat() / content).coerceIn(0f, 1f)
}

interface AsgardScrollbarState {
    val metrics: AsgardScrollbarMetrics
    val firstVisibleIndex: Int
    val lastVisibleIndex: Int
    val isScrolling: Boolean
    suspend fun scrollToFraction(fraction: Float)
}

@Composable
fun rememberAsgardScrollbarState(state: LazyListState): AsgardScrollbarState = remember(state) {
    object : AsgardScrollbarState {
        override val metrics get() = state.scrollIndicatorState?.let { AsgardScrollbarMetrics(it.scrollOffset, it.contentSize, it.viewportSize) } ?: AsgardScrollbarMetrics(0, 0, 0)
        override val firstVisibleIndex get() = state.layoutInfo.visibleItemsInfo.minOfOrNull { it.index } ?: 0
        override val lastVisibleIndex get() = state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: 0
        override val isScrolling get() = state.isScrollInProgress
        override suspend fun scrollToFraction(fraction: Float) {
            if (!fraction.isFinite()) return
            require(state.layoutInfo.orientation == androidx.compose.foundation.gestures.Orientation.Vertical) { "Scrollbar requires a vertical list" }
            val before = metrics
            state.scrollBy((fraction.coerceIn(0f, 1f) * before.range - before.offset) * if (state.layoutInfo.reverseLayout) -1f else 1f)
        }
    }
}

@Composable
fun rememberAsgardScrollbarState(state: LazyGridState): AsgardScrollbarState = remember(state) {
    object : AsgardScrollbarState {
        override val metrics get() = state.scrollIndicatorState?.let { AsgardScrollbarMetrics(it.scrollOffset, it.contentSize, it.viewportSize) } ?: AsgardScrollbarMetrics(0, 0, 0)
        override val firstVisibleIndex get() = state.layoutInfo.visibleItemsInfo.minOfOrNull { it.index } ?: 0
        override val lastVisibleIndex get() = state.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: 0
        override val isScrolling get() = state.isScrollInProgress
        override suspend fun scrollToFraction(fraction: Float) {
            if (!fraction.isFinite()) return
            require(state.layoutInfo.orientation == androidx.compose.foundation.gestures.Orientation.Vertical) { "Scrollbar requires a vertical grid" }
            val before = metrics
            state.scrollBy((fraction.coerceIn(0f, 1f) * before.range - before.offset) * if (state.layoutInfo.reverseLayout) -1f else 1f)
        }
    }
}

enum class AsgardScrollbarVisibility { Always, WhileScrolling }

/**
 * Vertical scrollbar occupying a 48dp-wide target at the host's logical end. Reserve a 48dp
 * end gutter beside the list/grid, and give this overlay bounded height. It does not resize the
 * underlying content. Position reflects Compose's content estimate; variable sizes can adjust it.
 * [rangeLabel] receives zero-based visible indices. Supply localized labels from the app.
 */
@Composable
fun AsgardLazyScrollbar(
    state: AsgardScrollbarState,
    modifier: Modifier = Modifier,
    label: String = "Scroll",
    rangeLabel: (Int, Int) -> String = { first, last -> "${first + 1}–${last + 1}" },
    visibility: AsgardScrollbarVisibility = AsgardScrollbarVisibility.Always,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    thumbWidth: Dp = 6.dp,
    minThumbHeight: Dp = 32.dp,
) {
    require(thumbWidth > 0.dp && thumbWidth <= 48.dp && minThumbHeight > 0.dp)
    val requests = remember(state) { Channel<Float>(Channel.CONFLATED) }
    LaunchedEffect(requests) { for (fraction in requests) state.scrollToFraction(fraction) }
    DisposableEffect(requests) { onDispose { requests.close() } }
    var focused by remember(state) { mutableStateOf(false) }
    var dragging by remember(state) { mutableStateOf(false) }
    var dragFraction by remember(state) { mutableFloatStateOf(0f) }
    val metrics = state.metrics
    if (metrics.range <= 0f) return
    // Keep the target in composition for focus/keyboard even when its paint is idle-hidden.
    val painted = visibility == AsgardScrollbarVisibility.Always || state.isScrolling || dragging || focused
    BoxWithConstraints(modifier.width(48.dp).fillMaxHeight()) {
        require(maxHeight != Dp.Infinity) { "Scrollbar needs bounded height" }
        val thumbHeight = maxOf(minThumbHeight, maxHeight * metrics.viewportFraction).coerceAtMost(maxHeight)
        val travel = (maxHeight - thumbHeight).coerceAtLeast(0.dp)
        val density = androidx.compose.ui.platform.LocalDensity.current
        val travelPx = with(density) { travel.toPx() }
        val currentTravelPx by rememberUpdatedState(travelPx)
        val fraction = if (dragging) dragFraction else metrics.fraction
        val interaction = Modifier
            .semantics {
                contentDescription = label
                stateDescription = rangeLabel(state.firstVisibleIndex, state.lastVisibleIndex)
                progressBarRangeInfo = ProgressBarRangeInfo(metrics.fraction, 0f..1f)
                setProgress { target -> if (target.isFinite()) { requests.trySend(target.coerceIn(0f, 1f)); true } else false }
            }
            .onKeyEvent {
                if (it.type != KeyEventType.KeyDown) false else {
                    val next = when (it.key) {
                        Key.DirectionUp -> metrics.fraction - .05f
                        Key.DirectionDown -> metrics.fraction + .05f
                        Key.PageUp -> metrics.fraction - metrics.viewportFraction
                        Key.PageDown -> metrics.fraction + metrics.viewportFraction
                        Key.MoveHome -> 0f
                        Key.MoveEnd -> 1f
                        else -> return@onKeyEvent false
                    }
                    requests.trySend(next.coerceIn(0f, 1f)); true
                }
            }.onFocusChanged { focused = it.isFocused }.focusable()
            .pointerInput(state) {
                detectDragGestures(
                    onDragStart = { dragging = true; dragFraction = state.metrics.fraction },
                    onDragEnd = { dragging = false }, onDragCancel = { dragging = false },
                ) { change, amount ->
                    change.consume()
                    if (currentTravelPx > 0f) {
                        dragFraction = (dragFraction + amount.y / currentTravelPx).coerceIn(0f, 1f)
                        requests.trySend(dragFraction)
                    }
                }
            }
        Box(Modifier.fillMaxSize().then(interaction)) {
            if (painted) {
                Box(Modifier.align(Alignment.CenterEnd).width(thumbWidth).fillMaxHeight().background(trackColor, CircleShape))
                Box(Modifier.align(Alignment.TopEnd).offset(y = travel * fraction).size(thumbWidth, thumbHeight).background(thumbColor, CircleShape))
                if (dragging) Text(rangeLabel(state.firstVisibleIndex, state.lastVisibleIndex),
                    Modifier.align(Alignment.TopEnd).offset(x = (-48).dp, y = travel * fraction).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(8.dp))
            }
        }
    }
}
