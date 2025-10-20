package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.ui.screens.GestionAcademicaScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.GestionPedidosScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.GestionUsuariosScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.dashboard.DashboardScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.HomeInternalScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.inventario.InventarioScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.recetas.RecetasScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.solicitud.SolicitudScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.usuarios.UsuariosScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.perfil.PerfilUsuarioScreen
import kotlinx.coroutines.launch
// ✅ NUEVOS IMPORTS


import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onLogout: () -> Unit,
    // ✅ NUEVOS PARÁMETROS - ViewModels
    solicitudViewModel: SolicitudViewModel,
    pedidoViewModel: PedidoViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current

    // Crear repositorios y manager manualmente
    val database = remember { AppDatabase.obtener(context) }
    val usuarioRepository = remember { UsuarioRepository(database.usuarioDao()) }
    val rolRepository = remember { RolRepository(database.rolDao()) }
    val docenteRepository = remember { DocenteRepository(database.docenteDao()) }
    val perfilManager = remember { PerfilUsuarioManager.getInstance() }

    // Estado para usuarios
    var usuarios by remember { mutableStateOf<List<com.example.kubhubsystem_gp13_dam.model.Usuario>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        try {
            val usuariosEntity = usuarioRepository.obtenerTodos()
            usuarios = usuariosEntity.map { entity ->
                com.example.kubhubsystem_gp13_dam.model.Usuario(
                    idUsuario = entity.idUsuario,
                    rol = com.example.kubhubsystem_gp13_dam.model.Rol.desdeId(entity.idRol)
                        ?: com.example.kubhubsystem_gp13_dam.model.Rol.DOCENTE,
                    primeroNombre = entity.primeroNombre,
                    segundoNombre = entity.segundoNombre,
                    apellidoMaterno = entity.apellidoMaterno,
                    apellidoPaterno = entity.apellidoPaterno,
                    email = entity.email,
                    username = entity.username,
                    password = entity.password
                )
            }

            // Inicializar perfiles
            if (usuarios.isNotEmpty()) {
                perfilManager.inicializarPerfiles(usuarios)
            }

            cargando = false
        } catch (e: Exception) {
            cargando = false
            e.printStackTrace()
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Kubhub System",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = currentRoute == "home_internal",
                    onClick = {
                        navController.navigate("home_internal") {
                            popUpTo("home_internal") { inclusive = true }
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // 👤 Item de Perfil (justo después de Inicio)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text("Perfil") },
                    selected = currentRoute == "perfil",
                    onClick = {
                        navController.navigate("perfil")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("Inventario") },
                    selected = currentRoute == "inventario",
                    onClick = {
                        navController.navigate("inventario")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Class, contentDescription = null) },
                    label = { Text("Ramos-Admin") },
                    selected = currentRoute == "asignaturas",
                    onClick = {
                        navController.navigate("asignaturas")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    label = { Text("Gestión de Recetas") },
                    selected = currentRoute == "recetas",
                    onClick = {
                        navController.navigate("recetas")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // ✅ SOLICITUDES
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    label = { Text("Solicitudes") },
                    selected = currentRoute == "solicitud",
                    onClick = {
                        navController.navigate("solicitud")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // ✅ GESTIÓN DE PEDIDOS
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Gestión de Pedidos") },
                    selected = currentRoute == "gestion_pedidos",
                    onClick = {
                        navController.navigate("gestion_pedidos")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Usuarios") },
                    selected = currentRoute == "usuarios",
                    onClick = {
                        navController.navigate("usuarios")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when(currentRoute) {
                                "perfil" -> "Perfil"
                                "dashboard" -> "Dashboard"
                                "inventario" -> "Inventario"
                                "solicitud" -> "Solicitudes"
                                "gestion_pedidos" -> "Gestión de Pedidos"
                                "asignaturas" -> "Gestión de Asignaturas"
                                "recetas" -> "Gestión de Recetas"
                                "usuarios" -> "Gestión Usuarios"
                                else -> "Kubhub System"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home_internal",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home_internal") {
                    HomeInternalScreen(
                        pedidoViewModel = pedidoViewModel,
                        onNavigateToPedidos = {
                            navController.navigate("gestion_pedidos")  // O la ruta que uses
                        }
                    )
                }

                // Ruta de Perfil
                composable("perfil") {
                    if (cargando) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val usuarioActual = usuarios.firstOrNull()

                        if (usuarioActual != null) {
                            // Versión simplificada del PerfilUsuarioScreen
                            PerfilUsuarioScreenSimple(
                                usuario = usuarioActual,
                                perfilManager = perfilManager,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "No se pudo cargar el perfil",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }

                composable("dashboard") {
                    DashboardScreen()
                }
                composable("inventario") {
                    InventarioScreen()
                }
                composable("asignaturas") {
                    GestionAcademicaScreen()
                }
                composable("recetas") {
                    RecetasScreen()
                }



                // ✅ NUEVA RUTA - SOLICITUD
                composable("solicitud") {
                    SolicitudScreen(
                        viewModel = solicitudViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // ✅ NUEVA RUTA - GESTIÓN DE PEDIDOS
                composable("gestion_pedidos") {
                    GestionPedidosScreen(
                        viewModel = pedidoViewModel,
                        onNavigateToSolicitud = { idSolicitud ->
                            // Cargar solicitud para editar si existe
                            if (idSolicitud != null) {
                                solicitudViewModel.cargarSolicitudParaEditar(idSolicitud)
                            }
                            navController.navigate("solicitud")
                        }
                    )
                }

                composable("usuarios") {
                    GestionUsuariosScreen()
                }
            }
        }
    }
}


// Versión simplificada de PerfilUsuarioScreen que no usa ViewModels
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerfilUsuarioScreenSimple(
    usuario: com.example.kubhubsystem_gp13_dam.model.Usuario,
    perfilManager: PerfilUsuarioManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val perfiles by perfilManager.perfiles.collectAsState()
    val perfil = perfiles[usuario.idUsuario]

    var mostrarDialogoPermisos by remember { mutableStateOf(false) }

    // Image picker
    val imagePickerState = com.example.kubhubsystem_gp13_dam.utils.rememberImagePickerLauncher(
        onImageSelected = { uri ->
            perfilManager.actualizarFotoPerfil(usuario.idUsuario, uri)
        },
        onPermissionDenied = {
            mostrarDialogoPermisos = true
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(modifier = Modifier.size(160.dp)) {
                com.example.kubhubsystem_gp13_dam.ui.components.AvatarUsuario(
                    perfil = perfil,
                    size = 160.dp,
                    onClick = { imagePickerState.solicitarImagen() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre
            Text(
                text = "${usuario.primeroNombre} ${usuario.segundoNombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}".trim(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón cambiar foto
            Button(onClick = { imagePickerState.solicitarImagen() }) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar foto")
            }

            if (perfil?.fotoPerfil != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    perfilManager.actualizarFotoPerfil(usuario.idUsuario, null)
                }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar foto")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Información
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Email", usuario.email)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Username", usuario.username)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Rol", usuario.rol.obtenerNombre())
                }
            }
        }
    }

    // Diálogo de permisos
    if (mostrarDialogoPermisos) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPermisos = false },
            icon = {
                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Permiso necesario") },
            text = {
                Text("La app necesita acceso a tus fotos para cambiar la imagen de perfil.\n\n¿Deseas abrir la configuración?")
            },
            confirmButton = {
                Button(onClick = {
                    com.example.kubhubsystem_gp13_dam.utils.ImagePickerHelper.abrirConfiguracionApp(context)
                    mostrarDialogoPermisos = false
                }) {
                    Text("Abrir configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPermisos = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}