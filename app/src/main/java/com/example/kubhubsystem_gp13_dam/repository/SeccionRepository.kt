package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.SeccionDAO
import com.example.kubhubsystem_gp13_dam.local.entities.SeccionEntity
import com.example.kubhubsystem_gp13_dam.model.Seccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SeccionRepository(private val seccionDAO: SeccionDAO) {

    suspend fun inicializarSecciones() {
        val seccionesIniciales = listOf(
            // Panadería Básica (idAsignatura = 1)
            SeccionEntity(idSeccion = 1, idAsignatura = 1, nombreSeccion = "001", idDocente = null),
            SeccionEntity(idSeccion = 2, idAsignatura = 1, nombreSeccion = "002", idDocente = null),
            SeccionEntity(idSeccion = 3, idAsignatura = 1, nombreSeccion = "003", idDocente = null),

            // Pastelería Avanzada (idAsignatura = 2)
            SeccionEntity(idSeccion = 4, idAsignatura = 2, nombreSeccion = "001", idDocente = null),
            SeccionEntity(idSeccion = 5, idAsignatura = 2, nombreSeccion = "002", idDocente = null),

            // Cocina Internacional (idAsignatura = 3)
            SeccionEntity(idSeccion = 6, idAsignatura = 3, nombreSeccion = "001", idDocente = null),
            SeccionEntity(idSeccion = 7, idAsignatura = 3, nombreSeccion = "002", idDocente = null),

            // Cocina Chilena (idAsignatura = 4)
            SeccionEntity(idSeccion = 8, idAsignatura = 4, nombreSeccion = "001", idDocente = null),
            SeccionEntity(idSeccion = 9, idAsignatura = 4, nombreSeccion = "002", idDocente = null)
        )

        seccionesIniciales.forEach { seccion ->
            val existe = seccionDAO.existeSeccion(seccion.idSeccion)
            if (existe == 0) {
                seccionDAO.insertarSeccion(seccion)
            }
        }
    }

    fun obtenerTodasLasSecciones(): Flow<List<Seccion>> {
        return seccionDAO.obtenerTodasLasSecciones().map { entities ->
            entities.map { it.toSeccion() }
        }
    }

    suspend fun obtenerSeccionPorId(idSeccion: Int): Seccion? {
        return seccionDAO.obtenerSeccionPorId(idSeccion)?.toSeccion()
    }

    suspend fun obtenerSeccionesPorAsignatura(idAsignatura: Int): List<Seccion> {
        return seccionDAO.obtenerSeccionesPorAsignatura(idAsignatura).map { it.toSeccion() }
    }

    suspend fun insertarSeccion(seccion: Seccion, idAsignatura: Int) {
        seccionDAO.insertarSeccion(seccion.toEntity(idAsignatura))
    }

    suspend fun actualizarSeccion(seccion: Seccion, idAsignatura: Int) {
        seccionDAO.actualizarSeccion(seccion.toEntity(idAsignatura))
    }

    suspend fun eliminarSeccion(seccion: Seccion) {
        seccionDAO.eliminarSeccion(seccion.toEntity(0))
    }

    suspend fun buscarSeccionesPorNombre(nombre: String): List<Seccion> {
        return seccionDAO.buscarSeccionesPorNombre(nombre).map { it.toSeccion() }
    }

    suspend fun asignarDocente(idSeccion: Int, idDocente: Int, idAsignatura: Int) {
        val seccion = seccionDAO.obtenerSeccionPorId(idSeccion)
        seccion?.let {
            val seccionActualizada = it.copy(idDocente = idDocente)
            seccionDAO.actualizarSeccion(seccionActualizada)
        }
    }

    suspend fun removerDocente(idSeccion: Int, idAsignatura: Int) {
        val seccion = seccionDAO.obtenerSeccionPorId(idSeccion)
        seccion?.let {
            val seccionActualizada = it.copy(idDocente = null)
            seccionDAO.actualizarSeccion(seccionActualizada)
        }
    }

    // Extension functions para convertir entre Entity y Model
    private fun SeccionEntity.toSeccion(): Seccion {
        return Seccion(
            idSeccion = this.idSeccion,
            nombreSeccion = this.nombreSeccion,
            idDocente = this.idDocente,
            nombreDocente = "", // Se obtendrá del usuario
            horarios = emptyList(),
            estaActiva = true
        )
    }

    private fun Seccion.toEntity(idAsignatura: Int): SeccionEntity {
        return SeccionEntity(
            idSeccion = this.idSeccion,
            idAsignatura = idAsignatura,
            nombreSeccion = this.nombreSeccion,
            idDocente = this.idDocente
        )
    }
}