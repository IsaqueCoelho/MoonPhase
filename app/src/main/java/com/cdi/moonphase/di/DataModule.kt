package com.cdi.moonphase.di

import com.cdi.moonphase.data.repository.LocationRepositoryImpl
import com.cdi.moonphase.data.repository.MoonRepositoryImpl
import com.cdi.moonphase.data.repository.UserPrefsRepositoryImpl
import com.cdi.moonphase.domain.repository.LocationRepository
import com.cdi.moonphase.domain.repository.MoonRepository
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain repository interfaces to their data-layer implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMoonRepository(impl: MoonRepositoryImpl): MoonRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindUserPrefsRepository(impl: UserPrefsRepositoryImpl): UserPrefsRepository
}
