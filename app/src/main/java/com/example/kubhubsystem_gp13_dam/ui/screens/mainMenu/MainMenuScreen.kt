package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.dashboard.DashboardScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.HomeInternalScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.inventario.InventarioScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.solicitud.SolicitudScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.usuarios.UsuariosScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(onLogout: () -> Unit) {
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
                        icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    label = { Text("Solicitudes") },
                    selected = currentRoute == "solicitud",
                    onClick = {
                        navController.navigate("solicitud")
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
                                "solicitud" -> "Solicitudes"
                                "usuarios" -> "Usuarios"
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
                composable("solicitud") {
                    SolicitudScreen()
                }
                composable("usuarios") {
                    UsuariosScreen()
                }
            }
        }
    }
}