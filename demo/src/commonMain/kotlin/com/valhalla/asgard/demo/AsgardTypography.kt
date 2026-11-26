package com.valhalla.asgard.demo

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import asgard.demo.generated.resources.*
import org.jetbrains.compose.resources.Font

private val baseline = Typography()

/** Thor's Asgard type roles. Static instances give Android and Skia the same weight outlines. */
@Composable
internal fun asgardTypography(): Typography {
    val outfit = FontFamily(
        Font(Res.font.outfit_400, FontWeight.Normal),
        Font(Res.font.outfit_500, FontWeight.Medium),
        Font(Res.font.outfit_600, FontWeight.SemiBold),
        Font(Res.font.outfit_700, FontWeight.Bold),
        Font(Res.font.outfit_800, FontWeight.ExtraBold),
        Font(Res.font.outfit_900, FontWeight.Black),
    )
    val fira = FontFamily(
        Font(Res.font.fira_code_400, FontWeight.Normal),
        Font(Res.font.fira_code_500, FontWeight.Medium),
    )
    return remember(outfit, fira) {
        Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = outfit, fontWeight = FontWeight.Black),
            displayMedium = baseline.displayMedium.copy(fontFamily = outfit, fontWeight = FontWeight.ExtraBold),
            displaySmall = baseline.displaySmall.copy(fontFamily = outfit, fontWeight = FontWeight.Bold),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = outfit, fontWeight = FontWeight.Bold),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = outfit, fontWeight = FontWeight.Bold),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = outfit, fontWeight = FontWeight.Medium),
            titleLarge = baseline.titleLarge.copy(fontFamily = outfit, fontWeight = FontWeight.SemiBold),
            titleMedium = baseline.titleMedium.copy(fontFamily = outfit, fontWeight = FontWeight.Medium),
            titleSmall = baseline.titleSmall.copy(fontFamily = outfit, fontWeight = FontWeight.Normal),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = outfit),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = outfit),
            bodySmall = baseline.bodySmall.copy(fontFamily = outfit),
            labelLarge = baseline.labelLarge.copy(fontFamily = fira, fontWeight = FontWeight.Medium),
            labelMedium = baseline.labelMedium.copy(fontFamily = fira, fontWeight = FontWeight.Normal),
            labelSmall = baseline.labelSmall.copy(fontFamily = fira, fontWeight = FontWeight.Normal),
        )
    }
}
