package com.cdi.moonphase.presentation.permission

import app.cash.turbine.test
import com.cdi.moonphase.domain.analytics.AnalyticsEvent
import com.cdi.moonphase.domain.analytics.PermissionPrimerActionType
import com.cdi.moonphase.domain.analytics.PermissionSystemResultType
import com.cdi.moonphase.util.FakeUserPrefsRepository
import com.cdi.moonphase.util.MainDispatcherRule
import com.cdi.moonphase.util.RecordingAnalyticsTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `granting permission moves to Granted, primes the flag and navigates home`() = runTest {
        val prefs = FakeUserPrefsRepository(initialPrimed = false)
        val analytics = RecordingAnalyticsTracker()
        val vm = PermissionViewModel(prefs, analytics)

        vm.navigateToHome.test {
            vm.onPermissionResult(granted = true)
            awaitItem() // navigation event emitted
        }

        assertEquals(PermissionStage.Granted, vm.uiState.value.stage)
        assertTrue(prefs.currentPrimed())
        assertTrue(analytics.events.any { it is AnalyticsEvent.PermissionPrimerViewed })
        assertTrue(
            analytics.events.any {
                it is AnalyticsEvent.PermissionSystemResult &&
                    it.result == PermissionSystemResultType.GRANTED
            },
        )
    }

    @Test
    fun `denying permission moves to DeniedOnce, primes the flag and does not navigate`() =
        runTest(mainDispatcherRule.dispatcher) {
            val prefs = FakeUserPrefsRepository(initialPrimed = false)
            val analytics = RecordingAnalyticsTracker()
            val vm = PermissionViewModel(prefs, analytics)

            vm.onPermissionResult(granted = false)
            advanceUntilIdle() // let the markPrimed() coroutine run

            assertEquals(PermissionStage.DeniedOnce, vm.uiState.value.stage)
            assertTrue(prefs.currentPrimed())
            assertTrue(
                analytics.events.any {
                    it is AnalyticsEvent.PermissionSystemResult &&
                        it.result == PermissionSystemResultType.DENIED
                },
            )

            vm.navigateToHome.test {
                expectNoEvents()
            }
        }

    @Test
    fun `continuing without location moves to Skipped, primes the flag and navigates home`() = runTest {
        val prefs = FakeUserPrefsRepository(initialPrimed = false)
        val analytics = RecordingAnalyticsTracker()
        val vm = PermissionViewModel(prefs, analytics)

        vm.navigateToHome.test {
            vm.onContinueWithoutLocation()
            awaitItem()
        }

        assertEquals(PermissionStage.Skipped, vm.uiState.value.stage)
        assertTrue(prefs.currentPrimed())
        assertTrue(
            analytics.events.any {
                it is AnalyticsEvent.PermissionPrimerAction &&
                    it.action == PermissionPrimerActionType.SKIP
            },
        )
    }

    @Test
    fun `tapping allow records the primer allow action`() = runTest {
        val prefs = FakeUserPrefsRepository(initialPrimed = false)
        val analytics = RecordingAnalyticsTracker()
        val vm = PermissionViewModel(prefs, analytics)

        vm.onAllowClicked()
        advanceUntilIdle()

        assertTrue(prefs.currentPrimed())
        assertTrue(
            analytics.events.any {
                it is AnalyticsEvent.PermissionPrimerAction &&
                    it.action == PermissionPrimerActionType.ALLOW
            },
        )
    }
}
