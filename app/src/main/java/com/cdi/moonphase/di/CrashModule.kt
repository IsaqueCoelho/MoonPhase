package com.cdi.moonphase.di

import com.cdi.moonphase.data.crash.FirebaseCrashReporter
import com.cdi.moonphase.domain.crash.CrashReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the crash-reporting seam. The Firebase backend self-disables (no-ops) until a default
 * FirebaseApp is configured, so [CrashReporter] is always injectable and simply does nothing
 * until google-services.json is added.
 */
@Module
@InstallIn(SingletonComponent::class)
object CrashModule {

    @Provides
    @Singleton
    fun provideCrashReporter(): CrashReporter = FirebaseCrashReporter.create()
}
