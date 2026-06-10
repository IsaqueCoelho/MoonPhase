package com.cdi.moonphase.domain.analytics

/**
 * Provider-agnostic analytics seam. The presentation layer talks only to this interface; the
 * concrete backend (Firebase, a debug logcat sink, or both) is bound in the data layer, so
 * swapping or adding a provider never touches feature code.
 *
 * Implementations are responsible for attaching the global properties (appVersion,
 * phaseFeatureSet, theme, locationMode, sessionId, firstOpenDate) to every tracked event, per
 * the Fase 2 analytics plan. [setTheme] and [setLocationMode] update the two global properties
 * that change at runtime.
 */
interface AnalyticsTracker {

    /** Record [event]. Global properties are merged in by the implementation. */
    fun track(event: AnalyticsEvent)

    /** Update the resolved-theme global property attached to every subsequent event. */
    fun setTheme(theme: AnalyticsTheme)

    /** Update the location-mode global property attached to every subsequent event. */
    fun setLocationMode(mode: LocationMode)
}
