package com.cdi.moonphase.domain.model

import kotlin.math.floor

/**
 * The Moon's age within the current synodic month — the elapsed time, in days, since the
 * last New Moon. Ranges over `[0.0, SYNODIC_MONTH_DAYS)`.
 */
@JvmInline
value class LunarDay(val ageDays: Double) {
    init {
        require(ageDays >= 0.0) { "Lunar age cannot be negative, was $ageDays" }
    }

    /** The 1-based calendar-style "day of the moon" (1..30), as commonly shown to users. */
    val dayOfCycle: Int get() = floor(ageDays).toInt() + 1

    companion object {
        /** Mean length of the synodic month (New Moon to New Moon), in days. */
        const val SYNODIC_MONTH_DAYS = 29.530588853
    }
}
