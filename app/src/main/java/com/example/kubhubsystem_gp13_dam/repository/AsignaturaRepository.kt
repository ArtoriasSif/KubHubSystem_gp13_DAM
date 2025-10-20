package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.local.dao.AsignaturaDAO
import com.example.kubhubsystem_gp13_dam.local.entities.AsignaturaEntity
import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class AsignaturaRepository(private val asignaturaDAO: AsignaturaDAO) {

    suspend fun inicializarAsignaturas() {
        val asignaturasIniciales = listOf(
            AsignaturaEntity(
                idAsignatura = 1,
                nombreAsignatura = "Panadería Básica",
                codigoAsignatura = "GAS-101"
            ),
            AsignaturaEntity(idAsignatura = 2, nombreAsignatura = "Pastelería Avanzada", codigoAsignatura = "GAS-102"),
            AsignaturaEntity(idAsignatura = 3, nombreAsignatura = "Cocina Internacional", codigoAsignatura = "GAS-201"),
            AsignaturaEntity(idAsignatura = 4, nombreAsignatura = "Cocina Chilena", codigoAsignatura = "GAS-202")
        )

        asignaturasIniciales.forEach { asignatura ->
            val existe = asignaturaDAO.existeAsignatura(asignatura.idAsignatura)
            if (existe == 0) {
                asignaturaDAO.insertarAsignatura(asignatura)
            }
        }
    }

    fun obtenerTodasLasAsignaturas(): Flow<List<Asignatura>> {
        return asignaturaDAO.obtenerTodasLasAsignaturas().map { entities ->
            entities.map { it.toAsignatura() }
        }
    }

    suspend fun obtenerAsignaturaPorId(idAsignatura: Int): Asignatura? {
        return asignaturaDAO.obtenerAsignaturaPorId(idAsignatura)?.toAsignatura()
    }

    suspend fun obtenerAsignaturaPorCodigo(codigo: String): Asignatura? {
        return asignaturaDAO.obtenerAsignaturaPorCodigo(codigo)?.toAsignatura()
    }

    suspend fun obtenerTodas(): List<Asignatura> {
        return asignaturaDAO.obtenerTodasLasAsignaturas().first().map { it.toAsignatura() }
    }
    suspend fun insertarAsignatura(asignatura: Asignatura) {
        asignaturaDAO.insertarAsignatura(asignatura.toEntity())
    }

    suspend fun actualizarAsignatura(asignatura: Asignatura) {
        asignaturaDAO.actualizarAsignatura(asignatura.toEntity())
    }

    suspend fun eliminarAsignatura(asignatura: Asignatura) {
        asignaturaDAO.eliminarAsignatura(asignatura.toEntity())
    }

    suspend fun buscarAsignaturasPorNombre(nombre: String): List<Asignatura> {
        return asignaturaDAO.buscarAsignaturasPorNombre(nombre).map { it.toAsignatura() }
    }

    // Extension functions para convertir entre Entity y Model
    private fun AsignaturaEntity.toAsignatura(): Asignatura {
        return Asignatura(
            idAsignatura = idAsignatura,
            nombreAsignatura = nombreAsignatura,
            codigoAsignatura = codigoAsignatura,
            periodo = ""
        )
    }

    private fun Asignatura.toEntity(): AsignaturaEntity {
        return AsignaturaEntity(
            idAsignatura = this.idAsignatura,
            nombreAsignatura = this.nombreAsignatura,
            codigoAsignatura = this.codigoAsignatura
        )
    }
}