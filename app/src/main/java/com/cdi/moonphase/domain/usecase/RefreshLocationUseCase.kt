package com.cdi.moonphase.domain.usecase

import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Attempts to (re)acquire the observer's approximate location. Always resolves: callers get
 * either [LocationResult.Available] or [LocationResult.Unavailable], never an exception.
 */
class RefreshLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): LocationResult =
        locationRepository.getCurrentLocation()
}
