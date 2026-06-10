package com.cdi.moonphase.presentation.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cdi.moonphase.domain.analytics.AnalyticsEvent
import com.cdi.moonphase.domain.analytics.AnalyticsTracker
import com.cdi.moonphase.domain.analytics.PermissionPrimerActionType
import com.cdi.moonphase.domain.analytics.PermissionSystemResultType
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Small ViewModel that only tracks permission *priming* state and decides the button label.
 * The Composable owns the actual permission launcher; this class never touches Android
 * permission APIs, keeping it unit-testable. Once primed, the persisted flag routes future
 * launches straight to Home.
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    private val _navigateToHome = Channel<Unit>(Channel.BUFFERED)
    val navigateToHome = _navigateToHome.receiveAsFlow()

    init {
        analytics.track(AnalyticsEvent.PermissionPrimerViewed)
    }

    /** The "Allow" primer button: records the choice, then the screen launches the system dialog. */
    fun onAllowClicked() {
        analytics.track(AnalyticsEvent.PermissionPrimerAction(PermissionPrimerActionType.ALLOW))
        markPrimed()
    }

    fun onPermissionResult(granted: Boolean) {
        markPrimed()
        analytics.track(
            AnalyticsEvent.PermissionSystemResult(
                if (granted) PermissionSystemResultType.GRANTED else PermissionSystemResultType.DENIED,
            ),
        )
        if (granted) {
            _uiState.update { it.copy(stage = PermissionStage.Granted) }
            goHome()
        } else {
            _uiState.update { it.copy(stage = PermissionStage.DeniedOnce) }
        }
    }

    fun onContinueWithoutLocation() {
        analytics.track(AnalyticsEvent.PermissionPrimerAction(PermissionPrimerActionType.SKIP))
        markPrimed()
        _uiState.update { it.copy(stage = PermissionStage.Skipped) }
        goHome()
    }

    private fun markPrimed() {
        viewModelScope.launch { userPrefsRepository.setHasPrimedLocationPermission(true) }
    }

    private fun goHome() {
        viewModelScope.launch { _navigateToHome.send(Unit) }
    }
}
