package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height 
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight 
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow 
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.ui.theme.loginTextFieldColors
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {}
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberSession by remember { mutableStateOf(false) }
    var forgotPressed by remember { mutableStateOf(false) }
    
    // Box principal que ocupa toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupa todoo el espacio disponible
            .background(MaterialTheme.colorScheme.surface )
    ) {
        // Box central donde estará el formulario de login
        Box(
            modifier = Modifier
                .fillMaxWidth(0.80f) // 85% del ancho de la pantalla
                .wrapContentHeight() // Ajusta su altura al contenido
                .align(Alignment.Center) // Lo centra en el Box principal
                .background(MaterialTheme.colorScheme.secondaryContainer , shape = RoundedCornerShape(16.dp)) //esquinas redondeadas
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 30.dp , vertical = 10.dp) // <-- Aquí agregas más separación del borde
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Título principal
                Text(
                    text = "Iniciar sesión",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )

                // Subtítulo
                Text(
                    text = "Ingrese sus credenciales para acceder",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )


                // Aquí van los elementos del login
                // OutlinedTextField para correo
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico*", color = MaterialTheme.colorScheme.onSurface ) },
                    placeholder = { Text("correo@ejemplo.com", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)) },
                    //leadingIcon = {
                    //Icon(
                    //imageVector = Icons.Default.Email,
                    //contentDescription = "Icono de correo",
                    //tint = Color.Gray
                    //)
                    //},
                    singleLine = true,
                    shape = RoundedCornerShape(2.dp),
                    colors = loginTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // OutlinedTextField para contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña*", color = MaterialTheme.colorScheme.onSurface ) },
                    placeholder = { Text("********", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)) },
                    //leadingIcon = {
                    //Icon(
                    // imageVector = Icons.Default.Lock,
                    // contentDescription = "Icono de contraseña",
                    // tint = Color.Gray
                    //)
                    //},
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    colors = loginTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Fila: Recordar sesión y ¿Olvidó su contraseña?
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox cuadrado/redondeado con label
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberSession,
                            onCheckedChange = { rememberSession = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = true,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp)) // aplica la forma aquí
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Recordar sesión",
                            color = MaterialTheme.colorScheme.inverseSurface
                        )
                    }

                    // Texto “¿Olvidó su contraseña?” con efecto clic
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (forgotPressed) MaterialTheme.colorScheme.outlineVariant
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                            .clickable { forgotPressed = !forgotPressed }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "¿Olvidó su contraseña?",
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }


                }


                Button(
                    onClick = { /* Acción de login */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp) // <-- Aquí defines el radio de las esquinas
                ) {
                    Text(text = "Iniciar sesión",
                            color = MaterialTheme.colorScheme.scrim,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )

                }

                // Línea separadora horizontal
                Divider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), // color de la línea
                    thickness = 1.dp, // grosor de la línea
                    modifier = Modifier.fillMaxWidth()
                )

                // Box para textos de acceso rápido
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp), // poco espacio arriba y abajo
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp) // poco espacio entre textos
                    ) {
                        // Texto principal
                        Text(
                            text = "Acceso Rápido - Cuentas Demo",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center // centrado horizontalmente
                        )

                        // Texto secundario
                        Text(
                            text = "Haz clic en cualquier rol para autocompletar las credenciales",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center // centrado horizontalmente
                        )
                    }
                }

                RolesGrid()

            }//end Column box login


        }

        // Otros elementos debajo del Box central
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "© 2025 KuHub System | Version 0.1",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}


@Composable
fun RolesGrid() {
    // Lista con los 6 roles
    val roles = listOf(
        "Admin" to "Acceso total al sistema",
        "Co-Admin" to "Casi todos los permisos",
        "Gestor de pedidos" to "Gestión de pedidos",
        "Profesor" to "Solicitudes y consultas",
        "Bodega" to "Control de inventario",
        "Asistente" to "Bodega en tránsito"
    )

    // Estado: cuál está seleccionado (-1 = ninguno)
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dividimos la lista en filas de 3
        roles.chunked(3).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEachIndexed { colIndex, (title, subtitle) ->
                    val index = rowIndex * 3 + colIndex
                    RoleButton(
                        title = title,
                        subtitle = subtitle,
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun RoleButton(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        border = BorderStroke(
            1.dp,
            if (selected)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = if (selected)
                    MaterialTheme.colorScheme.scrim
                else
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = if (selected)
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }



}
