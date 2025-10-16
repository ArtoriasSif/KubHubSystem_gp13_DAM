package com.example.kubhubsystem_gp13_dam.ui.screens

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosEstado
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onNavigateToDetalleUsuario: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.obtener(context.applicationContext) }

    val usuarioRepository = remember { UsuarioRepository(database.usuarioDao()) }
    val rolRepository = remember { RolRepository(database.rolDao()) }
    val docenteRepository = remember { DocenteRepository(database.docenteDao()) }

    val viewModel: GestionUsuariosViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GestionUsuariosViewModel(
                    usuarioRepository = usuarioRepository,
                    rolRepository = rolRepository,
                    docenteRepository = docenteRepository
                ) as T
            }
        }
    )

    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Usuario?>(null) }
    var showNuevoUsuarioDialog by remember { mutableStateOf(false) }

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

    // Inicializar datos solo una vez
    LaunchedEffect(Unit) {
        viewModel.inicializarDatosSiEsNecesario()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // ✅ TopAppBar SIN flecha ni reload
            TopAppBar(
                title = { Text("Gestión de Usuarios") }
            )
        },
        floatingActionButton = {
            // ✅ FAB que ABRE el diálogo
            if (!estado.cargando) {
                ExtendedFloatingActionButton(
                    onClick = { showNuevoUsuarioDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Nuevo Usuario") }
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
                            text = if (estado.inicializando)
                                "Inicializando sistema..."
                            else
                                "Cargando usuarios...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            estado.usuarios.isEmpty() && !estado.cargando -> {
                PantallaInicial(
                    onInicializar = { viewModel.inicializarDatos() },
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                ContenidoPrincipal(
                    viewModel = viewModel,
                    estado = estado,
                    onFiltroRolChange = viewModel::onFiltroRolChange,
                    onBuscarTextoChange = viewModel::onBuscarTextoChange,
                    onEditarUsuario = onNavigateToDetalleUsuario,
                    onEliminarUsuario = { showDeleteDialog = it },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // ✅ Diálogo de confirmación de eliminación
    showDeleteDialog?.let { usuario ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Eliminar Usuario") },
            text = {
                Text("¿Está seguro que desea eliminar a ${usuario.primeroNombre} ${usuario.apellidoPaterno}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarUsuario(usuario)
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

    // ✅ Diálogo FUNCIONAL de nuevo usuario
    if (showNuevoUsuarioDialog) {
        NuevoUsuarioDialog(
            onDismiss = { showNuevoUsuarioDialog = false },
            onCrear = { primeroNombre, segundoNombre, apellidoPaterno, apellidoMaterno,
                        email, username, password, rol ->
                viewModel.crearUsuario(
                    primeroNombre = primeroNombre,
                    segundoNombre = segundoNombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    email = email,
                    username = username,
                    password = password,
                    rol = rol
                )
                showNuevoUsuarioDialog = false
            }
        )
    }
}

// ✅ DIÁLOGO DE NUEVO USUARIO
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoUsuarioDialog(
    onDismiss: () -> Unit,
    onCrear: (String, String, String, String, String, String, String, Rol) -> Unit
) {
    var primeroNombre by remember { mutableStateOf("") }
    var segundoNombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol.DOCENTE) }
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
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error message
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
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = primeroNombre,
                        onValueChange = { primeroNombre = it },
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
                        label = { Text("Apellido Paterno *") },
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

                    Divider()

                    Text(
                        text = "Credenciales",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                        label = { Text("Username *") },
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

                    Divider()

                    Text(
                        text = "Rol del Usuario",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Selector de rol
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Rol.values().forEach { rol ->
                            RolSelectionCard(
                                rol = rol,
                                selected = rolSeleccionado == rol,
                                onClick = { rolSeleccionado = rol }
                            )
                        }
                    }
                }

                // Botones de acción
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
                                primeroNombre.isBlank() -> errorMessage = "El primer nombre es obligatorio"
                                apellidoPaterno.isBlank() -> errorMessage = "El apellido paterno es obligatorio"
                                email.isBlank() -> errorMessage = "El email es obligatorio"
                                !email.contains("@") -> errorMessage = "Email inválido"
                                username.isBlank() -> errorMessage = "El username es obligatorio"
                                password.isBlank() -> errorMessage = "La contraseña es obligatoria"
                                password.length < 6 -> errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                else -> {
                                    onCrear(
                                        primeroNombre.trim(),
                                        segundoNombre.trim(),
                                        apellidoPaterno.trim(),
                                        apellidoMaterno.trim(),
                                        email.trim().lowercase(),
                                        username.trim().lowercase(),
                                        password,
                                        rolSeleccionado
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Usuario")
                    }
                }
            }
        }
    }
}

@Composable
fun RolSelectionCard(
    rol: Rol,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                obtenerColorRol(rol).copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, obtenerColorRol(rol))
        } else null
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
                    Text(
                        text = rol.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = obtenerColorRol(rol)
                )
            }
        }
    }
}

// ✅ Pantalla inicial cuando no hay usuarios
@Composable
private fun PantallaInicial(
    onInicializar: () -> Unit,
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
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido al Sistema",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No hay usuarios en el sistema.\nInicialice los datos para comenzar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onInicializar,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inicializar Datos del Sistema")
            }
        }
    }
}

@Composable
private fun ContenidoPrincipal(
    viewModel: GestionUsuariosViewModel,
    estado: GestionUsuariosEstado,
    onFiltroRolChange: (String) -> Unit,
    onBuscarTextoChange: (String) -> Unit,
    onEditarUsuario: (Int) -> Unit,
    onEliminarUsuario: (Usuario) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        // Header con estadísticas
        item {
            HeaderEstadisticas(
                totalUsuarios = estado.usuarios.size,
                totalDocentes = estado.totalDocentes,
                totalRoles = estado.totalRoles
            )
        }

        // Filtros
        item {
            FiltrosUsuarios(
                filtroRol = estado.filtroRol,
                onFiltroRolChange = onFiltroRolChange,
                buscarTexto = estado.buscarTexto,
                onBuscarTextoChange = onBuscarTextoChange
            )
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

        // Lista de usuarios
        if (estado.usuariosFiltrados.isEmpty()) {
            item {
                EmptyStateView(modifier = Modifier.padding(32.dp))
            }
        } else {
            items(estado.usuariosFiltrados) { usuario ->
                TarjetaUsuario(
                    usuario = usuario,
                    esDocente = viewModel.esUsuarioDocente(usuario.idUsuario),
                    onClick = { onEditarUsuario(usuario.idUsuario) },
                    onEliminar = { onEliminarUsuario(usuario) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun HeaderEstadisticas(
    totalUsuarios: Int,
    totalDocentes: Int,
    totalRoles: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC107)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                etiqueta = "Usuarios",
                color = Color(0xFF6B4E00)
            )

            EstadisticaChip(
                icon = Icons.Default.School,
                valor = totalDocentes.toString(),
                etiqueta = "Docentes",
                color = Color(0xFF6B4E00)
            )

            EstadisticaChip(
                icon = Icons.Default.Security,
                valor = totalRoles.toString(),
                etiqueta = "Roles",
                color = Color(0xFF6B4E00)
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
                .background(color.copy(alpha = 0.2f), shape = CircleShape),
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
            color = color.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosUsuarios(
    filtroRol: String,
    onFiltroRolChange: (String) -> Unit,
    buscarTexto: String,
    onBuscarTextoChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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

        val roles = listOf("Todos", "Admin", "Co-Admin", "Gestor de pedidos", "Docente", "Bodega", "Asistente")
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
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            obtenerColorRol(usuario.rol).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${usuario.primeroNombre.first()}${usuario.apellidoPaterno.first()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = obtenerColorRol(usuario.rol)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "${usuario.primeroNombre} ${usuario.apellidoPaterno}",
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

private fun obtenerColorRol(rol: Rol): Color {
    return when (rol) {
        Rol.ADMIN -> Color(0xFFF44336)
        Rol.CO_ADMIN -> Color(0xFFFF9800)
        Rol.GESTOR_PEDIDOS -> Color(0xFF4CAF50)
        Rol.DOCENTE -> Color(0xFF2196F3)
        Rol.BODEGA -> Color(0xFF9C27B0)
        Rol.ASISTENTE -> Color(0xFF00BCD4)
    }
}

private fun obtenerIconoRol(rol: Rol): ImageVector {
    return when (rol) {
        Rol.ADMIN -> Icons.Default.Security
        Rol.CO_ADMIN -> Icons.Default.SupervisorAccount
        Rol.GESTOR_PEDIDOS -> Icons.Default.Assignment
        Rol.DOCENTE -> Icons.Default.School
        Rol.BODEGA -> Icons.Default.Inventory
        Rol.ASISTENTE -> Icons.Default.Person
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