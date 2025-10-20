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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.ui.screens.dashboard.DashboardScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.HomeInternalScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.recetas.RecetasScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.perfil.PerfilUsuarioScreenSimple
import kotlinx.coroutines.launch
// ✅ NUEVOS IMPORTS


import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel

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
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        try {
            val usuariosEntity = usuarioRepository.obtenerTodos()
            usuarios = usuariosEntity.map { entity ->
                Usuario(
                    idUsuario = entity.idUsuario,
                    rol = Rol.desdeId(entity.idRol)
                        ?: Rol.DOCENTE,
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
                            contentAlignment = Alignment.Center
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
                            navController.navigate("solicitud")
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
