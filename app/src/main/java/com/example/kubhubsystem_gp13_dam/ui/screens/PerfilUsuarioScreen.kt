package com.example.kubhubsystem_gp13_dam.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.utils.ImagePickerHelper
import com.example.kubhubsystem_gp13_dam.utils.rememberImagePickerWithCameraLauncher
import com.example.kubhubsystem_gp13_dam.viewmodel.PerfilUsuarioViewModel

/**
 * Pantalla de perfil de usuario con funcionalidades completas:
 * - Scroll vertical en toda la pantalla
 * - Bottom sheet para elegir entre cámara o galería
 * - Manejo individual de permisos
 * - Diálogos informativos cuando se deniegan permisos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    usuario: Usuario,
    perfilViewModel: PerfilUsuarioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Estados del ViewModel
    val estado by perfilViewModel.estado.collectAsState()
    val perfiles by perfilViewModel.perfiles.collectAsState()
    val perfil = perfiles[usuario.idUsuario]

    // Estados locales de UI
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var mostrarDialogoPermisosGaleria by remember { mutableStateOf(false) }
    var mostrarDialogoPermisosCamara by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarMenuOpciones by remember { mutableStateOf(false) }

    // Image picker con soporte completo de cámara y galería
    val imagePickerState = rememberImagePickerWithCameraLauncher(
        onImageSelected = { uri ->
            println("✅ Imagen seleccionada: $uri")
            perfilViewModel.actualizarFotoPerfil(usuario.idUsuario, uri)
        },
        onGalleryPermissionDenied = {
            println("❌ Permiso de galería denegado")
            mostrarDialogoPermisosGaleria = true
        },
        onCameraPermissionDenied = {
            println("❌ Permiso de cámara denegado")
            mostrarDialogoPermisosCamara = true
        }
    )

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de éxito/error del ViewModel
    LaunchedEffect(estado.mensajeExito, estado.error) {
        estado.mensajeExito?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            perfilViewModel.limpiarMensajes()
        }
        estado.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            perfilViewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Menú de opciones (3 puntos verticales)
                    IconButton(onClick = { mostrarMenuOpciones = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }

                    DropdownMenu(
                        expanded = mostrarMenuOpciones,
                        onDismissRequest = { mostrarMenuOpciones = false }
                    ) {
                        // Opción: Eliminar foto (solo visible si tiene foto)
                        if (perfil?.fotoPerfil != null) {
                            DropdownMenuItem(
                                text = { Text("Eliminar foto") },
                                onClick = {
                                    mostrarMenuOpciones = false
                                    mostrarDialogoEliminar = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            )
                        }

                        // Opción: Abrir configuración de la app
                        DropdownMenuItem(
                            text = { Text("Abrir configuración") },
                            onClick = {
                                mostrarMenuOpciones = false
                                ImagePickerHelper.abrirConfiguracionApp(context)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Columna principal con scroll
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // ✅ SCROLL HABILITADO
        ) {
            // Sección: Foto de perfil grande
            SeccionFotoPerfil(
                perfil = perfil,
                usuario = usuario,
                onCambiarFoto = {
                    mostrarBottomSheet = true // Abre bottom sheet en vez de galería directamente
                },
                onEliminarFoto = {
                    mostrarDialogoEliminar = true
                },
                procesando = estado.procesando
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Sección: Información del usuario
            SeccionInformacionUsuario(usuario = usuario)

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Sección: Estadísticas del perfil
            SeccionEstadisticas(perfilViewModel = perfilViewModel)

            // Espaciado final para permitir scroll completo
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ✅ BOTTOM SHEET: Elegir entre Cámara o Galería
    if (mostrarBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { mostrarBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Título del bottom sheet
                Text(
                    text = "Seleccionar foto de perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                // Opción 1: Tomar foto con cámara
                ListItem(
                    headlineContent = { Text("Tomar foto") },
                    supportingContent = { Text("Usar la cámara") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            println("📸 Usuario seleccionó: Tomar foto")
                            imagePickerState.solicitarDesdeCamara()
                            mostrarBottomSheet = false
                        }
                        .fillMaxWidth()
                )

                // Opción 2: Elegir de galería
                ListItem(
                    headlineContent = { Text("Elegir de galería") },
                    supportingContent = { Text("Seleccionar foto existente") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            println("🖼️ Usuario seleccionó: Elegir de galería")
                            imagePickerState.solicitarDesdeGaleria()
                            mostrarBottomSheet = false
                        }
                        .fillMaxWidth()
                )

                // Opción 3: Cancelar
                ListItem(
                    headlineContent = { Text("Cancelar") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            println("❌ Usuario canceló selección")
                            mostrarBottomSheet = false
                        }
                        .fillMaxWidth()
                )
            }
        }
    }

    // Diálogo: Permiso de galería denegado
    if (mostrarDialogoPermisosGaleria) {
        DialogoPermisos(
            tipo = "galería",
            onDismiss = { mostrarDialogoPermisosGaleria = false },
            onAbrirConfiguracion = {
                ImagePickerHelper.abrirConfiguracionApp(context)
                mostrarDialogoPermisosGaleria = false
            }
        )
    }

    // Diálogo: Permiso de cámara denegado
    if (mostrarDialogoPermisosCamara) {
        DialogoPermisos(
            tipo = "cámara",
            onDismiss = { mostrarDialogoPermisosCamara = false },
            onAbrirConfiguracion = {
                ImagePickerHelper.abrirConfiguracionApp(context)
                mostrarDialogoPermisosCamara = false
            }
        )
    }

    // Diálogo: Confirmación para eliminar foto
    if (mostrarDialogoEliminar) {
        DialogoEliminarFoto(
            onDismiss = { mostrarDialogoEliminar = false },
            onConfirmar = {
                perfilViewModel.actualizarFotoPerfil(usuario.idUsuario, null)
                mostrarDialogoEliminar = false
            }
        )
    }
}

// ========================================================================================
// COMPONENTES PRIVADOS
// ========================================================================================

/**
 * Sección de foto de perfil con overlay de cámara
 */
@Composable
private fun SeccionFotoPerfil(
    perfil: com.example.kubhubsystem_gp13_dam.model.PerfilUsuario?,
    usuario: Usuario,
    onCambiarFoto: () -> Unit,
    onEliminarFoto: () -> Unit,
    procesando: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar grande (160dp) con indicador de carga
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            if (perfil?.fotoPerfil != null) {
                // Mostrar foto de perfil usando Coil
                AsyncImage(
                    model = perfil.fotoPerfil,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !procesando) { onCambiarFoto() },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Mostrar iniciales con color de fondo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(perfil?.colorFondo ?: 0xFFBDBDBD))
                        .clickable(enabled = !procesando) { onCambiarFoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = perfil?.iniciales ?: "??",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Overlay: Ícono de cámara en la esquina
            if (!procesando) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Indicador de carga
            if (procesando) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre completo del usuario
        Text(
            text = "${usuario.primerNombre} ${usuario.segundoNombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}".trim(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Badge con el rol del usuario
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = usuario.rol.obtenerNombre(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones de acción
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón: Cambiar foto
            Button(
                onClick = onCambiarFoto,
                enabled = !procesando
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar foto")
            }

            // Botón: Eliminar foto (solo si tiene foto)
            if (perfil?.fotoPerfil != null) {
                OutlinedButton(
                    onClick = onEliminarFoto,
                    enabled = !procesando
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

/**
 * Sección de información del usuario
 */
@Composable
private fun SeccionInformacionUsuario(usuario: Usuario) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Información",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Email
        ItemInformacion(
            icono = Icons.Default.Email,
            etiqueta = "Email",
            valor = usuario.email
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Username
        ItemInformacion(
            icono = Icons.Default.Person,
            etiqueta = "Username",
            valor = usuario.primerNombre
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rol
        ItemInformacion(
            icono = Icons.Default.Security,
            etiqueta = "Rol",
            valor = usuario.rol.obtenerNombre()
        )
    }
}

/**
 * Item individual de información con icono
 */
@Composable
private fun ItemInformacion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Sección de estadísticas del perfil
 */
@Composable
private fun SeccionEstadisticas(perfilViewModel: PerfilUsuarioViewModel) {
    val estadisticas = perfilViewModel.obtenerEstadisticas()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Total de perfiles
            EstadisticaCard(
                valor = estadisticas.totalPerfiles.toString(),
                etiqueta = "Total",
                icono = Icons.Default.People
            )

            // Perfiles con foto
            EstadisticaCard(
                valor = estadisticas.perfilesConFoto.toString(),
                etiqueta = "Con foto",
                icono = Icons.Default.Photo
            )

            // Perfiles sin foto
            EstadisticaCard(
                valor = estadisticas.perfilesSinFoto.toString(),
                etiqueta = "Sin foto",
                icono = Icons.Default.AccountCircle
            )
        }
    }
}

/**
 * Card individual de estadística
 */
@Composable
private fun EstadisticaCard(
    valor: String,
    etiqueta: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Diálogo de permisos denegados
 */
@Composable
private fun DialogoPermisos(
    tipo: String, // "galería" o "cámara"
    onDismiss: () -> Unit,
    onAbrirConfiguracion: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null
            )
        },
        title = {
            Text("Permiso necesario")
        },
        text = {
            Text(
                ImagePickerHelper.obtenerMensajeExplicacion(tipo == "cámara")
            )
        },
        confirmButton = {
            TextButton(onClick = onAbrirConfiguracion) {
                Text("Abrir configuración")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo de confirmación para eliminar foto
 */
@Composable
private fun DialogoEliminarFoto(
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Eliminar foto de perfil")
        },
        text = {
            Text("¿Estás seguro de que deseas eliminar tu foto de perfil? Se mostrarán tus iniciales en su lugar.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}