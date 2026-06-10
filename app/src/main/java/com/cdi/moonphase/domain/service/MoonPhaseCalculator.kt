package com.cdi.moonphase.domain.service

import com.cdi.moonphase.domain.model.IlluminationFraction
import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonPhase
import com.cdi.moonphase.domain.model.PhaseType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.cos
import kotlin.math.floor

/**
 * Pure, stateless service that computes the Moon's phase, age and illumination for any
 * instant, and projects upcoming cardinal phases.
 *
 * This is the correctness core of the app and the reason the architecture pays for itself:
 * it depends only on `kotlin.math` and `java.time`, never on Android, so it is exhaustively
 * unit-testable on the JVM against known ephemeris dates.
 *
 * The model is the classic mean-synodic approximation: phases are derived from the elapsed
 * fraction of the mean lunation since a known reference New Moon. It is accurate to within a
 * few hours — more than enough for a daily "what phase is it" app, and entirely offline.
 */
class MoonPhaseCalculator {

    /** A fully resolved computation for a single instant. */
    data class Computation(
        val phase: MoonPhase,
        val illumination: IlluminationFraction,
        val lunarDay: LunarDay,
        /** Position within the lunation, in `[0.0, 1.0)`: 0 = New, 0.5 = Full. */
        val cycleFraction: Double,
    )

    /**
     * Compute the Moon's state at [instant].
     */
    fun computeAt(instant: Instant): Computation {
        val cycleFraction = cycleFractionAt(instant)
        val ageDays = cycleFraction * LunarDay.SYNODIC_MONTH_DAYS

        // Illumination follows the cosine of the phase angle: 0 at New, 1 at Full.
        val illumination = 0.5 * (1.0 - cos(2.0 * Math.PI * cycleFraction))

        return Computation(
            phase = MoonPhase(
                type = phaseTypeFor(cycleFraction),
                isWaxing = cycleFraction < 0.5,
            ),
            illumination = IlluminationFraction(illumination.coerceIn(0.0, 1.0)),
            lunarDay = LunarDay(ageDays),
            cycleFraction = cycleFraction,
        )
    }

    /**
     * Convenience overload computing the state at the start of a calendar [date] in [zone].
     * Midday is used rather than midnight so the snapshot best represents the whole day.
     */
    fun computeForDate(date: LocalDate, zone: ZoneId): Computation =
        computeAt(date.atTime(12, 0).atZone(zone).toInstant())

    /**
     * The next cardinal phase (New, First Quarter, Full or Last Quarter) strictly after
     * [from], in [zone]. Returns the event's calendar date.
     */
    fun nextCardinalEvent(from: Instant, zone: ZoneId): LunarEvent {
        val current = cycleFractionAt(from)

        // Cardinal points within the cycle, plus 1.0 to represent the next New Moon.
        val cardinals = listOf(0.0, 0.25, 0.5, 0.75, 1.0)
        // A small margin avoids re-reporting the cardinal we are essentially sitting on.
        val margin = 0.001
        val targetFraction = cardinals.first { it > current + margin }

        val daysUntil = (targetFraction - current) * LunarDay.SYNODIC_MONTH_DAYS
        val eventInstant = from.plusSeconds((daysUntil * SECONDS_PER_DAY).toLong())
        val eventDate = eventInstant.atZone(zone).toLocalDate()

        val phase = when (targetFraction) {
            0.25 -> PhaseType.FIRST_QUARTER
            0.5 -> PhaseType.FULL
            0.75 -> PhaseType.LAST_QUARTER
            else -> PhaseType.NEW // 0.0 or 1.0
        }
        return LunarEvent(phase, eventDate)
    }

    /**
     * The next [count] cardinal phases strictly after [from], in chronological order. Because the
     * cardinals cycle in a fixed order (First Quarter → Full → Last Quarter → New), asking for the
     * next four yields exactly one upcoming occurrence of each — what the "próximas fases" panel
     * lists. Each step advances a cursor just past the event it found, so the following call lands
     * on the next cardinal rather than re-reporting the same one.
     */
    fun upcomingCardinalEvents(from: Instant, zone: ZoneId, count: Int = 4): List<LunarEvent> {
        require(count >= 0) { "count must be non-negative, was $count" }
        val events = ArrayList<LunarEvent>(count)
        var cursor = from
        repeat(count) {
            val event = nextCardinalEvent(cursor, zone)
            events += event
            // Advance one day past the event's date: cardinals are ~7.4 days apart, so this never
            // skips the next one, and it reliably moves the cursor beyond the one just found.
            cursor = event.date.plusDays(1).atStartOfDay(zone).toInstant()
        }
        return events
    }

    /** Position within the current lunation in `[0.0, 1.0)`. */
    private fun cycleFractionAt(instant: Instant): Double {
        val daysSinceReference =
            (instant.toEpochMilli() - REFERENCE_NEW_MOON.toEpochMilli()) / MILLIS_PER_DAY
        val cycles = daysSinceReference / LunarDay.SYNODIC_MONTH_DAYS
        // Normalize into [0, 1) even for dates before the reference New Moon.
        return cycles - floor(cycles)
    }

    private fun phaseTypeFor(fraction: Double): PhaseType = when {
        fraction < 0.0625 -> PhaseType.NEW
        fraction < 0.1875 -> PhaseType.WAXING_CRESCENT
        fraction < 0.3125 -> PhaseType.FIRST_QUARTER
        fraction < 0.4375 -> PhaseType.WAXING_GIBBOUS
        fraction < 0.5625 -> PhaseType.FULL
        fraction < 0.6875 -> PhaseType.WANING_GIBBOUS
        fraction < 0.8125 -> PhaseType.LAST_QUARTER
        fraction < 0.9375 -> PhaseType.WANING_CRESCENT
        else -> PhaseType.NEW // wraps back toward the next New Moon
    }

    companion object {
        /** Reference New Moon: 2000-01-06 18:14 UTC (a well-established epoch). */
        private val REFERENCE_NEW_MOON: Instant =
            LocalDate.of(2000, 1, 6).atTime(18, 14).toInstant(ZoneOffset.UTC)

        private const val MILLIS_PER_DAY = 86_400_000.0
        private const val SECONDS_PER_DAY = 86_400.0
    }
}
