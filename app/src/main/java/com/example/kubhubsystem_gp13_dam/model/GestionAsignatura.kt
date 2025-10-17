package com.example.kubhubsystem_gp13_dam.model

data class Asignatura(
    val idAsignatura: Int = 0,
    val nombreAsignatura: String,
    val codigoAsignatura: String,
    val periodo: String,
    val secciones: List<Seccion> = emptyList()
)

data class Seccion(
    val idSeccion: Int = 0,
    val nombreSeccion: String,
    val idDocente: Int? = null,  // ID del usuario que es docente
    val nombreDocente: String = "",
    val horarios: List<HorarioBloque> = emptyList(),
    val estaActiva: Boolean = true
)

data class HorarioBloque(
    val diaSemana: DiaSemana,
    val bloqueHorario: Int,
    val sala: Sala
)

data class Sala(
    val idSala: Int = 0,
    val codigoSala: String
)

data class ReservaSala(
    val idReservaSala: Int = 0,
    val seccion: Seccion,
    val asignatura: Asignatura,
    val sala: Sala,
    val diaSemana: DiaSemana,
    val bloqueHorario: Int
)

enum class DiaSemana(val nombreMostrar: String, val orden: Int) {
    LUNES("Lunes", 0),
    MARTES("Martes", 1),
    MIERCOLES("Miércoles", 2),
    JUEVES("Jueves", 3),
    VIERNES("Viernes", 4),
    SABADO("Sábado", 5),
    DOMINGO("Domingo", 6)
}