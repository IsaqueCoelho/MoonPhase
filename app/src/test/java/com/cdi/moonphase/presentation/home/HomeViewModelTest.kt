package com.cdi.moonphase.presentation.home

import app.cash.turbine.test
import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.usecase.GetMoonInfoForDate
import com.cdi.moonphase.domain.usecase.GetUpcomingPhases
import com.cdi.moonphase.domain.usecase.RefreshLocationUseCase
import com.cdi.moonphase.util.FakeLocationRepository
import com.cdi.moonphase.util.FakeMoonRepository
import com.cdi.moonphase.util.FakeUserPrefsRepository
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
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2024-01-25T12:00:00Z"), ZoneOffset.UTC)

    private fun viewModel(
        locationRepository: FakeLocationRepository = FakeLocationRepository(),
        prefs: FakeUserPrefsRepository = FakeUserPrefsRepository(),
    ) = HomeViewModel(
        getMoonInfoForDate = GetMoonInfoForDate(FakeMoonRepository()),
        getUpcomingPhases = GetUpcomingPhases(FakeMoonRepository()),
        refreshLocation = RefreshLocationUseCase(locationRepository),
        userPrefsRepository = prefs,
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
}
