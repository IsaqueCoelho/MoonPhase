package com.cdi.moonphase.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdi.moonphase.R
import com.cdi.moonphase.domain.model.IlluminationFraction
import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonPhase
import com.cdi.moonphase.domain.model.PhaseType
import com.cdi.moonphase.domain.model.ThemeMode
import com.cdi.moonphase.presentation.common.UpcomingPhasesPanel
import com.cdi.moonphase.presentation.common.descriptionRes
import com.cdi.moonphase.presentation.common.nameRes
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonPhaseTheme
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import com.cdi.moonphase.presentation.splash.SplashBrand
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PT_BR = Locale("pt", "BR")

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
        onSelectTheme = viewModel::setTheme,
        modifier = modifier,
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onEnableLocation: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectTheme: (ThemeMode) -> Unit = {},
) {
    val palette = MoonTheme.colors
    val moonInfo = uiState.moonInfo

    Surface(modifier = modifier.fillMaxSize(), color = palette.background) {
        // First load shows the brand phase (REQ-02 stage 2) — never a bare spinner on white.
        if (uiState.isLoading && moonInfo == null) {
            SplashBrand()
            return@Surface
        }
        if (moonInfo == null) return@Surface

        // Top + sides only: the bottom bar (in the Scaffold) already reserves the bottom inset.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeContent.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                ),
        ) {
            // Top row: theme toggle on the right, generous 48 dp target.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                ThemeToggleButton(onSelectTheme = onSelectTheme)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(18.dp))
                MoonHero(moonInfo, uiState.upcomingEvents)
            }

            LocationFooter(
                label = uiState.locationLabel,
                isRefreshing = uiState.isRefreshing,
                onEnableLocation = onEnableLocation,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun MoonHero(moonInfo: MoonInfo, upcomingEvents: List<LunarEvent>) {
    val palette = MoonTheme.colors
    val phaseName = stringResource(moonInfo.phase.type.nameRes)

    MoonDisk(
        illumination = moonInfo.illumination.value.toFloat(),
        waxing = moonInfo.phase.isWaxing,
        haloPulse = true,
        contentDescription = phaseName,
        modifier = Modifier
            .size(180.dp)
            .aspectRatio(1f),
    )

    Spacer(Modifier.height(8.dp))
    Text(
        text = phaseName,
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = palette.textPrimary,
    )

    Spacer(Modifier.height(2.dp))
    Text(
        text = formattedDate(moonInfo.date),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = palette.textSecondary,
    )

    Spacer(Modifier.height(14.dp))
    IlluminationPill(moonInfo.illumination)

    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.home_lunar_day_format, moonInfo.lunarDay.dayOfCycle),
        fontSize = 12.sp,
        color = palette.textTertiary,
    )

    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(moonInfo.phase.type.descriptionRes),
        fontSize = 14.sp,
        lineHeight = 22.sp, // ~1.6 line-height
        color = palette.textSecondary,
        textAlign = TextAlign.Center,
    )

    // Fase 2: the single "próxima fase" card grows into the próximas-fases panel. Falls back to
    // the single next-event card if the upcoming list could not be projected.
    if (upcomingEvents.isNotEmpty()) {
        Spacer(Modifier.height(24.dp))
        UpcomingPhasesPanel(events = upcomingEvents, today = moonInfo.date)
    } else {
        moonInfo.nextEvent?.let { event ->
            Spacer(Modifier.height(24.dp))
            NextPhaseCard(event)
        }
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun IlluminationPill(illumination: IlluminationFraction) {
    val palette = MoonTheme.colors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(palette.surface)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = stringResource(R.string.home_illumination_format, illumination.percent),
            fontSize = 13.sp,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun NextPhaseCard(event: LunarEvent) {
    val palette = MoonTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.home_next_phase_label),
            fontSize = 11.sp,
            color = palette.textTertiary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.home_next_event_format,
                stringResource(event.phase.nameRes),
                formattedShortDate(event.date),
            ),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textPrimary,
        )
    }
}

@Composable
private fun LocationFooter(
    label: LocationLabel,
    isRefreshing: Boolean,
    onEnableLocation: () -> Unit,
    onRefresh: () -> Unit,
) {
    val palette = MoonTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        when (label) {
            LocationLabel.Undefined -> Spacer(Modifier.size(0.dp))
            LocationLabel.Active -> Text(
                text = stringResource(R.string.home_location_label_nearby),
                fontSize = 12.sp,
                color = palette.textTertiary,
            )
            LocationLabel.Disabled -> Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onEnableLocation)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stringResource(R.string.home_location_disabled),
                    fontSize = 12.sp,
                    color = palette.amber,
                )
            }
        }

        // "Atualizar": re-reads location and recomputes; >= 48 dp touch target.
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .widthIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = !isRefreshing, onClick = onRefresh)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.home_refresh),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isRefreshing) palette.textTertiary else palette.amber,
            )
        }
    }
}

/** A tappable sun/moon glyph that flips the theme. Shows the destination state's icon. */
@Composable
private fun ThemeToggleButton(onSelectTheme: (ThemeMode) -> Unit) {
    val palette = MoonTheme.colors
    val description = stringResource(R.string.home_toggle_theme)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onSelectTheme(if (palette.isDark) ThemeMode.LIGHT else ThemeMode.DARK) }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
            val tint = palette.textSecondary
            if (palette.isDark) {
                // Sun: filled core + rays (tap to go light).
                val core = size.minDimension * 0.28f
                drawCircle(color = tint, radius = core, center = center)
                val rayInner = size.minDimension * 0.40f
                val rayOuter = size.minDimension * 0.50f
                repeat(8) { i ->
                    val angle = Math.toRadians((i * 45).toDouble())
                    val dx = kotlin.math.cos(angle).toFloat()
                    val dy = kotlin.math.sin(angle).toFloat()
                    drawLine(
                        color = tint,
                        start = Offset(center.x + dx * rayInner, center.y + dy * rayInner),
                        end = Offset(center.x + dx * rayOuter, center.y + dy * rayOuter),
                        strokeWidth = size.minDimension * 0.06f,
                        cap = StrokeCap.Round,
                    )
                }
            } else {
                // Crescent moon: a disk with an offset cut-out painted in the background color
                // (the toggle sits directly on the solid screen background). Tap to go dark.
                val r = size.minDimension * 0.42f
                drawCircle(color = tint, radius = r, center = center)
                drawCircle(
                    color = palette.background,
                    radius = r,
                    center = Offset(center.x + r * 0.55f, center.y - r * 0.30f),
                )
            }
        }
    }
}

private fun formattedDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR)
    return date.format(formatter).replaceFirstChar { it.titlecase(PT_BR) }
}

private fun formattedShortDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d 'de' MMM", PT_BR))

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    MoonPhaseTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                moonInfo = MoonInfo(
                    date = LocalDate.now(),
                    phase = MoonPhase(PhaseType.WAXING_GIBBOUS, isWaxing = true),
                    illumination = IlluminationFraction(0.78),
                    lunarDay = LunarDay(11.0),
                    nextEvent = LunarEvent(PhaseType.FULL, LocalDate.now().plusDays(3)),
                    location = null,
                ),
                locationLabel = LocationLabel.Disabled,
            ),
            onRefresh = {},
            onEnableLocation = {},
        )
    }
}
