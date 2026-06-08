package com.cdi.moonphase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cdi.moonphase.domain.model.ThemeMode
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import com.cdi.moonphase.presentation.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Drives the system splash hold and the app-wide theme. The splash stays up while this is
 * [MainUiState.Loading]; once preferences resolve, it exposes the theme and the start
 * destination (Permission on first launch, Home thereafter).
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    userPrefsRepository: UserPrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        userPrefsRepository.themeMode,
        userPrefsRepository.hasPrimedLocationPermission,
    ) { themeMode, hasPrimed ->
        MainUiState.Ready(
            themeMode = themeMode,
            startDestination = if (hasPrimed) Destination.Home else Destination.Permission,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MainUiState.Loading,
    )
}

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Ready(
        val themeMode: ThemeMode,
        val startDestination: Destination,
    ) : MainUiState
}
