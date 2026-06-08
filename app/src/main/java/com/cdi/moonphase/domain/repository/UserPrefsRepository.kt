package com.cdi.moonphase.domain.repository

import com.cdi.moonphase.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Persisted user preferences and one-shot flags, backed by DataStore in the data layer.
 */
interface UserPrefsRepository {

    /** The selected theme mode, defaulting to [ThemeMode.SYSTEM]. */
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)

    /**
     * Whether the native location-permission dialog has already been shown ("primed") once.
     * Used to route first launch to the Permission screen and to flip the permission button
     * to "Open settings" after the OS stops showing the system dialog.
     */
    val hasPrimedLocationPermission: Flow<Boolean>

    suspend fun setHasPrimedLocationPermission(primed: Boolean)
}
