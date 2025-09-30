package com.example.kubhubsystem_gp13_dam.ui.theme

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
fun HomeScreen() {

    // Estado para mostrar los componentes después de la animación
    var showContent by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Animación del tamaño de la bolita
    val circleSize = remember { Animatable(50f) }

    // Lanzamos la animación al iniciar
    LaunchedEffect(true) {
        // Animación inicial de la bolita
        circleSize.animateTo(
            targetValue = 300f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(500) // espera antes de mostrar "¡Bienvenido!"
        showContent = true

        delay(1000) // espera extra antes de abrir el menú
        showMenu = true
    }

    // Fondo de toda la pantalla con color Primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        when {
            !showContent -> {
                // Bolita animada
                Box(
                    modifier = Modifier
                        .size(circleSize.value.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
            showMenu -> {
                // Mostrar el menú
                MenuScreen()
            }
            else -> {
                // Mostrar mensaje de bienvenida
                Text(
                    text = "¡Bienvenido a KubHub!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
