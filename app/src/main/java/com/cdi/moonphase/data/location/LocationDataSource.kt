package com.cdi.moonphase.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.cdi.moonphase.data.mapper.toMoonLocation
import com.cdi.moonphase.di.IoDispatcher
import com.cdi.moonphase.domain.crash.CrashReporter
import com.cdi.moonphase.domain.model.LocationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Encapsulates [FusedLocationProviderClient]. Returns a domain [LocationResult], translating
 * every "no fix" condition — missing permission, disabled services, timeout, exception —
 * into [LocationResult.Unavailable] so the rest of the app stays offline-first and crash-free.
 */
class LocationDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedClient: FusedLocationProviderClient,
    private val crashReporter: CrashReporter,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    @SuppressLint("MissingPermission") // Permission is explicitly checked before any call.
    suspend fun currentLocation(): LocationResult = withContext(dispatcher) {
        if (!hasCoarseLocationPermission()) return@withContext LocationResult.Unavailable

        val location = runCatching {
            withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                val tokenSource = CancellationTokenSource()
                fusedClient
                    .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                    .await()
                    ?: fusedClient.lastLocation.await()
            }
        }.onFailure { error ->
            // Coroutine cancellation must propagate; only a real provider fault is a non-fatal.
            if (error is CancellationException) throw error
            crashReporter.recordException(error)
        }.getOrNull()

        location?.toMoonLocation()?.let(LocationResult::Available) ?: LocationResult.Unavailable
    }

    private fun hasCoarseLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        // REQ-02: the location attempt must never block start-up beyond ~2 s; on timeout we
        // fall back to a date-only computation.
        const val LOCATION_TIMEOUT_MS = 2_000L
    }
}
