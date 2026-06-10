package com.cdi.moonphase.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import com.cdi.moonphase.presentation.designsystem.MoonTheme

/**
 * Snapshots the active palette into a Compose-free [ShareTheme] so the off-screen renderer can
 * draw the export in the current theme. The gradient reuses the brand splash gradient
 * (crepuscular in light, noturno in dark), matching Tela 07.
 */
@Composable
fun rememberShareTheme(): ShareTheme {
    val palette = MoonTheme.colors
    return remember(palette) {
        ShareTheme(
            gradientTop = palette.splashGradient.first().toArgb(),
            gradientBottom = palette.splashGradient.last().toArgb(),
            moonLit = palette.moonLit.toArgb(),
            moonShadow = palette.moonShadow.toArgb(),
            textPrimary = palette.textPrimary.toArgb(),
            textSecondary = palette.textSecondary.toArgb(),
            accent = palette.amber.toArgb(),
        )
    }
}
