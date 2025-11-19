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
 * Estado de la UI para la ubicación - Versión 2
 * ✅ Refactorizado para mejor gestión de estado
 */
data class LocationUiState2(
    val isLoading: Boolean = false,
    val location: Location? = null,
    val errorMessage: String? = null,
    val hasPermission: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val permissionRequested: Boolean = false,
    val lastUpdateTimestamp: Long = 0L
)

/**
 * ViewModel para gestionar la obtención de ubicación - Versión 2
 * ✅ Sincronizado con LoginViewModel2
 * ✅ Mejor manejo de permisos y estados
 * ✅ Logging mejorado
 */
class LocationViewModel2(context: Context) : ViewModel() {

    private val locationHelper = LocationHelper(context)

    private val _uiState = MutableStateFlow(LocationUiState2())
    val uiState: StateFlow<LocationUiState2> = _uiState.asStateFlow()

    init {
        println("✅ LocationViewModel2 inicializado")
    }

    override fun onCleared() {
        super.onCleared()
        println("🧹 LocationViewModel2: Limpiando recursos")
    }

    /**
     * Verifica si los permisos están concedidos
     */
    fun checkPermissions() {
        val hasPermission = locationHelper.hasLocationPermission()
        _uiState.update {
            it.copy(
                hasPermission = hasPermission,
                errorMessage = if (!hasPermission) "Permisos de ubicación no concedidos" else null
            )
        }
        println("📍 Permisos de ubicación: ${if (hasPermission) "✅ Concedidos" else "❌ No concedidos"}")
    }

    /**
     * Marca que los permisos fueron solicitados
     */
    fun markPermissionsRequested() {
        _uiState.update { it.copy(permissionRequested = true) }
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
                            hasPermission = false,
                            isLoading = false
                        )
                    }
                    println("❌ No se puede obtener ubicación: permisos no concedidos")
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

                println("🔍 Obteniendo ubicación...")

                // Obtener ubicación
                val location = locationHelper.getCurrentLocation()

                if (location != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            location = location,
                            isLocationEnabled = true,
                            errorMessage = null,
                            lastUpdateTimestamp = System.currentTimeMillis()
                        )
                    }
                    println("✅ Ubicación obtenida: ${locationHelper.formatLocation(location)}")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo obtener la ubicación. Verifique que el GPS esté activado.",
                            isLocationEnabled = false
                        )
                    }
                    println("❌ No se pudo obtener ubicación")
                }

            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Permisos de ubicación insuficientes",
                        isLocationEnabled = false,
                        hasPermission = false
                    )
                }
                println("❌ Error de seguridad al obtener ubicación: ${e.message}")
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al obtener ubicación: ${e.localizedMessage}",
                        isLocationEnabled = false
                    )
                }
                println("❌ Error al obtener ubicación: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Obtiene la ubicación de inmediato sin retraso
     */
    fun getLocationNow() {
        getLocationWithDelay(delayMillis = 0L)
    }

    /**
     * Reintenta obtener la ubicación
     */
    fun retryLocation() {
        println("🔄 Reintentando obtener ubicación...")
        clearError()
        getLocationNow()
    }

    /**
     * Limpia el mensaje de error actual
     */
    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /**
     * Limpia el estado completamente
     */
    fun clearState() {
        _uiState.update {
            LocationUiState2()
        }
        println("🧹 Estado de ubicación limpiado")
    }

    /**
     * Formatea la ubicación actual
     */
    fun getFormattedLocation(): String {
        return locationHelper.formatLocation(_uiState.value.location)
    }

    /**
     * Obtiene coordenadas en formato corto
     */
    fun getShortLocationString(): String? {
        val location = _uiState.value.location ?: return null
        return String.format("%.4f, %.4f", location.latitude, location.longitude)
    }

    /**
     * Verifica si hay ubicación disponible
     */
    fun hasLocation(): Boolean {
        return _uiState.value.location != null
    }

    /**
     * Obtiene la latitud actual
     */
    fun getLatitude(): Double? {
        return _uiState.value.location?.latitude
    }

    /**
     * Obtiene la longitud actual
     */
    fun getLongitude(): Double? {
        return _uiState.value.location?.longitude
    }

    /**
     * Obtiene información detallada de ubicación
     */
    fun getLocationDetails(): LocationDetails? {
        val location = _uiState.value.location ?: return null
        return LocationDetails(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = if (location.hasAltitude()) location.altitude else null,
            speed = if (location.hasSpeed()) location.speed else null,
            bearing = if (location.hasBearing()) location.bearing else null,
            provider = location.provider,
            timestamp = location.time
        )
    }
}

/**
 * Clase de datos para detalles de ubicación
 */
data class LocationDetails(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double?,
    val speed: Float?,
    val bearing: Float?,
    val provider: String?,
    val timestamp: Long
) {
    fun toFormattedString(): String {
        return buildString {
            appendLine("📍 Latitud: ${"%.6f".format(latitude)}")
            appendLine("📍 Longitud: ${"%.6f".format(longitude)}")
            appendLine("🎯 Precisión: ${"%.1f".format(accuracy)}m")
            altitude?.let { appendLine("⛰️ Altitud: ${"%.1f".format(it)}m") }
            speed?.let { appendLine("🚗 Velocidad: ${"%.1f".format(it * 3.6f)} km/h") }
            provider?.let { appendLine("📡 Proveedor: $it") }
        }.trim()
    }
}