package com.example.kubhubsystem_gp13_dam.ui.theme.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToLogin: () -> Unit) {

    var showContent by remember { mutableStateOf(false) }

    val circleSize = remember { Animatable(50f) }

    LaunchedEffect(true) {
        // Animación inicial de la bolita
        circleSize.animateTo(
            targetValue = 300f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(500)
        showContent = true
        delay(1000)
        onNavigateToLogin() // callback para mostrar LoginScreen
    }

    // Caja principal centrada
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (!showContent) {
            // Bolita animada
            Box(
                modifier = Modifier
                    .size(circleSize.value.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        } else {
            // Texto de bienvenida centrado
            Text(
                text = "¡Bienvenido a KubHub!",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
