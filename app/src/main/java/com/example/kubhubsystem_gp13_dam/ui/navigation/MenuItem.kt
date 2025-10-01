package com.example.kubhubsystem_gp13_dam.ui.navigation

sealed class MenuItem(val title: String) {
    object Dashboard : MenuItem("Dashboard")
    object Solicitud : MenuItem("Solicitud")
    object Usuarios : MenuItem("Usuarios y Roles")
}