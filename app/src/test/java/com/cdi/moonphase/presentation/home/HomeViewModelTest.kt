package com.cdi.moonphase.presentation.home

import app.cash.turbine.test
import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.analytics.AnalyticsEvent
import com.cdi.moonphase.domain.analytics.LocationMode
import com.cdi.moonphase.domain.analytics.ScreenName
import com.cdi.moonphase.domain.usecase.GetMoonInfoForDate
import com.cdi.moonphase.domain.usecase.GetUpcomingPhases
import com.cdi.moonphase.domain.usecase.RefreshLocationUseCase
import com.cdi.moonphase.util.FakeLocationRepository
import com.cdi.moonphase.util.FakeMoonRepository
import com.cdi.moonphase.util.FakeUserPrefsRepository
import com.cdi.moonphase.util.MainDispatcherRule
import com.cdi.moonphase.util.RecordingAnalyticsTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2024-01-25T12:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(
        locationRepository: FakeLocationRepository = FakeLocationRepository(),
        prefs: FakeUserPrefsRepository = FakeUserPrefsRepository(),
        analytics: RecordingAnalyticsTracker = RecordingAnalyticsTracker(),
    ) = HomeViewModel(
        getMoonInfoForDate = GetMoonInfoForDate(FakeMoonRepository()),
        getUpcomingPhases = GetUpcomingPhases(FakeMoonRepository()),
        refreshLocation = RefreshLocationUseCase(locationRepository),
        userPrefsRepository = prefs,
        analytics = analytics,
        clock = fixedClock,
    )

    @Test
    fun `emits loading then loaded with disabled label when location is unavailable`() = runTest {
        val vm = viewModel(FakeLocationRepository(LocationResult.Unavailable))

        vm.uiState.test {
            // Initial state is loading.
            assertEquals(true, awaitItem().isLoading)

            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            assertNotNull(loaded.moonInfo)
            assertNull(loaded.moonInfo?.location)
            assertEquals(LocationLabel.Disabled, loaded.locationLabel)
        }
    }

    @Test
    fun `emits loaded with active label and location when a fix is available`() = runTest {
        val location = MoonLocation(latitude = -23.5, longitude = -46.6)
        val vm = viewModel(FakeLocationRepository(LocationResult.Available(location)))

        vm.uiState.test {
            assertEquals(true, awaitItem().isLoading)

            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            assertEquals(location, loaded.moonInfo?.location)
            assertEquals(LocationLabel.Active, loaded.locationLabel)
        }
    }

    @Test
    fun `computes for today as given by the clock`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            awaitItem() // loading
            val loaded = awaitItem()
            assertEquals(java.time.LocalDate.of(2024, 1, 25), loaded.moonInfo?.date)
        }
    }

    @Test
    fun `tracks home screen_view on creation`() = runTest {
        val analytics = RecordingAnalyticsTracker()

        viewModel(analytics = analytics)
        advanceUntilIdle()

        assertTrue(
            analytics.events.any {
                it is AnalyticsEvent.ScreenView && it.screen == ScreenName.HOME
            },
        )
    }

    @Test
    fun `reports date_fallback location mode once when no fix is available`() = runTest {
        val analytics = RecordingAnalyticsTracker()

        viewModel(
            locationRepository = FakeLocationRepository(LocationResult.Unavailable),
            analytics = analytics,
        )
        advanceUntilIdle()

        assertEquals(LocationMode.DATE_FALLBACK, analytics.locationMode)
        assertEquals(
            1,
            analytics.events.count {
                it is AnalyticsEvent.LocationModeActive && it.mode == LocationMode.DATE_FALLBACK
            },
        )
    }

    @Test
    fun `reports gps location mode when a fix is available`() = runTest {
        val analytics = RecordingAnalyticsTracker()

        viewModel(
            locationRepository = FakeLocationRepository(
                LocationResult.Available(MoonLocation(latitude = -23.5, longitude = -46.6)),
            ),
            analytics = analytics,
        )
        advanceUntilIdle()

        assertEquals(LocationMode.GPS, analytics.locationMode)
        assertTrue(
            analytics.events.any {
                it is AnalyticsEvent.LocationModeActive && it.mode == LocationMode.GPS
            },
        )
    }
}
