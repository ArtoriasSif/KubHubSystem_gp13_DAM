package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.AsignaturaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.ReservaSalaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SalaDAO
import com.example.kubhubsystem_gp13_dam.local.entities.ReservaSalaEntity
import com.example.kubhubsystem_gp13_dam.model.Asignatura
import com.example.kubhubsystem_gp13_dam.model.DiaSemana
import com.example.kubhubsystem_gp13_dam.model.ReservaSala
import com.example.kubhubsystem_gp13_dam.model.Sala
import com.example.kubhubsystem_gp13_dam.model.Seccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReservaSalaRepository(private val reservaSalaDAO: ReservaSalaDAO,
                            private val salaDAO: SalaDAO,
                            private val asignaturaDAO: AsignaturaDAO
) {


    suspend fun inicializarReservas() {
        val reservasIniciales = listOf(
            // Panadería Básica - Sección 001 (idSeccion = 1)
            ReservaSalaEntity(idReservaSala = 1, idSeccion = 1, idSala = 1, codigoTaller = "TALLER-001", bloque = 1, diaSemana = "LUNES"),
            ReservaSalaEntity(
                idReservaSala = 2,
                idSeccion = 1,
                idSala = 1,
                codigoTaller = "TALLER-001",
                bloque = 2,
                diaSemana = "LUNES"
            ),
            ReservaSalaEntity(idReservaSala = 3, idSeccion = 1, idSala = 1, codigoTaller = "TALLER-001", bloque = 3, diaSemana = "MIERCOLES"),

            // Panadería Básica - Sección 002 (idSeccion = 2)
            ReservaSalaEntity(idReservaSala = 4, idSeccion = 2, idSala = 2, codigoTaller = "TALLER-002", bloque = 5, diaSemana = "MARTES"),
            ReservaSalaEntity(idReservaSala = 5, idSeccion = 2, idSala = 2, codigoTaller = "TALLER-002", bloque = 5, diaSemana = "JUEVES"),

            // Panadería Básica - Sección 003 (idSeccion = 3)
            ReservaSalaEntity(idReservaSala = 6, idSeccion = 3, idSala = 3, codigoTaller = "TALLER-003", bloque = 10, diaSemana = "VIERNES"),

            // Pastelería Avanzada - Sección 001 (idSeccion = 4)
            ReservaSalaEntity(idReservaSala = 7, idSeccion = 4, idSala = 1, codigoTaller = "TALLER-004", bloque = 7, diaSemana = "LUNES"),
            ReservaSalaEntity(idReservaSala = 8, idSeccion = 4, idSala = 1, codigoTaller = "TALLER-004", bloque = 7, diaSemana = "MIERCOLES"),

            // Pastelería Avanzada - Sección 002 (idSeccion = 5)
            ReservaSalaEntity(idReservaSala = 9, idSeccion = 5, idSala = 4, codigoTaller = "TALLER-005", bloque = 12, diaSemana = "MARTES"),

            // Cocina Internacional - Sección 001 (idSeccion = 6)
            ReservaSalaEntity(idReservaSala = 10, idSeccion = 6, idSala = 5, codigoTaller = "TALLER-006", bloque = 15, diaSemana = "LUNES"),
            ReservaSalaEntity(idReservaSala = 11, idSeccion = 6, idSala = 5, codigoTaller = "TALLER-006", bloque = 15, diaSemana = "MIERCOLES"),

            // Cocina Internacional - Sección 002 (idSeccion = 7)
            ReservaSalaEntity(idReservaSala = 12, idSeccion = 7, idSala = 3, codigoTaller = "TALLER-007", bloque = 18, diaSemana = "JUEVES"),

            // Cocina Chilena - Sección 001 (idSeccion = 8)
            ReservaSalaEntity(idReservaSala = 13, idSeccion = 8, idSala = 2, codigoTaller = "TALLER-008", bloque = 9, diaSemana = "MARTES"),

            // Cocina Chilena - Sección 002 (idSeccion = 9)
            ReservaSalaEntity(idReservaSala = 14, idSeccion = 9, idSala = 4, codigoTaller = "TALLER-009", bloque = 14, diaSemana = "VIERNES")
        )

        reservasIniciales.forEach { reserva ->
            val existe = reservaSalaDAO.existeReserva(reserva.idReservaSala)
            if (existe == 0) {
                // Verificar que no haya conflicto antes de insertar
                val conflicto = reservaSalaDAO.verificarDisponibilidad(
                    reserva.idSala,
                    reserva.diaSemana,
                    reserva.bloque
                )
                if (conflicto == 0) {
                    reservaSalaDAO.insertarReserva(reserva)
                }
            }
        }
    }

    fun obtenerTodasLasReservas(): Flow<List<ReservaSala>> {
        return reservaSalaDAO.obtenerTodasLasReservas().map { entities ->
            entities.map { it.toReservaSala() }
        }
    }

    suspend fun obtenerReservaPorId(idReserva: Int): ReservaSala? {
        return reservaSalaDAO.obtenerReservaPorId(idReserva)?.toReservaSala()
    }

    suspend fun obtenerReservasPorSeccion(idSeccion: Int): List<ReservaSala> {
        val reservasEntity = reservaSalaDAO.obtenerReservasPorSeccion(idSeccion)  // ✅ CORREGIDO

        return reservasEntity.map { entity ->
            val salaEntity = salaDAO.obtenerSalaPorId(entity.idSala)
            val asignaturaEntity = asignaturaDAO.findAsignaturaByidSeccion(entity.idSeccion)  // ✅ Tu método custom

            ReservaSala(
                idReservaSala = entity.idReservaSala,
                seccion = Seccion(idSeccion, ""),
                asignatura = asignaturaEntity?.let { asig ->
                    Asignatura(
                        idAsignatura = asig.idAsignatura,
                        nombreAsignatura = asig.nombreAsignatura,
                        codigoAsignatura = asig.codigoAsignatura,
                        periodo = ""
                    )
                } ?: Asignatura(0, "Sin asignatura", "", ""),
                sala = salaEntity?.let { sala ->
                    Sala(sala.idSala, sala.codigoSala)
                } ?: Sala(0, "Sin sala"),
                diaSemana = DiaSemana.valueOf(entity.diaSemana ?: "LUNES"),  // ✅ Agregar null-safety
                bloqueHorario = entity.bloque
            )
        }
    }

    suspend fun obtenerReservasPorSala(idSala: Int): List<ReservaSala> {
        return reservaSalaDAO.obtenerReservasPorSala(idSala).map { it.toReservaSala() }
    }

    suspend fun verificarDisponibilidad(idSala: Int, diaSemana: DiaSemana, bloque: Int): Boolean {
        return reservaSalaDAO.verificarDisponibilidad(idSala, diaSemana.name, bloque) == 0
    }

    suspend fun insertarReserva(reserva: ReservaSala) {
        // Verificar disponibilidad antes de insertar
        val disponible = verificarDisponibilidad(
            reserva.sala.idSala,
            reserva.diaSemana,
            reserva.bloqueHorario
        )
        if (disponible) {
            reservaSalaDAO.insertarReserva(reserva.toEntity())
        } else {
            throw Exception("La sala no está disponible en ese horario")
        }
    }

    suspend fun actualizarReserva(reserva: ReservaSala) {
        reservaSalaDAO.actualizarReserva(reserva.toEntity())
    }

    suspend fun eliminarReserva(reserva: ReservaSala) {
        reservaSalaDAO.eliminarReserva(reserva.toEntity())
    }

    suspend fun obtenerReservasPorDiaYBloque(dia: DiaSemana, bloque: Int): List<ReservaSala> {
        return reservaSalaDAO.obtenerReservasPorDiaYBloque(dia.name, bloque).map { it.toReservaSala() }
    }



    // Extension functions para convertir entre Entity y Model
    private fun ReservaSalaEntity.toReservaSala(): ReservaSala {
        return ReservaSala(
            idReservaSala = this.idReservaSala,
            seccion = Seccion(idSeccion = this.idSeccion, nombreSeccion = "", nombreDocente = ""),
            asignatura = Asignatura(
                idAsignatura = 0,
                nombreAsignatura = "",
                codigoAsignatura = "",
                periodo = ""
            ),
            sala = Sala(idSala = this.idSala, codigoSala = ""),
            diaSemana = DiaSemana.valueOf(this.diaSemana),
            bloqueHorario = this.bloque
        )
    }

    private fun ReservaSala.toEntity(): ReservaSalaEntity {
        return ReservaSalaEntity(
            idReservaSala = this.idReservaSala,
            idSeccion = this.seccion.idSeccion,
            idSala = this.sala.idSala,
            codigoTaller = "TALLER-${this.seccion.idSeccion.toString().padStart(3, '0')}",
            bloque = this.bloqueHorario,
            diaSemana = this.diaSemana.name
        )
    }
}