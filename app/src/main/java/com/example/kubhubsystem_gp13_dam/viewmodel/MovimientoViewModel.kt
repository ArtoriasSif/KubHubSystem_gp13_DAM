package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.local.entities.MovimientoEntity
import com.example.kubhubsystem_gp13_dam.repository.MovimientoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MovimientoViewModel(
    private val movimientoRepository: MovimientoRepository
) : ViewModel() {

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mensaje de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Observar todos los movimientos
    val movimientos: StateFlow<List<MovimientoEntity>> =
        movimientoRepository.observarMovimientos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Registra una entrada de inventario
     * La fecha se captura automáticamente
     */
    fun registrarEntrada(
        idInventario: Int,
        cantidad: Double
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                movimientoRepository.registrarMovimiento(
                    idInventario = idInventario,
                    cantidadMovimiento = cantidad,
                    tipoMovimiento = "ENTRADA"
                )

                _successMessage.value = "Entrada registrada: +$cantidad unidades"
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar entrada: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Registra una salida de inventario
     * La fecha se captura automáticamente
     * Valida que haya stock suficiente
     */
    fun registrarSalida(
        idInventario: Int,
        cantidad: Double
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                movimientoRepository.registrarMovimiento(
                    idInventario = idInventario,
                    cantidadMovimiento = cantidad,
                    tipoMovimiento = "SALIDA"
                )

                _successMessage.value = "Salida registrada: -$cantidad unidades"
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar salida: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtener movimientos de un inventario específico
     */
    fun obtenerMovimientosPorInventario(idInventario: Int): Flow<List<MovimientoEntity>> {
        return movimientos.map { lista ->
            lista.filter { it.idInventario == idInventario }
                .sortedByDescending { it.fechaMovimiento }
        }
    }

    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}