package com.cdi.moonphase.domain.model

/**
 * The outcome of attempting to resolve the observer's approximate location.
 *
 * The app is fully functional without a location (the phase is computed from the date
 * alone), so "unavailable" is a first-class, expected result — not an error — covering
 * denied permission, disabled location services, or a timed-out fix.
 */
sealed interface LocationResult {
    data class Available(val location: MoonLocation) : LocationResult
    data object Unavailable : LocationResult
}
