package com.cdi.moonphase.data.repository

import com.cdi.moonphase.di.DefaultDispatcher
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.repository.MoonRepository
import com.cdi.moonphase.domain.service.MoonPhaseCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * [MoonRepository] backed entirely by the offline [MoonPhaseCalculator]. No network, no
 * persistence — just deterministic math moved off the main thread.
 */
class MoonRepositoryImpl @Inject constructor(
    private val calculator: MoonPhaseCalculator,
    private val zoneId: ZoneId,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : MoonRepository {

    override suspend fun getMoonInfo(date: LocalDate, location: MoonLocation?): MoonInfo =
        withContext(dispatcher) {
            val computation = calculator.computeForDate(date, zoneId)
            MoonInfo(
                date = date,
                phase = computation.phase,
                illumination = computation.illumination,
                lunarDay = computation.lunarDay,
                nextEvent = computeNextEvent(date),
                location = location,
            )
        }

    override suspend fun getNextEvent(date: LocalDate): LunarEvent =
        withContext(dispatcher) { computeNextEvent(date) }

    private fun computeNextEvent(date: LocalDate): LunarEvent {
        val instant = date.atTime(12, 0).atZone(zoneId).toInstant()
        return calculator.nextCardinalEvent(instant, zoneId)
    }
}
