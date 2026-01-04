package com.cdi.moonphase.data.repository

import com.cdi.moonphase.domain.model.MoonPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Repository that calculates moon phases locally.
 */
class MoonRepository(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    // Reference new moon (from NASA): 2000-01-06 18:14 UTC
    private val referenceNewMoonUtc = LocalDateTime.of(2000, 1, 6, 18, 14).atZone(ZoneId.of("UTC")).toInstant()

    // Synodic month length in days
    private val synodicMonthDays = 29.530588853

    suspend fun getCurrentMoonPhase(now: LocalDateTime = LocalDateTime.now(zoneId)): MoonPhase = withContext(Dispatchers.Default) {
        val phaseInfo = computePhase(now)
        MoonPhase(
            index = phaseInfo.index,
            name = phaseName(phaseInfo.index),
            isWaxing = phaseInfo.isWaxing,
            illumination = phaseInfo.illumination.toFloat(),
            timestamp = now
        )
    }

    data class PhaseCalc(val index: Int, val isWaxing: Boolean, val illumination: Double, val ageDays: Double)

    private fun computePhase(now: LocalDateTime): PhaseCalc {
        val nowInstant = now.atZone(zoneId).toInstant()
        val daysSince = Duration.between(referenceNewMoonUtc, nowInstant).toMillis() / 86_400_000.0
        val lunations = daysSince / synodicMonthDays
        val frac = ((lunations % 1 + 1) % 1) // 0..1
        val ageDays = frac * synodicMonthDays
        val illumination = 0.5 * (1 - kotlin.math.cos(2 * Math.PI * frac)) // 0..1
        // 8-index mapping by fraction
        val index = when {
            frac < 0.0625 -> 0 // New
            frac < 0.1875 -> 1 // Waxing Crescent
            frac < 0.3125 -> 2 // First Quarter
            frac < 0.4375 -> 3 // Waxing Gibbous
            frac < 0.5625 -> 4 // Full
            frac < 0.6875 -> 5 // Waning Gibbous
            frac < 0.8125 -> 6 // Last Quarter
            frac < 0.9375 -> 7 // Waning Crescent
            else -> 0 // wrap to new
        }
        val isWaxing = frac < 0.5
        return PhaseCalc(index, isWaxing, illumination, ageDays)
    }

    fun phaseName(index: Int): String = when (index) {
        0 -> "New Moon"
        1 -> "Waxing Crescent"
        2 -> "First Quarter"
        3 -> "Waxing Gibbous"
        4 -> "Full Moon"
        5 -> "Waning Gibbous"
        6 -> "Last Quarter"
        7 -> "Waning Crescent"
        else -> "Unknown"
    }

    /**
     * Compute the next date-times for the next `count` major phases (New, First Quarter, Full, Last Quarter)
     * starting after `from`.
     */
    suspend fun getNextMajorPhases(from: LocalDateTime = LocalDateTime.now(zoneId), count: Int = 4): List<Pair<LocalDateTime, Int>> = withContext(Dispatchers.Default) {
        val majorTargets = listOf(0, 2, 4, 6)
        val results = mutableListOf<Pair<LocalDateTime, Int>>()
        var cursor = from
        // step through days until we collect enough phase transitions close to targets
        // We'll look ahead up to two synodic months to be safe
        val maxDays = (synodicMonthDays * 2).roundToInt()
        var daysChecked = 0
        while (results.size < count && daysChecked < maxDays) {
            // binary search within the next 2 days for best match to target phase
            for (target in majorTargets) {
                val match = findNearestPhase(cursor, target)
                if (match.isAfter(from) && results.none { kotlin.math.abs(Duration.between(it.first, match).toHours()) < 12 }) {
                    results.add(match to target)
                    if (results.size == count) break
                }
            }
            cursor = cursor.plusDays(1)
            daysChecked++
        }
        results.sortedBy { it.first }
    }

    private fun findNearestPhase(start: LocalDateTime, targetIndex: Int): LocalDateTime {
        // Simple search: increment by 3 hours to find when index matches and illumination slope around threshold
        var t = start
        var best: LocalDateTime = t
        var bestScore = Double.MAX_VALUE
        repeat(400) { // ~50 days at 3h step
            val p = computePhase(t)
            val score = kotlin.math.abs(normalizeIndex(p.index) - normalizeIndex(targetIndex)) + kotlin.math.abs(p.illumination - targetIllumination(targetIndex))
            if (score < bestScore) {
                bestScore = score
                best = t
            }
            t = t.plusHours(3)
        }
        return best
    }

    private fun normalizeIndex(index: Int) = when (index) { 0,1,2,3,4,5,6,7 -> index.toDouble() else -> 8.0 }
    private fun targetIllumination(index: Int) = when(index) {
        0 -> 0.0
        2 -> 0.5
        4 -> 1.0
        6 -> 0.5
        else -> 0.0
    }
}
