package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.data.repository.MoonPhaseCalculator
import com.cdi.moonphase.domain.model.MoonPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId

class GetCurrentMoonPhaseUseCase(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    suspend operator fun invoke(now: LocalDateTime = LocalDateTime.now(zoneId)): MoonPhase = withContext(Dispatchers.Default) {
        val result = MoonPhaseCalculator.fromLocalDateTime(now, zoneId)
        val fraction = result.fraction
        val index = when {
            fraction < 0.0625 -> 0 // New Moon
            fraction < 0.1875 -> 1 // Waxing Crescent
            fraction < 0.3125 -> 2 // First Quarter
            fraction < 0.4375 -> 3 // Waxing Gibbous
            fraction < 0.5625 -> 4 // Full Moon
            fraction < 0.6875 -> 5 // Waning Gibbous
            fraction < 0.8125 -> 6 // Last Quarter
            fraction < 0.9375 -> 7 // Waning Crescent
            else -> 0
        }
        val isWaxing = fraction < 0.5
        MoonPhase(
            index = index,
            name = result.phaseName,
            isWaxing = isWaxing,
            illumination = result.illumination.toFloat(),
            timestamp = now
        )
    }
}
