package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onNavigateToDetalleUsuario: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // ✅ CAMBIO PRINCIPAL: Ya no necesita database ni factory
    val viewModel: GestionUsuariosViewModel = viewModel()

    // 🆕 Obtener el manager de perfiles
    val perfilManager = remember { PerfilUsuarioManager.getInstance() }
    val perfiles by perfilManager.perfiles.collectAsState()

    val estado by viewModel.estado.collectAsState()
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Usuario?>(null) }
    var showNuevoUsuarioDialog by remember { mutableStateOf(false) }

    // 🆕 Sincronizar perfiles cuando cambien los usuarios
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
                // ✅ CAMBIO: Ya no mostramos pantalla de inicialización
                // porque los datos vienen del backend
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
                    onBuscarTextoChange = { viewModel.onBuscarTextoChange(it) },
                    onEditarUsuario = onNavigateToDetalleUsuario,
                    onEliminarUsuario = { showDeleteDialog = it },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // Diálogo de confirmación de eliminación
    showDeleteDialog?.let { usuario ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Eliminar Usuario") },
            text = {
                Text("¿Está seguro que desea eliminar a ${usuario.obtenerNombreCompleto()}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarUsuario(usuario)
                        perfilManager.eliminarPerfil(usuario.idUsuario)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de nuevo usuario
    if (showNuevoUsuarioDialog) {
        DialogoNuevoUsuario(
            viewModel = viewModel,
            onDismiss = { showNuevoUsuarioDialog = false }
        )
    }
}

@Composable
private fun ContenidoPrincipal(
    viewModel: GestionUsuariosViewModel,
    estado: GestionUsuariosEstado,
    perfiles: Map<Int, com.example.kubhubsystem_gp13_dam.model.PerfilUsuario>,
    onFiltroRolChange: (String) -> Unit,
    onBuscarTextoChange: (String) -> Unit,
    onEditarUsuario: (Int) -> Unit,
    onEliminarUsuario: (Usuario) -> Unit,
    modifier: Modifier = Modifier
) {
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
            onFiltroRolChange = onFiltroRolChange,
            onBuscarTextoChange = onBuscarTextoChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de usuarios
        if (estado.usuariosFiltrados.isEmpty()) {
            EmptyStateView()
        } else {
            LazyColumn(
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
                        onEliminar = { onEliminarUsuario(usuario) }
                    )
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
    onFiltroRolChange: (String) -> Unit,
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

        Text(
            text = "Filtrar por rol:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ✅ ACTUALIZADO: Ahora con los 7 roles del backend
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
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: Usuario,
    perfil: com.example.kubhubsystem_gp13_dam.model.PerfilUsuario?,
    esDocente: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    Text(
                        text = usuario.obtenerNombreCompleto(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

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

// ✅ ACTUALIZADO: Ahora con los 7 roles
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

// ✅ ACTUALIZADO: Ahora con los 7 roles
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