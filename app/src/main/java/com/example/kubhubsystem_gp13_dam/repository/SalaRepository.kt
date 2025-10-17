package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.local.dao.SalaDAO
import com.example.kubhubsystem_gp13_dam.local.entities.SalaEntity
import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SalaRepository (
    private val salaDAO: SalaDAO
) {

    suspend fun inicializarSalas() {
        val salasIniciales = listOf(
            SalaEntity(idSala = 1, codigoSala = "C301"),
            SalaEntity(idSala = 2, codigoSala = "C302"),
            SalaEntity(idSala = 3, codigoSala = "C303"),
            SalaEntity(idSala = 4, codigoSala = "C304"),
            SalaEntity(idSala = 5, codigoSala = "C305"),
            SalaEntity(idSala = 6, codigoSala = "LAB-101"),
            SalaEntity(idSala = 7, codigoSala = "LAB-102"),
            SalaEntity(idSala = 8, codigoSala = "TALLER-201"),
            SalaEntity(idSala = 9, codigoSala = "TALLER-202"),
            SalaEntity(idSala = 10, codigoSala = "AUDITORIO")
        )

        salasIniciales.forEach { sala ->
            val existe = salaDAO.obtenerSalaPorId(sala.idSala)
            if (existe == null) {
                salaDAO.insertarSala(sala)
            }
        }
    }

    fun obtenerTodasLasSalas(): Flow<List<Sala>> {
        return salaDAO.obtenerTodasLasSalas().map { entities ->
            entities.map { it.toSala() }
        }
    }

    suspend fun obtenerSalaPorId(idSala: Int): Sala? {
        return salaDAO.obtenerSalaPorId(idSala)?.toSala()
    }

    suspend fun obtenerSalaPorCodigo(codigo: String): Sala? {
        return salaDAO.obtenerSalaPorCodigo(codigo)?.toSala()
    }

    suspend fun insertarSala(sala: Sala) {
        salaDAO.insertarSala(sala.toEntity())
    }

    suspend fun actualizarSala(sala: Sala) {
        salaDAO.actualizarSala(sala.toEntity())
    }

    suspend fun eliminarSala(sala: Sala) {
        salaDAO.eliminarSala(sala.toEntity())
    }

    suspend fun buscarSalasPorCodigo(codigo: String): List<Sala> {
        return salaDAO.buscarSalasPorCodigo(codigo).map { it.toSala() }
    }

    // Extension functions para convertir entre Entity y Model
    private fun SalaEntity.toSala(): Sala {
        return Sala(
            idSala = this.idSala,
            codigoSala = this.codigoSala
        )
    }

    private fun Sala.toEntity(): SalaEntity {
        return SalaEntity(
            idSala = this.idSala,
            codigoSala = this.codigoSala
        )
    }
}