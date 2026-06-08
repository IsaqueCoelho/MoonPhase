package com.cdi.moonphase.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (Navigation Compose 2.8+). The system splash screen holds
 * until the start destination is resolved, so the graph effectively begins at either
 * [Permission] (first launch) or [Home] (returning user).
 */
sealed interface Destination {

    @Serializable
    data object Permission : Destination

    @Serializable
    data object Home : Destination
}
