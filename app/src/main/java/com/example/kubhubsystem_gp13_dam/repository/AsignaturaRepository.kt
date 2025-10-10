package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AsignaturaRepository {

    private val salaRepository = SalaRepository.getInstance()

    private val _asignaturas = MutableStateFlow<List<Asignatura>>(
        listOf(
            Asignatura(
                idRamo = 1,
                nombreRamo = "Panadería Básica",
                codigoRamo = "GAS-101",
                coordinador = "Juan Pérez García",
                creditos = 4,
                periodo = "2025-1",
                secciones = listOf(
                    Seccion(
                        idSeccion = 1,
                        numeroSeccion = "001",
                        docente = "María López",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.LUNES, 1, Sala(1, "C301", 30)),
                            HorarioConSala(DiaSemana.LUNES, 2, Sala(1, "C301", 30)),
                            HorarioConSala(DiaSemana.MIERCOLES, 3, Sala(1, "C301", 30))
                        ),
                        estaActiva = true
                    ),
                    Seccion(
                        idSeccion = 2,
                        numeroSeccion = "002",
                        docente = "Carlos Ruiz",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.MARTES, 5, Sala(2, "C302", 30)),
                            HorarioConSala(DiaSemana.JUEVES, 5, Sala(2, "C302", 30))
                        ),
                        estaActiva = true
                    ),
                    Seccion(
                        idSeccion = 3,
                        numeroSeccion = "003",
                        docente = "Ana Silva",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.VIERNES, 10, Sala(3, "C303", 25))
                        ),
                        estaActiva = true
                    )
                )
            ),
            Asignatura(
                idRamo = 2,
                nombreRamo = "Pastelería Avanzada",
                codigoRamo = "GAS-102",
                coordinador = "María González López",
                creditos = 5,
                periodo = "2025-1",
                secciones = listOf(
                    Seccion(
                        idSeccion = 4,
                        numeroSeccion = "001",
                        docente = "Roberto Pérez",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.LUNES, 7, Sala(1, "C301", 30)),
                            HorarioConSala(DiaSemana.MIERCOLES, 7, Sala(1, "C301", 30))
                        ),
                        estaActiva = true
                    ),
                    Seccion(
                        idSeccion = 5,
                        numeroSeccion = "002",
                        docente = "Laura Martínez",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.MARTES, 12, Sala(4, "C304", 35))
                        ),
                        estaActiva = true
                    )
                )
            ),
            Asignatura(
                idRamo = 3,
                nombreRamo = "Cocina Internacional",
                codigoRamo = "GAS-201",
                coordinador = "Pedro Sánchez Ruiz",
                creditos = 4,
                periodo = "2025-1",
                secciones = listOf(
                    Seccion(
                        idSeccion = 6,
                        numeroSeccion = "001",
                        docente = "Carmen Torres",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.LUNES, 15, Sala(5, "C305", 40)),
                            HorarioConSala(DiaSemana.MIERCOLES, 15, Sala(5, "C305", 40))
                        ),
                        estaActiva = true
                    ),
                    Seccion(
                        idSeccion = 7,
                        numeroSeccion = "002",
                        docente = "Diego Morales",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.JUEVES, 18, Sala(3, "C303", 25))
                        ),
                        estaActiva = true
                    )
                )
            ),
            Asignatura(
                idRamo = 4,
                nombreRamo = "Cocina Chilena",
                codigoRamo = "GAS-202",
                coordinador = "Ana Rodríguez Silva",
                creditos = 3,
                periodo = "2025-1",
                secciones = listOf(
                    Seccion(
                        idSeccion = 8,
                        numeroSeccion = "001",
                        docente = "Fernando Díaz",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.MARTES, 9, Sala(2, "C302", 30))
                        ),
                        estaActiva = true
                    ),
                    Seccion(
                        idSeccion = 9,
                        numeroSeccion = "002",
                        docente = "Patricia Vega",
                        horarios = listOf(
                            HorarioConSala(DiaSemana.VIERNES, 14, Sala(4, "C304", 35))
                        ),
                        estaActiva = true
                    )
                )
            )
        )
    )

    // ✅ Bloque init para registrar las reservas iniciales
    init {
        println("🔍 Inicializando AsignaturaRepository...")
        _asignaturas.value.forEach { asignatura ->
            asignatura.secciones.forEach { seccion ->
                salaRepository.registrarReservas(
                    seccionId = seccion.idSeccion,
                    asignaturaId = asignatura.idRamo,
                    nombreAsignatura = asignatura.nombreRamo,
                    numeroSeccion = seccion.numeroSeccion,
                    horarios = seccion.horarios
                )
                println("✅ Registradas ${seccion.horarios.size} reservas para ${asignatura.nombreRamo} - Sección ${seccion.numeroSeccion}")
            }
        }
        println("✅ Inicialización completa. Total reservas: ${salaRepository.reservas.value.size}")
    }

    val asignaturas: StateFlow<List<Asignatura>> = _asignaturas.asStateFlow()

    // Agregar nueva asignatura
    fun agregarAsignatura(asignatura: Asignatura) {
        val nuevoId = (_asignaturas.value.maxOfOrNull { it.idRamo } ?: 0) + 1
        val nuevaAsignatura = asignatura.copy(idRamo = nuevoId)
        _asignaturas.value = _asignaturas.value + nuevaAsignatura
    }

    // Actualizar asignatura
    fun actualizarAsignatura(asignatura: Asignatura) {
        _asignaturas.value = _asignaturas.value.map {
            if (it.idRamo == asignatura.idRamo) asignatura else it
        }
    }

    // Eliminar asignatura
    fun eliminarAsignatura(idRamo: Int) {
        _asignaturas.value = _asignaturas.value.filter { it.idRamo != idRamo }
    }

    // Agregar sección CON validación de salas
    fun agregarSeccion(idRamo: Int, seccion: Seccion) {
        _asignaturas.value = _asignaturas.value.map { asignatura ->
            if (asignatura.idRamo == idRamo) {
                val nuevoIdSeccion = (asignatura.secciones.maxOfOrNull { it.idSeccion } ?: 0) + 1
                val nuevaSeccion = seccion.copy(idSeccion = nuevoIdSeccion)

                // Registrar reservas de sala
                salaRepository.registrarReservas(
                    seccionId = nuevoIdSeccion,
                    asignaturaId = idRamo,
                    nombreAsignatura = asignatura.nombreRamo,
                    numeroSeccion = seccion.numeroSeccion,
                    horarios = seccion.horarios
                )

                asignatura.copy(secciones = asignatura.secciones + nuevaSeccion)
            } else {
                asignatura
            }
        }
    }

    // Actualizar sección
    fun actualizarSeccion(idRamo: Int, seccion: Seccion) {
        _asignaturas.value = _asignaturas.value.map { asignatura ->
            if (asignatura.idRamo == idRamo) {
                // Registrar nuevas reservas
                salaRepository.registrarReservas(
                    seccionId = seccion.idSeccion,
                    asignaturaId = idRamo,
                    nombreAsignatura = asignatura.nombreRamo,
                    numeroSeccion = seccion.numeroSeccion,
                    horarios = seccion.horarios
                )

                asignatura.copy(
                    secciones = asignatura.secciones.map {
                        if (it.idSeccion == seccion.idSeccion) seccion else it
                    }
                )
            } else {
                asignatura
            }
        }
    }

    // Eliminar sección
    fun eliminarSeccion(idRamo: Int, idSeccion: Int) {
        // Eliminar reservas de sala
        salaRepository.eliminarReservasSeccion(idSeccion)

        _asignaturas.value = _asignaturas.value.map { asignatura ->
            if (asignatura.idRamo == idRamo) {
                asignatura.copy(secciones = asignatura.secciones.filter { it.idSeccion != idSeccion })
            } else {
                asignatura
            }
        }
    }

    // ✅ Nueva función: Verificar conflicto considerando SALA
    fun verificarConflictoHorarioConSala(
        idRamo: Int,
        horarios: List<HorarioConSala>,
        seccionExcluida: Int? = null
    ): String? {
        horarios.forEach { horario ->
            if (!salaRepository.verificarDisponibilidadSala(
                    horario.sala.idSala,
                    horario.diaSemana,
                    horario.bloqueHorario,
                    seccionExcluida
                )) {
                return "La sala ${horario.sala.codigoSala} no está disponible el ${horario.diaSemana.displayName} en el bloque ${horario.bloqueHorario}"
            }
        }
        return null
    }




    companion object {
        @Volatile
        private var instance: AsignaturaRepository? = null

        fun getInstance(): AsignaturaRepository {
            return instance ?: synchronized(this) {
                instance ?: AsignaturaRepository().also { instance = it }
            }
        }
    }
}