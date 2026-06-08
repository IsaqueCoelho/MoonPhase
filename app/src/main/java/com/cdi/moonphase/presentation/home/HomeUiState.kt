package com.cdi.moonphase.presentation.home

import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.ThemeMode

/**
 * Immutable state rendered by the Home screen. Everything the view shows is derived from
 * this; the view contains no business logic.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val moonInfo: MoonInfo? = null,
    val locationLabel: LocationLabel = LocationLabel.Undefined,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

/** The footer location indicator state. */
enum class LocationLabel {
    /** Still resolving — show nothing definitive yet. */
    Undefined,

    /** An approximate location was obtained. */
    Active,

    /** No location (denied/disabled/timed out) — invite the user to enable it. */
    Disabled,
}
