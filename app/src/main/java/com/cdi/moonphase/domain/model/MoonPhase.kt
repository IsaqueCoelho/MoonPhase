package com.cdi.moonphase.domain.model

import java.time.LocalDateTime

/**
 * Represents the current moon phase.
 * @param index 0..7 (New, Waxing Crescent, First Quarter, Waxing Gibbous, Full, Waning Gibbous, Last Quarter, Waning Crescent)
 * @param name human readable phase name
 * @param isWaxing true if waxing, false if waning
 * @param illumination illuminated fraction [0.0, 1.0]
 * @param timestamp when this phase reading was computed
 */
data class MoonPhase(
    val index: Int,
    val name: String,
    val isWaxing: Boolean,
    val illumination: Float,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
