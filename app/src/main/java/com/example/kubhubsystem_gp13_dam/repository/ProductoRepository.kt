package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.local.remote.ProductoApiService
import com.example.kubhubsystem_gp13_dam.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ProductoRepository(
    private val productoApiService: ProductoApiService
) {
    private val cacheMutex = Mutex()

    // ✅ Flujos observables
    private val _categorias = MutableStateFlow<List<String>>(emptyList())
    val categorias: StateFlow<List<String>> = _categorias.asStateFlow()

    private val _unidadesMedida = MutableStateFlow<List<String>>(emptyList())
    val unidadesMedida: StateFlow<List<String>> = _unidadesMedida.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastFetchCategorias = 0L
    private var lastFetchUnidades = 0L
    private val CACHE_VALIDITY_DURATION = 30_000L // 30 segundos

    /**
     * ✅ Obtener categorías activas
     */
    suspend fun fetchCategoriasActivas(forceRefresh: Boolean = false): Result<List<String>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchCategorias) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _categorias.value.isNotEmpty()) {
                return Result.success(_categorias.value)
            }

            val response = productoApiService.getCategoriasActivas()

            cacheMutex.withLock {
                _categorias.value = response.distinct().sorted()
                lastFetchCategorias = currentTime
            }

            Result.success(_categorias.value)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * ✅ Obtener unidades de medida activas
     */
    suspend fun fetchUnidadesMedidaActivas(forceRefresh: Boolean = false): Result<List<String>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchUnidades) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _unidadesMedida.value.isNotEmpty()) {
                return Result.success(_unidadesMedida.value)
            }

            val response = productoApiService.getUnidadesMedidaActivas()

            cacheMutex.withLock {
                _unidadesMedida.value = response.distinct().sorted()
                lastFetchUnidades = currentTime
            }

            Result.success(_unidadesMedida.value)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * ✅ Manejo centralizado de errores
     */
    private fun handleError(e: Exception): Result<List<String>> {
        val msg = when (e) {
            is HttpException -> "Error HTTP ${e.code()}: ${e.message()}"
            is IOException -> "Error de conexión: ${e.message}"
            else -> "Error inesperado: ${e.message}"
        }
        _error.value = msg
        return Result.failure(e)
    }

    fun clearError() {
        _error.value = null
    }

    suspend fun invalidateCache() {
        cacheMutex.withLock {
            _categorias.value = emptyList()
            _unidadesMedida.value = emptyList()
            lastFetchCategorias = 0
            lastFetchUnidades = 0
        }
    }
}
