package com.cdi.moonphase.presentation.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry
import com.cdi.moonphase.R
import com.cdi.moonphase.presentation.designsystem.MoonTheme

/** The two persistent destinations of the bottom bar: Hoje (Home) and Calendário. */
private enum class BottomTab(val destination: Destination, val labelRes: Int) {
    Today(Destination.Home, R.string.nav_today),
    Calendar(Destination.Calendar, R.string.nav_calendar),
}

/**
 * The 2-item bottom navigation (Hoje + Calendário). Two tabs keep the app breathing; sharing is
 * an action, not a destination (see the Fase 2 navigation decisions). Tab icons are drawn on a
 * Canvas to match the app's icon-font-free style.
 */
@Composable
fun MoonBottomBar(
    navController: NavHostController,
    currentEntry: NavBackStackEntry?,
) {
    val palette = MoonTheme.colors

    NavigationBar(containerColor = palette.surface, tonalElevation = 0.dp) {
        BottomTab.entries.forEach { tab ->
            val selected = currentEntry?.destination?.hasRoute(tab.destination::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.destination) {
                            // Single-top within the two tabs; preserve and restore each tab's state.
                            popUpTo(Destination.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    val tint = if (selected) palette.amber else palette.textTertiary
                    Canvas(modifier = Modifier.size(22.dp)) {
                        when (tab) {
                            BottomTab.Today -> drawMoonGlyph(tint)
                            BottomTab.Calendar -> drawCalendarGlyph(tint)
                        }
                    }
                },
                label = {
                    Text(text = stringResource(tab.labelRes), color = if (selected) palette.amber else palette.textTertiary)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = palette.background,
                    selectedIconColor = palette.amber,
                    unselectedIconColor = palette.textTertiary,
                    selectedTextColor = palette.amber,
                    unselectedTextColor = palette.textTertiary,
                ),
            )
        }
    }
}

private fun DrawScope.drawMoonGlyph(tint: Color) {
    // A solid disc reads clearly as "moon / today" at 22 dp.
    drawCircle(color = tint, radius = size.minDimension * 0.42f, center = center)
}

private fun DrawScope.drawCalendarGlyph(tint: Color) {
    val w = size.minDimension
    val stroke = w * 0.08f
    val left = w * 0.16f
    val top = w * 0.20f
    val boxW = w * 0.68f
    val boxH = w * 0.62f
    drawRoundRect(
        color = tint,
        topLeft = androidx.compose.ui.geometry.Offset(left, top),
        size = androidx.compose.ui.geometry.Size(boxW, boxH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke, stroke),
        style = Stroke(width = stroke),
    )
    // Header rule.
    drawLine(
        color = tint,
        start = androidx.compose.ui.geometry.Offset(left, top + boxH * 0.32f),
        end = androidx.compose.ui.geometry.Offset(left + boxW, top + boxH * 0.32f),
        strokeWidth = stroke,
    )
    // Two binding ticks.
    val tickY = top - boxH * 0.12f
    drawLine(tint, androidx.compose.ui.geometry.Offset(left + boxW * 0.3f, tickY), androidx.compose.ui.geometry.Offset(left + boxW * 0.3f, top + boxH * 0.08f), stroke, StrokeCap.Round)
    drawLine(tint, androidx.compose.ui.geometry.Offset(left + boxW * 0.7f, tickY), androidx.compose.ui.geometry.Offset(left + boxW * 0.7f, top + boxH * 0.08f), stroke, StrokeCap.Round)
}
