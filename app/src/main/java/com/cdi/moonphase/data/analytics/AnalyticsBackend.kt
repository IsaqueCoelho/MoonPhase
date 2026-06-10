package com.cdi.moonphase.data.analytics

/**
 * A single destination for analytics output (Firebase, logcat, ...). Internal to the data
 * layer: feature code depends only on [com.cdi.moonphase.domain.analytics.AnalyticsTracker].
 * [DefaultAnalyticsTracker] merges in the global properties and fans out to every backend.
 */
interface AnalyticsBackend {

    /** Emit an event with its fully-merged params (event params + global properties). */
    fun logEvent(name: String, params: Map<String, Any?>)

    /** Set/clear a global ("user") property attached to all subsequent events. */
    fun setUserProperty(name: String, value: String?)
}
