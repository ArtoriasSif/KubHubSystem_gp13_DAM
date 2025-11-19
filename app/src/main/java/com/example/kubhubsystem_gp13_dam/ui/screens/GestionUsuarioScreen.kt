/**
package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.ui.components.AvatarUsuario
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosEstado
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de Gestión de Usuarios
 * ✅ ACTUALIZADO: Ahora usa el nuevo GestionUsuariosViewModel que se conecta al backend
 * ✅ MANTENIDO: Todo el estilo visual y componentes originales
 * 🆕 NUEVO: Confirmación de eliminación con texto "ELIMINAR"
 * 🆕 NUEVO: Botón activar/desactivar usuario
 * 🆕 NUEVO: Filtro por estado activo/inactivo
 * 🆕 NUEVO: Scroll mejorado con estado
 * 🆕 NUEVO: Diálogo de edición de usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onNavigateToDetalleUsuario: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: GestionUsuariosViewModel = viewModel()

    val perfilManager = remember { PerfilUsuarioManager.getInstance() }
    val perfiles by perfilManager.perfiles.collectAsState()

    val estado by viewModel.estado.collectAsState()
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Usuario?>(null) }
    var showNuevoUsuarioDialog by remember { mutableStateOf(false) }
    var showEditarUsuarioDialog by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(estado.usuarios) {
        if (estado.usuarios.isNotEmpty()) {
            perfilManager.inicializarPerfiles(estado.usuarios)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") }
            )
        },
        floatingActionButton = {
            if (!estado.cargando) {
                ExtendedFloatingActionButton(
                    onClick = { showNuevoUsuarioDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Nuevo Usuario") },
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color(0xFF6B4E00)
                )
            }
        }
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
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando usuarios...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            estado.usuarios.isEmpty() && !estado.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay usuarios registrados",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showNuevoUsuarioDialog = true }
                        ) {
                            Text("Crear Primer Usuario")
                        }
                    }
                }
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
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // 🆕 Diálogo de confirmación de eliminación con validación de texto
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

    // Diálogo de nuevo usuario
    if (showNuevoUsuarioDialog) {
        DialogoNuevoUsuario(
            viewModel = viewModel,
            onDismiss = { showNuevoUsuarioDialog = false }
        )
    }

    // 🆕 Diálogo de editar usuario
    showEditarUsuarioDialog?.let { usuario ->
        DialogoEditarUsuario(
            viewModel = viewModel,
            usuario = usuario,
            onDismiss = { showEditarUsuarioDialog = null }
        )
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
    onEliminarUsuario: (Usuario) -> Unit,
    onToggleEstadoUsuario: (Usuario) -> Unit,
    modifier: Modifier = Modifier
) {
    // 🆕 Estado del scroll para mejor navegación
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Estadísticas
        if (estado.estadisticas != null) {
            TarjetasEstadisticas(
                totalUsuarios = estado.estadisticas.totalUsuarios.toInt(),
                usuariosActivos = estado.estadisticas.usuariosActivos.toInt(),
                usuariosInactivos = estado.estadisticas.usuariosInactivos.toInt(),
                totalRoles = estado.estadisticas.totalRoles.toInt()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Búsqueda y filtros
        SeccionBusquedaYFiltros(
            buscarTexto = estado.buscarTexto,
            filtroRol = estado.filtroRol,
            filtroEstado = estado.filtroEstado,
            onFiltroRolChange = onFiltroRolChange,
            onFiltroEstadoChange = onFiltroEstadoChange,
            onBuscarTextoChange = onBuscarTextoChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de usuarios con scroll mejorado
        if (estado.usuariosFiltrados.isEmpty()) {
            EmptyStateView()
        } else {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = estado.usuariosFiltrados,
                        key = { it.idUsuario }
                    ) { usuario ->
                        TarjetaUsuario(
                            usuario = usuario,
                            perfil = perfiles[usuario.idUsuario],
                            esDocente = usuario.rol == Rol.DOCENTE,
                            onClick = { onEditarUsuario(usuario.idUsuario) },
                            onEliminar = { onEliminarUsuario(usuario) },
                            onToggleEstado = { onToggleEstadoUsuario(usuario) }
                        )
                    }
                }

                // 🆕 Botón de scroll rápido al inicio
                if (listState.firstVisibleItemIndex > 3) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Ir al inicio")
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetasEstadisticas(
    totalUsuarios: Int,
    usuariosActivos: Int,
    usuariosInactivos: Int,
    totalRoles: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaEstadistica(
            titulo = "Total",
            valor = totalUsuarios.toString(),
            icono = Icons.Default.People,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        TarjetaEstadistica(
            titulo = "Activos",
            valor = usuariosActivos.toString(),
            icono = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        TarjetaEstadistica(
            titulo = "Inactivos",
            valor = usuariosInactivos.toString(),
            icono = Icons.Default.Cancel,
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TarjetaEstadistica(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeccionBusquedaYFiltros(
    buscarTexto: String,
    filtroRol: String,
    filtroEstado: String,
    onFiltroRolChange: (String) -> Unit,
    onFiltroEstadoChange: (String) -> Unit,
    onBuscarTextoChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = buscarTexto,
            onValueChange = onBuscarTextoChange,
            label = { Text("Buscar usuario") },
            placeholder = { Text("Nombre, email o username") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (buscarTexto.isNotEmpty()) {
                    IconButton(onClick = { onBuscarTextoChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filtro por rol
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
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🆕 Filtro por estado activo/inactivo
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
                    } else null
                )
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: Usuario,
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
                        // 🆕 Indicador de estado activo/inactivo
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

            // 🆕 Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón editar
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

                // Botón activar/desactivar
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

                // Botón eliminar
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
private fun BadgeRol(rol: Rol, esDocente: Boolean) {
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

private fun obtenerColorRol(rol: Rol): Color {
    return when (rol) {
        Rol.ADMINISTRADOR -> Color(0xFFF44336)
        Rol.CO_ADMINISTRADOR -> Color(0xFFFF9800)
        Rol.GESTOR_PEDIDOS -> Color(0xFF4CAF50)
        Rol.PROFESOR_A_CARGO -> Color(0xFF3F51B5)
        Rol.DOCENTE -> Color(0xFF2196F3)
        Rol.ENCARGADO_BODEGA -> Color(0xFF9C27B0)
        Rol.ASISTENTE_BODEGA -> Color(0xFF00BCD4)
    }
}

private fun obtenerIconoRol(rol: Rol): ImageVector {
    return when (rol) {
        Rol.ADMINISTRADOR -> Icons.Default.Security
        Rol.CO_ADMINISTRADOR -> Icons.Default.SupervisorAccount
        Rol.GESTOR_PEDIDOS -> Icons.Default.Assignment
        Rol.PROFESOR_A_CARGO -> Icons.Default.ManageAccounts
        Rol.DOCENTE -> Icons.Default.School
        Rol.ENCARGADO_BODEGA -> Icons.Default.Inventory
        Rol.ASISTENTE_BODEGA -> Icons.Default.Person
    }
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
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

// 🆕 Diálogo de confirmación de eliminación con validación de texto
@Composable
private fun DialogoConfirmarEliminacion(
    usuario: Usuario,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    var textoConfirmacion by remember { mutableStateOf("") }
    val textoRequerido = "ELIMINAR"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
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
                    color = MaterialTheme.colorScheme.error
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
                    onValueChange = { textoConfirmacion = it },
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
    var rolSeleccionado by remember { mutableStateOf(Rol.DOCENTE) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Nuevo Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = primerNombre,
                    onValueChange = { primerNombre = it },
                    label = { Text("Primer Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = segundoNombre,
                    onValueChange = { segundoNombre = it },
                    label = { Text("Segundo Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apellidoPaterno,
                    onValueChange = { apellidoPaterno = it },
                    label = { Text("Apellido Paterno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apellidoMaterno,
                    onValueChange = { apellidoMaterno = it },
                    label = { Text("Apellido Materno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rol *",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de roles
                Rol.obtenerTodos().forEach { rol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rolSeleccionado = rol }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = rolSeleccionado == rol,
                            onClick = { rolSeleccionado = rol }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeRol(rol = rol, esDocente = rol == Rol.DOCENTE)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (primerNombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                viewModel.crearUsuario(
                                    primerNombre = primerNombre,
                                    segundoNombre = segundoNombre.ifBlank { null },
                                    apellidoPaterno = apellidoPaterno.ifBlank { null },
                                    apellidoMaterno = apellidoMaterno.ifBlank { null },
                                    email = email,
                                    username = username.ifBlank { null },
                                    password = password,
                                    rol = rolSeleccionado
                                )
                                onDismiss()
                            }
                        },
                        enabled = primerNombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}

// 🆕 Diálogo de editar usuario
@Composable
private fun DialogoEditarUsuario(
    viewModel: GestionUsuariosViewModel,
    usuario: Usuario,
    onDismiss: () -> Unit
) {
    var primerNombre by remember { mutableStateOf(usuario.primerNombre) }
    var segundoNombre by remember { mutableStateOf(usuario.segundoNombre ?: "") }
    var apellidoPaterno by remember { mutableStateOf(usuario.apellidoPaterno ?: "") }
    var apellidoMaterno by remember { mutableStateOf(usuario.apellidoMaterno ?: "") }
    var email by remember { mutableStateOf(usuario.email) }
    var username by remember { mutableStateOf(usuario.username ?: "") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Detectar si hubo cambios
    val huboAlgunCambio = remember(primerNombre, segundoNombre, apellidoPaterno, apellidoMaterno, email, username, password, rolSeleccionado) {
        primerNombre != usuario.primerNombre ||
                segundoNombre != (usuario.segundoNombre ?: "") ||
                apellidoPaterno != (usuario.apellidoPaterno ?: "") ||
                apellidoMaterno != (usuario.apellidoMaterno ?: "") ||
                email != usuario.email ||
                username != (usuario.username ?: "") ||
                password.isNotBlank() ||
                rolSeleccionado != usuario.rol
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Editar Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = primerNombre,
                    onValueChange = { primerNombre = it },
                    label = { Text("Primer Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = segundoNombre,
                    onValueChange = { segundoNombre = it },
                    label = { Text("Segundo Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apellidoPaterno,
                    onValueChange = { apellidoPaterno = it },
                    label = { Text("Apellido Paterno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apellidoMaterno,
                    onValueChange = { apellidoMaterno = it },
                    label = { Text("Apellido Materno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nueva Contraseña (dejar vacío si no cambia)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rol *",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de roles
                Rol.obtenerTodos().forEach { rol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rolSeleccionado = rol }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = rolSeleccionado == rol,
                            onClick = { rolSeleccionado = rol }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeRol(rol = rol, esDocente = rol == Rol.DOCENTE)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (primerNombre.isNotBlank() && email.isNotBlank()) {
                                val usuarioActualizado = usuario.copy(
                                    primerNombre = primerNombre,
                                    segundoNombre = segundoNombre.ifBlank { null },
                                    apellidoPaterno = apellidoPaterno.ifBlank { null },
                                    apellidoMaterno = apellidoMaterno.ifBlank { null },
                                    email = email,
                                    username = username.ifBlank { null },
                                    password = password,
                                    rol = rolSeleccionado
                                )
                                viewModel.actualizarUsuario(usuarioActualizado)
                                onDismiss()
                            }
                        },
                        enabled = huboAlgunCambio && primerNombre.isNotBlank() && email.isNotBlank()
                    ) {
                        Text("Actualizar")
                    }
                }
            }
        }
    }
}*/