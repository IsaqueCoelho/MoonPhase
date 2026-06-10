package com.cdi.moonphase.presentation.calendar

import com.cdi.moonphase.domain.model.CalendarMonth
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import java.time.LocalDate
import java.time.YearMonth

/**
 * Immutable state for the Calendar screen. The grid ([calendar]) tracks [visibleMonth] as the
 * user navigates; [upcomingEvents] is anchored to [today] and does not change with navigation.
 * A non-null [selectedDay] means the day-detail bottom sheet is open for that date.
 */
data class CalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val calendar: CalendarMonth? = null,
    val upcomingEvents: List<LunarEvent> = emptyList(),
    val selectedDay: MoonInfo? = null,
    val isLoading: Boolean = true,
)
