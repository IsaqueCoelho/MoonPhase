package com.cdi.moonphase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cdi.moonphase.domain.model.MoonPhase
import com.cdi.moonphase.domain.usecase.GetCurrentMoonPhaseUseCase
import com.cdi.moonphase.domain.usecase.ScheduleMoonNotificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoonPhaseViewModel(
    private val getCurrentMoonPhase: GetCurrentMoonPhaseUseCase,
    private val scheduleMoonNotifications: ScheduleMoonNotificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<MoonPhase?>(null)
    val state: StateFlow<MoonPhase?> = _state.asStateFlow()

    init {
        refresh()
        schedule()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = getCurrentMoonPhase()
        }
    }

    private fun schedule() {
        viewModelScope.launch {
            scheduleMoonNotifications()
        }
    }
}
