package com.valhalla.asgard.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.AsgardContainer
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeSourceRetention
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource

/** A backdrop shared by surfaces on one screen. Mark the scrolling content with [asgardGlassSource]. */
@Stable
class AsgardGlassSource internal constructor(internal val state: HazeState?) {
    private val container = FrostedContainer(state)

    /** Opt this instance into frost. Passing false selects its ordinary Material container. */
    fun frosted(enabled: Boolean = true): AsgardContainer =
        if (enabled) container else AsgardContainer.Material
}

/**
 * Creates a screen-local source. Set [enabled] to false for an app-level reduced-transparency
 * preference. Disabled or unsupported sources install neither capture nor blur modifiers.
 */
@Composable
fun rememberAsgardGlassSource(enabled: Boolean = true): AsgardGlassSource {
    val available = enabled && platformAllowsFrost()
    return remember(available) { AsgardGlassSource(if (available) HazeState() else null) }
}

@Composable
internal expect fun platformAllowsFrost(): Boolean

/** Records this subtree for explicitly selected sibling overlays. Place it before background drawing. */
fun Modifier.asgardGlassSource(source: AsgardGlassSource): Modifier =
    source.state?.let { hazeSource(it) } ?: this

/**
 * Gives this element a frosted background while keeping its foreground sharp. Apply before inner
 * padding. For a component that draws its own container, set that fill to transparent and keep
 * its content and state colors explicit. [shape] clips the treatment and the component's content.
 *
 * Per-item [enabled] = false leaves the modifier untouched. When glass is requested but the source
 * is disabled, missing or unsupported, the element uses its themed [containerColor] background.
 */
@Composable
fun Modifier.asgardGlass(
    source: AsgardGlassSource,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
): Modifier {
    if (!enabled) return this
    return clip(shape).then(source.frosted().backgroundModifier(containerColor))
}

private class FrostedContainer(state: HazeState?) : AsgardContainer {
    private val input = state?.let { HazeInput.Sources(it, retention = HazeSourceRetention.ClearWhenUnavailable) }

    @Composable
    override fun backgroundModifier(containerColor: Color): Modifier {
        val surface = MaterialTheme.colorScheme.surface.copy(alpha = 1f)
        val base = containerColor.takeOrElse { surface }.compositeOver(surface)
        val background = Modifier.background(base)
        if (input == null) return background
        val style = remember(base) {
            HazeBlurStyle {
                backgroundColor(base)
                blurRadius(18.dp)
                noiseFactor(0f)
                colorEffects(listOf(HazeColorEffect.tint(base.copy(alpha = 0.78f))))
                fallbackColorEffect(HazeColorEffect.tint(base))
            }
        }
        return background.hazeBlur(input, style)
    }
}

/** A shaped glass surface for caller-owned content, with the same opt-in and fallback as Asgard controls. */
@Composable
fun AsgardGlassSurface(
    source: AsgardGlassSource,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(modifier, shape = shape, color = containerColor, contentColor = contentColor, shadowElevation = shadowElevation) {
        Box(source.frosted(enabled).backgroundModifier(containerColor), propagateMinConstraints = true) {
            content()
        }
    }
}
