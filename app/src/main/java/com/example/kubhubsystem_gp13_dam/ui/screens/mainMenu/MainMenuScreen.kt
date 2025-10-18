package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.ui.screens.GestionAcademicaScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.GestionPedidosScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.GestionUsuariosScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.SolicitudScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.dashboard.DashboardScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.HomeInternalScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.inventario.InventarioScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.recetas.RecetasScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.usuarios.UsuariosScreen
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
                    HomeInternalScreen()
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