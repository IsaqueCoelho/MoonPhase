package com.cdi.moonphase.domain.crash

/**
 * Provider-agnostic crash-reporting seam, mirroring [com.cdi.moonphase.domain.analytics.AnalyticsTracker].
 * Feature code records non-fatals and breadcrumbs through this interface; the concrete backend
 * (Firebase Crashlytics) is bound in the data layer and stays dormant until Firebase is
 * configured, so swapping providers never touches feature code.
 *
 * Uncaught crashes are captured automatically by the backend — this interface is for the
 * context that makes a report actionable: handled exceptions, breadcrumbs and filterable keys.
 */
interface CrashReporter {

    /** Report a handled (non-fatal) exception. */
    fun recordException(throwable: Throwable)

    /** Leave a breadcrumb attached to subsequent crash reports. */
    fun log(message: String)

    /** Set a custom key for filtering/segmenting crashes (e.g. theme, locationMode). */
    fun setCustomKey(key: String, value: String)

    fun setCustomKey(key: String, value: Boolean)

    fun setCustomKey(key: String, value: Int)
}
