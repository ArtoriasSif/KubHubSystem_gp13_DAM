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
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.utils.ImagePickerHelper
import com.example.kubhubsystem_gp13_dam.utils.rememberImagePickerWithCameraLauncher
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil de usuario (versión simple sin ViewModel).
 * ✅ ACTUALIZADO: Ahora carga el usuario completo desde el backend
 *
 * Funcionalidades:
 * - ✅ Scroll completo en toda la pantalla
 * - ✅ Bottom sheet para elegir cámara o galería
 * - ✅ Manejo de permisos individual por opción
 * - ✅ Actualización directa con PerfilUsuarioManager
 * - ✅ Carga usuario actualizado desde el backend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreenSimple(
    idUsuario: Int, // ✅ Cambio: Ahora solo recibe el ID
    perfilManager: PerfilUsuarioManager = PerfilUsuarioManager.getInstance(),
    usuarioRepository: UsuarioRepository = UsuarioRepository(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Estado para cargar el usuario desde el backend
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var cargandoUsuario by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }

    // ✅ Cargar usuario desde el backend
    LaunchedEffect(idUsuario) {
        cargandoUsuario = true
        errorCarga = null
        try {
            val usuarioCargado = usuarioRepository.obtenerPorId(idUsuario)
            usuario = usuarioCargado
            if (usuarioCargado == null) {
                errorCarga = "No se encontró el usuario"
            }
        } catch (e: Exception) {
            errorCarga = "Error al cargar usuario: ${e.message}"
            e.printStackTrace()
        } finally {
            cargandoUsuario = false
        }
    }

    // Estado del perfil desde el manager
    val perfiles by perfilManager.perfiles.collectAsState()
    val perfil = perfiles[idUsuario]

    // Estados de UI
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var mostrarDialogoPermisosGaleria by remember { mutableStateOf(false) }
    var mostrarDialogoPermisosCamara by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker con soporte de cámara y galería
    val imagePickerState = rememberImagePickerWithCameraLauncher(
        onImageSelected = { uri ->
            println("✅ Imagen seleccionada: $uri")
            perfilManager.actualizarFotoPerfil(idUsuario, uri)
            scope.launch {
                snackbarHostState.showSnackbar("Foto de perfil actualizada")
            }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // ✅ Mostrar loading mientras carga
        when {
            cargandoUsuario -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando perfil...")
                    }
                }
            }
            errorCarga != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorCarga ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Volver")
                        }
                    }
                }
            }
            usuario != null -> {
                // ✅ Column con scroll vertical habilitado
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Avatar grande con click para cambiar foto
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (perfil?.fotoPerfil != null) {
                            AsyncImage(
                                model = perfil.fotoPerfil,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                                    .clickable { mostrarBottomSheet = true },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                                    .background(Color(perfil?.colorFondo ?: 0xFFBDBDBD))
                                    .clickable { mostrarBottomSheet = true },
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

                        // Overlay: Ícono de cámara
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ Nombre completo construido desde los campos individuales
                    Text(
                        text = buildString {
                            append(usuario!!.primerNombre)
                            if (!usuario!!.segundoNombre.isNullOrBlank()) {
                                append(" ${usuario!!.segundoNombre}")
                            }
                            if (!usuario!!.apellidoPaterno.isNullOrBlank()) {
                                append(" ${usuario!!.apellidoPaterno}")
                            }
                            if (!usuario!!.apellidoMaterno.isNullOrBlank()) {
                                append(" ${usuario!!.apellidoMaterno}")
                            }
                        }.trim(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge con el rol
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = usuario!!.rol.obtenerNombre(),
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
                        Button(
                            onClick = { mostrarBottomSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cambiar foto")
                        }

                        if (perfil?.fotoPerfil != null) {
                            OutlinedButton(
                                onClick = { mostrarDialogoEliminar = true }
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

                    Spacer(modifier = Modifier.height(32.dp))

                    // Card de información del usuario
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow("Email", usuario!!.email, Icons.Default.Email)
                            Spacer(modifier = Modifier.height(16.dp))
                            // ✅ Username desde el backend
                            InfoRow(
                                "Username",
                                usuario!!.username ?: "No asignado",
                                Icons.Default.Person
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoRow("Rol", usuario!!.rol.obtenerNombre(), Icons.Default.Security)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
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
                Text(
                    text = "Seleccionar foto de perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

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

    // Diálogo: Permisos de Galería denegados
    if (mostrarDialogoPermisosGaleria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisosGaleria = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            title = { Text("Permiso de galería necesario") },
            text = {
                Text(ImagePickerHelper.obtenerMensajeExplicacion(esCamara = false))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ImagePickerHelper.abrirConfiguracionApp(context)
                        mostrarDialogoPermisosGaleria = false
                    }
                ) {
                    Text("Abrir configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisosGaleria = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo: Permisos de Cámara denegados
    if (mostrarDialogoPermisosCamara) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisosCamara = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            title = { Text("Permiso de cámara necesario") },
            text = {
                Text(ImagePickerHelper.obtenerMensajeExplicacion(esCamara = true))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ImagePickerHelper.abrirConfiguracionApp(context)
                        mostrarDialogoPermisosCamara = false
                    }
                ) {
                    Text("Abrir configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisosCamara = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo: Confirmación para eliminar foto
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar foto de perfil") },
            text = {
                Text("¿Estás seguro de que deseas eliminar tu foto de perfil? Se mostrarán tus iniciales en su lugar.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        perfilManager.actualizarFotoPerfil(idUsuario, null)
                        mostrarDialogoEliminar = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Foto de perfil eliminada")
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Fila de información con icono, etiqueta y valor.
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}