package com.cdi.moonphase.domain.model

import kotlin.math.roundToInt

/**
 * The illuminated fraction of the Moon's disk, in `[0.0, 1.0]`
 * (0.0 = New Moon, 1.0 = Full Moon).
 *
 * Modeled as a value class so the unit ("a fraction, not a percentage") is encoded in the
 * type and the constraint is enforced at construction — illegal values can never flow up.
 */
@JvmInline
value class IlluminationFraction(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Illumination must be in [0.0, 1.0], was $value" }
    }

    /** The fraction expressed as a whole percentage in `[0, 100]`, for display. */
    val percent: Int get() = (value * 100).roundToInt()
}
