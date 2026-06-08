package com.cdi.moonphase.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.cdi.moonphase.R
import com.cdi.moonphase.domain.model.IlluminationFraction
import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonPhase
import com.cdi.moonphase.domain.model.PhaseType
import com.cdi.moonphase.presentation.common.descriptionRes
import com.cdi.moonphase.presentation.common.nameRes
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonPhaseTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onEnableLocation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onEnableLocation = onEnableLocation,
        modifier = modifier,
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onEnableLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val moonInfo = uiState.moonInfo
            if (uiState.isLoading || moonInfo == null) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.home_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MoonDetails(moonInfo = moonInfo)
                Spacer(Modifier.size(28.dp))
                Button(onClick = onRefresh, enabled = !uiState.isRefreshing) {
                    Text(stringResource(R.string.home_refresh))
                }
            }

            Spacer(Modifier.size(24.dp))
            LocationFooter(label = uiState.locationLabel, onEnableLocation = onEnableLocation)
        }
    }
}

@Composable
private fun MoonDetails(moonInfo: MoonInfo) {
    MoonDisk(
        illumination = moonInfo.illumination.value.toFloat(),
        waxing = moonInfo.phase.isWaxing,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f),
    )
    Spacer(Modifier.size(24.dp))
    Text(
        text = stringResource(moonInfo.phase.type.nameRes),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.size(4.dp))
    Text(
        text = stringResource(R.string.home_illumination_format, moonInfo.illumination.percent),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.size(2.dp))
    Text(
        text = stringResource(R.string.home_lunar_day_format, moonInfo.lunarDay.dayOfCycle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.size(12.dp))
    Text(
        text = stringResource(moonInfo.phase.type.descriptionRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    moonInfo.nextEvent?.let { event ->
        Spacer(Modifier.size(12.dp))
        val formatter = remember { DateTimeFormatter.ofPattern("d 'de' MMM", Locale("pt", "BR")) }
        Text(
            text = stringResource(
                R.string.home_next_event_format,
                stringResource(event.phase.nameRes),
                event.date.format(formatter),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LocationFooter(label: LocationLabel, onEnableLocation: () -> Unit) {
    when (label) {
        LocationLabel.Undefined -> Unit
        LocationLabel.Active -> Text(
            text = stringResource(R.string.home_location_active),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LocationLabel.Disabled -> Text(
            text = stringResource(R.string.home_location_disabled),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEnableLocation),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    MoonPhaseTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                moonInfo = MoonInfo(
                    date = LocalDate.now(),
                    phase = MoonPhase(PhaseType.FULL, false),
                    illumination = IlluminationFraction(1.0),
                    lunarDay = LunarDay(14.0),
                    nextEvent = LunarEvent(PhaseType.LAST_QUARTER, LocalDate.now().plusDays(7)),
                    location = null
                ),
                locationLabel = LocationLabel.Active
            ),
            onRefresh = {},
            onEnableLocation = {}
        )
    }
}
