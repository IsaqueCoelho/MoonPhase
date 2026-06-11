package com.cdi.moonphase.data.crash

import android.util.Log
import com.cdi.moonphase.BuildConfig
import com.cdi.moonphase.domain.crash.CrashReporter
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Firebase Crashlytics implementation of [CrashReporter]. The backing [FirebaseCrashlytics] is
 * resolved only when a default [FirebaseApp] exists, i.e. once a valid `app/google-services.json`
 * is present (see app/build.gradle.kts). Until then every call is a no-op, so the app builds and
 * runs with crash reporting dormant — dropping in the config file is all that's needed.
 *
 * In debug builds, recorded non-fatals are also echoed to logcat under `MoonCrash` for local
 * visibility without a Firebase project.
 */
internal class FirebaseCrashReporter private constructor(
    private val crashlytics: FirebaseCrashlytics?,
) : CrashReporter {

    override fun recordException(throwable: Throwable) {
        if (BuildConfig.DEBUG) Log.w(TAG, "non-fatal recorded", throwable)
        crashlytics?.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics?.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics?.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics?.setCustomKey(key, value)
    }

    companion object {
        private const val TAG = "MoonCrash"

        fun create(): FirebaseCrashReporter = FirebaseCrashReporter(crashlyticsOrNull())

        private fun crashlyticsOrNull(): FirebaseCrashlytics? = runCatching {
            // Throws IllegalStateException when no default app is configured yet.
            FirebaseApp.getInstance()
            FirebaseCrashlytics.getInstance()
        }.getOrNull()
    }
}
