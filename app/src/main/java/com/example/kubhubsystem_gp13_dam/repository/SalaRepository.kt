package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SalaRepository {

    private val _salas = MutableStateFlow<List<Sala>>(
        listOf(
            Sala(idSala = 1, codigoSala = "C301", capacidad = 30, tipoSala = TipoSala.AULA_NORMAL),
            Sala(idSala = 2, codigoSala = "C302", capacidad = 30, tipoSala = TipoSala.AULA_NORMAL),
            Sala(idSala = 3, codigoSala = "C303", capacidad = 25, tipoSala = TipoSala.LABORATORIO),
            Sala(idSala = 4, codigoSala = "C304", capacidad = 35, tipoSala = TipoSala.TALLER),
            Sala(idSala = 5, codigoSala = "C305", capacidad = 40, tipoSala = TipoSala.AULA_NORMAL)
        )
    )

    val salas: StateFlow<List<Sala>> = _salas.asStateFlow()

    // Almacena todas las reservas de salas
    private val _reservas = MutableStateFlow<List<ReservaSala>>(emptyList())
    val reservas: StateFlow<List<ReservaSala>> = _reservas.asStateFlow()

    fun getSalaById(idSala: Int): Sala? {
        return _salas.value.find { it.idSala == idSala }
    }

    /**
     * Verifica si una sala está disponible en un horario específico
     */
    fun verificarDisponibilidadSala(
        salaId: Int,
        diaSemana: DiaSemana,
        bloqueHorario: Int,
        excluirSeccionId: Int? = null
    ): Boolean {
        return _reservas.value.none { reserva ->
            reserva.sala.idSala == salaId &&
                    reserva.diaSemana == diaSemana &&
                    reserva.bloqueHorario == bloqueHorario &&
                    (excluirSeccionId == null || reserva.seccionId != excluirSeccionId)
        }
    }

    /**
     * Obtiene todas las salas disponibles para un horario específico
     */
    fun getSalasDisponibles(
        diaSemana: DiaSemana,
        bloqueHorario: Int,
        excluirSeccionId: Int? = null
    ): List<Sala> {
        val salasOcupadas = _reservas.value
            .filter { reserva ->
                reserva.diaSemana == diaSemana &&
                        reserva.bloqueHorario == bloqueHorario &&
                        (excluirSeccionId == null || reserva.seccionId != excluirSeccionId)
            }
            .map { it.sala.idSala }
            .toSet()

        return _salas.value.filter { it.idSala !in salasOcupadas }
    }

    /**
     * Registra las reservas de sala para una sección
     */
    fun registrarReservas(
        seccionId: Int,
        asignaturaId: Int,
        nombreAsignatura: String,
        numeroSeccion: String,
        horarios: List<HorarioConSala>
    ) {
        // Eliminar reservas anteriores de esta sección
        _reservas.value = _reservas.value.filter { it.seccionId != seccionId }

        // Agregar nuevas reservas
        val nuevasReservas = horarios.map { horario ->
            ReservaSala(
                sala = horario.sala,
                diaSemana = horario.diaSemana,
                bloqueHorario = horario.bloqueHorario,
                seccionId = seccionId,
                asignaturaId = asignaturaId,
                nombreAsignatura = nombreAsignatura,
                numeroSeccion = numeroSeccion
            )
        }

        _reservas.value = _reservas.value + nuevasReservas
    }

    /**
     * Elimina todas las reservas de una sección
     */
    fun eliminarReservasSeccion(seccionId: Int) {
        _reservas.value = _reservas.value.filter { it.seccionId != seccionId }
    }

    /**
     * Obtiene el horario completo de una sala (para mostrar su ocupación)
     */
    fun getHorarioSala(salaId: Int): Map<DiaSemana, Map<Int, ReservaSala?>> {
        val horario = mutableMapOf<DiaSemana, MutableMap<Int, ReservaSala?>>()

        DiaSemana.values().forEach { dia ->
            horario[dia] = mutableMapOf()
            (1..20).forEach { bloque ->
                val reserva = _reservas.value.find {
                    it.sala.idSala == salaId &&
                            it.diaSemana == dia &&
                            it.bloqueHorario == bloque
                }
                horario[dia]!![bloque] = reserva
            }
        }

        return horario
    }

    companion object {
        @Volatile
        private var instance: SalaRepository? = null

        fun getInstance(): SalaRepository {
            return instance ?: synchronized(this) {
                instance ?: SalaRepository().also { instance = it }
            }
        }
    }
}