package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.data.repository.MoonRepository
import com.cdi.moonphase.domain.model.MoonPhase
import java.time.LocalDateTime

class GetCurrentMoonPhaseUseCase(
    private val repository: MoonRepository
) {
    suspend operator fun invoke(now: LocalDateTime = LocalDateTime.now()): MoonPhase {
        return repository.getCurrentMoonPhase(now)
    }
}
