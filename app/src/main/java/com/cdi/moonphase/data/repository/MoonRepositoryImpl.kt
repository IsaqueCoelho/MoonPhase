package com.cdi.moonphase.data.repository

import com.cdi.moonphase.di.DefaultDispatcher
import com.cdi.moonphase.domain.model.CalendarDay
import com.cdi.moonphase.domain.model.CalendarMonth
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.repository.MoonRepository
import com.cdi.moonphase.domain.service.MoonPhaseCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
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

    override suspend fun getMonth(month: YearMonth): CalendarMonth =
        withContext(dispatcher) {
            val days = (1..month.lengthOfMonth()).map { dayOfMonth ->
                val date = month.atDay(dayOfMonth)
                val computation = calculator.computeForDate(date, zoneId)
                CalendarDay(
                    date = date,
                    phase = computation.phase,
                    illumination = computation.illumination,
                )
            }
            CalendarMonth(yearMonth = month, days = days)
        }

    override suspend fun getUpcomingEvents(date: LocalDate, count: Int): List<LunarEvent> =
        withContext(dispatcher) {
            val instant = date.atTime(12, 0).atZone(zoneId).toInstant()
            calculator.upcomingCardinalEvents(instant, zoneId, count)
        }

    private fun computeNextEvent(date: LocalDate): LunarEvent {
        val instant = date.atTime(12, 0).atZone(zoneId).toInstant()
        return calculator.nextCardinalEvent(instant, zoneId)
    }
}
