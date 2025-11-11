package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.remote.InventarioApiService
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateUpdateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductoResponseDTO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/**
 * ✅ REPOSITORIO OPTIMIZADO CON BACKEND
 * - Comunicación directa con Spring Boot
 * - Cache inteligente con Flow
 * - Sin Room DB
 * - Manejo de errores robusto
 */
class InventarioRepository(
    private val apiService: InventarioApiService
) {
    // ========== CACHE EN MEMORIA ==========
    private val cacheMutex = Mutex()
    private val _cachedInventarios = MutableStateFlow<List<InventoryWithProductoResponseDTO>>(emptyList())

    /**
     * Flow público para observar inventarios con cache
     * Se actualiza automáticamente cuando cambia el cache
     */
    val inventarios: StateFlow<List<InventoryWithProductoResponseDTO>> = _cachedInventarios.asStateFlow()

    // ========== ESTADO DE CARGA Y ERRORES ==========
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========== TIMESTAMP DEL CACHE ==========
    private var lastFetchTime: Long = 0
    private val CACHE_VALIDITY_DURATION = 30_000L // 30 segundos

    /**
     * ✅ Obtener todos los inventarios activos
     * Usa cache inteligente para optimizar consultas
     */
    suspend fun fetchAllActiveInventories(forceRefresh: Boolean = false): Result<List<InventoryWithProductoResponseDTO>> {
        return try {
            // Verificar si el cache es válido
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchTime) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _cachedInventarios.value.isNotEmpty()) {
                // Retornar cache válido
                return Result.success(_cachedInventarios.value)
            }

            _isLoading.value = true
            _error.value = null

            // Llamar al backend
            val response = apiService.getAllActiveInventories()

            // Actualizar cache de forma thread-safe
            cacheMutex.withLock {
                _cachedInventarios.value = response
                lastFetchTime = currentTime
            }

            _isLoading.value = false
            Result.success(response)

        } catch (e: HttpException) {
            _isLoading.value = false
            val errorMsg = "Error HTTP ${e.code()}: ${e.message()}"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: IOException) {
            _isLoading.value = false
            val errorMsg = "Error de conexión: ${e.message}"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "Error inesperado: ${e.message}"
            _error.value = errorMsg
            Result.failure(e)
        }
    }

    /**
     * ✅ Crear nuevo producto con inventario
     */
    suspend fun createInventoryWithProduct(
        dto: InventoryWithProductCreateUpdateDTO
    ): Result<InventoryWithProductCreateUpdateDTO> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = apiService.createInventoryWithProduct(dto)

            // Refrescar cache después de crear
            fetchAllActiveInventories(forceRefresh = true)

            _isLoading.value = false
            Result.success(response)

        } catch (e: HttpException) {
            _isLoading.value = false
            val errorMsg = "Error al crear: HTTP ${e.code()}"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: IOException) {
            _isLoading.value = false
            val errorMsg = "Error de conexión al crear"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "Error inesperado al crear: ${e.message}"
            _error.value = errorMsg
            Result.failure(e)
        }
    }

    /**
     * ✅ Actualizar inventario existente
     */
    suspend fun updateInventoryWithProduct(
        dto: InventoryWithProductCreateUpdateDTO
    ): Result<InventoryWithProductCreateUpdateDTO> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = apiService.updateInventoryWithProduct(dto)

            // Refrescar cache después de actualizar
            fetchAllActiveInventories(forceRefresh = true)

            _isLoading.value = false
            Result.success(response)

        } catch (e: HttpException) {
            _isLoading.value = false
            val errorMsg = "Error al actualizar: HTTP ${e.code()}"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: IOException) {
            _isLoading.value = false
            val errorMsg = "Error de conexión al actualizar"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "Error inesperado al actualizar: ${e.message}"
            _error.value = errorMsg
            Result.failure(e)
        }
    }

    /**
     * ✅ Eliminación lógica (activo = false)
     */
    suspend fun logicalDeleteInventory(inventarioId: Int): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            val response = apiService.logicalDeleteInventoryItem(inventarioId)

            if (response.isSuccessful) {
                // Actualizar cache localmente (optimización)
                cacheMutex.withLock {
                    _cachedInventarios.value = _cachedInventarios.value.filter {
                        it.idInventario != inventarioId
                    }
                }

                _isLoading.value = false
                Result.success(Unit)
            } else {
                _isLoading.value = false
                val errorMsg = "Error al eliminar: HTTP ${response.code()}"
                _error.value = errorMsg
                Result.failure(Exception(errorMsg))
            }

        } catch (e: HttpException) {
            _isLoading.value = false
            val errorMsg = "Error al eliminar: HTTP ${e.code()}"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: IOException) {
            _isLoading.value = false
            val errorMsg = "Error de conexión al eliminar"
            _error.value = errorMsg
            Result.failure(e)

        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "Error inesperado al eliminar: ${e.message}"
            _error.value = errorMsg
            Result.failure(e)
        }
    }

    /**
     * ✅ Limpiar mensajes de error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * ✅ Invalidar cache manualmente
     */
    suspend fun invalidateCache() {
        cacheMutex.withLock {
            _cachedInventarios.value = emptyList()
            lastFetchTime = 0
        }
    }

    /**
     * ✅ Obtener un item específico del cache
     */
    fun getInventoryFromCache(idInventario: Int): InventoryWithProductoResponseDTO? {
        return _cachedInventarios.value.find { it.idInventario == idInventario }
    }
}