package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.layout.*
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
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionUsuariosViewModel
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel
import com.example.kubhubsystem_gp13_dam.ui.screens.dashboard.DashboardScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.HomeInternalScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.recetas.RecetasScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.perfil.PerfilUsuarioScreenSimple
import kotlinx.coroutines.launch

/**
 * ✅ PANTALLA PRINCIPAL DEL MENÚ
 * - Integra todas las funcionalidades de ambas versiones
 * - Sin conflictos entre mainMenuConflict y MainMenuScreen
 * - Backend-first para usuarios
 * - Manejo de perfiles en memoria
 */
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

    // ✅ Manager de perfiles (fotos en memoria)
    val perfilManager = remember { PerfilUsuarioManager.getInstance() }

    // ✅ ViewModel para gestión de usuarios (obtiene del backend)
    val gestionUsuariosViewModel: GestionUsuariosViewModel = viewModel()
    val estadoUsuarios by gestionUsuariosViewModel.estado.collectAsState()

    // Inicializar perfiles cuando se cargan usuarios del backend
    LaunchedEffect(estadoUsuarios.usuarios) {
        if (estadoUsuarios.usuarios.isNotEmpty()) {
            perfilManager.inicializarPerfiles(estadoUsuarios.usuarios)
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

                // 🏠 INICIO
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

                // 👤 PERFIL
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

                // 📦 INVENTARIO
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

                // 📚 RAMOS-ADMIN
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

                // 🍳 GESTIÓN DE RECETAS
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

                // 📋 SOLICITUDES
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

                // 🛒 GESTIÓN DE PEDIDOS
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

                // 👥 USUARIOS
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
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // Mostrar loading mientras se cargan los usuarios
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
                        // 🏠 HOME INTERNAL
                        composable("home_internal") {
                            HomeInternalScreen(
                                pedidoViewModel = pedidoViewModel,
                                onNavigateToPedidos = {
                                    navController.navigate("gestion_pedidos")
                                }
                            )
                        }

                        // 👤 PERFIL
                        composable("perfil") {
                            // Obtener usuario actual desde LoginRepository
                            val loginRepository = remember {
                                com.example.kubhubsystem_gp13_dam.data.repository.LoginRepository.getInstance(context)
                            }
                            val usuarioActual = remember { loginRepository.obtenerUsuarioLogueado() }

                            if (usuarioActual != null) {
                                // ✅ ACTUALIZADO: Ahora solo pasa el idUsuario
                                PerfilUsuarioScreenSimple(
                                    idUsuario = usuarioActual.idUsuario, // Solo el ID
                                    perfilManager = perfilManager,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                // Si no hay usuario en sesión
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
                        }

                        // 📊 DASHBOARD
                        composable("dashboard") {
                            DashboardScreen()
                        }

                        // 📦 INVENTARIO
                        composable("inventario") {
                            InventarioScreen()
                        }

                        // 📚 ASIGNATURAS
                        composable("asignaturas") {
                            GestionAcademicaScreen()
                        }

                        // 🍳 RECETAS
                        composable("recetas") {
                            RecetasScreen2()
                        }

                        // 📋 SOLICITUDES
                        composable("solicitud") {
                            SolicitudScreen(
                                viewModel = solicitudViewModel,
                                onNavigateBack = {
                                    navController.navigate("solicitud")
                                }
                            )
                        }

                        // 🛒 GESTIÓN DE PEDIDOS
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

                        // 👥 USUARIOS
                        composable("usuarios") {
                            GestionUsuariosScreen(
                                onNavigateToDetalleUsuario = { idUsuario ->
                                    // TODO: Implementar navegación a detalle de usuario
                                    // navController.navigate("usuarios/detalle/$idUsuario")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}