package com.example.kubhubsystem_gp13_dam.ui.viewmodel
/*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.PeriodoRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SolicitudRepository
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SolicitudViewModel(
    private val solicitudRepository: SolicitudRepository = SolicitudRepository.getInstance(),
    private val asignaturaRepository: AsignaturaRepository = AsignaturaRepository.getInstance(),
    private val productoRepository: ProductoRepository = ProductoRepository.getInstance(),
    private val recetaRepository: RecetaRepository = RecetaRepository.getInstance(),
    private val periodoRepository: PeriodoRepository = PeriodoRepository.getInstance()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedEstado = MutableStateFlow<EstadoSolicitud?>(null)
    val selectedEstado: StateFlow<EstadoSolicitud?> = _selectedEstado.asStateFlow()

    val solicitudes: StateFlow<List<Solicitud>> = solicitudRepository.solicitudes
    val asignaturas: StateFlow<List<Asignatura>> = asignaturaRepository.asignaturas
    val productos: StateFlow<List<Producto>> = productoRepository.productos
    val recetas: StateFlow<List<Receta>> = recetaRepository.recetas
    val periodoActual: StateFlow<PeriodoRecoleccion?> = periodoRepository.periodoActual

    val solicitudesFiltradas: StateFlow<List<Solicitud>> = combine(
        solicitudes,
        _searchQuery,
        _selectedEstado
    ) { solicitudes, query, estado ->
        solicitudes.filter { solicitud ->
            val matchesSearch = query.isEmpty() ||
                    solicitud.asignatura.nombreRamo.contains(query, ignoreCase = true) ||
                    solicitud.profesor.contains(query, ignoreCase = true)
            val matchesEstado = estado == null || solicitud.estado == estado
            matchesSearch && matchesEstado
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedEstado(estado: EstadoSolicitud?) {
        _selectedEstado.value = estado
    }

    fun agregarSolicitud(solicitud: Solicitud) {
        viewModelScope.launch {
            solicitudRepository.agregarSolicitud(solicitud)

            // Agregar al periodo actual si existe
            periodoActual.value?.let { periodo ->
                periodoRepository.agregarSolicitudAPeriodo(periodo.idPeriodo, solicitud.idSolicitud)
            }
        }
    }

    fun actualizarSolicitud(solicitud: Solicitud) {
        viewModelScope.launch {
            solicitudRepository.actualizarSolicitud(solicitud)
        }
    }

    fun eliminarSolicitud(idSolicitud: Int) {
        viewModelScope.launch {
            solicitudRepository.eliminarSolicitud(idSolicitud)
        }
    }

    fun cambiarEstado(idSolicitud: Int, nuevoEstado: EstadoSolicitud) {
        viewModelScope.launch {
            solicitudRepository.cambiarEstado(idSolicitud, nuevoEstado)
        }
    }

    fun getSeccionesPorAsignatura(idAsignatura: Int): List<Seccion> {
        return asignaturas.value.find { it.idRamo == idAsignatura }?.secciones ?: emptyList()
    }
}

 */