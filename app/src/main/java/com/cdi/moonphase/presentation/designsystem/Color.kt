package com.cdi.moonphase.presentation.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The full set of semantic design tokens, taken verbatim from the technical specs
 * (REQ-01/02/03). Material's [androidx.compose.material3.ColorScheme] does not have enough
 * named slots for the moon imagery (lit/shadow/halo) or the three-tier text ramp, so the
 * tokens live here in a dedicated palette exposed through [LocalMoonPalette]. A minimal
 * Material scheme is still derived from these in [MoonPhaseTheme] so stock components inherit
 * sensible colors.
 */
@Immutable
data class MoonPalette(
    val isDark: Boolean,
    /** Screen background. */
    val background: Color,
    /** Cards and pills. */
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    /** Lit fill of the Moon disk. */
    val moonLit: Color,
    /** Shadowed side of the Moon (the terminator region). */
    val moonShadow: Color,
    /** Disk outline — black 6% in light; transparent in dark (the halo carries the edge). */
    val moonOutline: Color,
    /** Base color of the radial halo around the Moon. */
    val halo: Color,
    /** Amber accent used for interactive text/icons/buttons; AA-safe on its background. */
    val amber: Color,
    /** Foreground used on top of an [amber] fill (e.g. the primary button label). */
    val onAmber: Color,
    /** Decorative gradient behind the brand/splash surfaces. */
    val splashGradient: List<Color>,
    /** Whether [splashGradient] should be painted radially (dark) or linearly (light). */
    val splashRadial: Boolean,
)

val LightMoonPalette = MoonPalette(
    isDark = false,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF4F2EC),
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF5F5E5A),
    textTertiary = Color(0xFF888780),
    moonLit = Color(0xFFF4D58A),
    moonShadow = Color(0xFFEDEAE1),
    moonOutline = Color(0x0F000000), // black 6%
    halo = Color(0xFFBA7517),
    amber = Color(0xFFBA7517), // amber-600, AA on light surfaces
    onAmber = Color(0xFFFFFFFF),
    splashGradient = listOf(Color(0xFFDCE7F2), Color(0xFFEFE7DB)),
    splashRadial = false,
)

val DarkMoonPalette = MoonPalette(
    isDark = true,
    background = Color(0xFF0E1422),
    surface = Color(0x0DFFFFFF), // rgba(255,255,255,0.05)
    textPrimary = Color(0xFFF5F6FA),
    textSecondary = Color(0xFF9AA3B5),
    textTertiary = Color(0xFF6E7790),
    moonLit = Color(0xFFF2E3B3),
    moonShadow = Color(0xFF2A3142),
    moonOutline = Color.Transparent,
    halo = Color(0xFFF2E3B3),
    amber = Color(0xFFF2E3B3),
    onAmber = Color(0xFF1A1A1A),
    splashGradient = listOf(Color(0xFF1B2540), Color(0xFF0E1422)),
    splashRadial = true,
)

/** The active palette. Provided by [MoonPhaseTheme]; read anywhere via [MoonTheme.colors]. */
val LocalMoonPalette = staticCompositionLocalOf { LightMoonPalette }
