package com.example.kubhubsystem_gp13_dam.navigation

sealed class MenuRoutes(val route: String) {
    object Home : MenuRoutes("home")
    object Login : MenuRoutes("login")
    object MainMenu : MenuRoutes("main_menu")
    object Dashboard : MenuRoutes("dashboard")
    object Inventario : MenuRoutes("inventario")
    object Asignaturas : MenuRoutes("asignaturas")
    object Recetas : MenuRoutes("recetas")
    object Solicitud : MenuRoutes("solicitud")
    object Usuarios : MenuRoutes("usuarios")
}