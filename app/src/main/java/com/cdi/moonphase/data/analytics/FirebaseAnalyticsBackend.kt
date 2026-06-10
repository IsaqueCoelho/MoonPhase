package com.cdi.moonphase.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Analytics destination. Created only when a default [FirebaseApp] exists, i.e. once a
 * valid `app/google-services.json` is present and the google-services plugin has wired it in
 * (see app/build.gradle.kts). Until then [createOrNull] returns null and the tracker simply
 * routes to its other backends — so the app builds and runs with analytics dormant, and
 * dropping in the config file is all that's needed to activate real reporting.
 */
internal class FirebaseAnalyticsBackend private constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsBackend {

    override fun logEvent(name: String, params: Map<String, Any?>) {
        firebaseAnalytics.logEvent(name, params.toBundle())
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    private fun Map<String, Any?>.toBundle(): Bundle = Bundle().apply {
        for ((key, value) in this@toBundle) {
            when (value) {
                null -> Unit
                is String -> putString(key, value)
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putDouble(key, value.toDouble())
                // Firebase has no boolean param type; persist as a queryable string.
                is Boolean -> putString(key, value.toString())
                else -> putString(key, value.toString())
            }
        }
    }

    companion object {
        fun createOrNull(context: Context): FirebaseAnalyticsBackend? = runCatching {
            // Throws IllegalStateException when no default app is configured yet.
            FirebaseApp.getInstance()
            FirebaseAnalyticsBackend(FirebaseAnalytics.getInstance(context))
        }.getOrNull()
    }
}
