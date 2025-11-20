package com.example.kubhubsystem_gp13_dam.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.model.Rol2
import com.example.kubhubsystem_gp13_dam.ui.theme.loginTextFieldColors
import com.example.kubhubsystem_gp13_dam.viewmodel.LocationViewModel2
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginUiState
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    val loginViewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(context) as T
            }
        }
    )

    val locationViewModel: LocationViewModel2 = remember { LocationViewModel2(context) }
    val uiState by loginViewModel.uiState.collectAsState()
    val locationUiState by locationViewModel.uiState.collectAsState()

    var showDemoAccounts by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Animación de fondo
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Permisos de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            locationViewModel.checkPermissions()
            locationViewModel.getLocationWithDelay(delayMillis = 3000L)
        } else {
            println("⚠️ Permisos de ubicación denegados por el usuario")
        }
    }

    LaunchedEffect(Unit) {
        locationViewModel.checkPermissions()
        if (locationUiState.hasPermission) {
            locationViewModel.getLocationWithDelay(delayMillis = 3000L)
        } else if (!locationUiState.permissionRequested) {
            delay(1000L)
            locationViewModel.markPermissionsRequested()
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
            .background(MaterialTheme.colorScheme.surface) // ✅ Color base coherente
    ) {
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo animado
            AnimatedLogo()

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de login principal
            LoginCard(
                uiState = uiState,
                onEmailChange = { loginViewModel.updateEmail(it) },
                onPasswordChange = { loginViewModel.updatePassword(it) },
                onRememberSessionChange = { loginViewModel.updateRememberSession(it) },
                onForgotPasswordClick = { showForgotPasswordDialog = true },
                onLoginClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    loginViewModel.login(onSuccess = onLoginSuccess)
                },
                onToggleDemoAccounts = {
                    showDemoAccounts = !showDemoAccounts
                    if (!showDemoAccounts) {
                        loginViewModel.clearDemoSelection()
                    }
                },
                showDemoAccounts = showDemoAccounts
            )

            Spacer(modifier = Modifier.height(24.dp))

            // d de roles demo
            AnimatedVisibility(
                visible = showDemoAccounts,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DemoAccounts(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = { role ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        loginViewModel.selectDemoRole(role)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card de estado de ubicación
            AnimatedVisibility(
                visible = locationUiState.isLoading || locationUiState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LocationStatusCard(
                    isLoading = locationUiState.isLoading,
                    errorMessage = locationUiState.errorMessage,
                    onRetry = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        locationViewModel.retryLocation()
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer - ✅ Colores del tema aplicados
            Text(
                text = "© 2025 KuHub System | Version 0.2",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showForgotPasswordDialog) {
        ModernForgotPasswordDialog(
            email = uiState.email,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}

@Composable
fun AnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer, // ✅ Color del tema
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.scrim, // ✅ Color del tema
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "KuHub System",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primaryContainer
        )

        Text(
            text = "Sistema de Gestión Gastronómica",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun LoginCard(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberSessionChange: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit,
    onToggleDemoAccounts: () -> Unit,
    showDemoAccounts: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp)), // ✅ Mismo shadow que LoginScreen
        shape = RoundedCornerShape(16.dp), // ✅ Mismo radius que LoginScreen
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer // ✅ Color del tema
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 10.dp), // ✅ Mismo padding que LoginScreen
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // ✅ Mismo spacing que LoginScreen
        ) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
            )

            Text(
                text = "Ingrese sus credenciales para acceder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), // ✅ Color del tema
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email field
            ModernTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Correo Electrónico",
                placeholder = "correo@ejemplo.com",
                leadingIcon = Icons.Default.Email,
                isError = uiState.errorMessage != null,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )

            // Password field
            ModernPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                placeholder = "********",
                isError = uiState.errorMessage != null
            )

            // Error message
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error, // ✅ Color del tema
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Remember & Forgot row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.rememberSession,
                        onCheckedChange = onRememberSessionChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primaryContainer, // ✅ Color del tema
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recordar sesión",
                        color = MaterialTheme.colorScheme.inverseSurface, // ✅ Color del tema
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Botón "¿Olvidó su contraseña?"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer) // ✅ Color del tema
                        .clickable { onForgotPasswordClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "¿Olvidó su contraseña?",
                        color = MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login button - ✅ Estilo del tema aplicado
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // ✅ Color del tema
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.scrim, // ✅ Color del tema
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar sesión",
                        color = MaterialTheme.colorScheme.scrim, // ✅ Color del tema
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
            }

            // Demo accounts button - ✅ Colores del tema aplicados
            Button(
                onClick = onToggleDemoAccounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showDemoAccounts && uiState.selectedRole != null)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f) // ✅ Color del tema
                    else
                        MaterialTheme.colorScheme.surfaceVariant, // ✅ Color del tema
                    contentColor = if (showDemoAccounts && uiState.selectedRole != null)
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
                    Text(
                        text = if (showDemoAccounts && uiState.selectedRole != null)
                            "Cerrar y Limpiar Selección"
                        else if (showDemoAccounts)
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
}

// Card de estado de ubicación - ✅ Colores del tema aplicados
@Composable
fun LocationStatusCard(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (errorMessage != null)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) // ✅ Color del tema
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) // ✅ Color del tema
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, // ✅ Color del tema
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = errorMessage ?: "Obteniendo ubicación...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (errorMessage != null)
                        MaterialTheme.colorScheme.error // ✅ Color del tema
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            if (errorMessage != null) {
                TextButton(onClick = onRetry) {
                    Text(
                        "Reintentar",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
                    )
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isError: Boolean = false,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) }, // ✅ Color del tema
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // ✅ Color del tema
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(8.dp), // ✅ Mismo radius que LoginScreen
        colors = loginTextFieldColors(), // ✅ Usa la misma función que LoginScreen
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType
        )
    )
}

@Composable
fun ModernPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) }, // ✅ Color del tema
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // ✅ Color del tema
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(8.dp), // ✅ Mismo radius que LoginScreen
        colors = loginTextFieldColors(), // ✅ Usa la misma función que LoginScreen
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
        )
    )
}

@Composable
fun DemoAccounts(
    selectedRole: Rol2?,
    onRoleSelected: (Rol2) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer // ✅ Color del tema
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cuentas Demo Disponibles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
                )

            Text(
                text = "Haz clic en cualquier rol para autocompletar las credenciales",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant, // ✅ Color del tema
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Grid de roles
            Rol2.obtenerTodos().chunked(2).forEach { rowRoles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowRoles.forEach { rol ->
                        ModernRoleCard(
                            rol = rol,
                            selected = selectedRole == rol,
                            onClick = { onRoleSelected(rol) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowRoles.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ModernRoleCard(
    rol: Rol2,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp), // ✅ RESTAURADO: Altura original
        shape = RoundedCornerShape(16.dp), // ✅ RESTAURADO: Radio original
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
            else
                MaterialTheme.colorScheme.secondaryContainer // ✅ Color del tema (antes era surface)
        ),
        border = if (selected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer) // ✅ Color del tema (antes era primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // ✅ Color del tema (antes era outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally // ✅ RESTAURADO: Centrado
        ) {
            // ✅ RESTAURADO: Ícono del rol
            Icon(
                imageVector = getRoleIcon(rol),
                contentDescription = null,
                tint = if (selected)
                    MaterialTheme.colorScheme.scrim // ✅ Color del tema (antes era primary)
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rol.nombreRol,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected)
                    MaterialTheme.colorScheme.scrim // ✅ Color del tema (antes era primary)
                else
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun getRoleIcon(rol: Rol2): ImageVector {
    return when (rol) {
        Rol2.ADMINISTRADOR -> Icons.Default.AdminPanelSettings
        Rol2.CO_ADMINISTRADOR -> Icons.Default.SupervisorAccount
        Rol2.GESTOR_PEDIDOS -> Icons.Default.Assignment
        Rol2.PROFESOR_A_CARGO -> Icons.Default.School
        Rol2.DOCENTE -> Icons.Default.Person
        Rol2.ENCARGADO_BODEGA -> Icons.Default.Warehouse
        Rol2.ASISTENTE_BODEGA -> Icons.Default.Inventory
    }
}

@Composable
fun ModernForgotPasswordDialog(
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
        } else if (email.isEmpty()) {
            "Ingrese un correo primero"
        } else {
            email
        }
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Para restaurar su contraseña, acceda al correo electrónico:",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), // ✅ Color del tema
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = maskedEmail,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
                    )
                }
                Text(
                    "Recibirá un enlace para restablecer su contraseña.",
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ Color del tema
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Entendido",
                    color = MaterialTheme.colorScheme.primaryContainer // ✅ Color del tema
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}