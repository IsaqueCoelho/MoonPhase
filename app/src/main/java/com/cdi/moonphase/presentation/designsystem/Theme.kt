package com.cdi.moonphase.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.cdi.moonphase.domain.model.ThemeMode

private val DarkColors = darkColorScheme(
    primary = MoonColors.NightHalo,
    background = MoonColors.NightBackground,
    surface = MoonColors.NightSurface,
    onBackground = MoonColors.NightOnSurface,
    onSurface = MoonColors.NightOnSurface,
    onSurfaceVariant = MoonColors.NightOnSurfaceMuted,
)

private val LightColors = lightColorScheme(
    primary = MoonColors.DayHalo,
    background = MoonColors.DayBackground,
    surface = MoonColors.DaySurface,
    onBackground = MoonColors.DayOnSurface,
    onSurface = MoonColors.DayOnSurface,
    onSurfaceVariant = MoonColors.DayOnSurfaceMuted,
)

/**
 * App theme. A custom palette is used (not dynamic color) so the moon imagery reads
 * consistently across devices. [themeMode] resolves the user's persisted preference.
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
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
