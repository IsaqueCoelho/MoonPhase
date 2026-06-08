package com.cdi.moonphase.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import com.cdi.moonphase.domain.usecase.GetMoonInfoForDate
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
    private val refreshLocation: RefreshLocationUseCase,
    private val userPrefsRepository: UserPrefsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTheme()
        load(isRefresh = false)
    }

    /** Re-acquire location and recompute today's Moon info. */
    fun refresh() = load(isRefresh = true)

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

            val moonInfo = getMoonInfoForDate(LocalDate.now(clock), location)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    moonInfo = moonInfo,
                    locationLabel = if (location != null) LocationLabel.Active else LocationLabel.Disabled,
                )
            }
        }
    }
}
