package com.cdi.moonphase.util

import com.cdi.moonphase.domain.model.CalendarDay
import com.cdi.moonphase.domain.model.CalendarMonth
import com.cdi.moonphase.domain.model.IlluminationFraction
import com.cdi.moonphase.domain.model.LocationResult
import com.cdi.moonphase.domain.model.LunarDay
import com.cdi.moonphase.domain.model.LunarEvent
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.domain.model.MoonLocation
import com.cdi.moonphase.domain.model.MoonPhase
import com.cdi.moonphase.domain.analytics.AnalyticsEvent
import com.cdi.moonphase.domain.analytics.AnalyticsTheme
import com.cdi.moonphase.domain.analytics.AnalyticsTracker
import com.cdi.moonphase.domain.analytics.LocationMode
import com.cdi.moonphase.domain.model.PhaseType
import com.cdi.moonphase.domain.model.ThemeMode
import com.cdi.moonphase.domain.repository.LocationRepository
import com.cdi.moonphase.domain.repository.MoonRepository
import com.cdi.moonphase.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.YearMonth

fun fakeMoonInfo(
    date: LocalDate = LocalDate.of(2024, 1, 25),
    type: PhaseType = PhaseType.FULL,
    isWaxing: Boolean = false,
    illumination: Double = 1.0,
    location: MoonLocation? = null,
): MoonInfo = MoonInfo(
    date = date,
    phase = MoonPhase(type, isWaxing),
    illumination = IlluminationFraction(illumination),
    lunarDay = LunarDay(14.0),
    nextEvent = LunarEvent(PhaseType.LAST_QUARTER, date.plusDays(7)),
    location = location,
)

/** Returns a [MoonInfo] echoing whatever location it is handed, so tests can assert it flows through. */
class FakeMoonRepository(
    private val base: MoonInfo = fakeMoonInfo(),
) : MoonRepository {
    override suspend fun getMoonInfo(date: LocalDate, location: MoonLocation?): MoonInfo =
        base.copy(date = date, location = location)

    override suspend fun getNextEvent(date: LocalDate): LunarEvent =
        base.nextEvent ?: LunarEvent(PhaseType.NEW, date.plusDays(1))

    override suspend fun getMonth(month: YearMonth): CalendarMonth =
        CalendarMonth(
            yearMonth = month,
            days = (1..month.lengthOfMonth()).map { day ->
                CalendarDay(
                    date = month.atDay(day),
                    phase = base.phase,
                    illumination = base.illumination,
                )
            },
        )

    override suspend fun getUpcomingEvents(date: LocalDate, count: Int): List<LunarEvent> =
        List(count) { index ->
            LunarEvent(PhaseType.entries[index % PhaseType.entries.size], date.plusDays((index + 1).toLong() * 7))
        }
}

class FakeLocationRepository(
    var result: LocationResult = LocationResult.Unavailable,
) : LocationRepository {
    override suspend fun getCurrentLocation(): LocationResult = result
}

class FakeUserPrefsRepository(
    initialTheme: ThemeMode = ThemeMode.SYSTEM,
    initialPrimed: Boolean = false,
) : UserPrefsRepository {
    private val themeState = MutableStateFlow(initialTheme)
    private val primedState = MutableStateFlow(initialPrimed)
    private var firstOpenDate: String? = null

    override val themeMode: Flow<ThemeMode> = themeState
    override val hasPrimedLocationPermission: Flow<Boolean> = primedState

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeState.value = mode
    }

    override suspend fun setHasPrimedLocationPermission(primed: Boolean) {
        primedState.value = primed
    }

    override suspend fun firstOpenDateOrSet(today: String): String =
        firstOpenDate ?: today.also { firstOpenDate = it }

    fun currentPrimed(): Boolean = primedState.value
}

/** Records every tracked event and the latest global properties so tests can assert on them. */
class RecordingAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    var theme: AnalyticsTheme? = null
        private set
    var locationMode: LocationMode? = null
        private set

    override fun track(event: AnalyticsEvent) {
        events += event
    }

    override fun setTheme(theme: AnalyticsTheme) {
        this.theme = theme
    }

    override fun setLocationMode(mode: LocationMode) {
        locationMode = mode
    }
}
