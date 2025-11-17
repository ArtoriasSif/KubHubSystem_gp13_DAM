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
 * MainMenuScreen
 * ✅ Sin UsuarioRepository local
 * ✅ GestionUsuariosViewModel obtiene usuarios del backend
 * ✅ PerfilUsuarioManager maneja fotos en memoria
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

    // ✅ Solo necesitamos el manager de perfiles
    val perfilManager = remember { PerfilUsuarioManager.getInstance() }

    // ✅ GestionUsuariosViewModel obtiene usuarios del backend automáticamente
    val gestionUsuariosViewModel: GestionUsuariosViewModel = viewModel()
    val estadoUsuarios by gestionUsuariosViewModel.estado.collectAsState()

    // Inicializar perfiles cuando se cargan los usuarios
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

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Inicio") },
                    selected = currentRoute == "home_internal",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("home_internal") {
                                popUpTo("home_internal") { inclusive = true }
                            }
                        }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("dashboard") } }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory, null) },
                    label = { Text("Inventario") },
                    selected = currentRoute == "inventario",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("inventario") } }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, null) },
                    label = { Text("Solicitudes") },
                    selected = currentRoute == "solicitudes",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("solicitudes") } }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    label = { Text("Pedidos") },
                    selected = currentRoute == "pedidos",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("pedidos") } }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null) },
                    label = { Text("Recetas") },
                    selected = currentRoute == "recetas",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("recetas") } }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, null) },
                    label = { Text("Usuarios") },
                    selected = currentRoute == "usuarios",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("usuarios") } }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") },
                    selected = currentRoute == "perfil",
                    onClick = { scope.launch { drawerState.close(); navController.navigate("perfil") } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); onLogout() } }
                )
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("KubHub System") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                        }) {
                            Icon(Icons.Default.Menu, "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (estadoUsuarios.cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(navController, startDestination = "home_internal") {
                        composable("home_internal") { HomeInternalScreen() }
                        composable("dashboard") { DashboardScreen() }
                        composable("inventario") {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Pantalla de Inventario")
                            }
                        }
                        composable("solicitudes") {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Pantalla de Solicitudes")
                            }
                        }
                        composable("pedidos") {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Pantalla de Pedidos")
                            }
                        }
                        composable("recetas") { RecetasScreen() }
                        // ✅ CORREGIDO: Los parámetros ya tienen valores por defecto
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
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            } else {
                                // Si no hay usuario, mostrar error o redirigir a login
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "No hay usuario en sesión",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = onLogout) {
                                            Text("Volver al login")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}