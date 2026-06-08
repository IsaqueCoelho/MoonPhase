package com.cdi.moonphase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdi.moonphase.presentation.MainUiState
import com.cdi.moonphase.presentation.MainViewModel
import com.cdi.moonphase.presentation.designsystem.MoonPhaseTheme
import com.cdi.moonphase.presentation.navigation.MoonNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the native splash until preferences resolve the start destination + theme,
        // so we never flash an unthemed white frame before the first real screen.
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            when (val ready = state) {
                MainUiState.Loading -> Unit // splash is still showing
                is MainUiState.Ready -> MoonPhaseTheme(themeMode = ready.themeMode) {
                    MoonNavHost(startDestination = ready.startDestination)
                }
            }
        }
    }
}
