package com.example.kubhubsystem_gp13_dam.repository

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kubhubsystem_gp13_dam.local.remote.ProductoApiService
import com.example.kubhubsystem_gp13_dam.local.remote.RecetaApiService
import com.example.kubhubsystem_gp13_dam.model.ProductoEntityDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsCreateDTO
import com.example.kubhubsystem_gp13_dam.viewmodel.RecetaViewModel
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
class RecetaRepository(
    private val recetaApiService: RecetaApiService,
    private val productoApiService: ProductoApiService
) {
    // ========= CACHE DE RECETAS =========
    private val cacheMutex = Mutex()
    private val _cachedRecetas =
        MutableStateFlow<List<RecipeWithDetailsAnswerUpdateDTO>>(emptyList())
    val recetas: StateFlow<List<RecipeWithDetailsAnswerUpdateDTO>> =
        _cachedRecetas.asStateFlow()

    // ========= CACHE DE PRODUCTOS =========
    private val productoCacheMutex = Mutex()
    private val _cachedProductos = MutableStateFlow<List<ProductoEntityDTO>>(emptyList())
    val productosActivos: StateFlow<List<ProductoEntityDTO>> = _cachedProductos.asStateFlow()

    private val _cachedUnidadesMedida = MutableStateFlow<List<String>>(emptyList())
    val unidadesMedida: StateFlow<List<String>> = _cachedUnidadesMedida.asStateFlow()


    // ========= ESTADOS =========
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========= CONFIGURACIÓN DE CACHE =========
    private var lastFetchTimeRecetas: Long = 0
    private var lastFetchTimeProductos: Long = 0
    private var lastFetchTimeUnidades: Long = 0
    private val CACHE_VALIDITY_DURATION = 30_000L // 30 segundos

    // ========= MÉTODOS PRINCIPALES - RECETAS =========

    /**
     * ✅ Obtener todas las recetas activas con detalles
     */
    suspend fun fetchAllActiveRecipes(forceRefresh: Boolean = false): Result<List<RecipeWithDetailsAnswerUpdateDTO>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchTimeRecetas) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _cachedRecetas.value.isNotEmpty()) {
                Log.d("RecetaRepository", "📦 Usando cache válido (${_cachedRecetas.value.size} recetas)")
                return Result.success(_cachedRecetas.value)
            }

            _isLoading.value = true
            _error.value = null

            Log.d("RecetaRepository", "🌐 Llamando API: findAllRecipeWithDetailsActive()")
            val response = recetaApiService.findAllRecipeWithDetailsActive()
            Log.d("RecetaRepository", "✅ Respuesta API: ${response.size} recetas obtenidas")

            cacheMutex.withLock {
                _cachedRecetas.value = response
                lastFetchTimeRecetas = currentTime
                Log.d("RecetaRepository", "💾 Cache actualizado con ${response.size} recetas")
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
     * ✅ Crear nueva receta con detalles
     */
    suspend fun createRecipeWithDetails(dtoCreate: RecipeWithDetailsCreateDTO): Result<RecipeWithDetailsCreateDTO> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("RecetaRepository", "🆕 Creando receta: ${dtoCreate.nombreReceta}")
            Log.d("RecetaRepository", "📋 DTO Create: $dtoCreate")

            val response = recetaApiService.createRecipeWithDetails(dtoCreate)
            Log.d("RecetaRepository", "✅ Receta creada exitosamente")

            // Refrescar cache
            fetchAllActiveRecipes(forceRefresh = true)

            _isLoading.value = false
            Result.success(response)

        } catch (e: HttpException) {
            handleError("Error HTTP ${e.code()} al crear receta", e)
        } catch (e: IOException) {
            handleError("Error de conexión al crear receta", e)
        } catch (e: Exception) {
            handleError("Error inesperado al crear receta: ${e.message}", e)
        }
    }


    /**
     * ✅ Actualizar receta existente con detalles
     */
    suspend fun updateRecipeWithDetails(
        dtoUpdate: RecipeWithDetailsAnswerUpdateDTO  // 🔥 Cambiar tipo
    ): Result<RecipeWithDetailsAnswerUpdateDTO> {

        return try {
            Log.d("RecetaRepository", "---- INICIO ACTUALIZACIÓN RECETA ----")
            Log.d("RecetaRepository", "DTO ENVIADO: $dtoUpdate")

            _isLoading.value = true
            _error.value = null

            Log.d("RecetaRepository", "🌐 Llamando API: updateRecipeWithDetails()...")
            val recetaActualizada = recetaApiService.updateRecipeWithDetails(dtoUpdate)
            Log.d("RecetaRepository", "✅ Respuesta API: $recetaActualizada")

            // refrescar listas si corresponde
            fetchAllActiveRecipes(forceRefresh = true)

            _isLoading.value = false
            Log.d("RecetaRepository", "---- FIN ACTUALIZACIÓN EXITOSA ----")

            Result.success(recetaActualizada)

        } catch (e: HttpException) {
            val msg = "Error HTTP ${e.code()} al actualizar receta"
            Log.e("RecetaRepository", msg, e)
            handleError(msg, e)

        } catch (e: IOException) {
            val msg = "Error de conexión al actualizar receta"
            Log.e("RecetaRepository", msg, e)
            handleError(msg, e)

        } catch (e: Exception) {
            val msg = "Error inesperado al actualizar receta: ${e.message}"
            Log.e("RecetaRepository", msg, e)
            handleError(msg, e)
        }
    }

    suspend fun updateChangingStatusRecipeWith(idReceta: Int): Boolean {
        return try {
            Log.d("RecetaRepository", "🌐 Llamando API para cambiar estado ID: $idReceta")
            val response = recetaApiService.updateChangingStatusRecipeWith(idReceta)
            val success = response.isSuccessful
            Log.d("RecetaRepository", "✅ Respuesta: $success (code: ${response.code()})")
            success
        } catch (e: Exception) {
            Log.e("RecetaRepository", "❌ Error al cambiar estado", e)
            false
        }
    }


    /**
     * ✅ Desactivar receta (eliminación lógica)
     */
    suspend fun deactivateRecipe(idReceta: Int): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            Log.d("RecetaRepository", "🗑️ Desactivando receta ID: $idReceta")
            val response = recetaApiService.updateStatusActiveFalseRecipe(idReceta)

            if (response.isSuccessful) {
                Log.d("RecetaRepository", "✅ Receta desactivada exitosamente")

                // Actualizar cache eliminando la receta desactivada
                cacheMutex.withLock {
                    _cachedRecetas.value = _cachedRecetas.value.filterNot {
                        it.idReceta == idReceta
                    }
                    Log.d("RecetaRepository", "💾 Cache actualizado (receta removida)")
                }

                _isLoading.value = false
                Result.success(Unit)
            } else {
                val msg = "Error HTTP ${response.code()} al desactivar receta"
                Log.e("RecetaRepository", msg)
                _error.value = msg
                _isLoading.value = false
                Result.failure(Exception(msg))
            }

        } catch (e: HttpException) {
            handleError("Error HTTP ${e.code()} al desactivar receta", e)
        } catch (e: IOException) {
            handleError("Error de conexión al desactivar receta", e)
        } catch (e: Exception) {
            handleError("Error inesperado al desactivar receta: ${e.message}", e)
        }
    }





    // ========= MÉTODOS PRINCIPALES - PRODUCTOS =========

    /**
     * ✅ Obtener productos activos desde el backend
     */
    suspend fun fetchProductosActivos(forceRefresh: Boolean = false): Result<List<ProductoEntityDTO>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchTimeProductos) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _cachedProductos.value.isNotEmpty()) {
                Log.d("RecetaRepository", "📦 Usando cache de productos (${_cachedProductos.value.size} productos)")
                return Result.success(_cachedProductos.value)
            }

            Log.d("RecetaRepository", "🌐 Llamando API: getProductsByActivo(true)")
            val response = productoApiService.getProductsByActivo(true)
            Log.d("RecetaRepository", "✅ Respuesta API: ${response.size} productos activos obtenidos")

            productoCacheMutex.withLock {
                _cachedProductos.value = response.sortedBy { it.nombreProducto }
                lastFetchTimeProductos = currentTime
                Log.d("RecetaRepository", "💾 Cache de productos actualizado")
            }

            Result.success(_cachedProductos.value)

        } catch (e: HttpException) {
            val msg = "Error HTTP ${e.code()} al obtener productos"
            Log.e("RecetaRepository", msg, e)
            _error.value = msg
            Result.failure(e)
        } catch (e: IOException) {
            val msg = "Error de conexión al obtener productos"
            Log.e("RecetaRepository", msg, e)
            _error.value = msg
            Result.failure(e)
        } catch (e: Exception) {
            val msg = "Error inesperado al obtener productos: ${e.message}"
            Log.e("RecetaRepository", msg, e)
            _error.value = msg
            Result.failure(e)
        }
    }

    /**
     * ✅ Obtener unidades de medida disponibles
     */
    suspend fun fetchUnidadesMedida(forceRefresh: Boolean = false): Result<List<String>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchTimeUnidades) < CACHE_VALIDITY_DURATION

            if (!forceRefresh && isCacheValid && _cachedUnidadesMedida.value.isNotEmpty()) {
                Log.d("RecetaRepository", "📦 Usando cache de unidades (${_cachedUnidadesMedida.value.size} unidades)")
                return Result.success(_cachedUnidadesMedida.value)
            }

            Log.d("RecetaRepository", "🌐 Llamando API: getUnidadesMedidaActivas()")
            val response = productoApiService.getUnidadesMedidaActivas()
            Log.d("RecetaRepository", "✅ Respuesta API: ${response.size} unidades obtenidas")

            productoCacheMutex.withLock {
                _cachedUnidadesMedida.value = response.distinct().sorted()
                lastFetchTimeUnidades = currentTime
                Log.d("RecetaRepository", "💾 Cache de unidades actualizado")
            }

            Result.success(_cachedUnidadesMedida.value)

        } catch (e: HttpException) {
            val msg = "Error HTTP ${e.code()} al obtener unidades de medida"
            Log.e("RecetaRepository", msg, e)
            _error.value = msg
            Result.failure(e)
        } catch (e: IOException) {
            val msg = "Error de conexión al obtener unidades de medida"
            Log.e("RecetaRepository", msg, e)
            _error.value = msg
            Result.failure(e)
        } catch (e: Exception) {
            val msg = "Error inesperado al obtener unidades: ${e.message}"
            Log.e("RecetaRepository", msg, e)
            _error.value = msg
            Result.failure(e)
        }
    }



    // ========= MÉTODOS AUXILIARES =========

    /**
     * ✅ Actualizar manualmente la caché de recetas
     */
    suspend fun updateCache(nuevaLista: List<RecipeWithDetailsAnswerUpdateDTO>) {
        cacheMutex.withLock {
            _cachedRecetas.value = nuevaLista
            lastFetchTimeRecetas = System.currentTimeMillis()
            Log.d("RecetaRepository", "💾 Cache actualizado manualmente con ${nuevaLista.size} recetas")
        }
    }

    /**
     * ✅ Obtener receta del cache por ID
     */
    fun getRecipeFromCache(idReceta: Int): RecipeWithDetailsAnswerUpdateDTO? {
        return _cachedRecetas.value.find { it.idReceta == idReceta }
    }

    /**
     * ✅ Obtener producto del cache por ID
     */
    fun getProductoFromCache(idProducto: Int): ProductoEntityDTO? {
        return _cachedProductos.value.find { it.idProducto == idProducto }
    }

    /**
     * ✅ Limpiar errores
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * ✅ Invalidar cache completamente
     */
    suspend fun invalidateCache() {
        cacheMutex.withLock {
            _cachedRecetas.value = emptyList()
            lastFetchTimeRecetas = 0
            Log.d("RecetaRepository", "🗑️ Cache de recetas invalidado")
        }

        productoCacheMutex.withLock {
            _cachedProductos.value = emptyList()
            _cachedUnidadesMedida.value = emptyList()
            lastFetchTimeProductos = 0
            lastFetchTimeUnidades = 0
            Log.d("RecetaRepository", "🗑️ Cache de productos invalidado")
        }
    }

    /**
     * ✅ Obtener categorías únicas de las recetas cacheadas
     */
    fun getCategoriesFromCache(): List<String> {
        return _cachedRecetas.value
            .mapNotNull { it.descripcionReceta }
            .distinct()
            .sorted()
    }

// ========= UTILIDADES PRIVADAS =========

    private fun <T> handleError(message: String, e: Exception): Result<T> {
        _isLoading.value = false
        _error.value = message
        Log.e("RecetaRepository", message, e)
        return Result.failure(e)
    }

    // ========= FACTORY PARA VIEWMODEL =========

    @Suppress("UNCHECKED_CAST")
    class RecetasViewModelFactory2(
        private val recetaRepository: RecetaRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecetaViewModel::class.java)) {
                return RecetaViewModel(this@RecetasViewModelFactory2.recetaRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    fun createViewModelFactory(): ViewModelProvider.Factory {
        return RecetasViewModelFactory2(this)
    }
}