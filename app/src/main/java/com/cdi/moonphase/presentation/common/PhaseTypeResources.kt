package com.cdi.moonphase.presentation.common

import androidx.annotation.StringRes
import com.cdi.moonphase.R
import com.cdi.moonphase.domain.model.PhaseType

/**
 * Maps the pure domain [PhaseType] to localized presentation resources. Localization lives
 * here, in presentation, so the domain enum stays free of strings.
 */
@get:StringRes
val PhaseType.nameRes: Int
    get() = when (this) {
        PhaseType.NEW -> R.string.phase_new
        PhaseType.WAXING_CRESCENT -> R.string.phase_waxing_crescent
        PhaseType.FIRST_QUARTER -> R.string.phase_first_quarter
        PhaseType.WAXING_GIBBOUS -> R.string.phase_waxing_gibbous
        PhaseType.FULL -> R.string.phase_full
        PhaseType.WANING_GIBBOUS -> R.string.phase_waning_gibbous
        PhaseType.LAST_QUARTER -> R.string.phase_last_quarter
        PhaseType.WANING_CRESCENT -> R.string.phase_waning_crescent
    }

@get:StringRes
val PhaseType.descriptionRes: Int
    get() = when (this) {
        PhaseType.NEW -> R.string.phase_desc_new
        PhaseType.WAXING_CRESCENT -> R.string.phase_desc_waxing_crescent
        PhaseType.FIRST_QUARTER -> R.string.phase_desc_first_quarter
        PhaseType.WAXING_GIBBOUS -> R.string.phase_desc_waxing_gibbous
        PhaseType.FULL -> R.string.phase_desc_full
        PhaseType.WANING_GIBBOUS -> R.string.phase_desc_waning_gibbous
        PhaseType.LAST_QUARTER -> R.string.phase_desc_last_quarter
        PhaseType.WANING_CRESCENT -> R.string.phase_desc_waning_crescent
    }
