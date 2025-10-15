package com.example.kubhubsystem_gp13_dam.model

import java.time.LocalDate

data class PeriodoRecoleccion(
    val idPeriodo: Int = 0,
    val fechaInicio: LocalDate,
    val fechaCierre: LocalDate,
    val estaActivo: Boolean = true,
    val solicitudesIds: List<Int> = emptyList()
)