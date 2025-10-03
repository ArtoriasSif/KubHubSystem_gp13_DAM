package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.navigation.MenuRoutes

@Composable
fun AppContainer() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MenuRoutes.Home.route
    ) {
        // Pantalla de inicio/splash
        composable(MenuRoutes.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(MenuRoutes.Login.route) {
                        popUpTo(MenuRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de login
        composable(MenuRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MenuRoutes.MainMenu.route) {
                        popUpTo(MenuRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla principal con menú lateral
        composable(MenuRoutes.MainMenu.route) {
            MainMenuScreen(
                onLogout = {
                    navController.navigate(MenuRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}