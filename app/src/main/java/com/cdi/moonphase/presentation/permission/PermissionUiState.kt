package com.cdi.moonphase.presentation.permission

/**
 * State for the location-permission screen.
 */
data class PermissionUiState(
    val stage: PermissionStage = PermissionStage.Initial,
)

enum class PermissionStage {
    /** First view: offer to grant via the system dialog. */
    Initial,

    /** The user denied at least once: the primary action becomes "Open settings". */
    DeniedOnce,

    /** Permission granted — proceed to Home. */
    Granted,

    /** User chose to continue without location — proceed to Home in fallback mode. */
    Skipped,
}
