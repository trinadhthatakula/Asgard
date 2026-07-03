package com.valhalla.asgard.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    val scheme = if (dark) {
        darkColorScheme(primary = seed, secondary = seed, tertiary = seed)
    } else {
        lightColorScheme(primary = seed, secondary = seed, tertiary = seed)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
