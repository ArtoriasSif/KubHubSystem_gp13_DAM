package com.example.kubhubsystem_gp13_dam.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.model.PerfilUsuario
import com.example.kubhubsystem_gp13_dam.model.Usuario2
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.utils.rememberImagePickerWithCameraLauncher
import kotlinx.coroutines.launch

/**
 * ✅ COMPONENTE CORREGIDO
 * Decodifica Base64 directamente sin conversiones intermedias
 */
@Composable
fun AvatarUsuario(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null,
    mostrarBorde: Boolean = false,
    colorBorde: Color = MaterialTheme.colorScheme.primary
) {
    val baseModifier = modifier
        .size(size)
        .then(if (mostrarBorde) Modifier.border(2.dp, colorBorde, CircleShape) else Modifier)
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    if (perfil?.fotoPerfil != null) {
        val fotoString = perfil.fotoPerfil.toString()

        Log.d("AvatarUsuario", "🔍 Usuario ${perfil.idUsuario}")
        Log.d("AvatarUsuario", "   Preview: ${fotoString.take(50)}")
        Log.d("AvatarUsuario", "   Length: ${fotoString.length}")

        // ✅ DECODIFICAR BASE64
        val bitmap = remember(fotoString) {
            try {
                // Extraer el Base64 limpio
                val base64Clean = when {
                    // Si tiene prefijo data:image, extraer solo el Base64
                    fotoString.contains("base64,") -> {
                        fotoString.substringAfter("base64,").trim()
                    }
                    // Si es URL HTTP (no soportado en este componente)
                    fotoString.startsWith("http", ignoreCase = true) -> {
                        Log.w("AvatarUsuario", "   ⚠️ URL HTTP no soportada")
                        return@remember null
                    }
                    // Si es Base64 puro
                    else -> fotoString.trim()
                }

                Log.d("AvatarUsuario", "   🔄 Decodificando Base64 (${base64Clean.length} chars)...")

                // Decodificar
                val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                Log.d("AvatarUsuario", "   ✅ Bytes: ${bytes.size}")

                // Crear bitmap
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                if (bmp != null) {
                    Log.d("AvatarUsuario", "   ✅ Bitmap: ${bmp.width}x${bmp.height}")
                } else {
                    Log.e("AvatarUsuario", "   ❌ BitmapFactory retornó null")
                    // Debug: verificar primeros bytes
                    Log.e("AvatarUsuario", "   📋 Primeros bytes: ${bytes.take(10).joinToString()}")
                }

                bmp
            } catch (e: IllegalArgumentException) {
                Log.e("AvatarUsuario", "   ❌ Base64 inválido: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("AvatarUsuario", "   ❌ Error: ${e.message}")
                e.printStackTrace()
                null
            }
        }

        if (bitmap != null) {
            Log.d("AvatarUsuario", "   ✅ Renderizando imagen")
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto de perfil",
                modifier = baseModifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Log.w("AvatarUsuario", "   ⚠️ Fallback a iniciales")
            MostrarIniciales(perfil, baseModifier, size)
        }
    } else {
        Log.d("AvatarUsuario", "📭 Sin foto: usuario ${perfil?.idUsuario}")
        MostrarIniciales(perfil, baseModifier, size)
    }
}

@Composable
private fun MostrarIniciales(
    perfil: PerfilUsuario?,
    modifier: Modifier,
    size: Dp
) {
    Box(
        modifier = modifier
            .background(
                color = Color(perfil?.colorFondo ?: 0xFFBDBDBD),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = perfil?.iniciales ?: "??",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (size.value / 3).sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Avatares con tamaños predefinidos
 */
@Composable
fun AvatarPequeno(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AvatarUsuario(perfil, modifier, 40.dp, onClick)
}

@Composable
fun AvatarMediano(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AvatarUsuario(perfil, modifier, 56.dp, onClick)
}

@Composable
fun AvatarGrande(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AvatarUsuario(perfil, modifier, 120.dp, onClick)
}

@Composable
fun AvatarPlaceholder(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Usuario",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

@Composable
fun AvatarUsuarioConEstado(
    perfil: PerfilUsuario?,
    enLinea: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        AvatarUsuario(perfil, size = size, onClick = onClick)

        if (enLinea) {
            Box(
                modifier = Modifier
                    .size(size / 5)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
/**
 * ✅ PANTALLA CORREGIDA
 * Ahora muestra correctamente las imágenes Base64
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreenSimple(
    idUsuario: Int,
    perfilManager: PerfilUsuarioManager = PerfilUsuarioManager.getInstance(),
    usuarioRepository: UsuarioRepository = UsuarioRepository(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado del usuario
    var usuario by remember { mutableStateOf<Usuario2?>(null) }
    var cargandoUsuario by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }

    // Estado del perfil
    val perfiles by perfilManager.perfiles.collectAsState()
    val perfil = perfiles[idUsuario]

    // Estados de UI
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var mostrarDialogoPermisosGaleria by remember { mutableStateOf(false) }
    var mostrarDialogoPermisosCamara by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var subiendoFoto by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker
    val imagePickerState = rememberImagePickerWithCameraLauncher(
        onImageSelected = { uri ->
            Log.d("PerfilScreen", "✅ Imagen seleccionada: $uri")

            scope.launch {
                subiendoFoto = true
                try {
                    val usuarioActualizado = usuarioRepository.actualizarFotoPerfil(
                        context = context,
                        idUsuario = idUsuario,
                        imageUri = uri
                    )

                    if (usuarioActualizado != null) {
                        perfilManager.sincronizarPerfil(usuarioActualizado)
                        usuario = usuarioActualizado
                        snackbarHostState.showSnackbar("✅ Foto actualizada")
                    } else {
                        snackbarHostState.showSnackbar("❌ Error al subir la foto")
                    }
                } catch (e: Exception) {
                    Log.e("PerfilScreen", "❌ Error: ${e.message}")
                    snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                } finally {
                    subiendoFoto = false
                }
            }
        },
        onGalleryPermissionDenied = {
            mostrarDialogoPermisosGaleria = true
        },
        onCameraPermissionDenied = {
            mostrarDialogoPermisosCamara = true
        }
    )

    // Cargar usuario desde el backend
    LaunchedEffect(idUsuario) {
        cargandoUsuario = true
        errorCarga = null
        try {
            val usuarioCargado = usuarioRepository.obtenerPorId(idUsuario)
            usuario = usuarioCargado

            if (usuarioCargado != null) {
                perfilManager.sincronizarPerfil(usuarioCargado)
                Log.d("PerfilScreen", "✅ Usuario cargado: ${usuarioCargado.idUsuario}")
                Log.d("PerfilScreen", "   Foto presente: ${usuarioCargado.fotoPerfil != null}")
            } else {
                errorCarga = "No se encontró el usuario"
            }
        } catch (e: Exception) {
            errorCarga = "Error: ${e.message}"
            e.printStackTrace()
        } finally {
            cargandoUsuario = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(70.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            "Perfil de Usuario",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.scrim,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            cargandoUsuario || subiendoFoto -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (subiendoFoto) "Subiendo foto..." else "Cargando perfil...",
                            color = MaterialTheme.colorScheme.onBackground
                        )
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
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }

            usuario != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ AVATAR GRANDE CORREGIDO
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Renderizar la foto
                        if (perfil?.fotoPerfil != null) {
                            // ✅ Decodificar Base64 directamente
                            val bitmap = remember(perfil.fotoPerfil) {
                                try {
                                    val fotoString = perfil.fotoPerfil?.toString() ?: ""

                                    val base64Clean = when {
                                        fotoString.contains("base64,") ->
                                            fotoString.substringAfter("base64,").trim()

                                        else -> fotoString.trim()
                                    }

                                    val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                } catch (e: Exception) {
                                    Log.e("PerfilScreen", "❌ Error decodificando: ${e.message}")
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .clickable { mostrarBottomSheet = true },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback a iniciales
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(Color(perfil.colorFondo))
                                        .clickable { mostrarBottomSheet = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = perfil.iniciales,
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Sin foto: mostrar iniciales
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
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Ícono de cámara
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nombre completo
                    Text(
                        text = usuario!!.nombreCompleto,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge del rol
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = usuario!!.rol.obtenerNombre(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de acción
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { mostrarBottomSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,      // Fondo
                                contentColor = MaterialTheme.colorScheme.onPrimary                // Icono + texto
                            )
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
                            OutlinedButton(onClick = { mostrarDialogoEliminar = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFB00020),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Card de información
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow(
                                "Email",
                                usuario!!.email,
                                Icons.Default.Email,
                                iconTint = MaterialTheme.colorScheme.primaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            InfoRow("Username",
                                usuario!!.username,
                                Icons.Default.Person,
                                iconTint = Color(0xFF1565C0)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            InfoRow("Rol",
                                usuario!!.rol.obtenerNombre(),
                                Icons.Default.AdminPanelSettings,
                                iconTint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Bottom Sheet
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
                            imagePickerState.solicitarDesdeGaleria()
                            mostrarBottomSheet = false
                        }
                        .fillMaxWidth()
                )

                ListItem(
                    headlineContent = { Text("Cancelar") },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    },
                    modifier = Modifier
                        .clickable { mostrarBottomSheet = false }
                        .fillMaxWidth()
                )
            }
        }
    }

    // Diálogo: Eliminar foto
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
                        scope.launch {
                            subiendoFoto = true
                            try {
                                val exito = usuarioRepository.eliminarFotoPerfil(idUsuario)

                                if (exito) {
                                    perfilManager.actualizarFotoPerfil(idUsuario, null)
                                    val usuarioActualizado = usuarioRepository.obtenerPorId(idUsuario)
                                    if (usuarioActualizado != null) {
                                        usuario = usuarioActualizado
                                        perfilManager.sincronizarPerfil(usuarioActualizado)
                                    }
                                    snackbarHostState.showSnackbar("✅ Foto eliminada")
                                } else {
                                    snackbarHostState.showSnackbar("❌ Error al eliminar")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                            } finally {
                                subiendoFoto = false
                                mostrarDialogoEliminar = false
                            }
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

    // Diálogos de permisos
    if (mostrarDialogoPermisosGaleria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisosGaleria = false },
            icon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            title = { Text("Permiso de galería necesario") },
            text = { Text("KubHub necesita acceso a tus fotos para cambiar tu foto de perfil.") },
            confirmButton = {
                TextButton(onClick = {
                    com.example.kubhubsystem_gp13_dam.utils.ImagePickerHelper.abrirConfiguracionApp(context)
                    mostrarDialogoPermisosGaleria = false
                }) {
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

    if (mostrarDialogoPermisosCamara) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisosCamara = false },
            icon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            title = { Text("Permiso de cámara necesario") },
            text = { Text("KubHub necesita acceso a la cámara para tomar fotos de perfil.") },
            confirmButton = {
                TextButton(onClick = {
                    com.example.kubhubsystem_gp13_dam.utils.ImagePickerHelper.abrirConfiguracionApp(context)
                    mostrarDialogoPermisosCamara = false
                }) {
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
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: MaterialTheme.colorScheme.primary,
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