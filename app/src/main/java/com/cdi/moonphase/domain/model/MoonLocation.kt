package com.cdi.moonphase.domain.model

/**
 * An approximate observer position on Earth. The Moon's phase is global, but the location
 * lets the UI label results ("as seen from near you") and leaves room for future
 * hemisphere-aware rendering of the terminator.
 *
 * This is a domain type: it never carries an Android `Location`. Mappers in the data layer
 * translate platform types into this.
 */
data class MoonLocation(
    val latitude: Double,
    val longitude: Double,
) {
    /** Northern hemisphere observers see the lit limb mirrored relative to the south. */
    val isNorthernHemisphere: Boolean get() = latitude >= 0.0
}
