package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SalaRepository
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DocenteInfo(
    val idUsuario: Int,
    val nombreCompleto: String
)

data class GestionAcademicaEstado(
    val asignaturas: List<Asignatura> = emptyList(),
    val secciones: List<Seccion> = emptyList(),
    val salas: List<Sala> = emptyList(),
    val reservas: List<ReservaSala> = emptyList(),
    val docentes: List<DocenteInfo> = emptyList(),
    val cargando: Boolean = false,
    val inicializando: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val totalAsignaturas: Int = 0,
    val totalSecciones: Int = 0,
    val totalSalas: Int = 0,
    val totalReservas: Int = 0
)

class GestionAcademicaViewModel(
    private val asignaturaRepository: AsignaturaRepository,
    private val seccionRepository: SeccionRepository,
    private val salaRepository: SalaRepository,
    private val reservaSalaRepository: ReservaSalaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(GestionAcademicaEstado())
    val estado: StateFlow<GestionAcademicaEstado> = _estado.asStateFlow()

    private var datosInicializados = false

    init {
        cargarDatosCompletos()
    }

    fun inicializarDatosSiEsNecesario() {
        viewModelScope.launch {
            asignaturaRepository.obtenerTodasLasAsignaturas().first().let { asignaturas ->
                if (asignaturas.isEmpty() && !datosInicializados) {
                    inicializarDatos()
                }
            }
        }
    }

    fun inicializarDatos() {
        viewModelScope.launch {
            _estado.update {
                it.copy(
                    cargando = true,
                    inicializando = true,
                    error = null
                )
            }

            try {
                // 1. Inicializar salas
                salaRepository.inicializarSalas()

                // 2. Inicializar asignaturas
                asignaturaRepository.inicializarAsignaturas()

                // 3. Inicializar secciones (sin docentes asignados inicialmente)
                seccionRepository.inicializarSecciones()

                // 4. Inicializar reservas
                reservaSalaRepository.inicializarReservas()

                // Marcar como inicializado
                datosInicializados = true

                // 5. Cargar datos completos
                cargarDatosCompletos()

                _estado.update {
                    it.copy(
                        inicializando = false,
                        mensajeExito = "Datos académicos inicializados correctamente"
                    )
                }

            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        error = "Error al inicializar datos: ${e.message}",
                        cargando = false,
                        inicializando = false
                    )
                }
            }
        }
    }

    fun cargarDatosCompletos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            try {
                // Cargar docentes primero
                val todosUsuarios = usuarioRepository.obtenerTodos()
                val docentesInfo = todosUsuarios
                    .filter { usuario ->
                        // Filtrar usuarios con rol DOCENTE (id = 4)
                        usuario.rol == Rol2.DOCENTE
                    }
                    .map { usuario ->
                        DocenteInfo(
                            idUsuario = usuario.idUsuario,
                            nombreCompleto = "${usuario.primerNombre} ${usuario.apellidoPaterno} ${usuario.apellidoMaterno}"
                        )
                    }

                // Actualizar estado con docentes
                _estado.update { it.copy(docentes = docentesInfo) }

                // Combinar todos los flows
                combine(
                    asignaturaRepository.obtenerTodasLasAsignaturas(),
                    seccionRepository.obtenerTodasLasSecciones(),
                    salaRepository.obtenerTodasLasSalas(),
                    reservaSalaRepository.obtenerTodasLasReservas()
                ) { asignaturas, secciones, salas, reservas ->

                    // Enriquecer secciones con nombres de docentes
                    val seccionesConDocentes = secciones.map { seccion ->
                        val nombreDocente = if (seccion.idDocente != null) {
                            docentesInfo.find { it.idUsuario == seccion.idDocente }?.nombreCompleto ?: "Sin asignar"
                        } else {
                            "Sin asignar"
                        }
                        seccion.copy(nombreDocente = nombreDocente)
                    }

                    // Mapear asignaturas con sus secciones
                    val asignaturasConSecciones = asignaturas.map { asignatura ->
                        val seccionesDeAsignatura = obtenerSeccionesPorAsignaturaId(
                            asignatura.idAsignatura,
                            seccionesConDocentes,
                            reservas,
                            salas
                        )
                        asignatura.copy(secciones = seccionesDeAsignatura)
                    }

                    GestionAcademicaEstado(
                        asignaturas = asignaturasConSecciones,
                        secciones = seccionesConDocentes,
                        salas = salas,
                        reservas = reservas,
                        docentes = docentesInfo,
                        cargando = false,
                        totalAsignaturas = asignaturasConSecciones.size,
                        totalSecciones = seccionesConDocentes.size,
                        totalSalas = salas.size,
                        totalReservas = reservas.size
                    )
                }.collect { nuevoEstado ->
                    _estado.update {
                        it.copy(
                            asignaturas = nuevoEstado.asignaturas,
                            secciones = nuevoEstado.secciones,
                            salas = nuevoEstado.salas,
                            reservas = nuevoEstado.reservas,
                            cargando = nuevoEstado.cargando,
                            totalAsignaturas = nuevoEstado.totalAsignaturas,
                            totalSecciones = nuevoEstado.totalSecciones,
                            totalSalas = nuevoEstado.totalSalas,
                            totalReservas = nuevoEstado.totalReservas
                        )
                    }
                }

            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        error = "Error al cargar datos: ${e.message}",
                        cargando = false
                    )
                }
            }
        }
    }

    private suspend fun obtenerSeccionesPorAsignaturaId(
        idAsignatura: Int,
        todasLasSecciones: List<Seccion>,
        todasLasReservas: List<ReservaSala>,
        todasLasSalas: List<Sala>
    ): List<Seccion> {
        return try {
            val seccionesDeAsignatura = seccionRepository.obtenerSeccionesPorAsignatura(idAsignatura)

            seccionesDeAsignatura.map { seccion ->
                // Buscar el nombre del docente
                val seccionConDocente = todasLasSecciones.find { it.idSeccion == seccion.idSeccion }
                val nombreDocente = seccionConDocente?.nombreDocente ?: "Sin asignar"

                // Obtener horarios
                val reservasDeSeccion = todasLasReservas.filter {
                    it.seccion.idSeccion == seccion.idSeccion
                }

                val horarios = reservasDeSeccion.map { reserva ->
                    val sala = todasLasSalas.find { it.idSala == reserva.sala.idSala } ?: reserva.sala
                    HorarioBloque(
                        diaSemana = reserva.diaSemana,
                        bloqueHorario = reserva.bloqueHorario,
                        sala = sala
                    )
                }

                seccion.copy(
                    nombreDocente = nombreDocente,
                    horarios = horarios
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ============================================================================
    // MÉTODOS PARA ASIGNATURAS
    // ============================================================================

    fun agregarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try {
                asignaturaRepository.insertarAsignatura(asignatura)
                _estado.update {
                    it.copy(mensajeExito = "Asignatura creada correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al crear asignatura: ${e.message}")
                }
            }
        }
    }

    fun actualizarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try {
                asignaturaRepository.actualizarAsignatura(asignatura)
                _estado.update {
                    it.copy(mensajeExito = "Asignatura actualizada correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al actualizar asignatura: ${e.message}")
                }
            }
        }
    }

    fun eliminarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try {
                asignatura.secciones.forEach { seccion ->
                    seccionRepository.eliminarSeccion(seccion)
                }
                asignaturaRepository.eliminarAsignatura(asignatura)
                _estado.update {
                    it.copy(mensajeExito = "Asignatura eliminada correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al eliminar asignatura: ${e.message}")
                }
            }
        }
    }

    // ============================================================================
    // MÉTODOS PARA SECCIONES
    // ============================================================================

    fun agregarSeccion(seccion: Seccion, idAsignatura: Int) {
        viewModelScope.launch {
            try {
                seccionRepository.insertarSeccion(seccion, idAsignatura)
                _estado.update {
                    it.copy(mensajeExito = "Sección creada correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al crear sección: ${e.message}")
                }
            }
        }
    }

    fun actualizarSeccion(seccion: Seccion, idAsignatura: Int) {
        viewModelScope.launch {
            try {
                seccionRepository.actualizarSeccion(seccion, idAsignatura)
                _estado.update {
                    it.copy(mensajeExito = "Sección actualizada correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al actualizar sección: ${e.message}")
                }
            }
        }
    }

    fun eliminarSeccion(seccion: Seccion) {
        viewModelScope.launch {
            try {
                seccionRepository.eliminarSeccion(seccion)
                _estado.update {
                    it.copy(mensajeExito = "Sección eliminada correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al eliminar sección: ${e.message}")
                }
            }
        }
    }

    fun asignarDocenteASeccion(idSeccion: Int, idDocente: Int, idAsignatura: Int) {
        viewModelScope.launch {
            try {
                seccionRepository.asignarDocente(idSeccion, idDocente, idAsignatura)
                _estado.update {
                    it.copy(mensajeExito = "Docente asignado correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al asignar docente: ${e.message}")
                }
            }
        }
    }

    // ============================================================================
    // MÉTODOS PARA RESERVAS
    // ============================================================================

    fun agregarReserva(reserva: ReservaSala) {
        viewModelScope.launch {
            try {
                // Verificar disponibilidad antes de agregar
                val disponible = verificarDisponibilidadSala(
                    reserva.sala.idSala,
                    reserva.diaSemana,
                    reserva.bloqueHorario
                )

                if (disponible) {
                    reservaSalaRepository.insertarReserva(reserva)
                    _estado.update {
                        it.copy(mensajeExito = "Horario asignado correctamente")
                    }
                    cargarDatosCompletos()
                } else {
                    _estado.update {
                        it.copy(error = "La sala ya está ocupada en ese horario")
                    }
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al asignar horario: ${e.message}")
                }
            }
        }
    }

    fun eliminarReserva(reserva: ReservaSala) {
        viewModelScope.launch {
            try {
                reservaSalaRepository.eliminarReserva(reserva)
                _estado.update {
                    it.copy(mensajeExito = "Horario eliminado correctamente")
                }
                cargarDatosCompletos()
            } catch (e: Exception) {
                _estado.update {
                    it.copy(error = "Error al eliminar horario: ${e.message}")
                }
            }
        }
    }

    suspend fun verificarDisponibilidadSala(
        idSala: Int,
        diaSemana: DiaSemana,
        bloque: Int
    ): Boolean {
        return try {
            reservaSalaRepository.verificarDisponibilidad(idSala, diaSemana, bloque)
        } catch (e: Exception) {
            false
        }
    }

    // ============================================================================
    // MÉTODOS DE CONSULTA
    // ============================================================================

    fun obtenerSeccionesPorAsignatura(idAsignatura: Int): List<Seccion> {
        return _estado.value.asignaturas
            .find { it.idAsignatura == idAsignatura }
            ?.secciones
            ?: emptyList()
    }

    fun obtenerReservasPorSeccion(idSeccion: Int): List<ReservaSala> {
        return _estado.value.reservas.filter {
            it.seccion.idSeccion == idSeccion
        }
    }

    fun obtenerHorariosPorSeccion(idSeccion: Int): List<HorarioBloque> {
        return _estado.value.secciones
            .find { it.idSeccion == idSeccion }
            ?.horarios
            ?: emptyList()
    }

    fun obtenerBloqueDisponibles(): List<Int> {
        return (1..18).toList()
    }

    fun obtenerDiasSemanaDisponibles(): List<DiaSemana> {
        return listOf(
            DiaSemana.LUNES,
            DiaSemana.MARTES,
            DiaSemana.MIERCOLES,
            DiaSemana.JUEVES,
            DiaSemana.VIERNES,
            DiaSemana.SABADO
        )
    }

    fun obtenerNombreBloque(bloque: Int): String {
        val hora = 8 + (bloque - 1)
        return "$hora:00 - ${hora + 1}:00"
    }

    fun obtenerDocentes(): List<DocenteInfo> {
        return _estado.value.docentes
    }

    fun limpiarMensajes() {
        _estado.update {
            it.copy(
                error = null,
                mensajeExito = null
            )
        }
    }
}