package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.repository.MoonRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns the full [MoonInfo] snapshot for a date, optionally tied to an observer location.
 * The single entry point the Home screen uses to obtain everything it renders.
 */
class GetMoonInfoForDate @Inject constructor(
    private val moonRepository: MoonRepository,
) {
    suspend operator fun invoke(date: LocalDate, location: MoonLocation? = null): MoonInfo =
        moonRepository.getMoonInfo(date, location)
}
