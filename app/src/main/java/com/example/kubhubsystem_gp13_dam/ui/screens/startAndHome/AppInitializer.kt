package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import android.util.Log
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SalaRepository
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import kotlinx.coroutines.delay

class AppInitializer(private val db: AppDatabase) {

    suspend fun inicializarTodo(onProgress: (String) -> Unit = {}) {
        val tag = "AppInitializer"

        onProgress("Configurando roles")
        val rolRepository = RolRepository(db.rolDao())
        rolRepository.inicializarRoles()
        Log.d(tag, "✅ Roles inicializados")

        onProgress("Creando usuarios")
        val usuarioRepository = UsuarioRepository(db.usuarioDao())
        usuarioRepository.inicializarUsuarios()
        Log.d(tag, "✅ Usuarios creados")

        onProgress("Registrando docentes")
        val docenteRepository = DocenteRepository(db.docenteDao())
        docenteRepository.inicializarDocentes(usuarioRepository.obtenerTodos())
        Log.d(tag, "✅ Docentes registrados")

        onProgress("Inicializando productos")
        val productoRepository = ProductoRepository(db.productoDao())
        productoRepository.inicializarProductos()
        Log.d(tag, "✅ Productos inicializados")

        onProgress("Actualizando inventario")
        val inventarioRepository = InventarioRepository(db.inventarioDao(), db.productoDao())
        inventarioRepository.inicializarInventario()
        Log.d(tag, "✅ Inventario actualizado")

        onProgress("Cargando recetas")
        val recetaRepository = RecetaRepository(
            db.recetaDao(),
            db.detalleRecetaDao(),
            db.productoDao(),
            db.inventarioDao()
        )
        recetaRepository.inicializarRecetas()
        Log.d(tag, "✅ Recetas cargadas")

        onProgress("Cargando asignaturas")
        val asignaturaRepository = AsignaturaRepository(db.asignaturaDao())
        asignaturaRepository.inicializarAsignaturas()
        Log.d(tag, "✅ Asignaturas cargadas")

        onProgress("Preparando salas")
        val salaRepository = SalaRepository(db.salaDao())
        salaRepository.inicializarSalas()
        Log.d(tag, "✅ Salas preparadas")

        onProgress("Configurando secciones")
        val seccionRepository = SeccionRepository(db.seccionDao())
        seccionRepository.inicializarSecciones()
        Log.d(tag, "✅ Secciones configuradas")

        onProgress("Procesando reservas")
        // ✅ CORRECCIÓN: Pasar los 3 DAOs necesarios
        val reservaSalaRepository = ReservaSalaRepository(
            reservaSalaDAO = db.reservaSalaDao(),
            salaDAO = db.salaDao(),
            asignaturaDAO = db.asignaturaDao()
        )
        reservaSalaRepository.inicializarReservas()
        Log.d(tag, "✅ Reservas procesadas")

        onProgress("Completado")
        Log.d(tag, "🎉 Base de datos inicializada COMPLETAMENTE")
    }
}
/**
 * Log.d(...)
 * Es una clase utilitaria de Android que proporciona métodos para enviar mensajes al Logcat
 * (la ventana de registro del sistema Android en el IDE, como Android Studio).
 *
 * Log.d(tag, "...")
 * Es el método de la clase Log que indica el nivel de prioridad del mensaje.
 * La 'd' significa Debug (Depuración). Estos mensajes se usan para registrar
 * información detallada sobre el funcionamiento normal de un programa.
 *
 * Log.d("AppInitializer", ...)
 * El primer argumento. Es una cadena de texto (String) usada para identificar el origen del
 * mensaje de registro. En tu código, tag es la variable que contiene "AppInitializer".
 * Esto permite filtrar fácilmente los mensajes en Logcat
 *
 * Log.d(..., "✅ Roles inicializados")
 * El segundo argumento. Es el mensaje real que se va a mostrar. Suele describir el evento
 * que acaba de ocurrir. El emoji ✅ es útil para hacer el mensaje más visible y fácil de
 * identificar en el flujo de Logcat.
 * */