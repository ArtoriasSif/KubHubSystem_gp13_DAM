package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.repository.PedidoRepository
import com.example.kubhubsystem_gp13_dam.repository.SolicitudRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PedidoViewModel(
    private val pedidoRepository: PedidoRepository,
    private val solicitudRepository: SolicitudRepository
) : ViewModel() {

    // Estados
    private val _pedidoActivo = MutableStateFlow<Pedido?>(null)
    val pedidoActivo: StateFlow<Pedido?> = _pedidoActivo.asStateFlow()

    private val _pedidosAnteriores = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosAnteriores: StateFlow<List<Pedido>> = _pedidosAnteriores.asStateFlow()

    private val _aglomerado = MutableStateFlow<List<AglomeradoPedido>>(emptyList())
    val aglomerado: StateFlow<List<AglomeradoPedido>> = _aglomerado.asStateFlow()

    private val _solicitudesPedido = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudesPedido: StateFlow<List<Solicitud>> = _solicitudesPedido.asStateFlow()

    private val _progresoPedido = MutableStateFlow(0f)
    val progresoPedido: StateFlow<Float> = _progresoPedido.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _filtroAsignatura = MutableStateFlow<Int?>(null)
    val filtroAsignatura: StateFlow<Int?> = _filtroAsignatura.asStateFlow()

    private val _mostrarPedidoAnterior = MutableStateFlow(false)
    val mostrarPedidoAnterior: StateFlow<Boolean> = _mostrarPedidoAnterior.asStateFlow()

    init {
        inicializarEstados()
        observarPedidoActivo()
        observarPedidosAnteriores()
        observarSolicitudes()
    }

    private fun inicializarEstados() {
        viewModelScope.launch {
            try {
                pedidoRepository.inicializarEstadosPedido()
            } catch (e: Exception) {
                _errorMessage.value = "Error al inicializar: ${e.message}"
            }
        }
    }

    private fun observarPedidoActivo() {
        viewModelScope.launch {
            pedidoRepository.observarPedidoActivo().collect { pedido ->
                _pedidoActivo.value = pedido
                pedido?.let {
                    observarAglomerado(it.idPedido)
                    calcularProgreso(it.idPedido)
                }
            }
        }
    }

    private fun observarPedidosAnteriores() {
        viewModelScope.launch {
            pedidoRepository.observarPedidosAnteriores().collect { pedidos ->
                _pedidosAnteriores.value = pedidos
            }
        }
    }

    private fun observarSolicitudes() {
        viewModelScope.launch {
            solicitudRepository.observarTodasSolicitudes().collect { solicitudes ->
                _solicitudesPedido.value = solicitudes
            }
        }
    }

    private fun observarAglomerado(idPedido: Int) {
        viewModelScope.launch {
            combine(
                pedidoRepository.observarAglomeradoPorPedido(idPedido),
                _filtroAsignatura
            ) { aglomerado, filtroAsig ->
                if (filtroAsig != null) {
                    aglomerado.filter { it.asignatura?.idAsignatura == filtroAsig }
                } else {
                    aglomerado
                }
            }.collect { aglomeradoFiltrado ->
                _aglomerado.value = aglomeradoFiltrado
            }
        }
    }

    private fun calcularProgreso(idPedido: Int) {
        viewModelScope.launch {
            try {
                val progreso = pedidoRepository.calcularProgresoPedido(idPedido)
                _progresoPedido.value = progreso
            } catch (e: Exception) {
                _errorMessage.value = "Error al calcular progreso: ${e.message}"
            }
        }
    }

    fun iniciarNuevoPeriodo(fechaInicio: LocalDateTime, fechaFin: LocalDateTime) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                pedidoRepository.crearPedido(fechaInicio, fechaFin)
                _successMessage.value = "Nuevo período iniciado"
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar período: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarEstadoPedido(nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val pedido = _pedidoActivo.value ?: return@launch
                pedidoRepository.actualizarEstadoPedido(pedido.idPedido, nuevoEstado)
                _successMessage.value = "Estado actualizado a ${nuevoEstado.displayName}"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar estado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun aprobarSolicitud(idSolicitud: Int) {
        viewModelScope.launch {
            try {
                solicitudRepository.actualizarEstadoSolicitud(idSolicitud, "Aprobado")
                recalcularAglomerado()
                verificarEstadoPedido()
            } catch (e: Exception) {
                _errorMessage.value = "Error al aprobar: ${e.message}"
            }
        }
    }

    fun rechazarSolicitud(idSolicitud: Int) {
        viewModelScope.launch {
            try {
                solicitudRepository.actualizarEstadoSolicitud(idSolicitud, "Rechazado")
                verificarEstadoPedido()
            } catch (e: Exception) {
                _errorMessage.value = "Error al rechazar: ${e.message}"
            }
        }
    }

    private fun recalcularAglomerado() {
        viewModelScope.launch {
            try {
                val pedido = _pedidoActivo.value ?: return@launch
                pedidoRepository.recalcularAglomerado(pedido.idPedido)
            } catch (e: Exception) {
                _errorMessage.value = "Error al recalcular aglomerado: ${e.message}"
            }
        }
    }

    private fun verificarEstadoPedido() {
        viewModelScope.launch {
            try {
                val pedido = _pedidoActivo.value ?: return@launch
                val progreso = pedidoRepository.calcularProgresoPedido(pedido.idPedido)
                _progresoPedido.value = progreso

                // Si todas las solicitudes están procesadas, avanzar al siguiente estado
                if (progreso >= 1.0f && pedido.estadoPedido == EstadoPedido.PENDIENTE_REVISION) {
                    actualizarEstadoPedido(EstadoPedido.CHECK_INVENTARIO)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al verificar estado: ${e.message}"
            }
        }
    }

    fun actualizarCantidadAglomerado(idAglomerado: Int, nuevaCantidad: Double) {
        viewModelScope.launch {
            try {
                pedidoRepository.actualizarCantidadAglomerado(idAglomerado, nuevaCantidad)
                _successMessage.value = "Cantidad actualizada"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar cantidad: ${e.message}"
            }
        }
    }

    fun agregarProductoAglomerado(producto: Producto, cantidad: Double, idAsignatura: Int? = null) {
        viewModelScope.launch {
            try {
                val pedido = _pedidoActivo.value ?: return@launch
                pedidoRepository.agregarProductoAglomerado(pedido.idPedido, producto, cantidad, idAsignatura)
                _successMessage.value = "Producto agregado al aglomerado"
            } catch (e: Exception) {
                _errorMessage.value = "Error al agregar producto: ${e.message}"
            }
        }
    }

    fun aplicarFiltroAsignatura(idAsignatura: Int?) {
        _filtroAsignatura.value = idAsignatura
    }

    fun toggleMostrarPedidoAnterior() {
        _mostrarPedidoAnterior.value = !_mostrarPedidoAnterior.value
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}