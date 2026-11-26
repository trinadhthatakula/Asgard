package com.valhalla.asgard.demo

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Thor's Asgardian palettes, with its default AMOLED surfaces in dark mode.
private val asgardLight = lightColorScheme(
    primary = Color(0xFF354E15),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF4C662B),
    onPrimaryContainer = Color(0xFFF0FFD7),
    secondary = Color(0xFF55624C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E7CB),
    onSecondaryContainer = Color(0xFF131F0D),
    tertiary = Color(0xFF66355D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8D8EE),
    onTertiaryContainer = Color(0xFF2D112B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onBackground = Color(0xFF191C18),
    surface = Color(0xFFF8FAF3),
    onSurface = Color(0xFF191C18),
    surfaceVariant = Color(0xFFEDEFE8),
    onSurfaceVariant = Color(0xFF43493E),
    outline = Color(0xFF74796D),
    outlineVariant = Color(0xFFC3C8BC),
    background = Color(0xFFF8FAF3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F4ED),
    surfaceContainer = Color(0xFFEDEFE8),
    surfaceContainerHigh = Color(0xFFE7E9E2),
    surfaceContainerHighest = Color(0xFFE1E3DD),
)

private val asgardDark = darkColorScheme(
    primary = Color(0xFFF0FFD7),
    onPrimary = Color(0xFF4C672C),
    primaryContainer = Color(0xFFD5F6AB),
    onPrimaryContainer = Color(0xFF445E25),
    secondary = Color(0xFFC7C4DD),
    onSecondary = Color(0xFF3F3E52),
    secondaryContainer = Color(0xFF242436),
    onSecondaryContainer = Color(0xFFA4A1B9),
    tertiary = Color(0xFFC8BFFF),
    onTertiary = Color(0xFF3F3386),
    tertiaryContainer = Color(0xFFBAAFFF),
    onTertiaryContainer = Color(0xFF35287C),
    error = Color(0xFFFE7453),
    onError = Color(0xFF450900),
    errorContainer = Color(0xFF881F05),
    onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF000000),
    onSurfaceVariant = Color(0xFFABABAB),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF484848),
    inverseSurface = Color(0xFFF9F9F9),
    inverseOnSurface = Color(0xFF555555),
    inversePrimary = Color(0xFF4C672C),
    surfaceTint = Color(0xFFF0FFD7),
    background = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF131313),
    surfaceContainer = Color(0xFF191919),
    surfaceContainerHigh = Color(0xFF1F1F1F),
    surfaceContainerHighest = Color(0xFF262626),
)

internal enum class GalleryPalette(val label: String, val accent: Color) {
    Asgard("Asgard", Color(0xFF4C662B)),
    Indigo("Indigo", Color(0xFF5B5BD6)),
    Teal("Teal", Color(0xFF0E7C7B)),
    Amber("Amber", Color(0xFFB26A00)),
    Rose("Rose", Color(0xFFB3315F)),
}

private val alternatePalettes = GalleryPalette.entries.filter { it != GalleryPalette.Asgard }

@Composable
internal fun DemoTheme(dark: Boolean, palette: GalleryPalette, content: @Composable () -> Unit) {
    val base = if (dark) asgardDark else asgardLight
    val scheme = if (palette == GalleryPalette.Asgard) base else {
        // Retain the demo's rotating accent trio so multi-series samples keep distinct colors.
        val index = alternatePalettes.indexOf(palette)
        val primary = palette.accent
        val secondary = alternatePalettes[(index + 1) % alternatePalettes.size].accent
        val tertiary = alternatePalettes[(index + 2) % alternatePalettes.size].accent
        fun accent(seed: Color) = if (dark) lerp(seed, Color.White, .65f) else lerp(seed, Color.Black, .20f)
        fun container(seed: Color) = if (dark) lerp(seed, Color.Black, .55f) else lerp(seed, Color.White, .82f)
        val onAccent = if (dark) Color.Black else Color.White
        val onContainer = if (dark) Color.White else Color.Black
        base.copy(
            primary = accent(primary), onPrimary = onAccent,
            primaryContainer = container(primary), onPrimaryContainer = onContainer,
            secondary = accent(secondary), onSecondary = onAccent,
            secondaryContainer = container(secondary), onSecondaryContainer = onContainer,
            tertiary = accent(tertiary), onTertiary = onAccent,
            tertiaryContainer = container(tertiary), onTertiaryContainer = onContainer,
            surfaceTint = accent(primary),
        )
    }
    MaterialExpressiveTheme(colorScheme = scheme, typography = asgardTypography(), content = content)
}
