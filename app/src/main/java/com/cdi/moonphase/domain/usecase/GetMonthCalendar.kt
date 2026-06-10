package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.domain.model.CalendarMonth
import com.cdi.moonphase.domain.repository.MoonRepository
import java.time.YearMonth
import javax.inject.Inject

/**
 * Returns the full [CalendarMonth] for [month] — every day with its Moon resolved. The single
 * entry point the Calendar screen uses to populate the grid.
 */
class GetMonthCalendar @Inject constructor(
    private val moonRepository: MoonRepository,
) {
    suspend operator fun invoke(month: YearMonth): CalendarMonth =
        moonRepository.getMonth(month)
}
