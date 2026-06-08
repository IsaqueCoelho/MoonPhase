package com.cdi.moonphase.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cdi.moonphase.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Reads and writes the app's persisted preferences over a Preferences [DataStore].
 * Stored values are primitive; this source maps them to/from domain types.
 */
class UserPrefsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE]?.let { stored ->
            runCatching { ThemeMode.valueOf(stored) }.getOrNull()
        } ?: ThemeMode.SYSTEM
    }

    val hasPrimedLocationPermission: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HAS_PRIMED_LOCATION] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setHasPrimedLocationPermission(primed: Boolean) {
        dataStore.edit { it[HAS_PRIMED_LOCATION] = primed }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAS_PRIMED_LOCATION = booleanPreferencesKey("has_primed_location_permission")
    }
}
