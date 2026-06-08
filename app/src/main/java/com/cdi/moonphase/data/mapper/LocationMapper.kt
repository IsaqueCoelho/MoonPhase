package com.cdi.moonphase.data.mapper

import android.location.Location
import com.cdi.moonphase.domain.model.MoonLocation

/**
 * Translates the Android platform [Location] into the domain [MoonLocation]. Keeping the
 * mapping here ensures `android.location.Location` never leaks above the data layer.
 */
fun Location.toMoonLocation(): MoonLocation =
    MoonLocation(latitude = latitude, longitude = longitude)
