package com.cdi.moonphase.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.cdi.moonphase.domain.model.ThemeMode

/**
 * App theme. A fixed, brand-specific palette is used (not dynamic color) so the Moon imagery
 * reads consistently across devices. [themeMode] resolves the user's persisted preference,
 * defaulting to the system setting.
 *
 * The semantic [MoonPalette] is the source of truth (see [MoonTheme.colors]); the Material
 * [androidx.compose.material3.ColorScheme] below is derived from it only so stock components
 * (buttons, ripples) inherit matching colors.
 */
@Composable
fun MoonPhaseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (darkTheme) DarkMoonPalette else LightMoonPalette

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.amber,
            onPrimary = palette.onAmber,
            background = palette.background,
            surface = palette.surface,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            onSurfaceVariant = palette.textSecondary,
        )
    } else {
        lightColorScheme(
            primary = palette.amber,
            onPrimary = palette.onAmber,
            background = palette.background,
            surface = palette.surface,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            onSurfaceVariant = palette.textSecondary,
        )
    }

    CompositionLocalProvider(LocalMoonPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content,
        )
    }
}

/** Ergonomic accessor for the design tokens: `MoonTheme.colors.textPrimary`. */
object MoonTheme {
    val colors: MoonPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalMoonPalette.current
}
