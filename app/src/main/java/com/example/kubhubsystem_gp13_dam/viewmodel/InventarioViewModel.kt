package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.model.Inventario
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.repository.MovimientoRepository
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.model.EstadoInventario
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val productoRepository: ProductoRepository,
    private val inventarioRepository: InventarioRepository,
    private val movimientoRepository: MovimientoRepository? = null // Opcional
) : ViewModel() {

    // Estados base
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoria = MutableStateFlow("Todos")
    val selectedCategoria: StateFlow<String> = _selectedCategoria.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Inventarios desde Room DB
    val inventarios: StateFlow<List<Inventario>> = inventarioRepository.observarInventario()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Productos desde Room DB
    private val _productosFromDb: StateFlow<List<ProductoEntity>> =
        productoRepository.observarProductos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Categorías disponibles desde DB
    val categorias: StateFlow<List<String>> = productoRepository.categorias()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Productos filtrados (combinando búsqueda + categoría)
    val productosFiltrados: StateFlow<List<Producto>> = combine(
        _productosFromDb,
        _searchQuery,
        _selectedCategoria
    ) { productos, query, categoria ->
        productos
            .filter { producto ->
                val matchCategoria = categoria == "Todos" || producto.categoria == categoria
                val matchQuery = query.isEmpty() ||
                        producto.nombreProducto.contains(query, ignoreCase = true)
                matchCategoria && matchQuery
            }
            .map { it.toProducto() }
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
                productoRepository.inicializarProductos()
                inventarioRepository.inicializarInventario()
            } catch (e: Exception) {
                _errorMessage.value = "Error al inicializar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategoria(categoria: String) {
        _selectedCategoria.value = categoria
    }

    fun agregarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                productoRepository.guardarProducto(
                    idProducto = null,
                    nombreProducto = producto.nombreProducto,
                    categoria = producto.categoria,
                    unidadMedida = producto.unidadMedida
                )
                _successMessage.value = "Producto agregado exitosamente"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al agregar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun agregarProductoConInventario(
        producto: Producto,
        stockInicial: Double,
        ubicacion: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 1. Insertar el producto en la BD
                val idProductoGenerado = productoRepository.guardarProducto(
                    idProducto = null, // null = crear nuevo
                    nombreProducto = producto.nombreProducto,
                    categoria = producto.categoria,
                    unidadMedida = producto.unidadMedida
                )

                // 2. Crear el inventario para ese producto
                val nuevoInventario = Inventario(
                    idInventario = 0, // Se autogenera
                    idProducto = idProductoGenerado.toInt(),
                    nombreProducto = producto.nombreProducto, // Se usa en el modelo
                    ubicacion = ubicacion,
                    stock = stockInicial,
                    estado = calcularEstadoInventario(stockInicial)
                )

                // 3. Insertar el inventario
                inventarioRepository.insertInventario(nuevoInventario)

                _successMessage.value = "Producto '${producto.nombreProducto}' agregado con stock inicial de ${
                    if (stockInicial % 1 != 0.0) stockInicial
                    else stockInicial.toInt()
                } ${producto.unidadMedida}"

            } catch (e: Exception) {
                _errorMessage.value = "Error al agregar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * Calcula el estado según el stock
     * Puedes ajustar los umbrales según tus necesidades
     */
    private fun calcularEstadoInventario(stock: Double): EstadoInventario {
        return when {
            stock <= 0 -> EstadoInventario.AGOTADO
            stock < 20.0 -> EstadoInventario.BAJO_STOCK  // Mismo umbral que en el Repository
            else -> EstadoInventario.DISPONIBLE
        }
    }
    // ============================================
    // ✅ REEMPLAZAR LA FUNCIÓN actualizarProducto EXISTENTE
    // ============================================

    /**
     * Actualizar solo el producto (sin tocar inventario)
     * Actualiza también el nombre en inventario para mantener consistencia
     */
    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val entity = ProductoEntity(
                    idProducto = producto.idProducto,
                    nombreProducto = producto.nombreProducto,
                    categoria = producto.categoria,
                    unidad = producto.unidadMedida
                )
                productoRepository.actualizarProducto(entity)

                // También actualizar el nombre en inventario (aunque en tu diseño
                // el nombre se obtiene dinámicamente, esto es por si acaso)
                inventarioRepository.actualizarNombreProducto(
                    idProducto = producto.idProducto,
                    nuevoNombre = producto.nombreProducto
                )

                _successMessage.value = "Producto actualizado correctamente"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    /**
     * Actualiza el stock directamente (sin registrar movimiento)
     * Útil para ajustes manuales
     */
    fun actualizarStock(idInventario: Int, nuevoStock: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                inventarioRepository.actualizarStockYEstado(idInventario, nuevoStock)
                _successMessage.value = "Stock actualizado"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar stock: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Registra una ENTRADA de inventario con movimiento automático
     * La fecha se captura automáticamente como LocalDateTime.now()
     */
    fun registrarEntrada(idInventario: Int, cantidad: Double) {
        if (movimientoRepository == null) {
            _errorMessage.value = "Repositorio de movimientos no disponible"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                movimientoRepository.registrarMovimiento(
                    idInventario = idInventario,
                    cantidadMovimiento = cantidad,
                    tipoMovimiento = "ENTRADA"
                )
                _successMessage.value = "Entrada registrada: +$cantidad unidades"
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar entrada: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Registra una SALIDA de inventario con movimiento automático
     * La fecha se captura automáticamente como LocalDateTime.now()
     * Valida que haya stock suficiente
     */
    fun registrarSalida(idInventario: Int, cantidad: Double) {
        if (movimientoRepository == null) {
            _errorMessage.value = "Repositorio de movimientos no disponible"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                movimientoRepository.registrarMovimiento(
                    idInventario = idInventario,
                    cantidadMovimiento = cantidad,
                    tipoMovimiento = "SALIDA"
                )
                _successMessage.value = "Salida registrada: -$cantidad unidades"
                _errorMessage.value = null
            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar salida: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarProducto(id: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val producto = productoRepository.obtenerProducto(id)
                if (producto != null) {
                    productoRepository.eliminarProducto(producto)
                    _successMessage.value = "Producto eliminado"
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Producto no encontrado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    private fun ProductoEntity.toProducto(): Producto {
        return Producto(
            idProducto = this.idProducto,
            nombreProducto = this.nombreProducto,
            categoria = this.categoria,
            unidadMedida = this.unidad
        )
    }
}