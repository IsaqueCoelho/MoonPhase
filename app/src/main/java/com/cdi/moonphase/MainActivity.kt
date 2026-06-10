package com.cdi.moonphase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdi.moonphase.domain.analytics.AnalyticsTheme
import com.cdi.moonphase.domain.analytics.AnalyticsTracker
import com.cdi.moonphase.domain.model.ThemeMode
import com.cdi.moonphase.presentation.MainUiState
import com.cdi.moonphase.presentation.MainViewModel
import com.cdi.moonphase.presentation.designsystem.MoonPhaseTheme
import com.cdi.moonphase.presentation.navigation.MoonNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    /** The resolved theme is only known here (system mode needs the composition), so the
     *  analytics `theme` global property is published from this single point. */
    @Inject
    lateinit var analytics: AnalyticsTracker

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
                is MainUiState.Ready -> {
                    val darkTheme = when (ready.themeMode) {
                        ThemeMode.SYSTEM -> isSystemInDarkTheme()
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                    }
                    LaunchedEffect(darkTheme) {
                        analytics.setTheme(if (darkTheme) AnalyticsTheme.DARK else AnalyticsTheme.LIGHT)
                    }
                    MoonPhaseTheme(themeMode = ready.themeMode) {
                        MoonNavHost(startDestination = ready.startDestination)
                    }
                }
            }
        }
    }
}
