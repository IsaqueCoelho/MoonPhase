package com.cdi.moonphase.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cdi.moonphase.domain.analytics.AnalyticsEvent
import com.cdi.moonphase.domain.analytics.AnalyticsTracker
import com.cdi.moonphase.domain.analytics.LocationMode
import com.cdi.moonphase.domain.analytics.ScreenName
import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.model.ThemeMode
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import com.cdi.moonphase.domain.usecase.GetMoonInfoForDate
import com.cdi.moonphase.domain.usecase.GetUpcomingPhases
import com.cdi.moonphase.domain.usecase.RefreshLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMoonInfoForDate: GetMoonInfoForDate,
    private val getUpcomingPhases: GetUpcomingPhases,
    private val refreshLocation: RefreshLocationUseCase,
    private val userPrefsRepository: UserPrefsRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** location_mode_active fires once per session, when the mode is first determined. */
    private var locationModeReported = false

    init {
        analytics.track(AnalyticsEvent.ScreenView(ScreenName.HOME))
        observeTheme()
        load(isRefresh = false)
    }

    /** Re-acquire location and recompute today's Moon info. */
    fun refresh() = load(isRefresh = true)

    /**
     * Persist an explicit theme choice (the top sun/moon toggle). The Composable computes the
     * desired mode from the currently rendered theme, so this VM never needs to know whether
     * the device is in dark mode. The new value flows back through DataStore to the whole app.
     */
    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { userPrefsRepository.setThemeMode(mode) }
    }

    /**
     * Publishes the session's location mode as a global property and emits
     * [AnalyticsEvent.LocationModeActive] once per session. A refresh that flips the mode still
     * updates the global property, so later events are attributed to the current mode.
     */
    private fun reportLocationMode(mode: LocationMode) {
        analytics.setLocationMode(mode)
        if (!locationModeReported) {
            locationModeReported = true
            analytics.track(AnalyticsEvent.LocationModeActive(mode))
        }
    }

    private fun observeTheme() {
        userPrefsRepository.themeMode
            .onEach { mode -> _uiState.update { it.copy(themeMode = mode) } }
            .launchIn(viewModelScope)
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = !isRefresh, isRefreshing = isRefresh)
            }

            val location: MoonLocation? = when (val result = refreshLocation()) {
                is LocationResult.Available -> result.location
                LocationResult.Unavailable -> null
            }

            val today = LocalDate.now(clock)
            val upcoming = getUpcomingPhases(today)
            reportLocationMode(if (location != null) LocationMode.GPS else LocationMode.DATE_FALLBACK)

            val moonInfo = getMoonInfoForDate(LocalDate.now(clock), location)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    moonInfo = moonInfo,
                    upcomingEvents = upcoming,
                    locationLabel = if (location != null) LocationLabel.Active else LocationLabel.Disabled,
                )
            }
        }
    }
}
