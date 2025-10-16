package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecetasViewModel(
    private val recetaRepository: RecetaRepository,
    private val productoDAO: ProductoDAO
) : ViewModel() {

    // Estados de búsqueda y filtros
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow<String?>(null)
    val selectedCategoria: StateFlow<String?> = _selectedCategoria.asStateFlow()

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ✅ Productos desde Room DB convertidos a modelo de dominio
    val productos: StateFlow<List<Producto>> =
        productoDAO.observarTodos()
            .map { listaEntities ->
                listaEntities.map { entity ->
                    Producto(
                        idProducto = entity.idProducto,
                        nombreProducto = entity.nombreProducto,
                        categoria = entity.categoria,
                        unidadMedida = entity.unidad
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ✅ Categorías de recetas dinámicas desde la BD
    val categoriasRecetas: StateFlow<List<String>> =
        recetaRepository.obtenerCategorias()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ✅ Recetas filtradas por búsqueda y categoría
    val recetasFiltradas: StateFlow<List<Receta>> = combine(
        recetaRepository.observarRecetas(),
        _searchQuery,
        _selectedCategoria
    ) { recetas, query, categoria ->
        recetas.filter { receta ->
            val matchesSearch = query.isEmpty() ||
                    receta.nombre.contains(query, ignoreCase = true) ||
                    receta.descripcion.contains(query, ignoreCase = true)
            val matchesCategory = categoria == null || receta.categoria == categoria
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        inicializarDatos()
    }

    private fun inicializarDatos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recetaRepository.inicializarRecetas()
            } catch (e: Exception) {
                _errorMessage.value = "Error al inicializar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Funciones para actualizar estados de búsqueda
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategoria(categoria: String?) {
        _selectedCategoria.value = categoria
    }

    // ✅ Agregar nueva receta
    fun agregarReceta(receta: Receta) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recetaRepository.agregarReceta(receta)
                _successMessage.value = "Receta agregada exitosamente"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al agregar receta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Actualizar receta existente
    fun actualizarReceta(receta: Receta) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recetaRepository.actualizarReceta(receta)
                _successMessage.value = "Receta actualizada exitosamente"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar receta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Eliminar receta
    fun eliminarReceta(idReceta: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recetaRepository.eliminarReceta(idReceta)
                _successMessage.value = "Receta eliminada exitosamente"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar receta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Obtener receta específica por ID
    fun obtenerReceta(idReceta: Int, onResult: (Receta?) -> Unit) {
        viewModelScope.launch {
            try {
                val receta = recetaRepository.obtenerRecetaPorId(idReceta)
                onResult(receta)
            } catch (e: Exception) {
                _errorMessage.value = "Error al obtener receta: ${e.message}"
                onResult(null)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}