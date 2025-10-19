package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.repository.SolicitudRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SolicitudViewModel(
    private val solicitudRepository: SolicitudRepository,
    private val recetaRepository: RecetaRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    // Estados
    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    private val _solicitudActual = MutableStateFlow<Solicitud?>(null)
    val solicitudActual: StateFlow<Solicitud?> = _solicitudActual.asStateFlow()

    private val _detallesTemp = MutableStateFlow<List<DetalleSolicitud>>(emptyList())
    val detallesTemp: StateFlow<List<DetalleSolicitud>> = _detallesTemp.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Productos disponibles
    private val _productosDisponibles = MutableStateFlow<List<Producto>>(emptyList())
    val productosDisponibles: StateFlow<List<Producto>> = _productosDisponibles.asStateFlow()

    // Recetas
    val recetas: StateFlow<List<com.example.kubhubsystem_gp13_dam.ui.model.Receta>> =
        recetaRepository.observarRecetas()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _busquedaReceta = MutableStateFlow("")
    val busquedaReceta: StateFlow<String> = _busquedaReceta.asStateFlow()

    val recetasFiltradas: StateFlow<List<com.example.kubhubsystem_gp13_dam.ui.model.Receta>> =
        combine(_busquedaReceta, recetas) { busqueda, listaRecetas ->
            if (busqueda.isEmpty()) {
                listaRecetas
            } else {
                listaRecetas.filter {
                    it.nombre.contains(busqueda, ignoreCase = true) ||
                            it.categoria.contains(busqueda, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        observarSolicitudes()
        cargarProductos()
    }

    private fun observarSolicitudes() {
        viewModelScope.launch {
            solicitudRepository.observarTodasSolicitudes().collect { lista ->
                _solicitudes.value = lista
            }
        }
    }

    private fun cargarProductos() {
        viewModelScope.launch {
            productoRepository.observarProductos().collect { productos ->
                _productosDisponibles.value = productos.map { entity ->
                    Producto(
                        idProducto = entity.idProducto,
                        nombreProducto = entity.nombreProducto,
                        categoria = entity.categoria,
                        unidadMedida = entity.unidad
                    )
                }
            }
        }
    }

    fun actualizarBusquedaReceta(query: String) {
        _busquedaReceta.value = query
    }

    fun cargarProductosDesdeReceta(idReceta: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val receta = recetaRepository.obtenerRecetaPorId(idReceta)

                receta?.let {
                    val nuevosDetalles = it.ingredientes.map { ingrediente ->
                        DetalleSolicitud(
                            idDetalleSolicitud = 0,
                            idSolicitud = 0,
                            producto = ingrediente.producto,
                            cantidadUnidadMedida = ingrediente.cantidad
                        )
                    }
                    _detallesTemp.value = _detallesTemp.value + nuevosDetalles
                    _successMessage.value = "Productos de receta '${it.nombre}' agregados"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar receta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun agregarProductoManual(producto: Producto, cantidad: Double) {
        val nuevoDetalle = DetalleSolicitud(
            idDetalleSolicitud = 0,
            idSolicitud = 0,
            producto = producto,
            cantidadUnidadMedida = cantidad
        )
        _detallesTemp.value = _detallesTemp.value + nuevoDetalle
    }

    fun eliminarDetalleTemp(index: Int) {
        _detallesTemp.value = _detallesTemp.value.filterIndexed { i, _ -> i != index }
    }

    fun actualizarCantidadDetalle(index: Int, nuevaCantidad: Double) {
        _detallesTemp.value = _detallesTemp.value.mapIndexed { i, detalle ->
            if (i == index) detalle.copy(cantidadUnidadMedida = nuevaCantidad)
            else detalle
        }
    }

    fun guardarSolicitud() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Por ahora crear una solicitud básica
                // TODO: Completar con datos reales de asignatura/sección cuando se agregue esa funcionalidad
                val solicitudTemp = Solicitud(
                    idSolicitud = 0,
                    detalleSolicitud = _detallesTemp.value,
                    gestorPedidos = Usuario(
                        idUsuario = 1,
                        rol = Rol.GESTOR_PEDIDOS,
                        primeroNombre = "Admin",
                        segundoNombre = "",
                        apellidoMaterno = "",
                        apellidoPaterno = "Sistema",
                        email = "admin@sistema.cl",
                        username = "admin",
                        password = ""
                    ),
                    seccion = Seccion(1, "Sección A"),
                    docenteSeccion = Usuario(
                        idUsuario = 2,
                        rol = Rol.DOCENTE,
                        primeroNombre = "Juan",
                        segundoNombre = "",
                        apellidoMaterno = "",
                        apellidoPaterno = "Pérez",
                        email = "jperez@escuela.cl",
                        username = "jperez",
                        password = ""
                    ),
                    reservaSala = ReservaSala(
                        idReservaSala = 1,
                        seccion = Seccion(1, "Sección A"),
                        asignatura = Asignatura(1, "Cocina Básica", "COC101", ""),
                        sala = Sala(1, "Sala 101"),
                        diaSemana = DiaSemana.LUNES,
                        bloqueHorario = 1
                    ),
                    cantidadPersonas = 20,
                    fechaSolicitud = LocalDateTime.now(),
                    fechaCreacion = LocalDateTime.now()
                )

                solicitudRepository.crearSolicitud(solicitudTemp)

                _successMessage.value = "Solicitud creada exitosamente"
                limpiarSolicitudActual()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar solicitud: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarSolicitudParaEditar(idSolicitud: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val solicitud = solicitudRepository.obtenerSolicitud(idSolicitud)
                solicitud?.let {
                    _solicitudActual.value = it
                    _detallesTemp.value = it.detalleSolicitud
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar solicitud: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun limpiarSolicitudActual() {
        _solicitudActual.value = null
        _detallesTemp.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}