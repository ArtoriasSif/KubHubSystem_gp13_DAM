package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.repository.SolicitudRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class SolicitudViewModel(
    private val solicitudRepository: SolicitudRepository,
    private val recetaRepository: RecetaRepository,
    private val productoRepository: ProductoRepository,
    private val asignaturaRepository: AsignaturaRepository,  // ✅ AGREGAR
    private val seccionRepository: SeccionRepository,        // ✅ AGREGAR
    private val usuarioRepository: UsuarioRepository,
    private val reservaSalaRepository: ReservaSalaRepository
) : ViewModel() {

    // Estados

    private val _reservasSala = MutableStateFlow<List<ReservaSala>>(emptyList())
    val reservasSala: StateFlow<List<ReservaSala>> = _reservasSala.asStateFlow()

    private val _reservaSalaSeleccionada = MutableStateFlow<ReservaSala?>(null)
    val reservaSalaSeleccionada: StateFlow<ReservaSala?> = _reservaSalaSeleccionada.asStateFlow()
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
        cargarAsignaturas()
    }
    // ✅ NUEVOS ESTADOS
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



    private fun observarSolicitudes() {
        viewModelScope.launch {
            solicitudRepository.observarTodasSolicitudes().collect { lista ->
                _solicitudes.value = lista
            }
        }
    }

    /**private fun cargarProductos() {
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
    }*/

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

    fun guardarSolicitud(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val asignatura = _asignaturaSeleccionada.value
                val seccion = _seccionSeleccionada.value
                val reservaSala = _reservaSalaSeleccionada.value  // ✅ Usar la real

                if (asignatura == null || seccion == null) {
                    _errorMessage.value = "Debe seleccionar asignatura y sección"
                    return@launch
                }

                // ✅ Validar que haya reserva de sala
                if (reservaSala == null) {
                    _errorMessage.value = "Esta sección no tiene reservas de sala asignadas"
                    return@launch
                }

                if (_detallesTemp.value.isEmpty()) {
                    _errorMessage.value = "Debe agregar al menos un producto"
                    return@launch
                }

                // Obtener docente
                val docenteEntity = seccion.idDocente?.let { id ->
                    usuarioRepository.obtenerPorId(id)
                }

                val docente = docenteEntity?.let { entity ->
                    Usuario(
                        idUsuario = entity.idUsuario,
                        rol = Rol.desdeId(entity.idRol) ?: Rol.DOCENTE,
                        primeroNombre = entity.primeroNombre,
                        segundoNombre = entity.segundoNombre ?: "",
                        apellidoMaterno = entity.apellidoMaterno ?: "",
                        apellidoPaterno = entity.apellidoPaterno ?: "",
                        email = entity.email ?: "",
                        username = entity.username ?: "",
                        password = entity.password ?: ""
                    )
                } ?: Usuario(
                    idUsuario = 0,
                    rol = Rol.DOCENTE,
                    primeroNombre = "Sin",
                    segundoNombre = "",
                    apellidoMaterno = "",
                    apellidoPaterno = "Docente",
                    email = "",
                    username = "",
                    password = ""
                )

                val solicitud = Solicitud(
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
                    seccion = seccion,
                    docenteSeccion = docente,
                    reservaSala = reservaSala,  // ✅ Ahora es la reserva real
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


    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
    // ✅ NUEVOS MÉTODOS
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

        // ✅ NUEVO: Cargar reservas de sala de la sección
        cargarReservasDeSala(seccion.idSeccion)
    }
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

    // ✅ NUEVO: Método para seleccionar una reserva específica
    fun seleccionarReservaSala(reserva: ReservaSala) {
        _reservaSalaSeleccionada.value = reserva
    }

    private fun cargarDocenteDeSeccion(idDocente: Int) {
        viewModelScope.launch {
            try {
                val docenteEntity = usuarioRepository.obtenerPorId(idDocente)
                _nombreDocente.value = docenteEntity?.let { entity ->
                    "${entity.primeroNombre} ${entity.apellidoPaterno}"
                } ?: "Sin docente asignado"
            } catch (e: Exception) {
                println("❌ Error al cargar docente: ${e.message}")
                _nombreDocente.value = "Sin docente asignado"
            }
        }
    }

    fun actualizarNombreDocente(nuevoNombre: String) {
        _nombreDocente.value = nuevoNombre
    }

    fun actualizarCantidadPersonas(cantidad: Int) {
        _cantidadPersonas.value = cantidad
    }

    // Métodos existentes
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


    fun limpiarSolicitudActual() {
        _detallesTemp.value = emptyList()
        _asignaturaSeleccionada.value = null
        _seccionSeleccionada.value = null
        _nombreDocente.value = ""
        _cantidadPersonas.value = 20
        _reservasSala.value = emptyList()  // ✅ AGREGAR
        _reservaSalaSeleccionada.value = null  // ✅ AGREGAR
    }

}