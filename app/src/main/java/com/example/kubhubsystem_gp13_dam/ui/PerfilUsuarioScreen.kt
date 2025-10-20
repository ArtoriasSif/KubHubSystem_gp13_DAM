package com.example.kubhubsystem_gp13_dam.ui.screens.perfil

import android.content.Context
import androidx.compose.foundation.background
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
import com.example.kubhubsystem_gp13_dam.utils.rememberImagePickerLauncher
import com.example.kubhubsystem_gp13_dam.viewmodel.PerfilUsuarioViewModel

/**
 * Pantalla dedicada para visualizar y editar el perfil de un usuario
 * Muestra información del usuario y permite cambiar la foto de perfil
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
    val estado by perfilViewModel.estado.collectAsState()
    val perfiles by perfilViewModel.perfiles.collectAsState()
    val perfil = perfiles[usuario.idUsuario]

    var mostrarDialogoPermisos by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarMenuOpciones by remember { mutableStateOf(false) }

    // Image picker con manejo de permisos
    val imagePickerState = rememberImagePickerLauncher(
        onImageSelected = { uri ->
            perfilViewModel.actualizarFotoPerfil(usuario.idUsuario, uri)
        },
        onPermissionDenied = {
            mostrarDialogoPermisos = true
        }
    )

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de éxito/error
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
                    // Menú de opciones
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Sección de foto de perfil
            SeccionFotoPerfil(
                perfil = perfil,
                usuario = usuario,
                onCambiarFoto = {
                    imagePickerState.solicitarImagen()
                },
                onEliminarFoto = {
                    mostrarDialogoEliminar = true
                },
                procesando = estado.procesando
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Información del usuario
            SeccionInformacionUsuario(usuario = usuario)

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Estadísticas del perfil
            SeccionEstadisticas(perfilViewModel = perfilViewModel)
        }
    }

    // Diálogo de permisos
    if (mostrarDialogoPermisos) {
        DialogoPermisos(
            onDismiss = { mostrarDialogoPermisos = false },
            onAbrirConfiguracion = {
                ImagePickerHelper.abrirConfiguracionApp(context)
                mostrarDialogoPermisos = false
            }
        )
    }

    // Diálogo de confirmación para eliminar foto
    if (mostrarDialogoEliminar) {
        DialogoEliminarFoto(
            onDismiss = { mostrarDialogoEliminar = false },
            onConfirmar = {
                perfilViewModel.eliminarFotoPerfil(usuario.idUsuario)
                mostrarDialogoEliminar = false
            }
        )
    }
}

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
        // Avatar grande
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            if (perfil?.fotoPerfil != null) {
                // Foto de perfil
                AsyncImage(
                    model = perfil.fotoPerfil,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Iniciales con color
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(perfil?.colorFondo ?: 0xFFBDBDBD)),
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

            // Indicador de carga
            if (procesando) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(160.dp)
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre completo
        Text(
            text = "${usuario.primeroNombre} ${usuario.segundoNombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}".trim(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rol
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
            // Botón cambiar foto
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

            // Botón eliminar foto (solo si tiene foto)
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

@Composable
private fun SeccionInformacionUsuario(usuario: Usuario) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Información del Usuario",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        ItemInformacion(
            icono = Icons.Default.Email,
            titulo = "Email",
            valor = usuario.email
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Username
        ItemInformacion(
            icono = Icons.Default.Person,
            titulo = "Username",
            valor = usuario.username
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rol
        ItemInformacion(
            icono = Icons.Default.Badge,
            titulo = "Rol",
            valor = usuario.rol.obtenerNombre()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Descripción del rol
        ItemInformacion(
            icono = Icons.Default.Info,
            titulo = "Descripción del Rol",
            valor = usuario.rol.description
        )
    }
}

@Composable
private fun ItemInformacion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SeccionEstadisticas(perfilViewModel: PerfilUsuarioViewModel) {
    val estadisticas = perfilViewModel.obtenerEstadisticas()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Estadísticas de Perfiles",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tarjeta total perfiles
            EstadisticaCard(
                titulo = "Total Perfiles",
                valor = estadisticas.totalPerfiles.toString(),
                icono = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            )

            // Tarjeta con foto
            EstadisticaCard(
                titulo = "Con Foto",
                valor = estadisticas.perfilesConFoto.toString(),
                icono = Icons.Default.CameraAlt,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Porcentaje
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Perfiles con foto personalizada",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${estadisticas.porcentajeConFoto.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun EstadisticaCard(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DialogoPermisos(
    onDismiss: () -> Unit,
    onAbrirConfiguracion: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Permiso necesario")
        },
        text = {
            Text(
                "La aplicación necesita acceso a tus fotos para cambiar la imagen de perfil.\n\n" +
                        "¿Deseas abrir la configuración de la app para otorgar el permiso?"
            )
        },
        confirmButton = {
            Button(onClick = onAbrirConfiguracion) {
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
            Text("¿Estás seguro de que deseas eliminar tu foto de perfil? Se volverá a mostrar el icono con tus iniciales.")
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
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
