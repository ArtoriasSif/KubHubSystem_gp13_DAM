package com.example.kubhubsystem_gp13_dam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.ui.model.EstadoRecetaType
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeItemDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsCreateDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * ✅ VIEWMODEL OPTIMIZADO CON BACKEND
 * - Manejo de cache en memoria expuesto por flows
 * - Operaciones de CRUD con manejo de estados
 * - Filtros y búsqueda en tiempo real
 */
class RecetaViewModel(
    private val recetaRepository: RecetaRepository
) : ViewModel() {

    // ============================================================== //
    // Filtros / búsqueda                                              //
    // ============================================================== //
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow<String?>(null)
    val selectedCategoria: StateFlow<String?> = _selectedCategoria.asStateFlow()

    // ============================================================== //
    // Estados de UI                                                  //
    // ============================================================== //
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _estadoFiltro = MutableStateFlow("Todos")
    val estadoFiltro: StateFlow<String> = _estadoFiltro.asStateFlow()
    private val _statusChangeResult = MutableStateFlow<Boolean?>(null)
    val statusChangeResult: StateFlow<Boolean?> = _statusChangeResult.asStateFlow()

    fun setEstadoFiltro(nuevo: String) {
        _estadoFiltro.value = nuevo
    }


    // ============================================================== //
    // Productos y Unidades - Expuestos desde el Repository          //
    // ============================================================== //
    val productosActivos: StateFlow<List<com.example.kubhubsystem_gp13_dam.model.ProductoEntityDTO>> =
        this@RecetaViewModel.recetaRepository.productosActivos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unidadesMedida: StateFlow<List<String>> =
        this@RecetaViewModel.recetaRepository.unidadesMedida.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Helper síncrono y suspend para obtener item del cache
    fun getRecipe(idReceta: Int): RecipeWithDetailsAnswerUpdateDTO? {
        return this@RecetaViewModel.recetaRepository.getRecipeFromCache(idReceta)
    }

    suspend fun getRecipeFromCacheSuspend(idReceta: Int): RecipeWithDetailsAnswerUpdateDTO? {
        return withContext(Dispatchers.Default) {
            this@RecetaViewModel.recetaRepository.getRecipeFromCache(idReceta)
        }
    }

    // ============================================================== //
    // Filtrado                                                       //
    // ============================================================== //
    val recetasFiltradas: StateFlow<List<RecipeWithDetailsAnswerUpdateDTO>> = combine(
        this@RecetaViewModel.recetaRepository.recetas,
        _searchQuery,
        _selectedCategoria,
        _estadoFiltro
    ) { recetas, query, categoria, estadoFiltro ->
        recetas.filter { receta ->
            val matchQuery =
                query.isEmpty() ||
                        receta.nombreReceta?.contains(query, true) == true ||
                        receta.descripcionReceta?.contains(query, true) == true

            val matchCategoria = categoria == null || categoria.isEmpty() ||
                    receta.descripcionReceta?.equals(categoria, true) == true

            val matchEstado =
                estadoFiltro == "Todos" ||
                        receta.estadoReceta?.name.equals(estadoFiltro.uppercase(), true)

            matchQuery && matchCategoria && matchEstado
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // ============================================================== //
    // Categorías dinámicas                                           //
    // ============================================================== //
    val categoriasRecetas: StateFlow<List<String>> =
        this@RecetaViewModel.recetaRepository.recetas.map { recetas ->
            recetas.mapNotNull { it.descripcionReceta }
                .distinct()
                .sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ============================================================== //
    // Estados disponibles                                            //
    // ============================================================== //
    val estadosDisponibles: StateFlow<List<String>> =
        this@RecetaViewModel.recetaRepository.recetas.map { recetas ->
            recetas.mapNotNull { it.estadoReceta?.name }
                .distinct()
                .sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // ============================================================== //
    // Init                                                           //
    // ============================================================== //
    init {
        loadRecipes()
        loadProductosActivos()
        loadUnidadesMedida()
    }

    // ============================================================== //
    // Carga de productos activos                                     //
    // ============================================================== //
    fun loadProductosActivos(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d("RecetaViewModel", "🟡 Cargando productos activos...")

                val result = this@RecetaViewModel.recetaRepository.fetchProductosActivos(forceRefresh)

                result.onSuccess { productos ->
                    Log.d("RecetaViewModel", "✅ ${productos.size} productos activos cargados")
                }.onFailure { error ->
                    Log.e("RecetaViewModel", "❌ Error al cargar productos: ${error.message}", error)
                }

            } catch (e: Exception) {
                Log.e("RecetaViewModel", "💥 Error inesperado al cargar productos", e)
            }
        }
    }

    /**
     * ✅ Cargar unidades de medida disponibles
     */
    fun loadUnidadesMedida(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d("RecetaViewModel", "🟡 Cargando unidades de medida...")

                val result = this@RecetaViewModel.recetaRepository.fetchUnidadesMedida(forceRefresh)

                result.onSuccess { unidades ->
                    Log.d("RecetaViewModel", "✅ ${unidades.size} unidades de medida cargadas")
                }.onFailure { error ->
                    Log.e("RecetaViewModel", "❌ Error al cargar unidades: ${error.message}", error)
                }

            } catch (e: Exception) {
                Log.e("RecetaViewModel", "💥 Error inesperado al cargar unidades", e)
            }
        }
    }

    // ============================================================== //
    // Carga de recetas                                              //
    // ============================================================== //
    fun loadRecipes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            Log.d("RecetaViewModel", "🟡 loadRecipes iniciado | forceRefresh=$forceRefresh")

            try {
                val result = this@RecetaViewModel.recetaRepository.fetchAllActiveRecipes(forceRefresh)
                Log.d("RecetaViewModel", "✅ fetchAllActiveRecipes ejecutado. result=$result")

                result.getOrNull()?.let { nuevasRecetas ->
                    Log.d("RecetaViewModel", "📦 ${nuevasRecetas.size} recetas obtenidas")

                    this@RecetaViewModel.recetaRepository.updateCache(nuevasRecetas)
                    Log.d("RecetaViewModel", "💾 Cache de repositorio actualizada")


                } ?: run {
                    Log.w("RecetaViewModel", "⚠️ Result.getOrNull() devolvió null")
                }

                _isLoading.value = false
                Log.d("RecetaViewModel", "✅ loadRecipes finalizado correctamente")

            } catch (e: HttpException) {
                _isLoading.value = false
                _errorMessage.value = "Error HTTP ${e.code()}: ${e.message()}"
                Log.e("RecetaViewModel", "❌ HttpException: ${e.code()} - ${e.message()}", e)
            } catch (e: IOException) {
                _isLoading.value = false
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("RecetaViewModel", "🌐 IOException: ${e.message}", e)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error inesperado: ${e.message}"
                Log.e("RecetaViewModel", "🔥 Error inesperado: ${e.message}", e)
            }
        }
    }

    fun refresh() {
        loadRecipes(forceRefresh = true)
        loadProductosActivos(forceRefresh = true)
        loadUnidadesMedida(forceRefresh = true)
    }

    // ============================================================== //
    // Filtros helpers                                               //
    // ============================================================== //
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategoria(categoria: String?) {
        _selectedCategoria.value = categoria
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoria.value = null
    }

    // ============================================================== //
    // CRUD Operations                                               //
    // ============================================================== //

    /**
     * ✅ Crear nueva receta con detalles
     */
    fun createRecipeWithDetails(
        nombreReceta: String,
        descripcionReceta: String,
        ingredientes: List<RecipeItemDTO>,
        instrucciones: String,
        estadoReceta: EstadoRecetaType = EstadoRecetaType.ACTIVO
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            try {
                // Validaciones básicas
                if (nombreReceta.isBlank()) {
                    _errorMessage.value = "El nombre de la receta es obligatorio"
                    return@launch
                }
                if (ingredientes.isEmpty()) {
                    _errorMessage.value = "Debe agregar al menos un ingrediente"
                    return@launch
                }

                val dto = RecipeWithDetailsCreateDTO(
                    nombreReceta = nombreReceta.trim(),
                    descripcionReceta = descripcionReceta.trim().ifBlank { null },
                    listaItems = ingredientes,
                    instrucciones = instrucciones.trim().ifBlank { null },
                    estadoReceta = estadoReceta
                )

                Log.d("RecetaViewModel", "🆕 Creando receta: $dto")

                val result = this@RecetaViewModel.recetaRepository.createRecipeWithDetails(dto)

                result.onSuccess {
                    _successMessage.value = "Receta '$nombreReceta' creada exitosamente"
                    loadRecipes(forceRefresh = true)
                }.onFailure { error ->
                    _errorMessage.value = "Error al crear receta: ${error.message}"
                }

            } catch (e: HttpException) {
                _errorMessage.value = "Error HTTP ${e.code()}: ${e.message()}"
            } catch (e: IOException) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear receta: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * ✅ Actualizar receta existente con detalles
     */
    fun updateRecipeWithDetails(dto: RecipeWithDetailsAnswerUpdateDTO) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _successMessage.value = null

            Log.d("RecetaViewModel", "🟢 Iniciando updateRecipeWithDetails con dto=$dto")

            try {
                // --- VALIDACIONES BÁSICAS ---
                val nombre = dto.nombreReceta
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

                val listaItems = dto.listaItems

                Log.d("RecetaViewModel", "🔍 Validaciones -> nombre=$nombre, items=${listaItems?.size}")

                if (nombre == null) {
                    _errorMessage.value = "El nombre de la receta es obligatorio"
                    Log.e("RecetaViewModel", "❌ Error: nombreReceta es nulo o vacío")
                    return@launch
                }

                if (listaItems.isNullOrEmpty()) {
                    _errorMessage.value = "Debe tener al menos un ingrediente"
                    Log.e("RecetaViewModel", "❌ Error: listaItems está vacía")
                    return@launch
                }

                // --- NORMALIZAR DATOS ---
                val safeDto = dto.copy(
                    nombreReceta = nombre,
                    descripcionReceta = dto.descripcionReceta?.trim()?.ifBlank { null },
                    instrucciones = dto.instrucciones?.trim()?.ifBlank { null }
                    // ✅ estadoReceta se mantiene como Enum, Gson lo serializará como "ACTIVO" o "INACTIVO"
                )

                Log.d("RecetaViewModel", "📦 safeDto listo para enviar -> $safeDto")
                Log.d("RecetaViewModel", "🔍 Estado a enviar: ${safeDto.estadoReceta}")

                // --- LLAMAR AL REPO ---
                val result = this@RecetaViewModel.recetaRepository.updateRecipeWithDetails(safeDto)

                result.onSuccess {
                    Log.d("RecetaViewModel", "✅ Llamada al repositorio completada con éxito")
                    _successMessage.value = "Receta '${safeDto.nombreReceta}' actualizada exitosamente"
                    loadRecipes(forceRefresh = true)
                }.onFailure { error ->
                    _errorMessage.value = "Error al actualizar: ${error.message}"
                    Log.e("RecetaViewModel", "💥 Error en result.onFailure", error)
                }

            } catch (e: HttpException) {
                _errorMessage.value = "Error HTTP ${e.code()}: ${e.message()}"
                Log.e("RecetaViewModel", "🔥 Error HTTP ${e.code()}: ${e.message()}", e)

            } catch (e: IOException) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("RecetaViewModel", "🌐 Error de conexión: ${e.message}", e)

            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar: ${e.message}"
                Log.e("RecetaViewModel", "💥 Error inesperado al actualizar", e)

            } finally {
                _isSaving.value = false
                Log.d("RecetaViewModel", "📚 Finalizando updateRecipeWithDetails()")
            }
        }
    }


    /**
     * ✅ Cambiar estado de una receta (ACTIVO <-> INACTIVO)
     */
    fun updateChangingStatus(idReceta: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            Log.d("RecetaViewModel", "🔄 Iniciando cambio de estado para receta ID: $idReceta")

            try {
                val success = this@RecetaViewModel.recetaRepository.updateChangingStatusRecipeWith(idReceta)

                if (success) {
                    Log.d("RecetaViewModel", "✅ Estado cambiado exitosamente")
                    _successMessage.value = "Estado de la receta actualizado correctamente"
                    _statusChangeResult.value = true

                    // 🔥 REFRESCAR LA LISTA COMPLETA
                    loadRecipes(forceRefresh = true)
                } else {
                    Log.e("RecetaViewModel", "❌ No se pudo cambiar el estado")
                    _errorMessage.value = "No se pudo cambiar el estado de la receta"
                    _statusChangeResult.value = false
                }
            } catch (e: Exception) {
                Log.e("RecetaViewModel", "💥 Error al cambiar estado", e)
                _errorMessage.value = "Error al cambiar estado: ${e.message}"
                _statusChangeResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✅ Limpiar el resultado del cambio de estado
     */
    fun clearStatusChangeResult() {
        _statusChangeResult.value = null
    }

    /**
     * ✅ Desactivar receta (eliminación lógica)
     */
    fun deactivateRecipe(idReceta: Int, nombreReceta: String) {
        val TAG = "RecetaVM"

        Log.d(TAG, "➡️ Iniciando desactivación de receta...")
        Log.d(TAG, "📌 ID Receta recibido: $idReceta")
        Log.d(TAG, "📌 Nombre Receta: $nombreReceta")

        // Verificar ID inválido
        if (idReceta <= 0) {
            Log.e(TAG, "❌ ERROR: ID Receta inválido ($idReceta). Cancelando operación.")
            _errorMessage.value = "No se puede desactivar receta: ID inválido ($idReceta)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            Log.d(TAG, "🔄 Llamando a recetaRepository.deactivateRecipe($idReceta)...")

            val result = this@RecetaViewModel.recetaRepository.deactivateRecipe(idReceta)

            result.onSuccess { respuesta ->
                Log.d(TAG, "✅ Receta desactivada correctamente")
                Log.d(TAG, "📝 Respuesta del backend: $respuesta")

                _successMessage.value = "Receta '$nombreReceta' ELIMINADA exitosamente"

                Log.d(TAG, "🔄 Recargando lista de recetas...")
                loadRecipes(forceRefresh = true)
            }.onFailure { error ->
                Log.e(TAG, "❌ Error al desactivar receta con ID $idReceta", error)
                _errorMessage.value = "Error al desactivar receta (ID: $idReceta): ${error.message}"
            }

            _isLoading.value = false
            Log.d(TAG, "⏹ Finalizando proceso de desactivación")
        }
    }

    // ============================================================== //
    // Utilidades                                                     //
    // ============================================================== //

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }


}