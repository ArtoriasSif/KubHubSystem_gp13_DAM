package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Tamaño máximo cuadrado disponible en dp
        val maxSizeDp: Dp = min(maxWidth, maxHeight)

        // Tamaño del círculo en dp (45% del lado máximo) proporcinal a pantalla
        val circleSizeDp: Dp = maxSizeDp * 0.45f
        /*IMP TODO-- Animatable: es un estado que puede animarse suavemente entre valores .
        TODO-- En este caso, logoSize va a representar el tamaño actual del logo/círculo mientras se anima de 0 dp a circleSizeDp.
        TODO-- remember { ... }: hace que Compose recuerde este estado incluso si la UI se recompone, evitando que la animación se reinicie.
        TODO-- Dp.VectorConverter: le dice a Animatable cómo interpolar (animar) entre valores de Dp, ya que Dp no es un número simple sino una unidad de medida en Compose.*/
        val logoSize = remember { Animatable(0.dp, Dp.VectorConverter) }

        LaunchedEffect(true) {
            // Primera animación
            logoSize.snapTo(0.dp)
            logoSize.animateTo(circleSizeDp, animationSpec = tween(900))
            delay(500)
            step = 1

            // Segunda animación
            logoSize.snapTo(0.dp)
            logoSize.animateTo(circleSizeDp, animationSpec = tween(1))
            delay(1000)
            step = 2

            // Mostrar texto
            delay(1000)
            step = 3

            // Navegar a login
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
                        .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                )
            }
            1 -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_duoc_gastotro_v1_transp_55),
                    contentDescription = "Logo 2",
                    modifier = Modifier
                        .size(logoSize.value)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                )
            }
            2 -> {
                Text(
                    text = "¡Bienvenido a KubHub!",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}