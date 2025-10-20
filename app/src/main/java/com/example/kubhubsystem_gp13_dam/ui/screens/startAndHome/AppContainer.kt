package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.repository.*
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository  // ✅ AGREGAR
import com.example.kubhubsystem_gp13_dam.ui.navigation.MenuRoutes
import com.example.kubhubsystem_gp13_dam.ui.screens.login.LoginScreen
import com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.MainMenuScreen

import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel

@Composable
fun AppContainer() {
    val navController = rememberNavController()

    // Obtener contexto y base de datos
    val context = LocalContext.current
    val database = remember { AppDatabase.obtener(context) }

    // ============================================
    // REPOSITORIOS
    // ============================================

    // Usuario Repository
    val usuarioRepository = remember {
        UsuarioRepository(database.usuarioDao())
    }

    // Producto Repository
    val productoRepository = remember {
        ProductoRepository(
            dao = database.productoDao()
        )
    }

    // ✅ NUEVO: Asignatura Repository
    val asignaturaRepository = remember {
        AsignaturaRepository(
            asignaturaDAO = database.asignaturaDao()
        )
    }

    // ✅ NUEVO: Sección Repository
    val seccionRepository = remember {
        SeccionRepository(
            seccionDAO = database.seccionDao()
        )
    }

    // Solicitud Repository
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

    // Receta Repository
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

    // Pedido Repository
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
    // VIEW MODELS
    // ============================================

    // ✅ ACTUALIZADO: SolicitudViewModel con todos los repositorios
    val solicitudViewModel = remember {
        SolicitudViewModel(
            solicitudRepository = solicitudRepository,
            recetaRepository = recetaRepository,
            productoRepository = productoRepository,
            asignaturaRepository = asignaturaRepository,
            seccionRepository = seccionRepository,
            usuarioRepository = usuarioRepository,
            reservaSalaRepository = reservaSalaRepository  // ✅ NUEVO
        )
    }



    val pedidoViewModel = remember {
        PedidoViewModel(
            pedidoRepository = pedidoRepository,
            solicitudRepository = solicitudRepository
        )
    }

    // ============================================
    // NAVEGACIÓN
    // ============================================

    NavHost(
        navController = navController,
        startDestination = MenuRoutes.Home.route
    ) {
        // Pantalla de inicio/splash
        composable(MenuRoutes.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(MenuRoutes.Login.route) {
                        popUpTo(MenuRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de login
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

        // Pantalla principal con menú lateral
        composable(MenuRoutes.MainMenu.route) {
            MainMenuScreen(
                onLogout = {
                    navController.navigate(MenuRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                solicitudViewModel = solicitudViewModel,
                pedidoViewModel = pedidoViewModel
            )
        }
    }
}