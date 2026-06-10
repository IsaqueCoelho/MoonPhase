package com.cdi.moonphase.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * A whole calendar month with the Moon already resolved for each of its days — the read model
 * the Calendar screen renders. Leading/trailing blank cells needed to align the grid are a
 * pure presentation concern and are computed in the UI from [yearMonth]; the domain only
 * carries the real days.
 *
 * @property yearMonth the month this grid describes.
 * @property days one [CalendarDay] per actual day of the month, in ascending order.
 */
data class CalendarMonth(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>,
)

/**
 * The Moon's state on a single calendar day, reduced to exactly what a calendar cell draws:
 * the date, the lit fraction (drives the mini-disc terminator) and whether it is waxing.
 */
data class CalendarDay(
    val date: LocalDate,
    val phase: MoonPhase,
    val illumination: IlluminationFraction,
)
