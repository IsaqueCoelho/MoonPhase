package com.cdi.moonphase

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cdi.moonphase.domain.model.IlluminationFraction
import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonPhase
import com.cdi.moonphase.domain.model.PhaseType
import com.cdi.moonphase.presentation.designsystem.MoonPhaseTheme
import com.cdi.moonphase.presentation.home.HomeContent
import com.cdi.moonphase.presentation.home.HomeUiState
import com.cdi.moonphase.presentation.home.LocationLabel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class HomeContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val fullMoon = MoonInfo(
        date = LocalDate.of(2024, 1, 25),
        phase = MoonPhase(PhaseType.FULL, isWaxing = false),
        illumination = IlluminationFraction(1.0),
        lunarDay = LunarDay(14.0),
        nextEvent = null,
        location = null,
    )

    @Test
    fun fallbackFooter_isShown_andClickable_whenLocationDisabled() {
        var enableClicked = false
        composeRule.setContent {
            MoonPhaseTheme {
                HomeContent(
                    uiState = HomeUiState(
                        isLoading = false,
                        moonInfo = fullMoon,
                        locationLabel = LocationLabel.Disabled,
                    ),
                    onRefresh = {},
                    onEnableLocation = { enableClicked = true },
                )
            }
        }

        val footer = composeRule.activity.getString(R.string.home_location_disabled)
        composeRule.onNodeWithText(footer).assertIsDisplayed()
        composeRule.onNodeWithText(footer).performClick()
        assertTrue(enableClicked)
    }

    @Test
    fun phaseName_isShown_whenLoaded() {
        composeRule.setContent {
            MoonPhaseTheme {
                HomeContent(
                    uiState = HomeUiState(
                        isLoading = false,
                        moonInfo = fullMoon,
                        locationLabel = LocationLabel.Active,
                    ),
                    onRefresh = {},
                    onEnableLocation = {},
                )
            }
        }

        val fullMoonLabel = composeRule.activity.getString(R.string.phase_full)
        composeRule.onNodeWithText(fullMoonLabel).assertIsDisplayed()
    }
}
