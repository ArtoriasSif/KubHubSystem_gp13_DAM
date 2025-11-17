package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepositoryNotDelete
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.repository.SolicitudRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel para gestión de Solicitudes
 * ✅ ACTUALIZADO: Compatible con modelo Usuario del backend
 * ✅ ELIMINADO: Ya no depende de UsuarioRepository (migrado al backend)
 * ⚠️ TEMPORAL: Aún usa DAOs para Solicitud, Receta, Asignatura, Sección (migrar gradualmente)
 */
class SolicitudViewModel(
    private val solicitudRepository: SolicitudRepository,
    private val recetaRepositoryNotDelete: RecetaRepositoryNotDelete,
    private val productoRepository: ProductoRepository,
    private val asignaturaRepository: AsignaturaRepository,
    private val seccionRepository: SeccionRepository,
    // ✅ usuarioRepository eliminado - ya no es necesario
    private val reservaSalaRepository: ReservaSalaRepository
) : ViewModel() {

    // Estados de Reservas de Sala
    private val _reservasSala = MutableStateFlow<List<ReservaSala>>(emptyList())
    val reservasSala: StateFlow<List<ReservaSala>> = _reservasSala.asStateFlow()

    private val _reservaSalaSeleccionada = MutableStateFlow<ReservaSala?>(null)
    val reservaSalaSeleccionada: StateFlow<ReservaSala?> = _reservaSalaSeleccionada.asStateFlow()

    // Estados de Solicitudes
    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    private val _solicitudActual = MutableStateFlow<Solicitud?>(null)
    val solicitudActual: StateFlow<Solicitud?> = _solicitudActual.asStateFlow()

    private val _detallesTemp = MutableStateFlow<List<DetalleSolicitud>>(emptyList())
    val detallesTemp: StateFlow<List<DetalleSolicitud>> = _detallesTemp.asStateFlow()

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Productos disponibles
    private val _productosDisponibles = MutableStateFlow<List<Producto>>(emptyList())
    val productosDisponibles: StateFlow<List<Producto>> = _productosDisponibles.asStateFlow()

    // Estados de Recetas
    val recetas: StateFlow<List<com.example.kubhubsystem_gp13_dam.ui.model.Receta>> =
        recetaRepositoryNotDelete.observarRecetas()
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

    // Estados de Asignaturas y Secciones
    private val _asignaturas = MutableStateFlow<List<Asignatura>>(emptyList())
    val asignaturas: StateFlow<List<Asignatura>> = _asignaturas.asStateFlow()

    private val _secciones = MutableStateFlow<List<Seccion>>(emptyList())
    val secciones: StateFlow<List<Seccion>> = _secciones.asStateFlow()

    private val _asignaturaSeleccionada = MutableStateFlow<Asignatura?>(null)
    val asignaturaSeleccionada: StateFlow<Asignatura?> = _asignaturaSeleccionada.asStateFlow()

    private val _seccionSeleccionada = MutableStateFlow<Seccion?>(null)
    val seccionSeleccionada: StateFlow<Seccion?> = _seccionSeleccionada.asStateFlow()

    private val _nombreDocente = MutableStateFlow("")
    val nombreDocente: StateFlow<String> = _nombreDocente.asStateFlow()

    private val _cantidadPersonas = MutableStateFlow(20)
    val cantidadPersonas: StateFlow<Int> = _cantidadPersonas.asStateFlow()

    init {
        observarSolicitudes()
        cargarAsignaturas()
    }

    // ===========================================
    // OBSERVADORES Y CARGA INICIAL
    // ===========================================

    private fun observarSolicitudes() {
        viewModelScope.launch {
            solicitudRepository.observarTodasSolicitudes().collect { lista ->
                _solicitudes.value = lista
            }
        }
    }

    private fun cargarAsignaturas() {
        viewModelScope.launch {
            try {
                val asignaturas = asignaturaRepository.obtenerTodas()
                _asignaturas.value = asignaturas
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar asignaturas: ${e.message}"
            }
        }
    }

    // ===========================================
    // MÉTODOS DE BÚSQUEDA Y FILTRADO
    // ===========================================

    fun actualizarBusquedaReceta(query: String) {
        _busquedaReceta.value = query
    }

    // ===========================================
    // MÉTODOS DE GESTIÓN DE RECETAS
    // ===========================================

    fun cargarProductosDesdeReceta(idReceta: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val receta = recetaRepositoryNotDelete.obtenerRecetaPorId(idReceta)

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

    // ===========================================
    // MÉTODOS DE GESTIÓN DE PRODUCTOS
    // ===========================================

    fun agregarProductoManual(producto: Producto, cantidad: Double) {
        val nuevoDetalle = DetalleSolicitud(
            idDetalleSolicitud = 0,
            idSolicitud = 0,
            producto = producto,
            cantidadUnidadMedida = cantidad
        )
        _detallesTemp.value = _detallesTemp.value + nuevoDetalle
    }

    fun agregarProducto(producto: Producto, cantidad: Double) {
        val nuevoDetalle = DetalleSolicitud(
            idDetalleSolicitud = 0,
            idSolicitud = 0,
            producto = producto,
            cantidadUnidadMedida = cantidad
        )
        _detallesTemp.value = _detallesTemp.value + nuevoDetalle
    }

    fun eliminarProducto(detalle: DetalleSolicitud) {
        _detallesTemp.value = _detallesTemp.value.filter { it != detalle }
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

    // ===========================================
    // MÉTODOS DE GESTIÓN DE ASIGNATURAS Y SECCIONES
    // ===========================================

    fun seleccionarAsignatura(asignatura: Asignatura) {
        _asignaturaSeleccionada.value = asignatura
        cargarSeccionesPorAsignatura(asignatura.idAsignatura)
    }

    private fun cargarSeccionesPorAsignatura(idAsignatura: Int) {
        viewModelScope.launch {
            try {
                val secciones = seccionRepository.obtenerSeccionesPorAsignatura(idAsignatura)
                _secciones.value = secciones
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar secciones: ${e.message}"
            }
        }
    }

    fun seleccionarSeccion(seccion: Seccion) {
        _seccionSeleccionada.value = seccion

        // Cargar docente
        seccion.idDocente?.let { idDocente ->
            cargarDocenteDeSeccion(idDocente)
        } ?: run {
            _nombreDocente.value = "Sin docente asignado"
        }

        // Cargar reservas de sala de la sección
        cargarReservasDeSala(seccion.idSeccion)
    }

    /**
     * ✅ SIMPLIFICADO: Ya no usa UsuarioRepository
     * TODO: Implementar cuando Secciones se migren al backend con relación a Usuario
     */
    private fun cargarDocenteDeSeccion(idDocente: Int) {
        // Temporal: Mostrar ID hasta que se migre al backend
        _nombreDocente.value = "Docente ID: $idDocente (Por implementar)"

        // TODO: Cuando se migre Secciones al backend:
        // val usuarioRepo = UsuarioRepository()
        // val docente = usuarioRepo.obtenerPorId(idDocente)
        // _nombreDocente.value = docente?.obtenerNombreCompleto() ?: "Sin docente"
    }

    // ===========================================
    // MÉTODOS DE GESTIÓN DE RESERVAS DE SALA
    // ===========================================

    private fun cargarReservasDeSala(idSeccion: Int) {
        viewModelScope.launch {
            try {
                val reservas = reservaSalaRepository.obtenerReservasPorSeccion(idSeccion)
                _reservasSala.value = reservas

                // Seleccionar la primera automáticamente si existe
                _reservaSalaSeleccionada.value = reservas.firstOrNull()

                if (reservas.isEmpty()) {
                    _errorMessage.value = "Advertencia: Esta sección no tiene reservas de sala asignadas"
                }
            } catch (e: Exception) {
                println("❌ Error al cargar reservas de sala: ${e.message}")
                _reservasSala.value = emptyList()
                _reservaSalaSeleccionada.value = null
            }
        }
    }

    fun seleccionarReservaSala(reserva: ReservaSala) {
        _reservaSalaSeleccionada.value = reserva
    }

    // ===========================================
    // MÉTODOS DE ACTUALIZACIÓN DE ESTADO
    // ===========================================

    fun actualizarNombreDocente(nuevoNombre: String) {
        _nombreDocente.value = nuevoNombre
    }

    fun actualizarCantidadPersonas(cantidad: Int) {
        _cantidadPersonas.value = cantidad
    }

    // ===========================================
    // MÉTODOS DE GESTIÓN DE SOLICITUDES
    // ===========================================

    /**
     * Guarda una nueva solicitud
     * ✅ ACTUALIZADO: Compatible con modelo Usuario del backend
     */
    fun guardarSolicitud(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val asignatura = _asignaturaSeleccionada.value
                val seccion = _seccionSeleccionada.value
                val reservaSala = _reservaSalaSeleccionada.value

                if (asignatura == null || seccion == null) {
                    _errorMessage.value = "Debe seleccionar asignatura y sección"
                    return@launch
                }

                if (reservaSala == null) {
                    _errorMessage.value = "Esta sección no tiene reservas de sala asignadas"
                    return@launch
                }

                if (_detallesTemp.value.isEmpty()) {
                    _errorMessage.value = "Debe agregar al menos un producto"
                    return@launch
                }

                // ✅ ACTUALIZADO: Crear docente con atributos del backend
                val docente = Usuario(
                    idUsuario = seccion.idDocente ?: 0,
                    rol = Rol.DOCENTE,
                    primerNombre = "Docente",  // ✅ Corregido: primerNombre
                    segundoNombre = null,       // ✅ Nullable
                    apellidoPaterno = "Asignado",
                    apellidoMaterno = null,     // ✅ Nullable
                    email = "docente@temp.cl",
                    username = null,            // ✅ Nullable
                    password = "",
                    activo = true               // ✅ Agregado
                )

                // ✅ ACTUALIZADO: Gestor de pedidos con atributos del backend
                val gestorPedidos = Usuario(
                    idUsuario = 1,
                    rol = Rol.GESTOR_PEDIDOS,
                    primerNombre = "Admin",     // ✅ Corregido: primerNombre
                    segundoNombre = null,       // ✅ Nullable
                    apellidoPaterno = "Sistema",
                    apellidoMaterno = null,     // ✅ Nullable
                    email = "admin@sistema.cl",
                    username = "admin",
                    password = "",
                    activo = true               // ✅ Agregado
                )

                val solicitud = Solicitud(
                    idSolicitud = 0,
                    detalleSolicitud = _detallesTemp.value,
                    gestorPedidos = gestorPedidos,
                    seccion = seccion,
                    docenteSeccion = docente,
                    reservaSala = reservaSala,
                    cantidadPersonas = _cantidadPersonas.value,
                    fechaSolicitud = LocalDateTime.now(),
                    fechaCreacion = LocalDateTime.now()
                )

                solicitudRepository.crearSolicitud(solicitud)

                _successMessage.value = "Solicitud creada exitosamente"
                limpiarSolicitudActual()
                onSuccess()
            } catch (e: Exception) {
                println("❌ Error al guardar solicitud: ${e.message}")
                e.printStackTrace()
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

    fun limpiarSolicitudActual() {
        _detallesTemp.value = emptyList()
        _asignaturaSeleccionada.value = null
        _seccionSeleccionada.value = null
        _nombreDocente.value = ""
        _cantidadPersonas.value = 20
        _reservasSala.value = emptyList()
        _reservaSalaSeleccionada.value = null
    }

    // ===========================================
    // MÉTODOS DE LIMPIEZA DE MENSAJES
    // ===========================================

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}