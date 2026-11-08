package com.valhalla.asgard.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Seed colors offered in the demo toolbar to prove components inherit the host theme. */
val demoSeeds: List<Pair<String, Color>> = listOf(
    "Indigo" to Color(0xFF5B5BD6),
    "Teal" to Color(0xFF0E7C7B),
    "Amber" to Color(0xFFB26A00),
    "Rose" to Color(0xFFB3315F),
)

/**
 * Wraps [content] in a [MaterialTheme] built from [seed] in light or [dark] mode. The seed drives
 * the primary/secondary/tertiary roles so re-tinting is visible across the components.
 */
@Composable
fun DemoTheme(dark: Boolean, seed: Color, content: @Composable () -> Unit) {
    val index = demoSeeds.indexOfFirst { it.second == seed }.coerceAtLeast(0)
    val secondary = demoSeeds[(index + 1) % demoSeeds.size].second
    val tertiary = demoSeeds[(index + 2) % demoSeeds.size].second
    val scheme = if (dark) {
        darkColorScheme(primary = lerp(seed, Color.White, .4f), onPrimary = Color.Black,
            secondary = lerp(secondary, Color.White, .4f), onSecondary = Color.Black,
            tertiary = lerp(tertiary, Color.White, .4f), onTertiary = Color.Black)
    } else {
        lightColorScheme(primary = seed, onPrimary = Color.White, secondary = secondary,
            onSecondary = Color.White, tertiary = tertiary, onTertiary = Color.White)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
