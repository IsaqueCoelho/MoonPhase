package com.cdi.moonphase.domain.model

import java.time.LocalDate

/**
 * The complete, computed snapshot of the Moon for a given date — the central read model of
 * the app. Everything the Home screen renders is derived purely from this aggregate, with
 * no further business logic in the view.
 *
 * @property date the calendar date this snapshot describes.
 * @property phase which phase, and whether waxing or waning.
 * @property illumination the lit fraction of the disk, drives the terminator animation.
 * @property lunarDay the Moon's age within the current synodic month.
 * @property nextEvent the next cardinal phase, or null if it could not be projected.
 * @property location the observer position used, or null when computed from the date alone.
 */
data class MoonInfo(
    val date: LocalDate,
    val phase: MoonPhase,
    val illumination: IlluminationFraction,
    val lunarDay: LunarDay,
    val nextEvent: LunarEvent?,
    val location: MoonLocation?,
)
