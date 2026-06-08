package com.cdi.moonphase.domain.model

/**
 * The eight canonical phases of the synodic (lunation) cycle, ordered from New Moon.
 *
 * Pure domain enum: it carries no localized text and no Android types. Human-readable
 * labels and descriptions are resolved in the presentation layer from string resources.
 */
enum class PhaseType {
    NEW,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT;

    /** Major phases mark the cardinal points of the cycle (0, 1/4, 1/2, 3/4). */
    val isMajor: Boolean
        get() = this == NEW || this == FIRST_QUARTER || this == FULL || this == LAST_QUARTER
}
