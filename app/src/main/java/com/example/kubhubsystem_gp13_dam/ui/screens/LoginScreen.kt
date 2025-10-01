package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.R
import com.example.kubhubsystem_gp13_dam.ui.theme.ErrorTextStyle
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import com.example.kubhubsystem_gp13_dam.model.ErrorType

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    val images = listOf(R.drawable.plato1, R.drawable.plato2, R.drawable.plato3, R.drawable.plato4)
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            currentImageIndex = (currentImageIndex + 1) % images.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Image(
            painter = painterResource(id = images[currentImageIndex]),
            contentDescription = "Imagen de fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Usuario
            TextField(
                value = viewModel.username,
                onValueChange = { viewModel.username = it },
                label = { Text("Usuario") },
                singleLine = true,
                isError = viewModel.userError?.type == ErrorType.USERNAME || viewModel.userError?.type == ErrorType.BOTH
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Contraseña
            TextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = viewModel.userError?.type == ErrorType.PASSWORD || viewModel.userError?.type == ErrorType.BOTH
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                viewModel.login()
                if (viewModel.userError == null) {
                    onLoginSuccess()
                    viewModel.clearFields()
                }
            }) {
                Text("Ingresar")
            }

            viewModel.userError?.let { error ->
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = error.message,
                    style = ErrorTextStyle,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}