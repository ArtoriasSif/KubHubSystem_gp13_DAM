package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import android.util.Log
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SalaRepository
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository

/**
 * Inicializador de la aplicación
 * ✅ ACTUALIZADO: Ya no inicializa usuarios ni roles (vienen del backend)
 * ⚠️ MANTIENE: Inicialización de otros módulos que aún usan BD local
 */
class AppInitializer(private val db: AppDatabase) {

    suspend fun inicializarTodo(onProgress: (String) -> Unit = {}) {
        val tag = "AppInitializer"

        // ❌ ELIMINADO: Inicialización de roles y usuarios
        // Los datos de usuarios y roles ahora vienen del backend Spring Boot
        Log.d(tag, "⏭️ Usuarios y roles: Se obtienen del backend")

        onProgress("Registrando docentes")
        val docenteRepository = DocenteRepository(db.docenteDao())
        // TODO: Revisar lógica de docentes (puede necesitar adaptación)
        Log.d(tag, "✅ Docentes listos")

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
        val reservaSalaRepository = ReservaSalaRepository(
            reservaSalaDAO = db.reservaSalaDao(),
            salaDAO = db.salaDao(),
            asignaturaDAO = db.asignaturaDao()
        )
        reservaSalaRepository.inicializarReservas()
        Log.d(tag, "✅ Reservas procesadas")

        onProgress("Completado")
        Log.d(tag, "🎉 Inicialización COMPLETADA (conectado a backend para usuarios)")
    }
}