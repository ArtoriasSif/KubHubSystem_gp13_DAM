package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.InventarioDAO
import com.example.kubhubsystem_gp13_dam.local.entities.InventarioEntity
import com.example.kubhubsystem_gp13_dam.model.EstadoInventario
import com.example.kubhubsystem_gp13_dam.model.Inventario
import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventarioRepository(
    private val invDao: InventarioDAO,
    private val proDao: ProductoDAO
) {

    suspend fun inicializarInventario() {
        // Lista inicial con datos temporales para el nombre
        val idsInventario = listOf(1, 2, 3, 4, 5, 6, 7, 8)

        idsInventario.forEach { idInventario ->
            val existe = invDao.existeInventario(idInventario)
            if (existe == 0) {
                // Insertar con datos base, el nombre se obtiene dinámicamente en toDomain()
                invDao.insertar(
                    InventarioEntity(
                        idInventario = idInventario,
                        idProducto = idInventario, // Asumiendo que id producto = id inventario
                        ubicacion = "Bodega Principal",
                        stock = (idInventario * 10).toDouble()
                    )
                )
            }
        }
    }

    // ✅ CORREGIDO: Ahora es una función suspend para obtener el nombre
    suspend fun InventarioEntity.toDomain(): Inventario {
        val nombreProducto = proDao.buscarNombrePorId(idProducto) ?: "Producto Desconocido"

        return Inventario(
            idInventario = idInventario,
            idProducto = idProducto,
            nombreProducto = nombreProducto,
            ubicacion = ubicacion,
            stock = stock,
            estado = calcularEstado(stock)
        )
    }

    // ✅ CORREGIDO: Ahora convierte los entities a domain dentro del Flow
    fun observarInventario(): Flow<List<Inventario>> =
        invDao.observarTodos().map { listaEntities ->
            // ✅ Obtener todos los IDs de productos necesarios
            val idsProductos = listaEntities.map { it.idProducto }.distinct()

            // ✅ Una sola consulta para todos los productos
            val productos = proDao.obtenerPorIds(idsProductos)

            // ✅ Crear un mapa para búsqueda O(1)
            val productosMap = productos.associateBy { it.idProducto }

            // ✅ Mapear todos los inventarios usando el mapa
            listaEntities.map { entity ->
                val nombreProducto = productosMap[entity.idProducto]?.nombreProducto
                    ?: "Desconocido"

                Inventario(
                    idInventario = entity.idInventario,
                    idProducto = entity.idProducto,
                    nombreProducto = nombreProducto,
                    ubicacion = entity.ubicacion,
                    stock = entity.stock,
                    estado = calcularEstado(entity.stock)
                )
            }
        }

    suspend fun obtenerInventario(idInventario: Int): Inventario? {
        val entity = invDao.obtenerPorId(idInventario) ?: return null
        return entity.toDomain()
    }

    suspend fun guardarInventario(
        idInventario: Int?,
        idProducto: Int,
        ubicacion: String,
        stock: Double
    ) {
        if (idInventario == null || idInventario == 0) {
            invDao.insertar(
                InventarioEntity(
                    idProducto = idProducto,
                    ubicacion = ubicacion,
                    stock = stock
                )
            )
        } else {
            invDao.actualizar(
                InventarioEntity(
                    idInventario = idInventario,
                    idProducto = idProducto,
                    ubicacion = ubicacion,
                    stock = stock
                )
            )
        }
    }

    suspend fun actualizarInventario(inventario: InventarioEntity) = invDao.actualizar(inventario)

    suspend fun actualizarStockYEstado(id: Int, nuevoStock: Double) {
        val existe = invDao.existeInventario(id)
        if (existe > 0) { // ✅ CORREGIDO: Debe ser > 0 (si existe)
            invDao.actualizarStock(id, nuevoStock)
        }
    }

    suspend fun eliminarInventario(inventario: InventarioEntity) = invDao.eliminar(inventario)

    suspend fun eliminarTodoInventarioXD() = invDao.eliminarTodos()

    // Calcula el estado basado en el stock
    private fun calcularEstado(stock: Double): EstadoInventario {
        return when {
            stock == 0.0 -> EstadoInventario.AGOTADO
            stock < 20.0 -> EstadoInventario.BAJO_STOCK
            else -> EstadoInventario.DISPONIBLE
        }
    }
    suspend fun actualizarNombreProducto(idProducto: Int, nuevoNombre: String) {
        proDao.actualizarNombreProducto(idProducto, nuevoNombre)
    }

    /* Inserta un nuevo inventario desde el modelo Inventario
* Convierte de Inventario (modelo) a InventarioEntity (BD)
*/
    suspend fun insertInventario(inventario: Inventario): Long {
        val entity = InventarioEntity(
            idInventario = 0, // Se autogenera
            idProducto = inventario.idProducto,
            ubicacion = inventario.ubicacion,
            stock = inventario.stock
            // nombreProducto NO va aquí porque no existe en InventarioEntity
        )
        return invDao.insertar(entity)
    }

}