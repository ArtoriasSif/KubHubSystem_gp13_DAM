package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.local.remote.InventarioApiService
import com.example.kubhubsystem_gp13_dam.local.remote.ProductoApiService
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.repository.*
import com.example.kubhubsystem_gp13_dam.ui.navigation.MenuRoutes
import com.example.kubhubsystem_gp13_dam.ui.screens.*
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel

@Composable
fun AppContainer() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { AppDatabase.obtener(context) } // 🔹 Mantén esto hasta migrar todo

    // ============================================
    // 🔌 NUEVAS APIs desde Retrofit
    // ============================================
    val productoApi = remember { RetrofitClient.createService(ProductoApiService::class.java) }
    val inventarioApi = remember { RetrofitClient.createService(InventarioApiService::class.java) }

    // ============================================
    // 🧱 REPOSITORIOS
    // ============================================

    // 🔹 Repositorio de Producto conectado a microservicio
    val productoRepository = remember { ProductoRepository(apiService = productoApi) }

    // 🔹 Repositorio de Inventario conectado a microservicio
    val inventarioRepository = remember { InventarioRepository(apiService = inventarioApi) }

    // 🔹 Repositorios locales aún no migrados (mantener)
    val usuarioRepository = remember { UsuarioRepository(database.usuarioDao()) }
    val asignaturaRepository = remember { AsignaturaRepository(database.asignaturaDao()) }
    val seccionRepository = remember { SeccionRepository(database.seccionDao()) }
    val solicitudRepository = remember {
        SolicitudRepository(
            solicitudDao = database.solicitudDao(),
            detalleSolicitudDao = database.detalleSolicitudDao(),
            usuarioDao = database.usuarioDao(),
            seccionDao = database.seccionDao(),
            reservaSalaDao = database.reservaSalaDao(),
            productoDao = database.productoDao(),
            asignaturaDao = database.asignaturaDao(),
            salaDao = database.salaDao()
        )
    }
    val recetaRepository = remember {
        com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository(
            recetaDAO = database.recetaDao(),
            detalleDAO = database.detalleRecetaDao(),
            productoDAO = database.productoDao(),
            inventarioDAO = database.inventarioDao()
        )
    }
    val reservaSalaRepository = remember {
        ReservaSalaRepository(
            reservaSalaDAO = database.reservaSalaDao(),
            salaDAO = database.salaDao(),
            asignaturaDAO = database.asignaturaDao()
        )
    }
    val pedidoRepository = remember {
        PedidoRepository(
            pedidoDao = database.pedidoDao(),
            aglomeradoPedidoDao = database.aglomeradoPedidoDao(),
            solicitudDao = database.solicitudDao(),
            detalleSolicitudDao = database.detalleSolicitudDao(),
            estadoPedidoDao = database.estadoPedidoDao(),
            productoDao = database.productoDao(),
            asignaturaDao = database.asignaturaDao(),
            solicitudRepository = solicitudRepository
        )
    }

    // ============================================
    // 🧠 VIEWMODELS
    // ============================================
    val solicitudViewModel = remember {
        SolicitudViewModel(
            solicitudRepository = solicitudRepository,
            recetaRepository = recetaRepository,
            productoRepository = productoRepository,
            asignaturaRepository = asignaturaRepository,
            seccionRepository = seccionRepository,
            usuarioRepository = usuarioRepository,
            reservaSalaRepository = reservaSalaRepository
        )
    }

    val pedidoViewModel = remember {
        PedidoViewModel(
            pedidoRepository = pedidoRepository,
            solicitudRepository = solicitudRepository
        )
    }

    // ============================================
    // 🧭 NAVEGACIÓN
    // ============================================
    NavHost(
        navController = navController,
        startDestination = MenuRoutes.Home.route
    ) {
        composable(MenuRoutes.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(MenuRoutes.Login.route) {
                        popUpTo(MenuRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(MenuRoutes.Login.route) {
            LoginScreen(
                usuarioRepository = usuarioRepository,
                onLoginSuccess = {
                    navController.navigate(MenuRoutes.MainMenu.route) {
                        popUpTo(MenuRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(MenuRoutes.MainMenu.route) {
            MainMenuScreen(
                solicitudViewModel = solicitudViewModel,
                pedidoViewModel = pedidoViewModel,
                onLogout = {
                    navController.navigate(MenuRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
