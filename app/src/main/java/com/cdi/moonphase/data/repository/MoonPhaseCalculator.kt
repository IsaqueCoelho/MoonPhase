package com.cdi.moonphase.data.repository

import android.util.Log
import java.time.*
import kotlin.math.*

object MoonPhaseCalculator {
    // Mean length of synodic month (New Moon to New Moon)
    private const val SYNODIC_MONTH = 29.530588853

    // Reference New Moon: 2000-01-06 18:14 UT (JDN ≈ 2451550.1)
    private const val REFERENCE_NEW_MOON_JD = 2451550.1

    data class Result(
        val phaseName: String,      // e.g., "Waxing Gibbous"
        val ageDays: Double,        // lunar age in days [0..29.53)
        val fraction: Double,       // phase fraction [0..1) where 0=New, 0.5=Full
        val illumination: Double    // 0..1 (0%..100%)
    )

    fun todayPhase(now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): Result {
        val jd = toJulianDay(now.withZoneSameInstant(ZoneOffset.UTC))
        return fromJulianDay(jd)
    }

    fun fromLocalDateTime(local: LocalDateTime, zone: ZoneId): Result {
        val utc = local.atZone(zone).withZoneSameInstant(ZoneOffset.UTC)
        val jd = toJulianDay(utc)
        Log.e("MoonPhase", "fromLocalDateTime: jd=$jd utc=$utc")
        return fromJulianDay(jd)
    }

    private fun fromJulianDay(jdUtc: Double): Result {
        val daysSinceNew = jdUtc - REFERENCE_NEW_MOON_JD
        val cycles = daysSinceNew / SYNODIC_MONTH
        val fraction = cycles - floor(cycles) // [0..1)
        val age = fraction * SYNODIC_MONTH    // age in days

        // Phase angle (Sun-Earth-Moon) for illumination
        val phaseAngle = 2.0 * Math.PI * fraction
        val illumination = 0.5 * (1 - cos(phaseAngle)) // 0=New, 1=Full

        val name = phaseName(fraction)
        return Result(name, age, fraction, illumination)
    }

    // Julian Day Number for UTC datetime (astronomical formula)
    private fun toJulianDay(utc: ZonedDateTime): Double {
        val y0 = utc.year
        var y = y0
        var m = utc.monthValue
        val d = utc.dayOfMonth
        val hr = utc.hour
        val min = utc.minute
        val sec = utc.second
        val fracDay = (hr + min / 60.0 + sec / 3600.0) / 24.0

        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)

        val jd0 = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + b - 1524.5
        return jd0 + fracDay
    }

    private fun phaseName(fraction: Double): String {
        // Define small tolerance for exact quarter boundaries
        val eps = 1e-3
        return when {
            isNear(fraction, 0.0, eps) || fraction > 1.0 - eps -> "New Moon"
            fraction < 0.25 - eps -> "Waxing Crescent"
            isNear(fraction, 0.25, eps) -> "First Quarter"
            fraction < 0.50 - eps -> "Waxing Gibbous"
            isNear(fraction, 0.50, eps) -> "Full Moon"
            fraction < 0.75 - eps -> "Waning Gibbous"
            isNear(fraction, 0.75, eps) -> "Last Quarter"
            else -> "Waning Crescent"
        }
    }

    private fun isNear(v: Double, target: Double, eps: Double) = abs(v - target) < eps
}