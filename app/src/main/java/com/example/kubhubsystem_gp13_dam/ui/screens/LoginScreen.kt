package com.example.kubhubsystem_gp13_dam.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val loginViewModel: LoginViewModel = viewModel()
    val locationViewModel: LocationViewModel = remember { LocationViewModel(context) }

    val uiState by loginViewModel.uiState.collectAsState()
    val locationUiState by locationViewModel.uiState.collectAsState()

    var showDemoAccounts by remember { mutableStateOf(false) }
    var isLoadingDemoAccounts by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordPressed by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KubHub System",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gestión Integral de Recursos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { loginViewModel.updateEmail(it) },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = hasError,
                    colors = loginTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { loginViewModel.updatePassword(it) },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = hasError,
                    colors = loginTextFieldColors()
                )
                AnimatedVisibility(
                    visible = hasError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (!forgotPasswordPressed) {
                            forgotPasswordPressed = true
                            showForgotPasswordDialog = true
                        }
                    },
                    enabled = !isLoading && !forgotPasswordPressed
                ) {
                    Text("¿Olvidó su contraseña?")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { loginViewModel.login(onSuccess = onLoginSuccess) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Iniciar Sesión")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        if (!isLoadingDemoAccounts) {
                            isLoadingDemoAccounts = true
                            scope.launch {
                                delay(300)
                                showDemoAccounts = !showDemoAccounts
                                isLoadingDemoAccounts = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (showDemoAccounts) "Ocultar cuentas demo" else "Mostrar cuentas demo")
                }
                AnimatedVisibility(
                    visible = shouldShowDemoGrid,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        RolesGrid(selectedRole, { loginViewModel.selectDemoRole(it) })
                    }
                }
            }
        }
    }
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(uiState.email) {
            showForgotPasswordDialog = false
            forgotPasswordPressed = false
        }
    }
}

@Composable
fun RolesGrid(selectedRole: Rol?, onRoleSelected: (Rol) -> Unit) {
    val roles = remember { listOf(
        UserRole.ADMINISTRADOR, UserRole.CO_ADMINISTRADOR, UserRole.GESTOR_PEDIDOS,
        UserRole.PROFESOR_A_CARGO, UserRole.DOCENTE, UserRole.ENCARGADO_BODEGA, UserRole.ASISTENTE_BODEGA
    )}
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        roles.chunked(3).forEachIndexed { rowIndex, rowItems ->
            key(rowIndex) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { role ->
                        key(role) {
                            // ✅ Convertir UserRole a Rol antes de comparar
                            val rolSistema = convertirUserRoleARol(role)
                            RoleButton(
                                role.displayName,
                                role.description,
                                selectedRole == rolSistema,
                                { onRoleSelected(rolSistema) },
                                Modifier.weight(1f)
                            )
                        }
                    }
                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ✅ Función auxiliar para convertir UserRole a Rol
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
fun RoleButton(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ForgotPasswordDialog(email: String, onDismiss: () -> Unit) {
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
        title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Para restaurar su contraseña, acceda al correo electrónico:")
                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text(maskedEmail, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text("Recibirá un enlace para restablecer su contraseña.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendido") } },
        shape = RoundedCornerShape(16.dp)
    )
}