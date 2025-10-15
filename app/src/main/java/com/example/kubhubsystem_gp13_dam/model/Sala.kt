package com.example.kubhubsystem_gp13_dam.model

data class Sala(
    val idSala: Int,
    val codigoSala: String,  // C301, C302, etc.
    val capacidad: Int = 30,
    val tipoSala: TipoSala = TipoSala.AULA_NORMAL
)

enum class TipoSala(val displayName: String) {
    AULA_NORMAL("Aula Normal"),
    LABORATORIO("Laboratorio"),
    TALLER("Taller de Cocina"),
    AUDITORIO("Auditorio")
}

// Representa una reserva de sala en un horario específico
data class ReservaSala(
    val sala: Sala,
    val diaSemana: DiaSemana,
    val bloqueHorario: Int,
    val seccionId: Int,
    val asignaturaId: Int,
    val nombreAsignatura: String,
    val numeroSeccion: String
)