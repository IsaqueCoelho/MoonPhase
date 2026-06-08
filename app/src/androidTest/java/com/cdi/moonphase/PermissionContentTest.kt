package com.cdi.moonphase

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cdi.moonphase.presentation.designsystem.MoonPhaseTheme
import com.cdi.moonphase.presentation.permission.PermissionContent
import com.cdi.moonphase.presentation.permission.PermissionStage
import com.cdi.moonphase.presentation.permission.PermissionUiState
import org.junit.Rule
import org.junit.Test

class PermissionContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initialStage_showsAllowButton() {
        composeRule.setContent {
            MoonPhaseTheme {
                PermissionContent(
                    uiState = PermissionUiState(PermissionStage.Initial),
                    onRequestPermission = {},
                    onOpenSettings = {},
                    onContinueWithout = {},
                )
            }
        }

        val allow = composeRule.activity.getString(R.string.permission_allow)
        composeRule.onNodeWithText(allow).assertIsDisplayed()
    }

    @Test
    fun deniedStage_swapsPrimaryActionToOpenSettings() {
        composeRule.setContent {
            MoonPhaseTheme {
                PermissionContent(
                    uiState = PermissionUiState(PermissionStage.DeniedOnce),
                    onRequestPermission = {},
                    onOpenSettings = {},
                    onContinueWithout = {},
                )
            }
        }

        val openSettings = composeRule.activity.getString(R.string.permission_open_settings)
        composeRule.onNodeWithText(openSettings).assertIsDisplayed()
    }
}
