package com.cdi.moonphase.domain.analytics

import com.cdi.moonphase.domain.model.PhaseType
import java.time.LocalDate
import java.time.YearMonth

// ---------------------------------------------------------------------------------------------
// Naming convention (Fase 2 plan): events are snake_case (objeto_acao); property keys are
// camelCase. daysFromToday / monthsFromCurrent are ints where NEGATIVE = past.
// ---------------------------------------------------------------------------------------------

/** Resolved UI theme. A global property on every event and a share-card render param. */
enum class AnalyticsTheme(val wire: String) { LIGHT("light"), DARK("dark") }

/** Which location source the current session is using (gate-of-activation metric). */
enum class LocationMode(val wire: String) { GPS("gps"), DATE_FALLBACK("date_fallback") }

/** Screens that emit `screen_view`. */
enum class ScreenName(val wire: String) { HOME("home"), CALENDAR("calendar") }

/** Bottom-bar tabs for `tab_switched`. */
enum class AppTab(val wire: String) { HOME("home"), CALENDAR("calendar") }

enum class MonthChangeDirection(val wire: String) { FORWARD("forward"), BACK("back") }

enum class MonthChangeMethod(val wire: String) { CHEVRON("chevron"), SWIPE("swipe") }

enum class DetailSource(val wire: String) { CALENDAR("calendar"), UPCOMING_PANEL("upcoming_panel") }

enum class DetailDismissMethod(val wire: String) {
    SWIPE("swipe"), TAP_OUTSIDE("tap_outside"), BACK("back")
}

enum class UpcomingSurface(val wire: String) { HOME("home"), CALENDAR("calendar") }

enum class ShareContentType(val wire: String) { SINGLE_PHASE("single_phase"), MONTH("month") }

enum class ShareSurface(val wire: String) { DAY_DETAIL("day_detail"), CALENDAR_HEADER("calendar_header") }

enum class ShareFailureStage(val wire: String) { RENDER("render"), INTENT("intent") }

enum class PermissionPrimerActionType(val wire: String) { ALLOW("allow"), SKIP("skip") }

enum class PermissionSystemResultType(val wire: String) { GRANTED("granted"), DENIED("denied") }

/** Stable snake_case name for a phase, e.g. WAXING_GIBBOUS -> "waxing_gibbous". */
private fun PhaseType.wire(): String = name.lowercase()

/**
 * The closed catalog of product-analytics events for Fase 2 (calendário lunar). Each variant
 * carries strongly-typed inputs and exposes the wire [name] / [params] the backend serializes,
 * so call sites can never misspell an event or send an off-spec property value.
 *
 * Param values are restricted to types every backend can serialize (String / Int / Long /
 * Boolean). Global properties are appended by [AnalyticsTracker] implementations, not here.
 */
sealed class AnalyticsEvent(val name: String, val params: Map<String, Any?>) {

    // --- Telas ---------------------------------------------------------------------------------

    /** Entry into any tracked screen. `theme` rides along as a global property. */
    data class ScreenView(val screen: ScreenName) :
        AnalyticsEvent("screen_view", mapOf("screenName" to screen.wire))

    // --- Navegação -----------------------------------------------------------------------------

    data class TabSwitched(val fromTab: AppTab, val toTab: AppTab) :
        AnalyticsEvent("tab_switched", mapOf("fromTab" to fromTab.wire, "toTab" to toTab.wire))

    /**
     * Month change via chevron or swipe. [monthsFromCurrent] (negative = past) is the most
     * strategic signal of the phase: it separates planning (forward) from history (back).
     */
    data class CalendarMonthChanged(
        val direction: MonthChangeDirection,
        val method: MonthChangeMethod,
        val monthsFromCurrent: Int,
        val targetMonth: YearMonth,
    ) : AnalyticsEvent(
        "calendar_month_changed",
        mapOf(
            "direction" to direction.wire,
            "method" to method.wire,
            "monthsFromCurrent" to monthsFromCurrent,
            "targetMonth" to targetMonth.toString(), // YYYY-MM
        ),
    )

    data class CalendarReturnedToToday(val monthsTraveled: Int) :
        AnalyticsEvent("calendar_returned_to_today", mapOf("monthsTraveled" to monthsTraveled))

    // --- Calendário e detalhe ------------------------------------------------------------------

    data class CalendarDayTapped(
        val daysFromToday: Int,
        val phase: PhaseType,
        val isToday: Boolean,
    ) : AnalyticsEvent(
        "calendar_day_tapped",
        mapOf(
            "daysFromToday" to daysFromToday,
            "phaseName" to phase.wire(),
            "isToday" to isToday,
        ),
    )

    data class DayDetailOpened(
        val daysFromToday: Int,
        val phase: PhaseType,
        val source: DetailSource,
    ) : AnalyticsEvent(
        "day_detail_opened",
        mapOf(
            "daysFromToday" to daysFromToday,
            "phaseName" to phase.wire(),
            "source" to source.wire,
        ),
    )

    data class DayDetailDismissed(val method: DetailDismissMethod, val dwellMs: Long) :
        AnalyticsEvent(
            "day_detail_dismissed",
            mapOf("method" to method.wire, "dwellMs" to dwellMs),
        )

    // --- Próximas fases ------------------------------------------------------------------------

    data class UpcomingPanelViewed(val surface: UpcomingSurface) :
        AnalyticsEvent("upcoming_panel_viewed", mapOf("surface" to surface.wire))

    data class UpcomingPhaseTapped(
        val phase: PhaseType,
        val daysUntil: Int,
        val surface: UpcomingSurface,
    ) : AnalyticsEvent(
        "upcoming_phase_tapped",
        mapOf(
            "phaseName" to phase.wire(),
            "daysUntil" to daysUntil,
            "surface" to surface.wire,
        ),
    )

    // --- Compartilhamento (funil de 3 etapas) --------------------------------------------------

    data class ShareInitiated(
        val contentType: ShareContentType,
        val phase: PhaseType?,
        val referenceDate: LocalDate,
        val surface: ShareSurface,
    ) : AnalyticsEvent(
        "share_initiated",
        buildMap {
            put("contentType", contentType.wire)
            phase?.let { put("phaseName", it.wire()) }
            put("referenceDate", referenceDate.toString())
            put("surface", surface.wire)
        },
    )

    data class ShareImageGenerated(
        val contentType: ShareContentType,
        val cardTheme: AnalyticsTheme,
        val renderMs: Long,
    ) : AnalyticsEvent(
        "share_image_generated",
        mapOf(
            "contentType" to contentType.wire,
            "cardTheme" to cardTheme.wire,
            "renderMs" to renderMs,
        ),
    )

    /**
     * The native Android share sheet opened. This is the last point measurable with confidence
     * — treat it as the funnel conversion. There is deliberately no `share_completed`: an
     * ACTION_SEND chooser does not reliably report the chosen app or whether sending finished.
     */
    data class ShareSheetPresented(val contentType: ShareContentType) :
        AnalyticsEvent("share_sheet_presented", mapOf("contentType" to contentType.wire))

    data class ShareFailed(val stage: ShareFailureStage, val errorType: String) :
        AnalyticsEvent("share_failed", mapOf("stage" to stage.wire, "errorType" to errorType))

    // --- Permissão (herdada da Fase 1) ---------------------------------------------------------

    data object PermissionPrimerViewed : AnalyticsEvent("permission_primer_viewed", emptyMap())

    data class PermissionPrimerAction(val action: PermissionPrimerActionType) :
        AnalyticsEvent("permission_primer_action", mapOf("action" to action.wire))

    data class PermissionSystemResult(val result: PermissionSystemResultType) :
        AnalyticsEvent(
            "permission_system_result",
            mapOf("result" to result.wire, "precision" to "coarse"),
        )

    data class LocationModeActive(val mode: LocationMode) :
        AnalyticsEvent("location_mode_active", mapOf("mode" to mode.wire))
}
