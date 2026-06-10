# Analytics (Fase 2)

Implements the product-analytics tag plan in `fase 2/analytics-fase2.txt`.

## Architecture

Provider-agnostic seam, wired through Hilt:

- `domain/analytics/AnalyticsTracker` — the only type feature code touches.
- `domain/analytics/AnalyticsEvent` — closed, type-safe catalog of every event in the plan.
  Each variant owns its wire `name` (snake_case) and `params` (camelCase). Off-spec property
  values are impossible to send because every constrained value is an enum.
- `data/analytics/DefaultAnalyticsTracker` — owns the global properties and fans each event out
  to all backends.
- `data/analytics/AnalyticsBackend` — a destination. Two implementations:
  - `FirebaseAnalyticsBackend` — joins only when a default `FirebaseApp` exists.
  - `LogcatAnalyticsBackend` — debug builds only (tag `MoonAnalytics`).

### Global properties (attached to every event)

`appVersion`, `phaseFeatureSet` (`v2`), `theme`, `locationMode`, `sessionId`, `firstOpenDate`.
Set as Firebase user properties and merged into each event's params. `theme` is published from
`MainActivity` (only place the resolved light/dark is known); `locationMode` from `HomeViewModel`;
`firstOpenDate` is stamped once in DataStore (`UserPrefsRepository.firstOpenDateOrSet`).

## Enabling Firebase reporting

The build is intentionally dormant until configured, so it compiles and runs today:

1. Add your `app/google-services.json` from the Firebase console (package `com.cdi.moonphase`).
2. Rebuild. `app/build.gradle.kts` auto-applies the google-services plugin when that file is
   present, and `FirebaseAnalyticsBackend` then activates with no code change.

Until then, debug builds still print the full event stream to logcat.

## Event coverage

Wired into existing surfaces:

| Event | Where |
|-------|-------|
| `screen_view` (home) | `HomeViewModel.init` |
| `location_mode_active` | `HomeViewModel` (once per session) |
| `permission_primer_viewed` | `PermissionViewModel.init` |
| `permission_primer_action` (allow/skip) | `PermissionViewModel` |
| `permission_system_result` | `PermissionViewModel.onPermissionResult` |

Defined and ready, awaiting their Phase-2 screen (calendar, day-detail sheet, upcoming-phases
panel, share, bottom bar — none built on this branch yet): `screen_view` (calendar),
`tab_switched`, `calendar_month_changed`, `calendar_returned_to_today`, `calendar_day_tapped`,
`day_detail_opened`, `day_detail_dismissed`, `upcoming_panel_viewed`, `upcoming_phase_tapped`,
`share_initiated`, `share_image_generated`, `share_sheet_presented`, `share_failed`.

To emit one, inject `AnalyticsTracker` into the relevant ViewModel and call `track(...)` with the
matching `AnalyticsEvent`.
