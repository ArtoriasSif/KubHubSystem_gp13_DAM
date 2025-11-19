/**
package com.example.kubhubsystem_gp13_dam.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.UserRole
import com.example.kubhubsystem_gp13_dam.ui.theme.loginTextFieldColors
import com.example.kubhubsystem_gp13_dam.viewmodel.LocationViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    val loginViewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(context) as T
            }
        }
    )

    val locationViewModel: LocationViewModel = remember { LocationViewModel(context) }

    val uiState by loginViewModel.uiState.collectAsState()
    val locationUiState by locationViewModel.uiState.collectAsState()

    var showDemoAccounts by remember { mutableStateOf(false) }
    var isLoadingDemoAccounts by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordPressed by remember { mutableStateOf(false) }

    val hasError by remember { derivedStateOf { uiState.errorMessage != null } }
    val isLoading by remember { derivedStateOf { uiState.isLoading } }
    val selectedRole by remember { derivedStateOf { uiState.selectedRole } }
    val shouldShowDemoGrid by remember { derivedStateOf { showDemoAccounts && !isLoadingDemoAccounts } }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            locationViewModel.checkPermissions()
            locationViewModel.getLocationWithDelay(delayMillis = 3000L)
        }
    }

    LaunchedEffect(Unit) {
        locationViewModel.checkPermissions()
        if (locationUiState.hasPermission) {
            locationViewModel.getLocationWithDelay(delayMillis = 3000L)
        } else {
            delay(1000L)
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(locationUiState.location) {
        locationUiState.location?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "📍 Ubicación: ${locationViewModel.getFormattedLocation()}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 48.dp)
    ) {
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
                Text(
                    text = "Iniciar sesión",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )

                Text(
                    text = "Ingrese sus credenciales para acceder",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )

                LoginTextField(
                    value = uiState.email,
                    onValueChange = { loginViewModel.updateEmail(it) },
                    label = "Correo Electrónico*",
                    placeholder = "correo@ejemplo.com",
                    isError = hasError,
                    leadingIcon = Icons.Default.Email
                )

                PasswordTextField(
                    value = uiState.password,
                    onValueChange = { loginViewModel.updatePassword(it) },
                    label = "Contraseña*",
                    placeholder = "********",
                    isError = hasError
                )

                AnimatedVisibility(
                    visible = hasError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                RememberAndForgotRow(
                    rememberSession = false,
                    forgotPasswordPressed = forgotPasswordPressed,
                    onRememberSessionChange = { },
                    onForgotPasswordClick = {
                        forgotPasswordPressed = true
                        showForgotPasswordDialog = true
                    }
                )

                LoginButton(
                    isLoading = isLoading,
                    onClick = { loginViewModel.login(onSuccess = onLoginSuccess) }
                )

                val coroutineScope = rememberCoroutineScope()

                DemoAccountsButton(
                    isExpanded = showDemoAccounts,
                    isLoading = isLoadingDemoAccounts,
                    hasSelectedRole = selectedRole != null,
                    onClick = {
                        when {
                            showDemoAccounts && uiState.selectedRole != null -> {
                                showDemoAccounts = false
                                loginViewModel.clearDemoSelection()
                            }
                            showDemoAccounts && uiState.selectedRole == null -> {
                                showDemoAccounts = false
                            }
                            !showDemoAccounts -> {
                                isLoadingDemoAccounts = true
                                coroutineScope.launch {
                                    delay(1000)
                                    isLoadingDemoAccounts = false
                                    showDemoAccounts = true
                                }
                            }
                        }
                    }
                )

                AnimatedVisibility(
                    visible = shouldShowDemoGrid,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        Text(
                            text = "Haz clic en cualquier rol para autocompletar las credenciales",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showDemoAccounts && !isLoadingDemoAccounts,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    RolesGrid(
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = { role -> loginViewModel.selectDemoRole(role) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "© 2025 KuHub System | Version 0.1",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }

        SideEffect {
            if (showDemoAccounts && isLoadingDemoAccounts) {
                println("⚠️ ERROR: Ambos estados activos simultáneamente")
            }
            if (!showDemoAccounts && uiState.selectedRole != null) {
                println("⚠️ ADVERTENCIA: Rol seleccionado pero cuentas demo ocultas")
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = uiState.email,
            onDismiss = {
                showForgotPasswordDialog = false
                forgotPasswordPressed = false
            }
        )
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        },
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null) }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = loginTextFieldColors(),
        modifier = modifier.fillMaxWidth(),
        isError = isError
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible)
                        "Ocultar contraseña"
                    else
                        "Mostrar contraseña"
                )
            }
        },
        singleLine = true,
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        shape = RoundedCornerShape(8.dp),
        colors = loginTextFieldColors(),
        modifier = modifier.fillMaxWidth(),
        isError = isError
    )
}

@Composable
fun RememberAndForgotRow(
    rememberSession: Boolean,
    forgotPasswordPressed: Boolean,
    onRememberSessionChange: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = rememberSession,
                onCheckedChange = onRememberSessionChange,
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

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (forgotPasswordPressed) MaterialTheme.colorScheme.outlineVariant
                    else MaterialTheme.colorScheme.secondaryContainer
                )
                .clickable { onForgotPasswordClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "¿Olvidó su contraseña?",
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
fun LoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
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
}

@Composable
fun DemoAccountsButton(
    isExpanded: Boolean,
    isLoading: Boolean,
    hasSelectedRole: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isExpanded && hasSelectedRole)
                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isExpanded && hasSelectedRole)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cargando cuentas...",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            } else {
                Text(
                    text = if (isExpanded && hasSelectedRole)
                        "Cerrar y Limpiar Selección"
                    else if (isExpanded)
                        "Ocultar Cuentas Demo"
                    else
                        "Acceso Rápido - Cuentas Demo",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
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

@Composable
fun RolesGrid(
    selectedRole: Rol?,
    onRoleSelected: (Rol) -> Unit
) {
    val roles = remember {
        listOf(
            UserRole.ADMINISTRADOR,
            UserRole.CO_ADMINISTRADOR,
            UserRole.GESTOR_PEDIDOS,
            UserRole.PROFESOR_A_CARGO,
            UserRole.DOCENTE,
            UserRole.ENCARGADO_BODEGA,
            UserRole.ASISTENTE_BODEGA
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        roles.chunked(3).forEachIndexed { rowIndex, rowItems ->
            key(rowIndex) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { role ->
                        key(role) {
                            val rolSistema = convertirUserRoleARol(role)
                            RoleButton(
                                title = role.displayName,
                                subtitle = role.description,
                                selected = selectedRole == rolSistema,
                                onClick = { onRoleSelected(rolSistema) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun convertirUserRoleARol(userRole: UserRole): Rol {
    return when(userRole) {
        UserRole.ADMINISTRADOR -> Rol.ADMINISTRADOR
        UserRole.CO_ADMINISTRADOR -> Rol.CO_ADMINISTRADOR
        UserRole.GESTOR_PEDIDOS -> Rol.GESTOR_PEDIDOS
        UserRole.PROFESOR_A_CARGO -> Rol.PROFESOR_A_CARGO
        UserRole.DOCENTE -> Rol.DOCENTE
        UserRole.ENCARGADO_BODEGA -> Rol.ENCARGADO_BODEGA
        UserRole.ASISTENTE_BODEGA -> Rol.ASISTENTE_BODEGA
    }
}

@Composable
fun ForgotPasswordDialog(
    email: String,
    onDismiss: () -> Unit
) {
    val maskedEmail = remember(email) {
        if (email.contains("@") && email.length > 2) {
            val parts = email.split("@")
            val localPart = parts[0]
            val domain = parts.getOrNull(1) ?: ""
            if (localPart.length >= 2) {
                val lastTwo = localPart.takeLast(2)
                val masked = "*".repeat(maxOf(0, localPart.length - 2))
                "$masked$lastTwo@$domain"
            } else email
        } else if (email.isEmpty()) "Ingrese un correo primero" else email
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Recuperar Contraseña",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Para restaurar su contraseña, acceda al correo electrónico:",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = maskedEmail,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "Recibirá un enlace para restablecer su contraseña.",
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}*/