package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.model.UserRole
import com.example.kubhubsystem_gp13_dam.ui.theme.loginTextFieldColors
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var forgotPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Box central del formulario
        Box(
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 30.dp, vertical = 10.dp)
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

                // Campo de correo
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Correo Electrónico*", color = MaterialTheme.colorScheme.onSurface) },
                    placeholder = { Text("correo@ejemplo.com", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = loginTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage != null
                )

                // Campo de contraseña
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Contraseña*", color = MaterialTheme.colorScheme.onSurface) },
                    placeholder = { Text("********", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    colors = loginTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage != null
                )

                // Mensaje de error
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila: Recordar sesión y ¿Olvidó su contraseña?
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox de recordar sesión
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = uiState.rememberSession,
                            onCheckedChange = { viewModel.updateRememberSession(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = true,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Recordar sesión",
                            color = MaterialTheme.colorScheme.inverseSurface
                        )
                    }

                    // Texto "¿Olvidó su contraseña?"
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

                // Botón de iniciar sesión
                Button(
                    onClick = { viewModel.login(onLoginSuccess) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.scrim
                        )
                    } else {
                        Text(
                            text = "Iniciar sesión",
                            color = MaterialTheme.colorScheme.scrim,
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }

                // Línea separadora
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Textos de acceso rápido
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Acceso Rápido - Cuentas Demo",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Haz clic en cualquier rol para autocompletar las credenciales",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Grid de roles
                RolesGrid(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = { role -> viewModel.selectDemoRole(role) }
                )
            }
        }

        // Footer
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
fun RolesGrid(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit
) {
    // Lista de roles disponibles
    val roles = listOf(
        UserRole.ADMIN,
        UserRole.CO_ADMIN,
        UserRole.GESTOR_PEDIDOS,
        UserRole.PROFESOR,
        UserRole.BODEGA,
        UserRole.ASISTENTE
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dividimos en filas de 3
        roles.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { role ->
                    RoleButton(
                        title = role.displayName,
                        subtitle = role.description,
                        selected = selectedRole == role,
                        onClick = { onRoleSelected(role) },
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
                fontWeight = FontWeight.Normal
            )
        }
    }
}