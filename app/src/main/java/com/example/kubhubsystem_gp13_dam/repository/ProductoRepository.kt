package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.utils.SpanishSingularConverter
import com.example.kubhubsystem_gp13_dam.utils.SpanishTextValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    ): Long {
        return withContext(Dispatchers.IO) {
            // 1. Validar y formatear los textos
            val nombreProductoValidado = SpanishTextValidator.validarYFormatearTexto(nombreProducto)
            val categoriaValidada = SpanishTextValidator.validarYFormatearTexto(categoria)

            // 2. Verificar validez de los campos ANTES de continuar
            when {
                nombreProductoValidado == null && categoriaValidada == null ->
                    throw IllegalArgumentException("Nombre y categoría contienen errores de formato")

                nombreProductoValidado == null ->
                    throw IllegalArgumentException("Nombre de producto contiene errores de formato: '$nombreProducto'")

                categoriaValidada == null ->
                    throw IllegalArgumentException("Categoría contiene errores de formato: '$categoria'")
            }

            // 3. Normalizar el nombre a singular para búsqueda
            val nombreNormalizado = SpanishSingularConverter.normalizarTexto(nombreProductoValidado)

            // 4. Buscar productos existentes con nombre similar (en singular)
            val productosExistentes = dao.observarTodos().first()

            val productoConflictivo = productosExistentes.find { producto ->
                // Si estamos actualizando, ignorar el producto actual
                if (idProducto != null && producto.idProducto == idProducto) {
                    return@find false
                }

                // Comparar nombres normalizados (singular)
                val nombreExistenteNormalizado = SpanishSingularConverter.normalizarTexto(producto.nombreProducto)
                nombreExistenteNormalizado == nombreNormalizado
            }

            // 5. Si existe conflicto, lanzar excepción con información útil
            if (productoConflictivo != null) {
                throw IllegalArgumentException(
                    "Ya existe un producto similar: '${productoConflictivo.nombreProducto}'. " +
                            "No se puede guardar '${nombreProductoValidado}'"
                )
            }

            // 6. Guardar o actualizar el producto
            if (idProducto == null) {
                // Insertar nuevo producto con el nombre formateado (como lo escribió el usuario)
                dao.insertar(
                    ProductoEntity(
                        idProducto = 0,
                        nombreProducto = nombreProductoValidado,
                        categoria = categoriaValidada,
                        unidad = unidadMedida
                    )
                )
            } else {
                // Actualizar producto existente
                dao.actualizar(
                    ProductoEntity(
                        idProducto = idProducto,
                        nombreProducto = nombreProductoValidado,
                        categoria = categoriaValidada,
                        unidad = unidadMedida
                    )
                )
                idProducto.toLong()
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