package com.cdi.moonphase.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cdi.moonphase.presentation.calendar.CalendarScreen
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import com.cdi.moonphase.presentation.home.HomeScreen
import com.cdi.moonphase.presentation.permission.PermissionScreen

/**
 * Single-Activity navigation graph. The Permission flow is full-screen; once the user reaches
 * the app proper, a 2-item bottom bar (Hoje + Calendário) appears and is hidden again on the
 * Permission route. Each screen still manages its own top/side insets, so the only inset the
 * Scaffold injects is the space reserved for the bottom bar.
 */
@Composable
fun MoonNavHost(
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentEntry?.destination
    val showBottomBar = currentDestination?.let {
        it.hasRoute(Destination.Home::class) || it.hasRoute(Destination.Calendar::class)
    } ?: false

    Scaffold(
        modifier = modifier,
        containerColor = MoonTheme.colors.background,
        // Screens draw full-bleed and apply their own top/side insets; the Scaffold only reserves
        // room for the bottom bar (which itself consumes the navigation-bar inset).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MoonBottomBar(navController = navController, currentEntry = currentEntry)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<Destination.Permission> {
                PermissionScreen(
                    onNavigateToHome = {
                        navController.navigate(Destination.Home) {
                            popUpTo(Destination.Permission) { inclusive = true }
                        }
                    },
                )
            }
            composable<Destination.Home> {
                HomeScreen(
                    onEnableLocation = { navController.navigate(Destination.Permission) },
                )
            }
            composable<Destination.Calendar> {
                CalendarScreen()
            }
        }
    }
}
