package com.cdi.moonphase.data.analytics

import com.cdi.moonphase.BuildConfig
import com.cdi.moonphase.domain.analytics.AnalyticsEvent
import com.cdi.moonphase.domain.analytics.AnalyticsTheme
import com.cdi.moonphase.domain.analytics.AnalyticsTracker
import com.cdi.moonphase.domain.analytics.LocationMode
import com.cdi.moonphase.domain.crash.CrashReporter
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [AnalyticsTracker]: owns the Fase 2 global properties and fans every event out to all
 * configured [backends]. Global properties are kept current as user properties on each backend
 * *and* merged into each event's params, so a backend that only supports per-event params still
 * receives them.
 *
 * Global properties: appVersion, phaseFeatureSet (v2), theme, locationMode, sessionId,
 * firstOpenDate — enabling cuts by theme, location mode and install cohort.
 */
@Singleton
class DefaultAnalyticsTracker @Inject constructor(
    private val backends: List<@JvmSuppressWildcards AnalyticsBackend>,
    private val userPrefsRepository: UserPrefsRepository,
    private val crashReporter: CrashReporter,
    private val clock: Clock,
) : AnalyticsTracker {

    private val globalProperties = ConcurrentHashMap<String, String>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // sessionId: one per process launch. appVersion / phaseFeatureSet are fixed for the build.
        setGlobalProperty(KEY_SESSION_ID, UUID.randomUUID().toString())
        setGlobalProperty(KEY_APP_VERSION, BuildConfig.VERSION_NAME)
        setGlobalProperty(KEY_PHASE_FEATURE_SET, PHASE_FEATURE_SET)
        // firstOpenDate: persisted once, for install cohorts.
        scope.launch {
            val firstOpen = userPrefsRepository.firstOpenDateOrSet(LocalDate.now(clock).toString())
            setGlobalProperty(KEY_FIRST_OPEN_DATE, firstOpen)
        }
    }

    override fun track(event: AnalyticsEvent) {
        val merged = globalProperties + event.params
        backends.forEach { it.logEvent(event.name, merged) }
        // Mirror the event as a Crashlytics breadcrumb so a crash report shows what led up to it.
        crashReporter.log(event.name)
    }

    override fun setTheme(theme: AnalyticsTheme) = setGlobalProperty(KEY_THEME, theme.wire)

    override fun setLocationMode(mode: LocationMode) = setGlobalProperty(KEY_LOCATION_MODE, mode.wire)

    private fun setGlobalProperty(key: String, value: String) {
        globalProperties[key] = value
        backends.forEach { it.setUserProperty(key, value) }
        // Keep crash reports filterable by the same dimensions (theme, locationMode, sessionId, ...).
        crashReporter.setCustomKey(key, value)
    }

    private companion object {
        const val PHASE_FEATURE_SET = "v2"
        const val KEY_APP_VERSION = "appVersion"
        const val KEY_PHASE_FEATURE_SET = "phaseFeatureSet"
        const val KEY_THEME = "theme"
        const val KEY_LOCATION_MODE = "locationMode"
        const val KEY_SESSION_ID = "sessionId"
        const val KEY_FIRST_OPEN_DATE = "firstOpenDate"
    }
}
