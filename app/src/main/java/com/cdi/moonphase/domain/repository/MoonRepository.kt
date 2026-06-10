package com.cdi.moonphase.domain.repository

import com.cdi.moonphase.domain.model.CalendarMonth
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonLocation
import java.time.LocalDate
import java.time.YearMonth

/**
 * Source of computed Moon data. Declared in the domain and implemented in the data layer so
 * the dependency arrow points inward (data -> domain).
 */
interface MoonRepository {

    /**
     * The complete [MoonInfo] snapshot for [date]. [location] is optional and, when present,
     * is carried through for labeling/rendering; the phase itself is global.
     */
    suspend fun getMoonInfo(date: LocalDate, location: MoonLocation?): MoonInfo

    /** The next cardinal phase strictly after [date]. */
    suspend fun getNextEvent(date: LocalDate): LunarEvent

    /** The whole [month] with the Moon resolved for each of its days. */
    suspend fun getMonth(month: YearMonth): CalendarMonth

    /**
     * The next [count] cardinal phases after [date], chronologically — one upcoming occurrence
     * of each of New / First Quarter / Full / Last Quarter when [count] is 4.
     */
    suspend fun getUpcomingEvents(date: LocalDate, count: Int = 4): List<LunarEvent>
}
