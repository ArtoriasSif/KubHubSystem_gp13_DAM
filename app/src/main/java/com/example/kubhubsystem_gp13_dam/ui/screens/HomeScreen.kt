package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.kubhubsystem_gp13_dam.R
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(onNavigateToLogin: () -> Unit) {

    var step by remember { mutableStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        // Tamaño máximo cuadrado disponible
        val maxSize = min(maxWidth, maxHeight)

        // ✅ La bolita será un poco más grande (por ejemplo 45% del lado disponible)
        val circleSize = maxSize * 0.45f

        val logoSize = remember { Animatable(0.dp, Dp.VectorConverter) }

        LaunchedEffect(true) {
            // 🔹 1️⃣ Primera imagen circular
            logoSize.snapTo(0.dp)
            logoSize.animateTo(circleSize, animationSpec = tween(900))
            delay(500)
            step = 1

            // 🔹 2️⃣ Segunda imagen circular
            logoSize.snapTo(0.dp)
            logoSize.animateTo(circleSize, animationSpec = tween(1))
            delay(1000)
            step = 2

            // 🔹 3️⃣ Texto de bienvenida
            delay(1000)
            step = 3

            delay(1000)
            onNavigateToLogin()
        }

        when (step) {
            0 -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_webquest),
                    contentDescription = "Logo 1",
                    modifier = Modifier
                        .size(logoSize.value)
                        .clip(CircleShape)
                )
            }
            1 -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_duoc_gastotro_v1_transp_55),
                    contentDescription = "Logo 2",
                    modifier = Modifier
                        .size(logoSize.value)
                        .clip(CircleShape)
                )
            }
            2 -> {
                Text(
                    text = "¡Bienvenido a KubHub!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}