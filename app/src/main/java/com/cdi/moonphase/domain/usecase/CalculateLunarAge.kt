package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.service.MoonPhaseCalculator
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Computes the Moon's age (days since the last New Moon) for a date. A thin use case over
 * the pure calculator, exposed for callers that need only the age (e.g. a future widget).
 */
class CalculateLunarAge @Inject constructor(
    private val calculator: MoonPhaseCalculator,
) {
    operator fun invoke(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): LunarDay =
        calculator.computeForDate(date, zone).lunarDay
}
