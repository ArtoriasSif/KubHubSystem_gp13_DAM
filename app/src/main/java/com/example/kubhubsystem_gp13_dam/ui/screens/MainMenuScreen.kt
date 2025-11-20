package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.utils.PermisosManager
import com.example.kubhubsystem_gp13_dam.ui.navigation.MenuRoutes
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosViewModel
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel
import com.example.kubhubsystem_gp13_dam.ui.screens.dashboard.DashboardScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.HomeInternalScreen
import com.example.kubhubsystem_gp13_dam.repository.LoginRepository
import com.example.kubhubsystem_gp13_dam.ui.components.PerfilUsuarioScreenSimple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onLogout: () -> Unit,
    solicitudViewModel: SolicitudViewModel,
    pedidoViewModel: PedidoViewModel
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val perfilManager = remember { PerfilUsuarioManager.getInstance() }
    val gestionUsuariosViewModel: GestionUsuariosViewModel = viewModel()
    val estadoUsuarios by gestionUsuariosViewModel.estado.collectAsState()

    val loginRepository = remember {
        LoginRepository.getInstance(context)
    }
    val usuarioActual = remember { loginRepository.obtenerUsuarioLogueado() }
    val rolUsuario = usuarioActual?.rol

    // Debug: Verificar que el usuario está disponible
    LaunchedEffect(Unit) {
        println("🔍 MainMenuScreen - Usuario actual: ${usuarioActual?.email}")
        println("🔍 MainMenuScreen - Rol: ${rolUsuario?.nombreRol}")
    }

    LaunchedEffect(estadoUsuarios.usuarios) {
        if (estadoUsuarios.usuarios.isNotEmpty()) {
            perfilManager.inicializarPerfiles(estadoUsuarios.usuarios)
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                // 🔄 AÑADIDO: ScrollState para hacer el contenido scrolleable
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                ) {
                    // Header del drawer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Kubhub System",
                            style = MaterialTheme.typography.titleLarge
                        )
                        usuarioActual?.let {
                            Text(
                                text = it.nombreCompleto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it.rol.nombreRol,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider()

                    // 🏠 INICIO (siempre visible)
                    if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Home.route)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Inicio") },
                            selected = currentRoute == "home_internal",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("home_internal") {
                                        popUpTo("home_internal") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 👤 PERFIL (siempre visible)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        label = { Text("Perfil") },
                        selected = currentRoute == "perfil",
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate("perfil")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // 📊 DASHBOARD
                    if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Dashboard.route)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Dashboard") },
                            selected = currentRoute == "dashboard",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("dashboard")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 📦 INVENTARIO
                    if (rolUsuario != null && PermisosManager.puedeGestionarInventario(rolUsuario)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                            label = { Text("Inventario") },
                            selected = currentRoute == "inventario",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("inventario")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 📚 RAMOS-ADMIN
                    if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Asignaturas.route)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Class, contentDescription = null) },
                            label = { Text("Ramos-Admin") },
                            selected = currentRoute == "asignaturas",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("asignaturas")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 🍳 GESTIÓN DE RECETAS
                    if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Recetas.route)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                            label = { Text("Gestión de Recetas") },
                            selected = currentRoute == "recetas",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("recetas")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 📋 SOLICITUDES
                    if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Solicitud.route)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                            label = { Text("Solicitudes") },
                            selected = currentRoute == "solicitud",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("solicitud")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 🛒 GESTIÓN DE PEDIDOS
                    if (rolUsuario != null && PermisosManager.puedeGestionarPedidos(rolUsuario)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                            label = { Text("Gestión de Pedidos") },
                            selected = currentRoute == "gestion_pedidos",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("gestion_pedidos")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // 👥 USUARIOS
                    if (rolUsuario != null && PermisosManager.puedeGestionarUsuarios(rolUsuario)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.People, contentDescription = null) },
                            label = { Text("Usuarios") },
                            selected = currentRoute == "usuarios",
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("usuarios")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 🚪 CERRAR SESIÓN
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onLogout()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                "perfil" -> ""
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
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (estadoUsuarios.cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = "home_internal"
                    ) {
                        composable("home_internal") {
                            HomeInternalScreen(
                                pedidoViewModel = pedidoViewModel,
                                onNavigateToPedidos = {
                                    navController.navigate("gestion_pedidos")
                                }
                            )
                        }

                        composable("perfil") {
                            if (usuarioActual != null) {
                                PerfilUsuarioScreenSimple(
                                    idUsuario = usuarioActual.idUsuario,
                                    perfilManager = perfilManager,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                PantallaUsuarioNoEncontrado(onLogout = onLogout)
                            }
                        }

                        composable("dashboard") {
                            if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Dashboard.route)) {
                                DashboardScreen()
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }

                        composable("inventario") {
                            if (rolUsuario != null && PermisosManager.puedeGestionarInventario(rolUsuario)) {
                                InventarioScreen()
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }

                        composable("asignaturas") {
                            if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Asignaturas.route)) {
                                GestionAcademicaScreen()
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }

                        composable("recetas") {
                            if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Recetas.route)) {
                                RecetasScreen2()
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }

                        composable("solicitud") {
                            if (rolUsuario != null && PermisosManager.tieneAcceso(rolUsuario, MenuRoutes.Solicitud.route)) {
                                SolicitudScreen(
                                    viewModel = solicitudViewModel,
                                    onNavigateBack = {
                                        navController.navigate("solicitud")
                                    }
                                )
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }

                        composable("gestion_pedidos") {
                            if (rolUsuario != null && PermisosManager.puedeGestionarPedidos(rolUsuario)) {
                                GestionPedidosScreen(
                                    viewModel = pedidoViewModel,
                                    onNavigateToSolicitud = { idSolicitud ->
                                        if (idSolicitud != null) {
                                            solicitudViewModel.cargarSolicitudParaEditar(idSolicitud)
                                        }
                                        navController.navigate("solicitud")
                                    }
                                )
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }

                        composable("usuarios") {
                            if (rolUsuario != null && PermisosManager.puedeGestionarUsuarios(rolUsuario)) {
                                GestionUsuarioScreen(
                                    onNavigateToDetalleUsuario = { idUsuario ->
                                        // TODO: Implementar navegación a detalle
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                PantallaAccesoDenegado()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaAccesoDenegado() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Acceso Denegado",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "No tienes permisos para acceder a esta sección",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PantallaUsuarioNoEncontrado(onLogout: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "No hay usuario en sesión",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = onLogout) {
                Text("Volver al login")
            }
        }
    }
}