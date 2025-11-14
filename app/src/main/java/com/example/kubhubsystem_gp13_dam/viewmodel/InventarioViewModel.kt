package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.model.InventoryForm
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductResponseAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.model.toCreateDTO
import com.example.kubhubsystem_gp13_dam.model.toUpdateDTO
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.HttpException

/**
 * VIEWMODEL OPTIMIZADO CON BACKEND
 * - Manejo de cache en memoria expuesto por flows
 * - Operaciones de CRUD con manejo de estados
 */
class InventarioViewModel(
    private val inventarioRepository: InventarioRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    // ============================================================== //
    // Productos (categorías y unidades)                              //
    // ============================================================== //
    val categorias: StateFlow<List<String>> = productoRepository.categorias
    val unidadesMedida: StateFlow<List<String>> = productoRepository.unidadesMedida
    val errorProducto: StateFlow<String?> = productoRepository.error

    fun loadCategorias(forceRefresh: Boolean = false) {
        viewModelScope.launch { productoRepository.fetchCategoriasActivas(forceRefresh) }
    }

    fun loadUnidadesMedida(forceRefresh: Boolean = false) {
        viewModelScope.launch { productoRepository.fetchUnidadesMedidaActivas(forceRefresh) }
    }

    fun clearProductoError() {
        productoRepository.clearError()
    }

    // ============================================================== //
    // Filtros / búsqueda / paginación                                //
    // ============================================================== //
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow("Todos")
    val selectedCategoria: StateFlow<String> = _selectedCategoria.asStateFlow()

    private val _selectedEstado = MutableStateFlow("Todos")
    val selectedEstado: StateFlow<String> = _selectedEstado.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _itemsPerPage = MutableStateFlow(10)
    val itemsPerPage: StateFlow<Int> = _itemsPerPage.asStateFlow()

    // ============================================================== //
    // Estados de UI                                                  //
    // ============================================================== //
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false) // nuevo: bloqueo cuando se guarda (create/update)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ============================================================== //
    // Cache local (in-memory)                                        //
    // ============================================================== //
    private val cacheMutex = Mutex()
    private val _cachedInventarios =
        MutableStateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>>(emptyList())
    val cachedInventarios: StateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>> =
        _cachedInventarios.asStateFlow()

    fun actualizarCache(nuevaLista: List<InventoryWithProductResponseAnswerUpdateDTO>) {
        viewModelScope.launch {
            cacheMutex.withLock { _cachedInventarios.value = nuevaLista }
        }
    }

    // ============================================================== //
    // Fuente principal de inventarios (expuesta por el repo)        //
    // ============================================================== //
    private val allInventarios: StateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>> =
        inventarioRepository.inventarios.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // helper sincrónico y suspend para obtener item del cache
    fun getInventario(idInventario: Int): InventoryWithProductResponseAnswerUpdateDTO? {
        return inventarioRepository.getInventoryFromCache(idInventario)
    }

    suspend fun getInventoryFromCacheSuspend(idInventario: Int): InventoryWithProductResponseAnswerUpdateDTO? {
        return withContext(Dispatchers.Default) {
            inventarioRepository.getInventoryFromCache(idInventario)
        }
    }


    // ============================================================== //
    // Filtrado / paginado                                            //
    // ============================================================== //
    val inventariosFiltrados: StateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>> = combine(
        allInventarios,
        _searchQuery,
        _selectedCategoria,
        _selectedEstado
    ) { inventarios, query, categoria, estado ->
        inventarios.filter { item ->
            val matchQuery = query.isEmpty() ||
                    (item.nombreProducto?.contains(query, ignoreCase = true) == true) ||
                    (item.nombreCategoria?.contains(query, ignoreCase = true) == true)

            val matchCategoria = categoria == "Todos" ||
                    (item.nombreCategoria?.equals(categoria, ignoreCase = true) == true)

            val matchEstado = estado == "Todos" ||
                    (item.estadoStock.equals(estado, ignoreCase = true))

            matchQuery && matchCategoria && matchEstado
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventariosPaginados: StateFlow<List<InventoryWithProductResponseAnswerUpdateDTO>> = combine(
        inventariosFiltrados,
        _currentPage,
        _itemsPerPage
    ) { filtrados, page, perPage ->
        val startIndex = (page - 1) * perPage
        val endIndex = (startIndex + perPage).coerceAtMost(filtrados.size)
        if (startIndex >= filtrados.size) emptyList() else filtrados.subList(startIndex, endIndex)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPages: StateFlow<Int> = combine(inventariosFiltrados, _itemsPerPage) { filtrados, perPage ->
        if (filtrados.isEmpty()) 1 else ((filtrados.size + perPage - 1) / perPage).coerceAtLeast(1)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val estados: StateFlow<List<String>> = allInventarios.map { inventarios ->
        inventarios.mapNotNull { it.estadoStock }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ============================================================== //
    // Init                                                          //
    // ============================================================== //
    init {
        loadInventarios()
        loadCategorias()
        loadUnidadesMedida()
    }

    // ============================================================== //
    // Carga de inventarios                                          //
    // ============================================================== //
    fun loadInventarios(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            Log.d("InventoryViewModel", "🟡 loadInventarios iniciado | forceRefresh=$forceRefresh")

            try {
                val result = inventarioRepository.fetchAllActiveInventories(forceRefresh)
                Log.d("InventoryViewModel", "✅ fetchAllActiveInventories ejecutado. result=$result")

                result.getOrNull()?.let { nuevosInventarios ->
                    Log.d("InventoryViewModel", "📦 ${nuevosInventarios.size} inventarios obtenidos")

                    val inventariosConEstado = nuevosInventarios.map { item ->
                        val estado = when {
                            item.stockLimitMin == null || item.stockLimitMin == 0.0 -> "NO ASIGNADO"
                            (item.stock ?: 0.0) == 0.0 -> "AGOTADO"
                            (item.stock ?: 0.0) < (item.stockLimitMin ?: 0.0) -> "BAJO STOCK"
                            else -> "DISPONIBLE"
                        }

                        Log.d(
                            "InventoryViewModel",
                            "🧾 Item procesado: id=${item.idInventario}, nombre=${item.nombreProducto}, " +
                                    "stock=${item.stock}, stockLimitMin=${item.stockLimitMin}, estado=$estado"
                        )

                        item.copy(estadoStock = estado)
                    }

                    inventarioRepository.actualizarCache(inventariosConEstado)
                    Log.d("InventoryViewModel", "💾 Cache de repositorio actualizada")

                    cacheMutex.withLock {
                        _cachedInventarios.value = inventariosConEstado
                        Log.d("InventoryViewModel", "🔒 Cache local actualizada en ViewModel")
                    }
                } ?: run {
                    Log.w("InventoryViewModel", "⚠️ Result.getOrNull() devolvió null")
                }

                _isLoading.value = false
                Log.d("InventoryViewModel", "✅ loadInventarios finalizado correctamente")
            } catch (e: HttpException) {
                _isLoading.value = false
                _errorMessage.value = "Error HTTP ${e.code()}: ${e.message()}"
                Log.e("InventoryViewModel", "❌ HttpException: ${e.code()} - ${e.message()}", e)
            } catch (e: IOException) {
                _isLoading.value = false
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("InventoryViewModel", "🌐 IOException: ${e.message}", e)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error inesperado: ${e.message}"
                Log.e("InventoryViewModel", "🔥 Error inesperado: ${e.message}", e)
            }
        }
    }


    fun refresh() {
        loadInventarios(forceRefresh = true)
        loadCategorias(forceRefresh = true)
        loadUnidadesMedida(forceRefresh = true)
    }

    // ============================================================== //
    // Filtros helpers                                               //
    // ============================================================== //
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        resetToFirstPage()
    }

    fun updateCategoriaFilter(categoria: String) {
        _selectedCategoria.value = categoria
        resetToFirstPage()
    }

    fun updateSelectedEstado(estado: String) {
        _selectedEstado.value = estado
        resetToFirstPage()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoria.value = "Todos"
        _selectedEstado.value = "Todos"
        resetToFirstPage()
    }

    fun goToPage(page: Int) {
        _currentPage.value = page.coerceIn(1, totalPages.value)
    }

    fun nextPage() {
        if (_currentPage.value < totalPages.value) _currentPage.value++
    }

    fun previousPage() {
        if (_currentPage.value > 1) _currentPage.value--
    }

    private fun resetToFirstPage() {
        _currentPage.value = 1
    }

    // ============================================================== //
    // CRUD: crear / actualizar / eliminar                          //
    // ============================================================== //

    /**
     * Guardar desde formulario de UI (usa InventoryForm).
     * Ahora delega a create/update para centralizar comportamiento.
     */
    fun guardarDesdeFormulario(form: InventoryForm) {
        viewModelScope.launch {
            _errorMessage.value = null

            // Validaciones
            if (form.nombreProducto.isNullOrBlank()) {
                _errorMessage.value = "El nombre del producto es obligatorio"
                return@launch
            }
            if (form.nombreCategoria.isNullOrBlank()) {
                _errorMessage.value = "Debe seleccionar una categoría"
                return@launch
            }
            if (form.unidadMedida.isNullOrBlank()) {
                _errorMessage.value = "Debe seleccionar una unidad de medida"
                return@launch
            }

            val estado = calcularEstadoStock(form.stock, form.stockLimitMin)

            if (form.idInventario == null) {
                // Crear
                val createDto: InventoryWithProductCreateDTO = form.toCreateDTO()
                createInventoryWithProduct(createDto)
            } else {
                // Actualizar
                val updateDto: InventoryWithProductResponseAnswerUpdateDTO = form.toUpdateDTO(estado)
                updateInventoryWithProduct(updateDto)
            }
        }
    }

    /**
     * Crear item (usa repo suspend). Manejo consistente de isSaving / mensajes / recarga.
     */
    fun createInventoryWithProduct(dto: InventoryWithProductCreateDTO) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                // Asumimos que inventarioRepository.createInventoryWithProduct es suspend
                inventarioRepository.createInventoryWithProduct(dto)
                _successMessage.value = "Producto '${dto.nombreProducto}' creado exitosamente"
                // Refrescar lista (forzar refetch)
                loadInventarios(forceRefresh = true)
            } catch (e: HttpException) {
                _errorMessage.value = "Error HTTP ${e.code()}: ${e.message()}"
            } catch (e: IOException) {
                _errorMessage.value = "Error de conexión: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateInventoryWithProduct(dto: InventoryWithProductResponseAnswerUpdateDTO) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _successMessage.value = null

            Log.d("InventoryViewModel", "🟢 Iniciando updateInventoryWithProduct con dto=$dto")

            try {
                // --- VALIDACIONES BÁSICAS ---
                val nombre = dto.nombreProducto?.trim().takeIf { !it.isNullOrBlank() }
                val categoria = dto.nombreCategoria?.trim().takeIf { !it.isNullOrBlank() }
                val unidad = dto.unidadMedida?.trim().takeIf { !it.isNullOrBlank() }

                Log.d("InventoryViewModel", "🔍 Validaciones -> nombre=$nombre, categoria=$categoria, unidad=$unidad")

                if (nombre == null) {
                    _errorMessage.value = "El nombre del producto es obligatorio"
                    Log.e("InventoryViewModel", "❌ Error: nombreProducto es nulo o vacío")
                    return@launch
                }
                if (categoria == null) {
                    _errorMessage.value = "Debe seleccionar una categoría"
                    Log.e("InventoryViewModel", "❌ Error: nombreCategoria es nulo o vacío")
                    return@launch
                }
                if (unidad == null) {
                    _errorMessage.value = "Debe seleccionar una unidad de medida"
                    Log.e("InventoryViewModel", "❌ Error: unidadMedida es nulo o vacío")
                    return@launch
                }

                // --- CALCULAR ESTADO AUTOMÁTICAMENTE ---
                val stock = dto.stock ?: 0.0
                val stockMin = dto.stockLimitMin ?: 0.0

                val estado = when {
                    stockMin == 0.0 -> "NO ASIGNADO"
                    stock == 0.0 -> "AGOTADO"
                    stock < stockMin -> "BAJO STOCK"
                    else -> "DISPONIBLE"
                }

                Log.d("InventoryViewModel", "📊 Estado calculado -> stock=$stock, stockMin=$stockMin, estado=$estado")

                // --- NORMALIZAR DESCRIPCIÓN ---
                val descripcionFinal = dto.descripcionProducto
                    ?.trim()
                    ?.ifBlank { "Sin descripción" }
                    ?: "Sin descripción"

                Log.d("InventoryViewModel", "📝 Descripción final = '$descripcionFinal'")

                // --- CREAR DTO ACTUALIZADO Y CONSISTENTE ---
                val safeDto = dto.copy(
                    nombreProducto = nombre,
                    nombreCategoria = categoria,
                    unidadMedida = unidad,
                    descripcionProducto = descripcionFinal,
                    stock = stock,
                    stockLimitMin = stockMin,
                    estadoStock = estado
                )

                Log.d("InventoryViewModel", "📦 safeDto listo para enviar -> $safeDto")

                // --- LLAMAR AL REPO ---
                inventarioRepository.updateInventoryWithProduct(safeDto)
                Log.d("InventoryViewModel", "✅ Llamada al repositorio completada con éxito")

                _successMessage.value = "Producto '${safeDto.nombreProducto}' actualizado exitosamente"
                loadInventarios(forceRefresh = true)

            } catch (e: HttpException) {
                _errorMessage.value = "Error HTTP ${e.code()}: ${e.message()}"
                Log.e("InventoryViewModel", "🔥 Error HTTP ${e.code()}: ${e.message()}", e)
            } catch (e: IOException) {
                _errorMessage.value = "Error de conexión: ${e.message}"
                Log.e("InventoryViewModel", "🌐 Error de conexión: ${e.message}", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar: ${e.message}"
                Log.e("InventoryViewModel", "💥 Error inesperado al actualizar", e)
            } finally {
                _isSaving.value = false
                Log.d("InventoryViewModel", "🔚 Finalizando updateInventoryWithProduct()")
            }
        }
    }



    /**
     * Eliminar lógicamente (ya tenías implementación similar)
     */
    fun eliminarInventario(idInventario: Int, nombreProducto: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = inventarioRepository.logicalDeleteInventory(idInventario)
            result.onSuccess {
                _successMessage.value = "Producto '$nombreProducto' eliminado"
                // refrescar lista
                loadInventarios(forceRefresh = true)
            }.onFailure { error ->
                _errorMessage.value = "Error al eliminar: ${error.message}"
            }
            _isLoading.value = false
        }
    }

    // ============================================================== //
    // Utilidades                                                    //
    // ============================================================== //
    fun clearError() {
        _errorMessage.value = null
        productoRepository.clearError()
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun calcularEstadoStock(stock: Double?, stockLimitMin: Double?): String {
        return when {
            stockLimitMin == null || stockLimitMin == 0.0 -> "NO ASIGNADO"
            (stock ?: 0.0) == 0.0 -> "AGOTADO"
            (stock ?: 0.0) < (stockLimitMin ?: 0.0) -> "BAJO STOCK"
            else -> "DISPONIBLE"
        }
    }
}
