package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.EstadoProducto
import com.example.kubhubsystem_gp13_dam.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductoRepository {

    // Lista mutable privada de productos
    private val _productos = MutableStateFlow<List<Producto>>(
        listOf(
            Producto(1, "Harina", "Secos", 50, "kg", EstadoProducto.DISPONIBLE),
            Producto(2, "Aceite de Oliva", "Líquidos", 25, "l", EstadoProducto.DISPONIBLE),
            Producto(3, "Azúcar", "Secos", 30, "kg", EstadoProducto.DISPONIBLE),
            Producto(4, "Leche", "Lácteos", 40, "l", EstadoProducto.DISPONIBLE),
            Producto(5, "Huevos", "Frescos", 120, "unidad", EstadoProducto.DISPONIBLE),
            Producto(6, "Sal", "Secos", 15, "kg", EstadoProducto.BAJO_STOCK),
            Producto(7, "Mantequilla", "Lácteos", 8, "kg", EstadoProducto.BAJO_STOCK),
            Producto(8, "Tomates", "Frescos", 0, "kg", EstadoProducto.AGOTADO)
        )
    )

    // Exposición pública como StateFlow inmutable
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Obtener producto por ID
    fun getProductoById(id: Int): Producto? {
        return _productos.value.find { it.id == id }
    }

    // Agregar nuevo producto
    fun agregarProducto(producto: Producto) {
        val nuevoId = (_productos.value.maxOfOrNull { it.id } ?: 0) + 1
        val nuevoProducto = producto.copy(id = nuevoId)
        _productos.value = _productos.value + nuevoProducto
    }

    // Actualizar producto existente
    fun actualizarProducto(producto: Producto) {
        _productos.value = _productos.value.map {
            if (it.id == producto.id) producto else it
        }
    }

    // Eliminar producto
    fun eliminarProducto(id: Int) {
        _productos.value = _productos.value.filter { it.id != id }
    }

    // Actualizar solo el stock de un producto
    fun actualizarStock(id: Int, nuevoStock: Int) {
        _productos.value = _productos.value.map { producto ->
            if (producto.id == id) {
                val nuevoEstado = calcularEstado(nuevoStock)
                producto.copy(stock = nuevoStock, estado = nuevoEstado)
            } else {
                producto
            }
        }
    }

    // Buscar productos por nombre
    fun buscarProductos(query: String): List<Producto> {
        return _productos.value.filter {
            it.nombre.contains(query, ignoreCase = true)
        }
    }

    // Filtrar por categoría
    fun filtrarPorCategoria(categoria: String): List<Producto> {
        return if (categoria == "Todos") {
            _productos.value
        } else {
            _productos.value.filter { it.categoria == categoria }
        }
    }

    // Calcular estado según el stock
    private fun calcularEstado(stock: Int): EstadoProducto {
        return when {
            stock == 0 -> EstadoProducto.AGOTADO
            stock < 20 -> EstadoProducto.BAJO_STOCK
            else -> EstadoProducto.DISPONIBLE
        }
    }

    companion object {
        @Volatile
        private var instance: ProductoRepository? = null

        fun getInstance(): ProductoRepository {
            return instance ?: synchronized(this) {
                instance ?: ProductoRepository().also { instance = it }
            }
        }
    }
}