package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.kubhubsystem_gp13_dam.R
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.AppInitializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigateToLogin: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    var initializationComplete by remember { mutableStateOf(false) }
    var dotAnimation by remember { mutableStateOf(".") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val database = remember { AppDatabase.obtener(context) }
    val appInitializer = remember { AppInitializer(database) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        val maxSize = min(maxWidth, maxHeight)
        val circleSize = maxSize * 0.45f
        val logoSize = remember { Animatable(0.dp, Dp.VectorConverter) }

        // Animación de puntos suspensivos
        LaunchedEffect(initializationComplete) {
            var counter = 0
            while (!initializationComplete) {
                dotAnimation = when (counter % 3) {
                    0 -> "."
                    1 -> ".."
                    else -> "..."
                }
                counter++
                delay(500)
            }
            dotAnimation = ""
        }

        // Inicialización y animaciones
        LaunchedEffect(Unit) {
            // Animaciones iniciales de logos
            logoSize.snapTo(0.dp)
            logoSize.animateTo(circleSize, animationSpec = tween(900))
            delay(500)
            step = 1

            logoSize.snapTo(0.dp)
            logoSize.animateTo(circleSize, animationSpec = tween(900))
            delay(1000)
            step = 2

            delay(1000)
            step = 3

            // Inicialización silenciosa de la base de datos
            val initializationJob = coroutineScope.launch {
                try {
                    appInitializer.inicializarTodo { /* No mostramos progreso */ }
                    initializationComplete = true
                } catch (e: Exception) {
                    // En caso de error, igual procedemos
                    initializationComplete = true
                }
            }

            // Esperar a que la inicialización termine
            initializationJob.join()

            // Navegar después de un breve delay
            delay(1000)
            onNavigateToLogin()
        }

        when (step) {
            0 -> {
                Image(
                    painter = painterResource(id = R.drawable.questweboficial),
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
            2, 3 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "¡Bienvenido a KuHub!",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!initializationComplete) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando$dotAnimation",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    // ✅ No mostramos nada cuando la inicialización está completa
                    // Simplemente mantenemos el mensaje de bienvenida
                }
            }
        }
    }
}