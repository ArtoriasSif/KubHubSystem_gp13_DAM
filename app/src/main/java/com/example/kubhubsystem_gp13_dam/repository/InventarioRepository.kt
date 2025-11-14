package com.example.kubhubsystem_gp13_dam.repository

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kubhubsystem_gp13_dam.local.remote.InventarioApiService
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductResponseAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.InventarioViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/**
 * ✅ REPOSITORIO OPTIMIZADO CON BACKEND
 * - Comunicación directa con Spring Boot
 * - Cache inteligente con Flow
 * - Manejo de errores robusto
 * - Compatible con ViewModel actual
 */
class InventarioRepository(
    private val inventoryApiService: InventarioApiService
) {
    // ========= CACHE =========
    private val cacheMutex = Mutex()
    private val _cachedInventarios =
        MutableStateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>>(emptyList())
    val inventarios: StateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>> =
        _cachedInventarios.asStateFlow()

    // ========= ESTADOS =========
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========= CONFIGURACIÓN DE CACHE =========
    private var lastFetchTime: Long = 0
    private val CACHE_VALIDITY_DURATION = 30_000L // 30 segundos

    // ========= MÉTODOS PRINCIPALES =========
    /**
     * ✅ Obtener todos los inventarios activos
     */
    suspend fun fetchAllActiveInventories(forceRefresh: Boolean = false): Result<List<InventoryWithProductResponseAnswerUpdateDTO>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchTime) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _cachedInventarios.value.isNotEmpty()) {
                return Result.success(_cachedInventarios.value)
            }

            _isLoading.value = true
            _error.value = null

            val response = inventoryApiService.getAllActiveInventories()

            cacheMutex.withLock {
                _cachedInventarios.value = response
                lastFetchTime = currentTime
            }

            _isLoading.value = false
            Result.success(response)

        } catch (e: HttpException) {
            handleError("Error HTTP ${e.code()}: ${e.message()}", e)
        } catch (e: IOException) {
            handleError("Error de conexión: ${e.message}", e)
        } catch (e: Exception) {
            handleError("Error inesperado: ${e.message}", e)
        }
    }

    /**
     * ✅ Crear nuevo producto con inventario
     */
    suspend fun createInventoryWithProduct(dtoCreate: InventoryWithProductCreateDTO) {
        try {
            _isLoading.value = true
            _error.value = null

            inventoryApiService.createInventoryWithProduct(dtoCreate)
            fetchAllActiveInventories(forceRefresh = true)

        } catch (e: HttpException) {
            _error.value = "Error HTTP ${e.code()} al crear"
        } catch (e: IOException) {
            _error.value = "Error de conexión al crear"
        } catch (e: Exception) {
            _error.value = "Error inesperado al crear: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * ✅ Actualizar inventario existente
     */
    suspend fun updateInventoryWithProduct(dtoAnswerUpdateDTO: InventoryWithProductResponseAnswerUpdateDTO) {
        try {
            Log.d("UPDATE_INV", "---- INICIO ACTUALIZACIÓN ----")
            Log.d("UPDATE_INV", "DTO ENVIADO: $dtoAnswerUpdateDTO")

            _isLoading.value = true
            _error.value = null

            // Llamada API
            Log.d("UPDATE_INV", "Llamando API: updateInventoryWithProduct()...")
            val response = inventoryApiService.updateInventoryWithProduct(dtoAnswerUpdateDTO)
            Log.d("UPDATE_INV", "Respuesta API: $response")

            // Refrescar
            Log.d("UPDATE_INV", "Refrescando inventarios...")
            fetchAllActiveInventories(forceRefresh = true)
            Log.d("UPDATE_INV", "Inventarios refrescados correctamente")

            Log.d("UPDATE_INV", "---- FIN ACTUALIZACIÓN EXITOSA ----")

        } catch (e: HttpException) {
            val msg = "Error HTTP ${e.code()} al actualizar"
            Log.e("UPDATE_INV", msg, e)
            _error.value = msg

        } catch (e: IOException) {
            val msg = "Error de conexión al actualizar"
            Log.e("UPDATE_INV", msg, e)
            _error.value = msg

        } catch (e: Exception) {
            val msg = "Error inesperado al actualizar: ${e.message}"
            Log.e("UPDATE_INV", msg, e)
            _error.value = msg

        } finally {
            _isLoading.value = false
            Log.d("UPDATE_INV", "isLoading = false")
        }
    }

    /**
     * ✅ Eliminación lógica (activo = false)
     */
    suspend fun logicalDeleteInventory(inventarioId: Int): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = inventoryApiService.logicalDeleteInventoryItem(inventarioId)

            if (response.isSuccessful) {
                cacheMutex.withLock {
                    _cachedInventarios.value = _cachedInventarios.value.filterNot {
                        it.idInventario == inventarioId
                    }
                }
                _isLoading.value = false
                Result.success(Unit)
            } else {
                val msg = "Error HTTP ${response.code()} al eliminar"
                _error.value = msg
                _isLoading.value = false
                Result.failure(Exception(msg))
            }

        } catch (e: HttpException) {
            handleError("Error HTTP ${e.code()} al eliminar", e)
        } catch (e: IOException) {
            handleError("Error de conexión al eliminar", e)
        } catch (e: Exception) {
            handleError("Error inesperado al eliminar: ${e.message}", e)
        }
    }

    // ========= 🔹 IMPLEMENTACIÓN FALTANTE =========
    /**
     * ✅ Actualizar manualmente la caché de inventarios
     * (se usa cuando el ViewModel obtiene nuevos datos y quiere sincronizar)
     */
    suspend fun updateCache(nuevaLista: List<InventoryWithProductResponseAnswerUpdateDTO>) {
        cacheMutex.withLock {
            _cachedInventarios.value = nuevaLista
            lastFetchTime = System.currentTimeMillis()
        }
    }

    // ========= UTILIDADES =========
    fun clearError() {
        _error.value = null
    }

    suspend fun invalidateCache() {
        cacheMutex.withLock {
            _cachedInventarios.value = emptyList()
            lastFetchTime = 0
        }
    }

    fun getInventoryFromCache(idInventario: Int): InventoryWithProductResponseAnswerUpdateDTO? {
        return _cachedInventarios.value.find { it.idInventario == idInventario }
    }





    private fun <T> handleError(message: String, e: Exception): Result<T> {
        _isLoading.value = false
        _error.value = message
        return Result.failure(e)
    }

    @Suppress("UNCHECKED_CAST")
    class InventarioViewModelFactory(
        private val inventarioRepository: InventarioRepository,
        private val productoRepository: ProductoRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
                return InventarioViewModel(inventarioRepository, productoRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    // dentro de la clase InventarioRepository
    fun createViewModelFactory(productoRepository: ProductoRepository): ViewModelProvider.Factory {
        return InventarioViewModelFactory(this, productoRepository)
    }

}
