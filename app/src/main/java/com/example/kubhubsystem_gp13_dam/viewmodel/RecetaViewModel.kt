package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository

import com.example.kubhubsystem_gp13_dam.ui.model.CategoriaReceta
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecetasViewModel(
    private val recetaRepository: RecetaRepository = RecetaRepository.getInstance(),
    private val productoRepository: ProductoRepository = ProductoRepository.getInstance(),
    private val asignaturaRepository: AsignaturaRepository = AsignaturaRepository.getInstance()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow<CategoriaReceta?>(null)
    val selectedCategoria: StateFlow<CategoriaReceta?> = _selectedCategoria.asStateFlow()

    // Exponer productos y asignaturas para autocompletado
    val productos: StateFlow<List<com.example.kubhubsystem_gp13_dam.model.Producto>> =
        productoRepository.productos

    val asignaturas: StateFlow<List<com.example.kubhubsystem_gp13_dam.model.Asignatura>> =
        asignaturaRepository.asignaturas

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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategoria(categoria: CategoriaReceta?) {
        _selectedCategoria.value = categoria
    }

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