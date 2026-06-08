package com.cdi.moonphase.data.repository

import com.cdi.moonphase.data.prefs.UserPrefsDataSource
import com.cdi.moonphase.domain.model.ThemeMode
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPrefsRepositoryImpl @Inject constructor(
    private val dataSource: UserPrefsDataSource,
) : UserPrefsRepository {

    override val themeMode: Flow<ThemeMode> = dataSource.themeMode

    override val hasPrimedLocationPermission: Flow<Boolean> =
        dataSource.hasPrimedLocationPermission

    override suspend fun setThemeMode(mode: ThemeMode) = dataSource.setThemeMode(mode)

    override suspend fun setHasPrimedLocationPermission(primed: Boolean) =
        dataSource.setHasPrimedLocationPermission(primed)
}
