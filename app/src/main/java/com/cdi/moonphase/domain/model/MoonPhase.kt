package com.cdi.moonphase.domain.model

/**
 * A point in the lunation: which of the eight phases the Moon is in, and whether it is
 * currently waxing (illumination increasing) or waning (decreasing).
 */
data class MoonPhase(
    val type: PhaseType,
    val isWaxing: Boolean,
)
