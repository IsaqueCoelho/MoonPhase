package com.cdi.moonphase.di

import android.content.Context
import com.cdi.moonphase.BuildConfig
import com.cdi.moonphase.data.analytics.AnalyticsBackend
import com.cdi.moonphase.data.analytics.DefaultAnalyticsTracker
import com.cdi.moonphase.data.analytics.FirebaseAnalyticsBackend
import com.cdi.moonphase.data.analytics.LogcatAnalyticsBackend
import com.cdi.moonphase.domain.analytics.AnalyticsTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Wires the analytics seam. The set of backends is resolved at runtime: Firebase joins only
 * when a default FirebaseApp is configured (google-services.json present), and the logcat sink
 * is added in debug builds. The public [AnalyticsTracker] is always available — it simply has
 * no destinations until one is enabled.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsBackends(
        @ApplicationContext context: Context,
    ): List<AnalyticsBackend> = buildList {
        FirebaseAnalyticsBackend.createOrNull(context)?.let(::add)
        if (BuildConfig.DEBUG) add(LogcatAnalyticsBackend())
    }

    @Provides
    @Singleton
    fun provideAnalyticsTracker(tracker: DefaultAnalyticsTracker): AnalyticsTracker = tracker
}
