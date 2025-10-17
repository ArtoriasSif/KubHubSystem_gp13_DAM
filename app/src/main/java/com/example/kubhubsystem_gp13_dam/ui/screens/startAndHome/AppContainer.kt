package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.ui.navigation.MenuRoutes
import com.example.kubhubsystem_gp13_dam.ui.screens.login.LoginScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.MainMenuScreen

@Composable
fun AppContainer() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ✅ Crear las dependencias necesarias
    val database = remember { AppDatabase.obtener(context) }
    val usuarioRepository = remember { UsuarioRepository(database.usuarioDao()) }

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

        // Pantalla de login - ✅ AHORA CON REPOSITORIO
        composable(MenuRoutes.Login.route) {
            LoginScreen(
                usuarioRepository = usuarioRepository, // ✅ Pasar el repositorio
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