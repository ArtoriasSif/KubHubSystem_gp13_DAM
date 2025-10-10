package com.example.kubhubsystem_gp13_dam.model

data class Asignatura(
    val idRamo: Int = 0,
    val nombreRamo: String,
    val codigoRamo: String,  // Ej: GAS-101
    val coordinador: String,
    val creditos: Int,
    val periodo: String,  // Ej: 2025-1
    val secciones: List<Seccion> = emptyList()
)

data class Seccion(
    val idSeccion: Int = 0,
    val numeroSeccion: String,
    val docente: String,
    val horarios: List<HorarioConSala> = emptyList(),  // ✅ Cambiado
    val estaActiva: Boolean = true
)

// ✅ Nuevo modelo que incluye sala
data class HorarioConSala(
    val diaSemana: DiaSemana,
    val bloqueHorario: Int,
    val sala: Sala
)

enum class DiaSemana(val displayName: String, val orden: Int) {
    LUNES("Lunes", 0),
    MARTES("Martes", 1),
    MIERCOLES("Miércoles", 2),
    JUEVES("Jueves", 3),
    VIERNES("Viernes", 4),
    SABADO("Sábado", 5)
}

// Utilidad para convertir bloques a horas legibles
object HorarioUtils {
    fun getBloqueHorario(bloque: Int): String {
        val horarios = mapOf(
            1 to "8:01:00 - 8:40:00",
            2 to "8:41:00 - 9:20:00",
            3 to "9:31:00 - 10:10:00",
            4 to "10:11:00 - 10:50:00",
            5 to "11:01:00 - 11:40:00",
            6 to "11:41:00 - 12:20:00",
            7 to "12:31:00 - 13:10:00",
            8 to "13:11:00 - 13:50:00",
            9 to "14:01:00 - 14:40:00",
            10 to "14:41:00 - 15:20:00",
            11 to "15:31:00 - 16:10:00",
            12 to "16:11:00 - 16:50:00",
            13 to "17:01:00 - 17:40:00",
            14 to "17:41:00 - 18:20:00",
            15 to "6:21:00 p.m. - 7:00:00 p.m.",
            16 to "7:01:00 p.m. - 7:40:00 p.m.",
            17 to "7:41:00 p.m. - 8:20:00 p.m.",
            18 to "8:31:00 p.m. - 9:10:00 p.m.",
            19 to "9:11:00 p.m. - 9:50:00 p.m.",
            20 to "9:51:00 p.m. - 10:30:00 p.m."
        )
        return horarios[bloque] ?: "Horario no definido"
    }

    // Versión corta para mostrar en chips o cards pequeñas
    fun getBloqueHorarioCorto(bloque: Int): String {
        val horarios = mapOf(
            1 to "8:01-8:40",
            2 to "8:41-9:20",
            3 to "9:31-10:10",
            4 to "10:11-10:50",
            5 to "11:01-11:40",
            6 to "11:41-12:20",
            7 to "12:31-13:10",
            8 to "13:11-13:50",
            9 to "14:01-14:40",
            10 to "14:41-15:20",
            11 to "15:31-16:10",
            12 to "16:11-16:50",
            13 to "17:01-17:40",
            14 to "17:41-18:20",
            15 to "18:21-19:00",
            16 to "19:01-19:40",
            17 to "19:41-20:20",
            18 to "20:31-21:10",
            19 to "21:11-21:50",
            20 to "21:51-22:30"
        )
        return horarios[bloque] ?: "N/A"
    }
}