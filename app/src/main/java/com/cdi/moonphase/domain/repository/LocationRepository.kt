package com.cdi.moonphase.domain.repository

import com.cdi.moonphase.domain.model.LocationResult

/**
 * Resolves the observer's approximate location. Implementations must never throw for the
 * common "no location" cases — they return [LocationResult.Unavailable] instead, so the
 * caller can fall back to a date-only computation.
 */
interface LocationRepository {
    suspend fun getCurrentLocation(): LocationResult
}
