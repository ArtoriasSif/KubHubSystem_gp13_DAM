package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.model.Rol2
import com.example.kubhubsystem_gp13_dam.model.Usuario2
import com.example.kubhubsystem_gp13_dam.ui.components.AvatarUsuario
import com.example.kubhubsystem_gp13_dam.ui.theme.loginTextFieldColors
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosEstado
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosViewModel
import kotlinx.coroutines.launch

/**
 * 🎨 GestionUsuarioScreen2 - Versión Mejorada Final
 * ✅ Apariencia visual mejorada con tema amarillo/dorado
 * ✅ TODAS las funcionalidades: activar/desactivar, editar, eliminar con confirmación
 * ✅ Scroll optimizado con LazyColumn
 * ✅ Filtros completos: rol + estado (activo/inactivo)
 * ✅ Integración completa con backend vía ViewModel
 * ✅ Usa Usuario2 y Rol2 correctamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuarioScreen2(
    onNavigateToDetalleUsuario: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: GestionUsuariosViewModel = viewModel()
    val perfilManager = remember { PerfilUsuarioManager.getInstance() }
    val perfiles by perfilManager.perfiles.collectAsState()

    val estado by viewModel.estado.collectAsState()
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf<Usuario2?>(null) }
    var showNuevoUsuarioDialog by remember { mutableStateOf(false) }
    var showEditarUsuarioDialog by remember { mutableStateOf<Usuario2?>(null) }

    // Sincronizar perfiles cuando cambien los usuarios
    LaunchedEffect(estado.usuarios) {
        if (estado.usuarios.isNotEmpty()) {
            perfilManager.inicializarPerfiles(estado.usuarios)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de éxito/error
    LaunchedEffect(estado.error, estado.mensajeExito) {
        estado.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
                viewModel.limpiarMensajes()
            }
        }

        estado.mensajeExito?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = mensaje,
                    duration = SnackbarDuration.Short
                )
                viewModel.limpiarMensajes()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            estado.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando usuarios...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            estado.usuarios.isEmpty() && !estado.cargando -> {
                PantallaVacia(
                    onCrearPrimero = { showNuevoUsuarioDialog = true },
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                ContenidoPrincipal(
                    viewModel = viewModel,
                    estado = estado,
                    perfiles = perfiles,
                    onFiltroRolChange = { viewModel.onFiltroRolChange(it) },
                    onFiltroEstadoChange = { viewModel.onFiltroEstadoChange(it) },
                    onBuscarTextoChange = { viewModel.onBuscarTextoChange(it) },
                    onEditarUsuario = { usuarioId ->
                        val usuario = viewModel.obtenerUsuarioPorId(usuarioId)
                        showEditarUsuarioDialog = usuario
                    },
                    onEliminarUsuario = { showDeleteDialog = it },
                    onToggleEstadoUsuario = { usuario ->
                        if (usuario.activo) {
                            viewModel.desactivarUsuario(usuario.idUsuario)
                        } else {
                            viewModel.activarUsuario(usuario.idUsuario)
                        }
                    },
                    onNuevoUsuario = { showNuevoUsuarioDialog = true },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // 🔒 Diálogo de confirmación de eliminación
    showDeleteDialog?.let { usuario ->
        DialogoConfirmarEliminacion(
            usuario = usuario,
            onConfirmar = {
                viewModel.eliminarUsuario(usuario)
                perfilManager.eliminarPerfil(usuario.idUsuario)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    // 📝 Diálogo de nuevo usuario
    if (showNuevoUsuarioDialog) {
        DialogoNuevoUsuario(
            viewModel = viewModel,
            onDismiss = { showNuevoUsuarioDialog = false }
        )
    }

    // ✏️ Diálogo de editar usuario
    showEditarUsuarioDialog?.let { usuario ->
        DialogoEditarUsuario(
            viewModel = viewModel,
            usuario = usuario,
            onDismiss = { showEditarUsuarioDialog = null }
        )
    }
}

@Composable
private fun RolSelectionCard(
    rol: Rol2,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                Color(0xFFFFC107).copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFC107))
        } else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = obtenerIconoRol(rol),
                    contentDescription = null,
                    tint = obtenerColorRol(rol),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = rol.obtenerNombre(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = Color(0xFFFFC107)
                )
            }
        }
    }
}

@Composable
private fun PantallaVacia(
    onCrearPrimero: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFFFC107).copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bienvenido al Sistema",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF6B4E00)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No hay usuarios registrados",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onCrearPrimero,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color(0xFF6B4E00)
                )
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Primer Usuario")
            }
        }
    }
}

@Composable
private fun ContenidoPrincipal(
    viewModel: GestionUsuariosViewModel,
    estado: GestionUsuariosEstado,
    perfiles: Map<Int, com.example.kubhubsystem_gp13_dam.model.PerfilUsuario>,
    onFiltroRolChange: (String) -> Unit,
    onFiltroEstadoChange: (String) -> Unit,
    onBuscarTextoChange: (String) -> Unit,
    onEditarUsuario: (Int) -> Unit,
    onEliminarUsuario: (Usuario2) -> Unit,
    onNuevoUsuario: () -> Unit,
    onToggleEstadoUsuario: (Usuario2) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // 📊 Estadísticas principales
            if (estado.estadisticas != null) {
                item {
                    HeaderEstadisticas(
                        totalUsuarios = estado.estadisticas.totalUsuarios.toInt(),
                        usuariosActivos = estado.estadisticas.usuariosActivos.toInt(),
                        usuariosInactivos = estado.estadisticas.usuariosInactivos.toInt(),
                        totalRoles = estado.estadisticas.totalRoles.toInt()
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Buscador y botón de Nuevo Usuario
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = estado.buscarTexto,
                            onValueChange = { onBuscarTextoChange(it) },
                            placeholder = { Text("Buscar usuario") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (estado.buscarTexto.isNotEmpty()) {
                                    IconButton(onClick = { onBuscarTextoChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = loginTextFieldColors()
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onNuevoUsuario() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107),
                                contentColor = Color(0xFF6B4E00)
                            ),
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Nuevo usuario",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nuevo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SeccionFiltros(
                        filtroRol = estado.filtroRol,
                        filtroEstado = estado.filtroEstado,
                        onFiltroRolChange = onFiltroRolChange,
                        onFiltroEstadoChange = onFiltroEstadoChange
                    )
                }
            }

            // Contador de resultados
            item {
                Text(
                    text = "${estado.usuariosFiltrados.size} usuario(s) encontrado(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 👥 Lista de usuarios
            if (estado.usuariosFiltrados.isEmpty()) {
                item {
                    EmptyStateView()
                }
            } else {
                items(
                    items = estado.usuariosFiltrados,
                    key = { it.idUsuario }
                ) { usuario ->
                    TarjetaUsuario(
                        usuario = usuario,
                        perfil = perfiles[usuario.idUsuario],
                        esDocente = usuario.rol == Rol2.DOCENTE,
                        onClick = { onEditarUsuario(usuario.idUsuario) },
                        onEliminar = { onEliminarUsuario(usuario) },
                        onToggleEstado = { onToggleEstadoUsuario(usuario) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderEstadisticas(
    totalUsuarios: Int,
    usuariosActivos: Int,
    usuariosInactivos: Int,
    totalRoles: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC107)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaChip(
                icon = Icons.Default.People,
                valor = totalUsuarios.toString(),
                etiqueta = "Total",
                color = Color(0xFF6B4E00)
            )
            EstadisticaChip(
                icon = Icons.Default.CheckCircle,
                valor = usuariosActivos.toString(),
                etiqueta = "Activos",
                color = Color(0xFF4CAF50)
            )
            EstadisticaChip(
                icon = Icons.Default.Cancel,
                valor = usuariosInactivos.toString(),
                etiqueta = "Inactivos",
                color = Color(0xFFF44336)
            )
            EstadisticaChip(
                icon = Icons.Default.Security,
                valor = totalRoles.toString(),
                etiqueta = "Roles",
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
private fun EstadisticaChip(
    icon: ImageVector,
    valor: String,
    etiqueta: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.3f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B4E00)
        )
    }
}

@Composable
private fun SeccionFiltros(
    filtroRol: String,
    filtroEstado: String,
    onFiltroRolChange: (String) -> Unit,
    onFiltroEstadoChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Filtrar por rol:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val roles = listOf(
            "Todos",
            "Administrador",
            "Co-Administrador",
            "Gestor de Pedidos",
            "Profesor a Cargo",
            "Docente",
            "Encargado de Bodega",
            "Asistente de Bodega"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roles) { rol ->
                FilterChip(
                    selected = filtroRol == rol,
                    onClick = { onFiltroRolChange(rol) },
                    label = { Text(rol) },
                    leadingIcon = if (filtroRol == rol) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFC107).copy(alpha = 0.3f),
                        selectedLabelColor = Color(0xFF6B4E00)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Filtrar por estado:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val estados = listOf("Todos", "Activos", "Inactivos")

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estados) { estado ->
                FilterChip(
                    selected = filtroEstado == estado,
                    onClick = { onFiltroEstadoChange(estado) },
                    label = { Text(estado) },
                    leadingIcon = if (filtroEstado == estado) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (estado) {
                            "Activos" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "Inactivos" -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> Color(0xFFFFC107).copy(alpha = 0.3f)
                        },
                        selectedLabelColor = when (estado) {
                            "Activos" -> Color(0xFF1B5E20)
                            "Inactivos" -> Color(0xFFB71C1C)
                            else -> Color(0xFF6B4E00)
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: Usuario2,
    perfil: com.example.kubhubsystem_gp13_dam.model.PerfilUsuario?,
    esDocente: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    onToggleEstado: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarUsuario(
                    perfil = perfil,
                    size = 56.dp,
                    mostrarBorde = perfil?.fotoPerfil != null,
                    colorBorde = obtenerColorRol(usuario.rol)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = usuario.obtenerNombreCompleto(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (usuario.activo) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (usuario.activo) "Activo" else "Inactivo",
                            tint = if (usuario.activo) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = usuario.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    BadgeRol(rol = usuario.rol, esDocente = esDocente)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }

                IconButton(
                    onClick = onToggleEstado,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (usuario.activo) Color(0xFFF44336) else Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = if (usuario.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (usuario.activo) "Desactivar" else "Activar"
                    )
                }

                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeRol(rol: Rol2, esDocente: Boolean) {
    val color = obtenerColorRol(rol)

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = obtenerIconoRol(rol),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rol.obtenerNombre(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron usuarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intenta cambiar los filtros de búsqueda",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DialogoNuevoUsuario(
    viewModel: GestionUsuariosViewModel,
    onDismiss: () -> Unit
) {
    var primerNombre by remember { mutableStateOf("") }
    var segundoNombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol2.DOCENTE) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFC107))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B4E00)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF6B4E00)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    errorMessage?.let { error ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    OutlinedTextField(
                        value = primerNombre,
                        onValueChange = { primerNombre = it },
                        label = { Text("Primer Nombre *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = segundoNombre,
                        onValueChange = { segundoNombre = it },
                        label = { Text("Segundo Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apellidoPaterno,
                        onValueChange = { apellidoPaterno = it },
                        label = { Text("Apellido Paterno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apellidoMaterno,
                        onValueChange = { apellidoMaterno = it },
                        label = { Text("Apellido Materno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider()

                    Text(
                        text = "Credenciales",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    if (mostrarPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (mostrarPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider()

                    Text(
                        text = "Rol del Usuario",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Rol2.obtenerTodos().forEach { rol ->
                            RolSelectionCard(
                                rol = rol,
                                selected = rolSeleccionado == rol,
                                onClick = { rolSeleccionado = rol }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            when {
                                primerNombre.isBlank() -> errorMessage = "El primer nombre es obligatorio"
                                email.isBlank() -> errorMessage = "El email es obligatorio"
                                !email.contains("@") -> errorMessage = "Email inválido"
                                password.isBlank() -> errorMessage = "La contraseña es obligatoria"
                                password.length < 6 -> errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                else -> {
                                    viewModel.crearUsuario(
                                        primerNombre = primerNombre.trim(),
                                        segundoNombre = segundoNombre.trim().ifBlank { null },
                                        apellidoPaterno = apellidoPaterno.trim().ifBlank { null },
                                        apellidoMaterno = apellidoMaterno.trim().ifBlank { null },
                                        email = email.trim().lowercase(),
                                        username = username.trim().ifBlank { null },
                                        password = password,
                                        rol = rolSeleccionado
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color(0xFF6B4E00)
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoEditarUsuario(
    viewModel: GestionUsuariosViewModel,
    usuario: Usuario2,
    onDismiss: () -> Unit
) {
    var primerNombre by remember { mutableStateOf(usuario.primerNombre) }
    var segundoNombre by remember { mutableStateOf(usuario.segundoNombre) }
    var apellidoPaterno by remember { mutableStateOf(usuario.apellidoPaterno) }
    var apellidoMaterno by remember { mutableStateOf(usuario.apellidoMaterno) }
    var email by remember { mutableStateOf(usuario.email) }
    var username by remember { mutableStateOf(usuario.username) }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val huboAlgunCambio = remember(primerNombre, segundoNombre, apellidoPaterno, apellidoMaterno, email, username, password, rolSeleccionado) {
        primerNombre != usuario.primerNombre ||
                segundoNombre != usuario.segundoNombre ||
                apellidoPaterno != usuario.apellidoPaterno ||
                apellidoMaterno != usuario.apellidoMaterno ||
                email != usuario.email ||
                username != usuario.username ||
                password.isNotBlank() ||
                rolSeleccionado != usuario.rol
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFC107))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B4E00)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF6B4E00)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    errorMessage?.let { error ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    OutlinedTextField(
                        value = primerNombre,
                        onValueChange = { primerNombre = it },
                        label = { Text("Primer Nombre *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = segundoNombre,
                        onValueChange = { segundoNombre = it },
                        label = { Text("Segundo Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apellidoPaterno,
                        onValueChange = { apellidoPaterno = it },
                        label = { Text("Apellido Paterno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = apellidoMaterno,
                        onValueChange = { apellidoMaterno = it },
                        label = { Text("Apellido Materno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider()

                    Text(
                        text = "Credenciales",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Nueva Contraseña (dejar vacío si no cambia)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    if (mostrarPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (mostrarPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider()

                    Text(
                        text = "Rol del Usuario",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Rol2.obtenerTodos().forEach { rol ->
                            RolSelectionCard(
                                rol = rol,
                                selected = rolSeleccionado == rol,
                                onClick = { rolSeleccionado = rol }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            when {
                                primerNombre.isBlank() -> errorMessage = "El primer nombre es obligatorio"
                                email.isBlank() -> errorMessage = "El email es obligatorio"
                                !email.contains("@") -> errorMessage = "Email inválido"
                                password.isNotBlank() && password.length < 6 -> errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                else -> {
                                    val usuarioActualizado = usuario.copy(
                                        primerNombre = primerNombre.trim(),
                                        segundoNombre = segundoNombre.trim(),
                                        apellidoPaterno = apellidoPaterno.trim(),
                                        apellidoMaterno = apellidoMaterno.trim(),
                                        email = email.trim().lowercase(),
                                        username = username.trim(),
                                        password = if (password.isNotBlank()) password else usuario.password,
                                        rol = rolSeleccionado
                                    )
                                    viewModel.actualizarUsuario(usuarioActualizado)
                                    onDismiss()
                                }
                            }
                        },
                        enabled = huboAlgunCambio,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color(0xFF6B4E00)
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoConfirmarEliminacion(
    usuario: Usuario2,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    var textoConfirmacion by remember { mutableStateOf("") }
    val textoRequerido = "ELIMINAR"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Eliminar Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "¿Está seguro que desea eliminar a ${usuario.obtenerNombreCompleto()}?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Para confirmar, escriba \"$textoRequerido\":",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textoConfirmacion,
                    onValueChange = { textoConfirmacion = it.uppercase() },
                    placeholder = { Text(textoRequerido) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = textoConfirmacion.isNotEmpty() && textoConfirmacion != textoRequerido
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = textoConfirmacion == textoRequerido,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
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

private fun obtenerColorRol(rol: Rol2): Color {
    return when (rol) {
        Rol2.ADMINISTRADOR -> Color(0xFFF44336)
        Rol2.CO_ADMINISTRADOR -> Color(0xFFFF9800)
        Rol2.GESTOR_PEDIDOS -> Color(0xFF4CAF50)
        Rol2.PROFESOR_A_CARGO -> Color(0xFF3F51B5)
        Rol2.DOCENTE -> Color(0xFF2196F3)
        Rol2.ENCARGADO_BODEGA -> Color(0xFF9C27B0)
        Rol2.ASISTENTE_BODEGA -> Color(0xFF00BCD4)
    }
}

private fun obtenerIconoRol(rol: Rol2): ImageVector {
    return when (rol) {
        Rol2.ADMINISTRADOR -> Icons.Default.Security
        Rol2.CO_ADMINISTRADOR -> Icons.Default.SupervisorAccount
        Rol2.GESTOR_PEDIDOS -> Icons.Default.Assignment
        Rol2.PROFESOR_A_CARGO -> Icons.Default.ManageAccounts
        Rol2.DOCENTE -> Icons.Default.School
        Rol2.ENCARGADO_BODEGA -> Icons.Default.Inventory
        Rol2.ASISTENTE_BODEGA -> Icons.Default.Person
    }
}