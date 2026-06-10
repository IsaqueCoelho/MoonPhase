package com.cdi.moonphase.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cdi.moonphase.domain.usecase.GetMonthCalendar
import com.cdi.moonphase.domain.usecase.GetMoonInfoForDate
import com.cdi.moonphase.domain.usecase.GetUpcomingPhases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMonthCalendar: GetMonthCalendar,
    private val getUpcomingPhases: GetUpcomingPhases,
    private val getMoonInfoForDate: GetMoonInfoForDate,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now(clock)
        _uiState.update {
            it.copy(today = today, visibleMonth = YearMonth.from(today))
        }
        loadMonth(YearMonth.from(today))
        loadUpcoming(today)
    }

    /** Step the visible month by one, forward or back (chevrons or horizontal swipe). */
    fun showPreviousMonth() = showMonth(uiState.value.visibleMonth.minusMonths(1))

    fun showNextMonth() = showMonth(uiState.value.visibleMonth.plusMonths(1))

    fun showMonth(month: YearMonth) {
        if (month == uiState.value.visibleMonth && uiState.value.calendar != null) return
        _uiState.update { it.copy(visibleMonth = month) }
        loadMonth(month)
    }

    /** Open the day-detail sheet for [date], computing its full snapshot. */
    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val info = getMoonInfoForDate(date, location = null)
            _uiState.update { it.copy(selectedDay = info) }
        }
    }

    /** Jump the grid to an upcoming event's month and open its detail. */
    fun selectUpcomingDate(date: LocalDate) {
        showMonth(YearMonth.from(date))
        selectDate(date)
    }

    fun dismissDayDetail() {
        _uiState.update { it.copy(selectedDay = null) }
    }

    private fun loadMonth(month: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val calendar = getMonthCalendar(month)
            _uiState.update { current ->
                // Guard against an out-of-order result if the user navigated again meanwhile.
                if (current.visibleMonth != month) current
                else current.copy(calendar = calendar, isLoading = false)
            }
        }
    }

    private fun loadUpcoming(from: LocalDate) {
        viewModelScope.launch {
            val events = getUpcomingPhases(from)
            _uiState.update { it.copy(upcomingEvents = events) }
        }
    }
}
