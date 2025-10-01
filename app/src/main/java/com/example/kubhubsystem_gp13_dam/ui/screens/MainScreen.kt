package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

@Composable
fun MainScreen() {
    var showLogin by remember { mutableStateOf(false) }
    var showHome by remember { mutableStateOf(true) }

    if (showHome) {
        HomeScreen(onNavigateToLogin = {
            showHome = false
            showLogin = true
        })
    } else if (showLogin) {
        LoginScreen(onLoginSuccess = {
            showLogin = false
            showHome = true // o abrir MenuScreen
        })
    }
}