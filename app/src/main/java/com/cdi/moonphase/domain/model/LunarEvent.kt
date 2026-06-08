package com.cdi.moonphase.domain.model

import java.time.LocalDate

/**
 * An upcoming cardinal phase event (New, First Quarter, Full or Last Quarter) and the
 * date on which it occurs. Used to tell the user "what's next" on the Home screen.
 */
data class LunarEvent(
    val phase: PhaseType,
    val date: LocalDate,
)
