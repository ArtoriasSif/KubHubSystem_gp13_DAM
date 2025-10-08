package com.example.kubhubsystem_gp13_dam.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.model.UserRole
import com.example.kubhubsystem_gp13_dam.ui.theme.loginTextFieldColors
import com.example.kubhubsystem_gp13_dam.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Esta función dibuja la pantalla de inicio de sesión.
 *
 * - Usa el ViewModel para conocer y manejar el estado de la UI:
 *      email, contraseña, errores, indicadores de carga, etc.
 * - Recibe una función `onLoginSuccess` que se ejecuta cuando el login
 *      se realiza correctamente, por ejemplo, para navegar a otra pantalla.
 * - Si no se pasa un ViewModel ni la función de éxito, Compose crea el ViewModel
 *      automáticamente y no hace nada al iniciar sesión.
 *
 * En resumen: esta función se encarga de mostrar la UI del login y de reaccionar
 * a los cambios de estado de forma segura y automática.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(), // ViewModel que maneja la lógica del login
    onLoginSuccess: () -> Unit = {}          // Callback que se ejecuta cuando el login es exitoso (navegar a otra pantalla)
) {
    /**📌 Obtenemos el estado completo de la UI desde el ViewModel
     *    uiState: trae toda la información actual de la pantalla desde el ViewModel (email, password, rol, errores, carga)
     *    que es observado mediante StateFlow, lo que garantiza recomposición automática ante cambios. En otras palabras GG
     */
    val uiState by viewModel.uiState.collectAsState()

    //📌 Estados locales de la UI
    var showDemoAccounts by remember { mutableStateOf(false) }          // Controla si el panel de "cuentas demo" se muestra
    var isLoadingDemoAccounts by remember { mutableStateOf(false) }     // Muestra animación de carga antes de desplegar cuentas demo
    var showForgotPasswordDialog by remember { mutableStateOf(false) }  // Controla la visibilidad del diálogo "¿Olvidó su contraseña?"
    var forgotPasswordPressed by remember { mutableStateOf(false) }     // Evita mantener presionado el botón de "olvidé contraseña"

    /**📌Estados derivados para optimizar recomposiciones, prevenir recomposiciones innecesarias con derivedStateOf
     *   Estos estados usan `derivedStateOf` para evitar que toda la UI se recomponga
     *   cuando solo cambian valores específicos del ViewModel. Mejoran el rendimiento.
     */
    val hasError by remember { derivedStateOf { uiState.errorMessage != null } } // True si hay error de login
    val isLoading by remember { derivedStateOf { uiState.isLoading } }           // True si el login está en proceso
    val selectedRole by remember { derivedStateOf { uiState.selectedRole } }     // Rol actualmente seleccionado en modo demo (puede ser null)

    /**📌Valor memoizado para controlar la visibilidad del grid de roles demo, memorizar valores costosos (arreglo de control de error por sobrecarga)
     *   `shouldShowDemoGrid` controla la visibilidad del grid de roles demo.
     *   Se deriva de `showDemoAccounts` y `isLoadingDemoAccounts` para evitar recomposición innecesaria.
     */

    val shouldShowDemoGrid by remember { derivedStateOf { showDemoAccounts && !isLoadingDemoAccounts } }

    /**  💀 Diagnóstico de rendimiento (solo DEBUG)
     *   Este bloque mide cuánto tarda en componerse este Composable.
     *   Es útil para detectar "frame drops" o lentitud en la renderización,
     *   cuando hay demasiadas operaciones pesadas dentro de la composición. (Como es el caso XD)
     */
    if (false) { // Cambiar a true solo para debugging, cuando se quiera medir rendimiento
        DisposableEffect(Unit) {
            val startTime = System.currentTimeMillis()
            println("🔵 LoginScreen: Composición iniciada")

            onDispose {
                val duration = System.currentTimeMillis() - startTime
                println("🔴 LoginScreen: Composición terminada - Duración: ${duration}ms")
                if (duration > 16) {
                    println("⚠️ FRAME DROP: La composición tomó ${duration}ms (>${duration/16} frames)")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()  // Ocupa tod el espacio disponible de la pantalla
            .background(MaterialTheme.colorScheme.surface) // Establece el color de fondo principal según el tema actual (CAMBIO FUTURO A LOS COLORS)
            .padding(bottom = 48.dp) // Deja espacio extra en la parte inferior para no superponer el footer
    ) {
        /**  🟥 CONTENEDOR PRINCIPAL DEL FORMULARIO DE LOGIN
         *   Contiene todos los elementos del formulario de login
         */
        Box(
            modifier = Modifier
                .fillMaxWidth(0.80f) // El contenedor ocupa el 80% del ancho total
                .wrapContentHeight()// Su altura se adapta al contenido (sin forzar espacio extra)
                .align(Alignment.Center)// Centra el formulario vertical y horizontalmente dentro del Box padre
                .shadow(
                    elevation = 10.dp,                           // Aplica sombra para destacar el contenedor
                    shape = RoundedCornerShape(16.dp),    // Bordes redondeados suaves
                    clip = false                                 // No recorta el contenido al borde del contenedor
                )
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer, // Color de fondo del formulario según el tema
                    shape = RoundedCornerShape(16.dp)               // Misma curvatura que la sombra para mantener coherencia visual
                )
                .padding(horizontal = 30.dp, vertical = 10.dp)             // Margen interno (espaciado entre bordes y contenido) para tod los elementos
        ) {
            /**  🔳 ESTRUCTURA INTERNA: COLUMNA DEL FORMULARIO DE LOGIN
             *   Dentro del contenedor principal, se usa una columna para apilar los elementos verticalmente.
             */
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp), // Espaciado uniforme entre cada elemento hijo
                horizontalAlignment = Alignment.CenterHorizontally        // Centra todos los elementos horizontalmente
            ) {
                // Título Encabezado con tipografía grande y en negrita, transmitiendo jerarquía visual
                Text(
                    text = "Iniciar sesión",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize
                )

                // Subtítulo orienta al usuario, explicando la acción a realizar.
                Text(
                    text = "Ingrese sus credenciales para acceder",
                    // Se usa un color secundario con opacidad reducida (0.8f) para no competir visualmente con el título.
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )

                /**
                 * ✏️ CAMPOS DE ENTRADA DEL FORMULARIO
                 * Ambos campos son componentes personalizados (modulares) que encapsulan
                 * la lógica y el estilo de los TextField. Esto mejora la reutilización y
                 * la mantenibilidad del código.
                 * Se enlazan con el ViewModel a través del estado uiState, garantizando
                 * que los cambios de texto se reflejen de forma reactiva, pricipalmente en errores.
                 *
                 * CAMPO DE CORREO ELECTRÓNICO
                 * Usa un ícono de correo y muestra el estado de error si las  credenciales son inválidas o el formato no es correcto.
                 *
                 * CAMPO DE CONTRASEÑA
                 * Usa un ícono de candado y muestra el estado de error si las credenciales son inválidas.
                 * El componente PasswordTextField incluye funcionalidad para mostrar u ocultar el texto, reforzando la usabilidad.
                 */


                // Campo de correo (componente modular)
                LoginTextField(
                    value = uiState.email,  // Valor actual del campo, gestionado por el ViewModel
                    onValueChange = { viewModel.updateEmail(it) }, // Callback que actualiza el estado reactivo en el ViewModel
                    label = "Correo Electrónico*",  // Etiqueta visible sobre el campo
                    placeholder = "correo@ejemplo.com", // Texto guía cuando el campo está vacío
                    isError = hasError, // Control visual de error (bordes/colores rojos)
                    leadingIcon = Icons.Default.Email // Ícono al inicio, representando el tipo de dato (correo)
                )

                // Campo de contraseña (componente modular)
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },// Actualiza el estado de la contraseña
                    label = "Contraseña*",
                    placeholder = "********",
                    isError = hasError
                )

                /**
                 * ⚠️ MENSAJE DE ERROR ANIMADO
                 * Este bloque se encarga de mostrar mensajes de error de forma dinámica
                 * credenciales incorrectas o campos vacíos
                 *
                 * Componente que controla la visibilidad del mensaje de error
                 * - `visible = hasError`: el mensaje solo aparece si existe un error.(en este caso de no existir en el repo)
                 * - `enter`: define la animación al mostrarse (desvanecido + expansión vertical).(al no encuentrar o campo en blanco)
                 * - `exit`: define la animación al ocultarse (desvanecido inverso + contracción).(al ingresar algo en ambos campos)
                 */

                // Mensaje de error
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

                /**
                 * ♋ FILA: RECORDAR SESIÓN Y ¿OLVIDÓ SU CONTRASEÑA?
                 * que esta definida como una funcion en un box que contiene dos elementos (checkbox y boton) un componente modular reutilizable
                 * 1️⃣ La opción para mantener la sesión activa (checkbox). (No implementado dado actualmente dado que tiene que esta conectado a cerrar session)
                 * 2️⃣ El enlace para recuperar la contraseña olvidada.
                 *
                 * Componente que contiene las opciones secundarias del formulario.
                 * ----------------------------------------------------------
                 * - `rememberSession`: indica si el usuario desea mantener la sesión iniciada.
                 * - `forgotPasswordPressed`: controla la animación o estado visual del texto “¿Olvidó su contraseña?”.
                 * - `onRememberSessionChange`: callback que actualiza el estado en el ViewModel.
                 * - `onForgotPasswordClick`: callback que dispara la lógica de recuperación (abre diálogo y actualiza estado).
                 */

                // Fila: Recordar sesión y ¿Olvidó su contraseña?
                RememberAndForgotRow(
                    rememberSession = uiState.rememberSession,                                      // Estado actual del switch "Recordar sesión"
                    forgotPasswordPressed = forgotPasswordPressed,                                  // Indica si se presionó el texto de "¿Olvidó su contraseña?"
                    onRememberSessionChange = { viewModel.updateRememberSession(it) },   // Actualiza el valor en el ViewModel
                    onForgotPasswordClick = {
                        forgotPasswordPressed = true                           // Activa el estado visual de “presionado”
                        showForgotPasswordDialog = true                        // Muestra el diálogo de recuperación
                        viewModel.updateForgotPasswordRequest(true) // Notifica al ViewModel que se inició la solicitud
                    }
                )

                /**
                 * 🔘 BOTÓN PRINCIPAL: INICIAR SESIÓN
                 * Ejecuta el proceso de autenticación al hacer clic, mostrando junto con una animacion
                 * indicador de carga mientras se valida la información.
                 *
                 * Componente del botón de inicio de sesión.
                 * ----------------------------------------------------------
                 * - `isLoading`: indica si el proceso de autenticación está en curso.
                 *    Cuando es `true`, el botón suele mostrar un spinner o desactivar la interacción.
                 * - `onClick`: callback que dispara la función de login en el ViewModel.
                 *    Si el login es exitoso, se ejecuta `onLoginSuccess` (navegación o cambio de pantalla).
                 */
                // Botón de iniciar sesión
                LoginButton(
                    isLoading = isLoading,                                     // Estado de carga: muestra feedback visual al usuario
                    onClick = { viewModel.login(onLoginSuccess) }  // Llama al métodoo login del ViewModel y gestiona la respuesta
                )

                //Los metodos de Demos estan implementado de esta manera entediendo que no existira en la version final

                /**
                 * ⚡ ACCESO RÁPIDO: CUENTAS DEMO CON CORUTINAS
                 *
                 * (DemoAccountsButton). Permite mostrar u ocultar una lista de roles demo,
                 *  incluyendo animaciones de carga optimizadas con corutinas.
                 *
                 * Scope de corutinas vinculado al ciclo de vida del Composable.
                 * ----------------------------------------------------------
                 * - `rememberCoroutineScope()` crea un ámbito que se cancela automáticamente
                 *    cuando el Composable se elimina de la composición.
                 * - Es ideal para manejar delays, animaciones o llamadas asincrónicas
                 *    sin riesgo de fugas de memoria.
                 */
                val coroutineScope = rememberCoroutineScope()

                /**
                 * BOTÓN DE ACCESO RÁPIDO A CUENTAS DEMO
                 * ----------------------------------------------------------
                 * Controla la expansión/colapso de la lista de cuentas demo.
                 * - `isExpanded`: define si la lista está visible.
                 * - `isLoading`: muestra animación de carga mientras se simula la preparación de datos.
                 * - `hasSelectedRole`: indica si el usuario ya seleccionó un rol demo.
                 * - `onClick`: gestiona la lógica de alternancia entre mostrar, ocultar o limpiar selección.
                 */

                DemoAccountsButton(
                    isExpanded = showDemoAccounts,          // Estado visual actual del menú demo
                    isLoading = isLoadingDemoAccounts,      // Controla la animación de carga
                    hasSelectedRole = selectedRole != null, // Comprueba si hay rol seleccionado (memoizado)
                    onClick = {
                        when {
                            // Caso 1: Lista visible y rol seleccionado → cerrar y limpiar
                            showDemoAccounts && uiState.selectedRole != null -> {
                                // Cerrar y limpiar selección
                                showDemoAccounts = false
                                viewModel.clearDemoSelection()// Limpia el estado en el ViewModel
                            }

                            // Caso 2: Lista visible sin selección → solo ocultar
                            showDemoAccounts && uiState.selectedRole == null -> {
                                // Solo ocultar sin limpiar
                                showDemoAccounts = false
                            }

                            // Caso 3: Lista oculta → mostrar con animación de carga
                            !showDemoAccounts -> {
                                // Mostrar animación de carga (reducido de 2000ms a 1000ms)
                                isLoadingDemoAccounts = true // Activa el estado de carga
                                coroutineScope.launch {
                                    delay(1000)
                                    isLoadingDemoAccounts = false
                                    showDemoAccounts = true // Muestra la lista demo al finalizar el delay
                                }
                            }
                        }
                    }
                )

                /**
                 * Línea separadora (solo visible cuando se muestran las cuentas demo)
                 * Animación simplificada para mejor rendimiento
                 *
                 * Componente animado que controla la visibilidad del separador y texto.
                 * ----------------------------------------------------------
                 * - `visible = shouldShowDemoGrid`: solo se muestra cuando las cuentas demo están activas.
                 * - `enter` y `exit`: definen animaciones suaves de aparición/desaparición.
                 *   Se usa una versión simplificada para optimizar el rendimiento.
                 */
                AnimatedVisibility(
                    visible = shouldShowDemoGrid, // Estado memoizado que controla la visibilidad
                    enter = fadeIn() + expandVertically(),// Animación de entrada: opacidad + expansión vertical
                    exit = fadeOut() + shrinkVertically()// Animación de salida: opacidad inversa + contracción
                ) {
                    Column {
                        /**
                         * LÍNEA SEPARADORA VISUAL
                         * ----------------------------------------------------------
                         * Divide visualmente el formulario principal de la sección demo.
                         * Usa el color del tema con opacidad reducida para mantener
                         * coherencia cromática sin generar alto contraste.
                         */
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth() // Ocupa todo el ancho
                                .padding(vertical = 8.dp) // Espaciado vertical moderado
                        )

                        /**
                         * TEXTO INFORMATIVO
                         * ----------------------------------------------------------
                         * Indica al usuario cómo interactuar con la sección de cuentas demo.
                         * Centrado horizontalmente para mantener equilibrio visual.
                         */
                        Text(
                            text = "Haz clic en cualquier rol para autocompletar las credenciales",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),// Separación inferior
                            textAlign = TextAlign.Center// Centrado visual
                        )
                    }
                }

                /**
                 * GRID DE ROLES DEMO CON ANIMACIÓN OPTIMIZADA
                 * Este bloque muestra los botones de roles disponibles para cuentas demo.
                 * Se activa únicamente cuando `showDemoAccounts` es true y `isLoadingDemoAccounts` es false.
                 * La animación de entrada/salida es simplificada para mejorar el rendimiento.
                 *
                 * Componente animado que controla la visibilidad del grid de roles.
                 * ----------------------------------------------------------
                 * - `visible`: muestra el grid solo si las cuentas demo están activas y no hay carga en curso.
                 * - `enter` y `exit`: animaciones suaves de aparición y desaparición
                 *   (fade + expansión/contracción vertical), eliminando scaleIn/scaleOut
                 *   para optimizar el renderizado.
                 */
                AnimatedVisibility(
                    visible = showDemoAccounts && !isLoadingDemoAccounts,// Condición para mostrar el grid
                    enter = fadeIn() + expandVertically(),               // Animación de entrada
                    exit = fadeOut() + shrinkVertically()                // Animación de salida
                ) {
                    /**
                     * Grid de roles demo
                     * ----------------------------------------------------------
                     * - `selectedRole`: indica cuál rol está actualmente seleccionado.
                     * - `onRoleSelected`: callback que notifica al ViewModel qué rol fue seleccionado.
                     *   Permite autocompletar credenciales de la cuenta demo.
                     */
                    RolesGrid(
                        selectedRole = uiState.selectedRole, // Rol seleccionado actualmente (memoizado)
                        onRoleSelected = { role -> viewModel.selectDemoRole(role) }// Actualiza la selección en el ViewModel
                    )
                }
            }
        }

        /**
         * FOOTER DE LA PANTALLA
         * ----------------------------------------------------------
         * Muestra información estática de la aplicación al final de la pantalla.
         * - Centrado horizontalmente.
         * - Color tenue y tamaño pequeño para no competir con el contenido principal.
         */
        // Footer
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

        /**
         * LOGGING DE ESTADOS INCONSISTENTES
         * ----------------------------------------------------------
         * - Usa `SideEffect` en lugar de `LaunchedEffect` para evitar loops de recomposición.
         * - Permite detectar posibles inconsistencias en los estados de la UI.
         * 🔍 Logging solo cuando hay estados inconsistentes (posibles errores)
         * ✅ Usamos SideEffect en lugar de LaunchedEffect para evitar loops
         */


        SideEffect {
            // Caso anómalo: ambos estados activos simultáneamente
            if (showDemoAccounts && isLoadingDemoAccounts) {
                println("⚠️ ERROR: Ambos estados activos simultáneamente - showDemoAccounts=true | isLoadingDemoAccounts=true")
            }
            // Caso anómalo: rol seleccionado pero cuentas demo ocultas
            if (!showDemoAccounts && uiState.selectedRole != null) {
                println("⚠️ ADVERTENCIA: Rol seleccionado pero cuentas demo ocultas - selectedRole=${uiState.selectedRole}")
            }
        }
    }

    /**
     * 🔑 DIÁLOGO DE CONTRASEÑA OLVIDADA
     * Muestra un diálogo para recuperación de contraseña.
     * ----------------------------------------------------------
     * - `showForgotPasswordDialog` controla si el diálogo está visible.
     * - `email`: correo electrónico ingresado por el usuario.
     * - `onDismiss`: callback que cierra el diálogo y restaura los estados
     *    del botón "¿Olvidó su contraseña?" y la solicitud en el ViewModel.
     */
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = uiState.email,
            onDismiss = {
                showForgotPasswordDialog = false                        // Oculta el diálogo
                forgotPasswordPressed = false                           // Restaura estilo normal del botón
                viewModel.updateForgotPasswordRequest(false) // Actualiza estado en ViewModel
            }
        )
    }
}

// =====================================================================
// COMPONENTES MODULARES REUTILIZABLES
// =====================================================================

/**
 * Campo de texto estándar para login
 */
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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

/**
 * Campo de contraseña con toggle de visibilidad
 */
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

/**
 * Fila con checkbox "Recordar sesión" y botón "Olvidó contraseña"
 */
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
        // Checkbox de recordar sesión
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

        // Texto "¿Olvidó su contraseña?"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (forgotPasswordPressed) MaterialTheme.colorScheme.outlineVariant
                    else MaterialTheme.colorScheme.secondaryContainer
                )
                .clickable {
                    onForgotPasswordClick()
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "¿Olvidó su contraseña?",
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

/**
 * Botón principal de inicio de sesión con indicador de carga
 */
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

/**
 * Botón para mostrar/ocultar cuentas demo con animación de carga
 */
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

/**
 * Diálogo que muestra información para recuperar contraseña
 */
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
            } else {
                email
            }
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
}

// =====================================================================
// COMPONENTES DE ROLES (OPTIMIZADOS CON KEYS)
// =====================================================================

/**
 * Grid de roles con optimización de recomposición mediante keys
 * ✅ Usa Column en lugar de LazyColumn para listas pequeñas (6 elementos)
 */
@Composable
fun RolesGrid(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit
) {
    // Lista de roles disponibles
    val roles = remember {
        listOf(
            UserRole.ADMIN,
            UserRole.CO_ADMIN,
            UserRole.GESTOR_PEDIDOS,
            UserRole.PROFESOR,
            UserRole.BODEGA,
            UserRole.ASISTENTE
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dividimos en filas de 3
        // ✅ Añadido key() para optimizar recomposiciones
        roles.chunked(3).forEachIndexed { rowIndex, rowItems ->
            key(rowIndex) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { role ->
                        // ✅ Key individual para cada botón de rol
                        key(role) {
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
    }
}

/**
 * Botón individual de rol
 */
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