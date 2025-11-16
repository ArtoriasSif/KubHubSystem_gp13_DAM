package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import android.util.Log
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SalaRepository
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository

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