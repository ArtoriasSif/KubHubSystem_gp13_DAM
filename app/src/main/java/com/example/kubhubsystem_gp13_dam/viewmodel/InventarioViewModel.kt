package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.model.CategoriaProducto
import com.example.kubhubsystem_gp13_dam.model.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val repository: ProductoRepository = ProductoRepository.getInstance()
) : ViewModel() {

    // Estados del UI
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow(CategoriaProducto.TODOS)
    val selectedCategoria: StateFlow<CategoriaProducto> = _selectedCategoria.asStateFlow()

    // Productos filtrados
    val productosFiltrados: StateFlow<List<Producto>> = combine(
        repository.productos,
        _searchQuery,
        _selectedCategoria
    ) { productos, query, categoria ->
        productos.filter { producto ->
            val matchesSearch = query.isEmpty() ||
                    producto.nombre.contains(query, ignoreCase = true)
            val matchesCategory = categoria == CategoriaProducto.TODOS ||
                    producto.categoria.equals(
                        categoria.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        ignoreCase = true
                    )
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Actualizar búsqueda
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Actualizar categoría seleccionada
    fun updateSelectedCategoria(categoria: CategoriaProducto) {
        _selectedCategoria.value = categoria
    }

    // Agregar nuevo producto
    fun agregarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.agregarProducto(producto)
        }
    }

    // Actualizar producto
    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.actualizarProducto(producto)
        }
    }

    // Eliminar producto
    fun eliminarProducto(id: Int) {
        viewModelScope.launch {
            repository.eliminarProducto(id)
        }
    }

    // Actualizar solo el stock
    fun actualizarStock(id: Int, nuevoStock: Int) {
        viewModelScope.launch {
            repository.actualizarStock(id, nuevoStock)
        }
    }

    // Obtener producto por ID
    fun getProductoById(id: Int): Producto? {
        return repository.getProductoById(id)
    }
}