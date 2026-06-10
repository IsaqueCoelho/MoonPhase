package com.cdi.moonphase.presentation.calendar

import app.cash.turbine.test
import com.cdi.moonphase.domain.usecase.GetMonthCalendar
import com.cdi.moonphase.domain.usecase.GetMoonInfoForDate
import com.cdi.moonphase.domain.usecase.GetUpcomingPhases
import com.cdi.moonphase.util.FakeMoonRepository
import com.cdi.moonphase.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-06-10T12:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(): CalendarViewModel {
        val repo = FakeMoonRepository()
        return CalendarViewModel(
            getMonthCalendar = GetMonthCalendar(repo),
            getUpcomingPhases = GetUpcomingPhases(repo),
            getMoonInfoForDate = GetMoonInfoForDate(repo),
            clock = fixedClock,
        )
    }

    @Test
    fun `loads the current month and upcoming events on init`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            // Drain to the fully loaded state.
            var state = awaitItem()
            while (state.calendar == null || state.upcomingEvents.isEmpty()) {
                state = awaitItem()
            }

            assertEquals(YearMonth.of(2026, 6), state.visibleMonth)
            assertEquals(YearMonth.of(2026, 6), state.calendar?.yearMonth)
            assertEquals(30, state.calendar?.days?.size) // June has 30 days
            assertEquals(4, state.upcomingEvents.size)
            assertNull(state.selectedDay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigating forward advances the visible month`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            var state = awaitItem()
            while (state.calendar == null) state = awaitItem()

            vm.showNextMonth()

            var advanced = awaitItem()
            while (advanced.calendar?.yearMonth != YearMonth.of(2026, 7)) {
                advanced = awaitItem()
            }
            assertEquals(YearMonth.of(2026, 7), advanced.visibleMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting a date opens the day detail`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            var state = awaitItem()
            while (state.calendar == null) state = awaitItem()

            vm.selectDate(LocalDate.of(2026, 6, 20))

            var selected = awaitItem()
            while (selected.selectedDay == null) selected = awaitItem()
            assertNotNull(selected.selectedDay)
            assertEquals(LocalDate.of(2026, 6, 20), selected.selectedDay?.date)

            vm.dismissDayDetail()
            var dismissed = awaitItem()
            while (dismissed.selectedDay != null) dismissed = awaitItem()
            assertNull(dismissed.selectedDay)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
