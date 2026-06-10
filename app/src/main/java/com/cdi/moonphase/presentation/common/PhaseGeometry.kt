package com.cdi.moonphase.presentation.common

import com.cdi.moonphase.domain.model.PhaseType

/**
 * The canonical lit fraction for each phase, used to draw a representative mini-Moon icon when
 * only the [PhaseType] is known (e.g. the upcoming-phases list, where there is no per-day
 * illumination). For real days the measured [com.cdi.moonphase.domain.model.IlluminationFraction]
 * is preferred.
 */
val PhaseType.canonicalIllumination: Float
    get() = when (this) {
        PhaseType.NEW -> 0f
        PhaseType.WAXING_CRESCENT, PhaseType.WANING_CRESCENT -> 0.25f
        PhaseType.FIRST_QUARTER, PhaseType.LAST_QUARTER -> 0.5f
        PhaseType.WAXING_GIBBOUS, PhaseType.WANING_GIBBOUS -> 0.75f
        PhaseType.FULL -> 1f
    }

/** Whether the phase's lit limb is on the waxing (right) side. */
val PhaseType.canonicalWaxing: Boolean
    get() = when (this) {
        PhaseType.NEW,
        PhaseType.WAXING_CRESCENT,
        PhaseType.FIRST_QUARTER,
        PhaseType.WAXING_GIBBOUS,
        PhaseType.FULL,
        -> true

        PhaseType.WANING_GIBBOUS,
        PhaseType.LAST_QUARTER,
        PhaseType.WANING_CRESCENT,
        -> false
    }
