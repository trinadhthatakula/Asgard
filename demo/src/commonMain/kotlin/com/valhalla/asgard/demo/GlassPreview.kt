package com.valhalla.asgard.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import asgard.demo.generated.resources.Res
import asgard.demo.generated.resources.vatnajokull
import com.valhalla.asgard.glass.AsgardGlassSource
import com.valhalla.asgard.glass.asgardGlass
import com.valhalla.asgard.glass.asgardGlassSource
import com.valhalla.asgard.glass.rememberAsgardGlassSource
import org.jetbrains.compose.resources.painterResource

internal val LocalPreviewGlass = staticCompositionLocalOf { false }

@Composable
internal fun previewFill(ordinary: Color): Color =
    if (LocalPreviewGlass.current) Color.Transparent else ordinary

@Composable
internal fun previewToggleColors(): ToggleButtonColors? =
    if (LocalPreviewGlass.current) ToggleButtonDefaults.colors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        checkedContainerColor = MaterialTheme.colorScheme.primary,
        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
    ) else null

/** Keep the source in the same window as the sample, including inside sheets and dialogs. */
@Composable
internal fun PreviewBackdrop(content: @Composable (AsgardGlassSource) -> Unit) {
    val enabled = LocalPreviewGlass.current
    val source = rememberAsgardGlassSource(enabled)
    Box(propagateMinConstraints = true) {
        if (enabled) {
            Image(
                painterResource(Res.drawable.vatnajokull), contentDescription = null,
                modifier = Modifier.matchParentSize().clip(MaterialTheme.shapes.large).asgardGlassSource(source),
                contentScale = ContentScale.Crop,
            )
        }
        Box(if (enabled) Modifier.padding(10.dp) else Modifier, propagateMinConstraints = true) {
            content(source)
        }
    }
}

@Composable
internal fun GlassSample(
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    inset: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    PreviewBackdrop { source ->
        val enabled = LocalPreviewGlass.current
        Box(
            Modifier.asgardGlass(source, enabled, shape, color).padding(if (enabled) inset else 0.dp),
            propagateMinConstraints = true,
        ) { content() }
    }
}
