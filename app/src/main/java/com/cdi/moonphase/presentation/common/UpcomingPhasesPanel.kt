package com.cdi.moonphase.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdi.moonphase.R
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The "próximas fases" panel (Tela 05). Not a screen of its own: it lists the next key phases
 * with a mini-Moon, name, date and day-count, and lives both on the Home (expanding the Fase 1
 * "próxima fase" card) and in the Calendar footer. Tapping a row notifies [onEventClick] so the
 * caller can jump the calendar to that date and open its detail.
 */
@Composable
fun UpcomingPhasesPanel(
    events: List<LunarEvent>,
    today: LocalDate,
    modifier: Modifier = Modifier,
    onEventClick: ((LunarEvent) -> Unit)? = null,
) {
    if (events.isEmpty()) return
    val palette = MoonTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = palette.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(R.string.upcoming_phases_title),
                fontSize = 11.sp,
                color = palette.textTertiary,
            )
            Spacer(Modifier.height(8.dp))
            events.forEach { event ->
                UpcomingPhaseRow(
                    event = event,
                    today = today,
                    onClick = onEventClick?.let { { it(event) } },
                )
            }
        }
    }
}

@Composable
private fun UpcomingPhaseRow(
    event: LunarEvent,
    today: LocalDate,
    onClick: (() -> Unit)?,
) {
    val palette = MoonTheme.colors
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .heightIn(min = 44.dp)
        .padding(vertical = 6.dp)

    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        MoonDisk(
            illumination = event.phase.canonicalIllumination,
            waxing = event.phase.canonicalWaxing,
            decorate = false,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(event.phase.nameRes),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = event.date.formatShort(),
                fontSize = 13.sp,
                color = palette.textSecondary,
            )
            Text(
                text = relativeDays(today, event.date),
                fontSize = 11.sp,
                color = palette.textTertiary,
            )
        }
    }
}

@Composable
private fun relativeDays(today: LocalDate, date: LocalDate): String {
    val days = ChronoUnit.DAYS.between(today, date).toInt().coerceAtLeast(0)
    return when (days) {
        0 -> stringResource(R.string.upcoming_today)
        1 -> stringResource(R.string.upcoming_tomorrow)
        else -> stringResource(R.string.upcoming_in_days, days)
    }
}
