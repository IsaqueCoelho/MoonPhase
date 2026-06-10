package com.cdi.moonphase.data.analytics

import android.util.Log

/**
 * Prints events to logcat under the `MoonAnalytics` tag. Wired in debug builds only so the
 * full event stream (and its merged global properties) is inspectable without a Firebase
 * project — invaluable while validating the Fase 2 tag plan.
 */
internal class LogcatAnalyticsBackend : AnalyticsBackend {

    override fun logEvent(name: String, params: Map<String, Any?>) {
        val rendered = params.entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "$k=$v" }
        Log.d(TAG, "event=$name $rendered")
    }

    override fun setUserProperty(name: String, value: String?) {
        Log.d(TAG, "userProperty $name=$value")
    }

    private companion object {
        const val TAG = "MoonAnalytics"
    }
}
