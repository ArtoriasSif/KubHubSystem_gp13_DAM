package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateUpdateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductoResponseDTO
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ✅ VIEWMODEL OPTIMIZADO CON BACKEND
 * - Paginación (10 items por página)
 * - Búsqueda y filtros en tiempo real
 * - Cache inteligente con Flow
 * - Sin Room DB
 */
class InventarioViewModel(
    private val repository: InventarioRepository
) : ViewModel() {

    // ========== ESTADOS DE BÚSQUEDA Y FILTROS ==========
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow("Todos")
    val selectedCategoria: StateFlow<String> = _selectedCategoria.asStateFlow()

    private val _selectedEstado = MutableStateFlow("Todos")
    val selectedEstado: StateFlow<String> = _selectedEstado.asStateFlow()

    // ========== ESTADOS DE PAGINACIÓN ==========
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _itemsPerPage = MutableStateFlow(10)
    val itemsPerPage: StateFlow<Int> = _itemsPerPage.asStateFlow()

    // ========== ESTADOS DE CARGA Y MENSAJES ==========
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ========== DATOS DEL BACKEND ==========
    /**
     * ✅ Inventarios completos desde el backend
     * Se actualiza automáticamente cuando cambia el cache del repository
     */
    private val allInventarios: StateFlow<List<InventoryWithProductoResponseDTO>> =
        repository.inventarios
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * ✅ Inventarios filtrados según búsqueda, categoría y estado
     */
    val inventariosFiltrados: StateFlow<List<InventoryWithProductoResponseDTO>> = combine(
        allInventarios,
        _searchQuery,
        _selectedCategoria,
        _selectedEstado
    ) { inventarios, query, categoria, estado ->
        inventarios.filter { item ->
            // Filtro por búsqueda
            val matchQuery = query.isEmpty() ||
                    item.nombreProducto.contains(query, ignoreCase = true) ||
                    item.nombreCategoria.contains(query, ignoreCase = true)

            // Filtro por categoría
            val matchCategoria = categoria == "Todos" || item.nombreCategoria == categoria

            // Filtro por estado
            val matchEstado = estado == "Todos" || item.estadoStock == estado

            matchQuery && matchCategoria && matchEstado
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * ✅ Inventarios paginados (10 por página)
     */
    val inventariosPaginados: StateFlow<List<InventoryWithProductoResponseDTO>> = combine(
        inventariosFiltrados,
        _currentPage,
        _itemsPerPage
    ) { filtrados, page, perPage ->
        val startIndex = (page - 1) * perPage
        val endIndex = (startIndex + perPage).coerceAtMost(filtrados.size)

        if (startIndex >= filtrados.size) {
            emptyList()
        } else {
            filtrados.subList(startIndex, endIndex)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * ✅ Total de páginas
     */
    val totalPages: StateFlow<Int> = combine(
        inventariosFiltrados,
        _itemsPerPage
    ) { filtrados, perPage ->
        if (filtrados.isEmpty()) 1
        else ((filtrados.size + perPage - 1) / perPage).coerceAtLeast(1)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )

    /**
     * ✅ Categorías únicas disponibles
     */
    val categorias: StateFlow<List<String>> = allInventarios.map { inventarios ->
        inventarios
            .map { it.nombreCategoria }
            .distinct()
            .sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * ✅ Estados únicos disponibles
     */
    val estados: StateFlow<List<String>> = allInventarios.map { inventarios ->
        inventarios
            .map { it.estadoStock }
            .distinct()
            .sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Cargar datos iniciales
        loadInventarios()
    }

    // ========== FUNCIONES DE CARGA ==========

    /**
     * ✅ Cargar inventarios del backend
     */
    fun loadInventarios(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.fetchAllActiveInventories(forceRefresh)

            result.onSuccess {
                _isLoading.value = false
                _errorMessage.value = null
            }.onFailure { error ->
                _isLoading.value = false
                _errorMessage.value = error.message ?: "Error al cargar inventarios"
            }
        }
    }

    /**
     * ✅ Refrescar datos
     */
    fun refresh() {
        loadInventarios(forceRefresh = true)
    }

    // ========== FUNCIONES DE BÚSQUEDA Y FILTROS ==========

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        resetToFirstPage()
    }

    fun updateSelectedCategoria(categoria: String) {
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

    // ========== FUNCIONES DE PAGINACIÓN ==========

    fun goToPage(page: Int) {
        val maxPage = totalPages.value
        _currentPage.value = page.coerceIn(1, maxPage)
    }

    fun nextPage() {
        if (_currentPage.value < totalPages.value) {
            _currentPage.value++
        }
    }

    fun previousPage() {
        if (_currentPage.value > 1) {
            _currentPage.value--
        }
    }

    fun resetToFirstPage() {
        _currentPage.value = 1
    }

    // ========== CRUD OPERATIONS ==========

    /**
     * ✅ Crear O Actualizar inventario (unificado)
     */
    fun guardarInventario(dto: InventoryWithProductCreateUpdateDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = if (dto.idInventario == null || dto.idProducto == null) {
                // CREAR nuevo
                repository.createInventoryWithProduct(dto)
            } else {
                // ACTUALIZAR existente
                repository.updateInventoryWithProduct(dto)
            }

            result.onSuccess {
                _isLoading.value = false
                val accion = if (dto.idInventario == null) "creado" else "actualizado"
                _successMessage.value = "Producto '${dto.nombreProducto}' $accion exitosamente"
                _errorMessage.value = null
            }.onFailure { error ->
                _isLoading.value = false
                _errorMessage.value = "Error: ${error.message}"
            }
        }
    }
    /**
     * ✅ Eliminar inventario (lógico)
     */
    fun eliminarInventario(idInventario: Int, nombreProducto: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.logicalDeleteInventory(idInventario)

            result.onSuccess {
                _isLoading.value = false
                _successMessage.value = "Producto '$nombreProducto' eliminado"
                _errorMessage.value = null
            }.onFailure { error ->
                _isLoading.value = false
                _errorMessage.value = "Error al eliminar: ${error.message}"
            }
        }
    }

    // ========== UTILIDADES ==========

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    /**
     * ✅ Obtener un inventario específico del cache
     */
    fun getInventario(idInventario: Int): InventoryWithProductoResponseDTO? {
        return repository.getInventoryFromCache(idInventario)
    }
}