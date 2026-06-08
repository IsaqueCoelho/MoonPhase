package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.repository.MoonRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns the next cardinal phase (New / First Quarter / Full / Last Quarter) after [date].
 */
class GetNextLunarEvent @Inject constructor(
    private val moonRepository: MoonRepository,
) {
    suspend operator fun invoke(date: LocalDate): LunarEvent =
        moonRepository.getNextEvent(date)
}
