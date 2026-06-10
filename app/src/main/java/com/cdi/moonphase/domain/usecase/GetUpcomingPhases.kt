package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.repository.MoonRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns the next key phases after [date] — by default the next occurrence of each cardinal
 * phase (New, First Quarter, Full, Last Quarter), chronologically. Powers the "próximas fases"
 * panel shared by the Home and Calendar screens.
 */
class GetUpcomingPhases @Inject constructor(
    private val moonRepository: MoonRepository,
) {
    suspend operator fun invoke(date: LocalDate, count: Int = 4): List<LunarEvent> =
        moonRepository.getUpcomingEvents(date, count)
}
