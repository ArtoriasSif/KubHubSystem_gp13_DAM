package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProductoRepository(private val dao: ProductoDAO) {

    suspend fun inicializarProductos() {
        val listaInicial = listOf(
            Producto(1, "Harina", "Secos", "kg"),
            Producto(2, "Aceite de Oliva", "Líquidos", "l"),
            Producto(3, "Azúcar", "Secos", "kg"),
            Producto(4, "Leche", "Lácteos", "l"),
            Producto(5, "Huevos", "Frescos", "unidad"),
            Producto(6, "Sal", "Secos", "kg"),
            Producto(7, "Mantequilla", "Lácteos", "kg"),
            Producto(8, "Tomates", "Frescos", "kg")
        )

        listaInicial.forEach { producto ->
            val existe = dao.existeProducto(producto.idProducto)
            if (existe == 0) {
                dao.insertar(
                    ProductoEntity(
                        idProducto = producto.idProducto,
                        nombreProducto = producto.nombreProducto,
                        categoria = producto.categoria,
                        unidad = producto.unidadMedida.uppercase()
                    )
                )
            }
        }
    }

    fun observarProductos(): Flow<List<ProductoEntity>> = dao.observarTodos()

    suspend fun obtenerProducto(idProducto: Int) = dao.obtenerPorId(idProducto)

    suspend fun guardarProducto(
        idProducto: Int?,
        nombreProducto: String,
        categoria: String,
        unidadMedida: String
    ): Long { // Cambiar el tipo de retorno a Long (ID generado)
        return withContext(Dispatchers.IO) {
            if (idProducto == null) {
                // Insertar nuevo producto y retornar el ID generado
                dao.insertar(
                    ProductoEntity(
                        idProducto = 0,
                        nombreProducto = nombreProducto,
                        categoria = categoria,
                        unidad = unidadMedida
                    )
                )
            } else {
                // Actualizar producto existente
                dao.actualizar(
                    ProductoEntity(
                        idProducto = idProducto,
                        nombreProducto = nombreProducto,
                        categoria = categoria,
                        unidad = unidadMedida
                    )
                )
                idProducto.toLong() // Retornar el ID existente
            }
        }
    }
    suspend fun actualizarProducto (producto: ProductoEntity) = dao.actualizar(producto)

    suspend fun actualizarNombreProducto(nuevoNombre: String,id: Int) = dao.actualizarNombreProducto(id, nuevoNombre)


    fun filtrarPorCategoria(categoria: String): Flow<List<ProductoEntity>> {
        return if (categoria == "Todos") {
            dao.observarTodos()
        } else {
            dao.obtenerPorCategoria(categoria)
        }
    }

    fun categorias(): Flow<List<String>> = dao.obtenerCategorias()

    suspend fun eliminarProducto(producto: ProductoEntity) = dao.eliminar(producto)
    suspend fun eliminarTodosProductos() = dao.eliminarTodos()

    companion object


}