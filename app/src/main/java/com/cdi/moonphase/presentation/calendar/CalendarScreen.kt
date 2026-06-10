package com.cdi.moonphase.presentation.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdi.moonphase.R
import com.cdi.moonphase.domain.model.CalendarDay
import com.cdi.moonphase.domain.model.CalendarMonth
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.presentation.common.UpcomingPhasesPanel
import com.cdi.moonphase.presentation.common.formatMonthYear
import com.cdi.moonphase.presentation.common.nameRes
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import com.cdi.moonphase.presentation.share.ImageSharer
import com.cdi.moonphase.presentation.share.ShareCardRenderer
import com.cdi.moonphase.presentation.share.rememberShareTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarContent(
        uiState = uiState,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onSelectDate = viewModel::selectDate,
        onUpcomingEventClick = { viewModel.selectUpcomingDate(it.date) },
        onDismissDayDetail = viewModel::dismissDayDetail,
        modifier = modifier,
    )
}

@Composable
internal fun CalendarContent(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onUpcomingEventClick: (LunarEvent) -> Unit,
    onDismissDayDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MoonTheme.colors
    val shareTheme = rememberShareTheme()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weekdays = stringArrayResource(R.array.calendar_weekday_initials).toList()
    val monthTitle = uiState.visibleMonth.formatMonthYear()
    val brand = stringResource(R.string.share_brand)
    val monthChooserTitle = stringResource(R.string.share_month_chooser_title)

    Surface(modifier = modifier.fillMaxSize(), color = palette.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeContent.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                )
                .padding(horizontal = 20.dp),
        ) {
            CalendarHeader(
                title = monthTitle,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onShareMonth = uiState.calendar?.let { calendar ->
                    {
                        scope.launch {
                            val bitmap = withContext(Dispatchers.Default) {
                                ShareCardRenderer.renderMonthCard(
                                    theme = shareTheme,
                                    calendar = calendar,
                                    today = uiState.today,
                                    monthTitle = monthTitle,
                                    weekdayInitials = weekdays,
                                    brand = brand,
                                )
                            }
                            ImageSharer.share(context, bitmap, "moon-month.png", monthChooserTitle)
                        }
                    }
                },
            )

            Spacer(Modifier.height(8.dp))
            WeekdayRow(weekdays)
            Spacer(Modifier.height(4.dp))

            MonthGrid(
                visibleMonth = uiState.visibleMonth,
                calendar = uiState.calendar,
                today = uiState.today,
                onSelectDate = onSelectDate,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                modifier = Modifier.weight(1f),
            )

            if (uiState.upcomingEvents.isNotEmpty()) {
                UpcomingPhasesPanel(
                    events = uiState.upcomingEvents,
                    today = uiState.today,
                    onEventClick = onUpcomingEventClick,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
    }

    uiState.selectedDay?.let { day ->
        DayDetailSheet(moonInfo = day, onDismiss = onDismissDayDetail)
    }
}

@Composable
private fun CalendarHeader(
    title: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShareMonth: (() -> Unit)?,
) {
    val palette = MoonTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chevron(text = "‹", description = stringResource(R.string.calendar_prev_month), onClick = onPreviousMonth)
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Chevron(text = "›", description = stringResource(R.string.calendar_next_month), onClick = onNextMonth)
        ShareMonthAction(enabled = onShareMonth != null, onClick = onShareMonth ?: {})
    }
}

@Composable
private fun Chevron(text: String, description: String, onClick: () -> Unit) {
    val palette = MoonTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 22.sp, color = palette.textSecondary)
    }
}

@Composable
private fun ShareMonthAction(enabled: Boolean, onClick: () -> Unit) {
    val palette = MoonTheme.colors
    val description = stringResource(R.string.calendar_share_month)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⤴",
            fontSize = 18.sp,
            color = if (enabled) palette.amber else palette.textTertiary,
        )
    }
}

@Composable
private fun WeekdayRow(weekdays: List<String>) {
    val palette = MoonTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEach { label ->
            Text(
                text = label,
                fontSize = 11.sp,
                color = palette.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    visibleMonth: YearMonth,
    calendar: CalendarMonth?,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeModifier = modifier.pointerInput(visibleMonth) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onDragEnd = {
                val threshold = size.width * 0.15f
                when {
                    totalDrag > threshold -> onPreviousMonth()
                    totalDrag < -threshold -> onNextMonth()
                }
            },
        ) { _, dragAmount -> totalDrag += dragAmount }
    }

    Box(modifier = swipeModifier.fillMaxSize()) {
        // Slide + fade ~300 ms; direction follows the chronological order of the months shown.
        AnimatedContent(
            targetState = visibleMonth to calendar,
            transitionSpec = {
                val forward = targetState.first.isAfter(initialState.first)
                val width = if (forward) 1 else -1
                (slideInHorizontally(tween(300)) { w -> width * w } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { w -> -width * w } + fadeOut(tween(300)))
            },
            label = "month",
        ) { (month, monthCalendar) ->
            // Only the calendar matching this animated month is drawn; mismatches show nothing
            // (the brief loading frame), avoiding stale-grid flashes during fast navigation.
            if (monthCalendar != null && monthCalendar.yearMonth == month) {
                MonthGridContent(monthCalendar, today, onSelectDate)
            } else {
                Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun MonthGridContent(
    calendar: CalendarMonth,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    val firstDay = calendar.yearMonth.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value % 7 // Sunday-start: SUNDAY(7)%7 = 0
    val totalCells = leadingBlanks + calendar.days.size
    val rowCount = ceil(totalCells / 7.0).toInt()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        repeat(rowCount) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayIndex = cellIndex - leadingBlanks
                    val day = calendar.days.getOrNull(dayIndex)?.takeIf { dayIndex >= 0 }
                    Box(modifier = Modifier.weight(1f)) {
                        if (day != null) {
                            DayCell(day = day, isToday = day.date == today, onClick = { onSelectDate(day.date) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: CalendarDay, isToday: Boolean, onClick: () -> Unit) {
    val palette = MoonTheme.colors
    val phaseName = stringResource(day.phase.type.nameRes)
    val description = stringResource(
        R.string.calendar_day_cd,
        phaseName,
        day.illumination.percent,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val ringModifier = if (isToday) {
            Modifier.size(28.dp).border(2.dp, palette.amber, CircleShape)
        } else {
            Modifier.size(28.dp)
        }
        Box(modifier = ringModifier, contentAlignment = Alignment.Center) {
            MoonDisk(
                illumination = day.illumination.value.toFloat(),
                waxing = day.phase.isWaxing,
                decorate = false,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 10.sp,
            color = palette.textSecondary,
        )
    }
}
