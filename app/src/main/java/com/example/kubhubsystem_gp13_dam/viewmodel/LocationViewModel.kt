package com.example.kubhubsystem_gp13_dam.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.utils.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la ubicación
 */
data class LocationUiState(
    val isLoading: Boolean = false,
    val location: Location? = null,
    val errorMessage: String? = null,
    val hasPermission: Boolean = false,
    val isLocationEnabled: Boolean = false
)

/**
 * ViewModel para gestionar la obtención de ubicación
 */
class LocationViewModel(context: Context) : ViewModel() {

    private val locationHelper = LocationHelper(context)

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    /**
     * Verifica si los permisos están concedidos
     */
    fun checkPermissions() {
        val hasPermission = locationHelper.hasLocationPermission()
        _uiState.update { it.copy(hasPermission = hasPermission) }
    }

    /**
     * Obtiene la ubicación actual con retraso opcional
     *
     * @param delayMillis Tiempo de espera antes de obtener la ubicación (en milisegundos)
     */
    fun getLocationWithDelay(delayMillis: Long = 3000L) {
        viewModelScope.launch {
            try {
                println("⏳ Esperando ${delayMillis}ms antes de obtener ubicación...")

                // Retraso antes de iniciar
                delay(delayMillis)

                // Verificar permisos
                if (!locationHelper.hasLocationPermission()) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Permisos de ubicación no concedidos",
                            hasPermission = false
                        )
                    }
                    return@launch
                }

                // Mostrar carga
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        hasPermission = true
                    )
                }

                println("📍 Obteniendo ubicación...")

                // Obtener ubicación
                val location = locationHelper.getCurrentLocation()

                if (location != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            location = location,
                            isLocationEnabled = true,
                            errorMessage = null
                        )
                    }
                    println("✅ Ubicación obtenida: ${locationHelper.formatLocation(location)}")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener la ubicación",
                            isLocationEnabled = false
                        )
                    }
                    println("❌ No se pudo obtener ubicación")
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message}",
                        isLocationEnabled = false
                    )
                }
                println("❌ Error al obtener ubicación: ${e.message}")
            }
        }
    }

    /**
     * Limpia el estado
     */
    fun clearState() {
        _uiState.update {
            LocationUiState()
        }
    }

    /**
     * Formatea la ubicación actual
     */
    fun getFormattedLocation(): String {
        return locationHelper.formatLocation(_uiState.value.location)
    }
}