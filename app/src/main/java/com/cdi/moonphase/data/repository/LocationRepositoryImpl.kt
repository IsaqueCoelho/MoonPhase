package com.cdi.moonphase.data.repository

import com.cdi.moonphase.data.location.LocationDataSource
import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val dataSource: LocationDataSource,
) : LocationRepository {

    override suspend fun getCurrentLocation(): LocationResult =
        dataSource.currentLocation()
}
