package com.cdi.moonphase.presentation.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Design tokens. A night-sky palette: deep indigo backgrounds with a luminous moon and a
 * soft halo accent. Light theme is a daytime sky equivalent.
 */
internal object MoonColors {
    // Dark (night sky)
    val NightBackground = Color(0xFF0B0E16)
    val NightSurface = Color(0xFF141A26)
    val NightMoonLit = Color(0xFFEDEFF5)
    val NightMoonShadow = Color(0xFF0B0E16)
    val NightHalo = Color(0xFF8AA0D8)
    val NightOnSurface = Color(0xFFE6E9F2)
    val NightOnSurfaceMuted = Color(0xFF9AA3B8)

    // Light (day sky)
    val DayBackground = Color(0xFFF3F5FB)
    val DaySurface = Color(0xFFFFFFFF)
    val DayMoonLit = Color(0xFFCBD4E6)
    val DayMoonShadow = Color(0xFF2A2F3A)
    val DayHalo = Color(0xFF5A6B9E)
    val DayOnSurface = Color(0xFF1B1F2A)
    val DayOnSurfaceMuted = Color(0xFF5A6172)
}
