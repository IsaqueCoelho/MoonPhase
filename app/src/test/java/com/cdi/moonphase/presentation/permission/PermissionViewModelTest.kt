package com.cdi.moonphase.presentation.permission

import app.cash.turbine.test
import com.cdi.moonphase.util.FakeUserPrefsRepository
import com.cdi.moonphase.util.MainDispatcherRule
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
        val vm = PermissionViewModel(prefs)

        vm.navigateToHome.test {
            vm.onPermissionResult(granted = true)
            awaitItem() // navigation event emitted
        }

        assertEquals(PermissionStage.Granted, vm.uiState.value.stage)
        assertTrue(prefs.currentPrimed())
    }

    @Test
    fun `denying permission moves to DeniedOnce, primes the flag and does not navigate`() =
        runTest(mainDispatcherRule.dispatcher) {
            val prefs = FakeUserPrefsRepository(initialPrimed = false)
            val vm = PermissionViewModel(prefs)

            vm.onPermissionResult(granted = false)
            advanceUntilIdle() // let the markPrimed() coroutine run

            assertEquals(PermissionStage.DeniedOnce, vm.uiState.value.stage)
            assertTrue(prefs.currentPrimed())

            vm.navigateToHome.test {
                expectNoEvents()
            }
        }

    @Test
    fun `continuing without location moves to Skipped, primes the flag and navigates home`() = runTest {
        val prefs = FakeUserPrefsRepository(initialPrimed = false)
        val vm = PermissionViewModel(prefs)

        vm.navigateToHome.test {
            vm.onContinueWithoutLocation()
            awaitItem()
        }

        assertEquals(PermissionStage.Skipped, vm.uiState.value.stage)
        assertTrue(prefs.currentPrimed())
    }
}
