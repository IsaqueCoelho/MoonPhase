package com.cdi.moonphase.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cdi.moonphase.presentation.home.HomeScreen
import com.cdi.moonphase.presentation.permission.PermissionScreen

/**
 * Single-Activity navigation graph. Home is the only durable destination: both granting and
 * skipping the permission lead there, and the back stack to Permission is cleared on arrival.
 */
@Composable
fun MoonNavHost(
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
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
                onEnableLocation = {
                    navController.navigate(Destination.Permission)
                },
            )
        }
    }
}
