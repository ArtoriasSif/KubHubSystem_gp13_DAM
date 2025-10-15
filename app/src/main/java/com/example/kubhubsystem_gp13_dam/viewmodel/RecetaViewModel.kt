package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.ui.model.CategoriaReceta
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecetasViewModel(
    private val recetaRepository: RecetaRepository,
    private val productoRepository: ProductoRepository,
    private val asignaturaRepository: AsignaturaRepository
) : ViewModel() {

    // Estados de búsqueda y filtros
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow<CategoriaReceta?>(null)
    val selectedCategoria: StateFlow<CategoriaReceta?> = _selectedCategoria.asStateFlow()

    // Productos desde Room DB convertidos a modelo de dominio
    val productos: StateFlow<List<Producto>> =
        productoRepository.observarProductos()
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

    // Asignaturas desde repositorio
    val asignaturas: StateFlow<List<com.example.kubhubsystem_gp13_dam.model.Asignatura>> =
        asignaturaRepository.asignaturas

    // Recetas filtradas por búsqueda y categoría
    val recetasFiltradas: StateFlow<List<Receta>> = combine(
        recetaRepository.recetas,
        _searchQuery,
        _selectedCategoria
    ) { recetas, query, categoria ->
        recetas.filter { receta ->
            val matchesSearch = query.isEmpty() ||
                    receta.nombre.contains(query, ignoreCase = true) ||
                    receta.descripcion.contains(query, ignoreCase = true)
            val matchesCategory = categoria == null || receta.categoria == categoria
            matchesSearch && matchesCategory && receta.estaActiva
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Funciones para actualizar estados
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategoria(categoria: CategoriaReceta?) {
        _selectedCategoria.value = categoria
    }

    // Operaciones CRUD de recetas
    fun agregarReceta(receta: Receta) {
        viewModelScope.launch {
            recetaRepository.agregarReceta(receta)
        }
    }

    fun actualizarReceta(receta: Receta) {
        viewModelScope.launch {
            recetaRepository.actualizarReceta(receta)
        }
    }

    fun eliminarReceta(idReceta: Int) {
        viewModelScope.launch {
            recetaRepository.eliminarReceta(idReceta)
        }
    }
}