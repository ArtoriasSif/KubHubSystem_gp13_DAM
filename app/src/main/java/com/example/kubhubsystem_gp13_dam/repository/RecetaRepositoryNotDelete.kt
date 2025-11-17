package com.example.kubhubsystem_gp13_dam.data.repository

import android.util.Log
import com.example.kubhubsystem_gp13_dam.local.dao.DetalleRecetaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.InventarioDAO
import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.RecetaDAO
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleRecetaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.InventarioEntity
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.local.entities.RecetaEntity
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.ui.model.IngredienteReceta
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecetaRepositoryNotDelete(
    private val recetaDAO: RecetaDAO,
    private val detalleDAO: DetalleRecetaDAO,
    private val productoDAO: ProductoDAO,
    private val inventarioDAO: InventarioDAO
) {

    // ✅ Observar todas las recetas con sus ingredientes
    fun observarRecetas(): Flow<List<Receta>> =
        recetaDAO.observarTodas().map { listaRecetas ->
            listaRecetas.map { recetaEntity ->
                recetaEntity.toDomain()
            }
        }

    // ✅ Convertir RecetaEntity a Receta (modelo de dominio)
    private suspend fun RecetaEntity.toDomain(): Receta {
        // Obtener detalles de la receta
        val detallesEntities = detalleDAO.obtenerDetallesPorReceta(this.idReceta)

        val ingredientes = detallesEntities.mapNotNull { detalle ->
            // Obtener producto completo
            val productoEntity = productoDAO.obtenerPorId(detalle.idProducto)

            productoEntity?.let {
                IngredienteReceta(
                    idDetalle = detalle.idDetalleReceta,
                    producto = Producto(
                        idProducto = it.idProducto,
                        nombreProducto = it.nombreProducto,
                        categoria = it.categoria,
                        unidadMedida = it.unidad
                    ),
                    cantidad = detalle.cantidaUnidadMedida
                )
            }
        }

        return Receta(
            idReceta = this.idReceta,
            nombre = this.nombreReceta,
            descripcion = this.descripcionReceta,
            categoria = this.categoriaReceta,
            instrucciones = this.instrucciones,
            observaciones = this.observaciones,
            ingredientes = ingredientes
        )
    }

    // ✅ Obtener receta por ID
    suspend fun obtenerRecetaPorId(idReceta: Int): Receta? {
        val entity = recetaDAO.obtenerPorId(idReceta) ?: return null
        return entity.toDomain()
    }

    // ✅ Obtener categorías disponibles dinámicamente
    fun obtenerCategorias(): Flow<List<String>> = recetaDAO.obtenerCategorias()

    // ✅ Buscar recetas por nombre
    fun buscarRecetas(query: String): Flow<List<Receta>> =
        recetaDAO.buscarPorNombre(query).map { lista ->
            lista.map { it.toDomain() }
        }

    // ✅ Filtrar por categoría
    fun obtenerRecetasPorCategoria(categoria: String): Flow<List<Receta>> =
        recetaDAO.obtenerPorCategoria(categoria).map { lista ->
            lista.map { it.toDomain() }
        }

    // ✅ Agregar nueva receta con ingredientes
    suspend fun agregarReceta(receta: Receta) {
        // Insertar receta
        val idReceta = recetaDAO.insertar(
            RecetaEntity(
                idReceta = 0, // autoGenerate
                nombreReceta = receta.nombre.trim(),
                descripcionReceta = receta.descripcion.trim(),
                categoriaReceta = receta.categoria.trim(),
                instrucciones = receta.instrucciones.trim(),
                observaciones = receta.observaciones?.trim()
            )
        )

        // Insertar detalles (ingredientes)
        val detalles = receta.ingredientes.map { ing ->
            DetalleRecetaEntity(
                idDetalleReceta = 0, // autoGenerate
                idReceta = idReceta.toInt(),
                idProducto = ing.producto.idProducto,
                cantidaUnidadMedida = ing.cantidad
            )
        }
        detalleDAO.insertarVarios(detalles)
    }

    // ✅ Actualizar receta existente
    suspend fun actualizarReceta(receta: Receta) {
        // Actualizar receta
        recetaDAO.actualizar(
            RecetaEntity(
                idReceta = receta.idReceta,
                nombreReceta = receta.nombre.trim(),
                descripcionReceta = receta.descripcion.trim(),
                categoriaReceta = receta.categoria.trim(),
                instrucciones = receta.instrucciones.trim(),
                observaciones = receta.observaciones?.trim()
            )
        )

        // Eliminar detalles antiguos y agregar los nuevos
        detalleDAO.eliminarPorReceta(receta.idReceta)
        val detalles = receta.ingredientes.map { ing ->
            DetalleRecetaEntity(
                idDetalleReceta = 0, // autoGenerate
                idReceta = receta.idReceta,
                idProducto = ing.producto.idProducto,
                cantidaUnidadMedida = ing.cantidad
            )
        }
        detalleDAO.insertarVarios(detalles)
    }

    // ✅ Eliminar receta (elimina físicamente junto con sus detalles por CASCADE)
    suspend fun eliminarReceta(idReceta: Int) {
        val receta = recetaDAO.obtenerPorId(idReceta)
        if (receta != null) {
            recetaDAO.eliminar(receta)
            // Los detalles se eliminan automáticamente por CASCADE
        }
    }

    // ✅ Inicializar recetas de ejemplo (opcional)
    suspend fun inicializarRecetas() {
        try {
            val existePanFrances = recetaDAO.existeReceta(1)
            if (existePanFrances == 0) {

                // ===== PASO 1: Crear Harina =====
                var idHarina = productoDAO.buscarPorNombre("Harina")?.idProducto
                if (idHarina == null) {
                    idHarina = productoDAO.insertar(
                        ProductoEntity(
                            nombreProducto = "Harina",
                            categoria = "Secos",
                            unidad = "KG"
                        )
                    ).toInt()

                    // Crear inventario para Harina
                    inventarioDAO.insertar(
                        InventarioEntity(
                            idProducto = idHarina,
                            ubicacion = "Almacén Principal",
                            stock = 50.0 // Stock inicial
                        )
                    )
                } else {
                    // Verificar si tiene inventario
                    if (inventarioDAO.obtenerPorProducto(idHarina) == null) {
                        inventarioDAO.insertar(
                            InventarioEntity(
                                idProducto = idHarina,
                                ubicacion = "Almacén Principal",
                                stock = 50.0
                            )
                        )
                    }
                }

                // ===== PASO 2: Crear Azúcar =====
                var idAzucar = productoDAO.buscarPorNombre("Azúcar")?.idProducto
                if (idAzucar == null) {
                    idAzucar = productoDAO.insertar(
                        ProductoEntity(
                            nombreProducto = "Azúcar",
                            categoria = "Secos",
                            unidad = "KG"
                        )
                    ).toInt()

                    // Crear inventario para Azúcar
                    inventarioDAO.insertar(
                        InventarioEntity(
                            idProducto = idAzucar,
                            ubicacion = "Almacén Principal",
                            stock = 25.0 // Stock inicial
                        )
                    )
                } else {
                    // Verificar si tiene inventario
                    if (inventarioDAO.obtenerPorProducto(idAzucar) == null) {
                        inventarioDAO.insertar(
                            InventarioEntity(
                                idProducto = idAzucar,
                                ubicacion = "Almacén Principal",
                                stock = 25.0
                            )
                        )
                    }
                }

                // ===== PASO 3: Crear la Receta =====
                val idReceta = recetaDAO.insertar(
                    RecetaEntity(
                        nombreReceta = "Pan Francés",
                        descripcionReceta = "Pan clásico francés de corteza crujiente",
                        categoriaReceta = "Panaderia",
                        instrucciones = "1. Mezclar ingredientes secos\n2. Agregar agua\n3. Amasar\n4. Fermentar\n5. Hornear",
                        observaciones = null
                    )
                ).toInt()

                // ===== PASO 4: Crear Detalles de la Receta =====
                detalleDAO.insertarVarios(
                    listOf(
                        DetalleRecetaEntity(
                            idReceta = idReceta,
                            idProducto = idHarina,
                            cantidaUnidadMedida = 1.0
                        ),
                        DetalleRecetaEntity(
                            idReceta = idReceta,
                            idProducto = idAzucar,
                            cantidaUnidadMedida = 0.05
                        )
                    )
                )

                Log.d("InicializarRecetas", "Receta 'Pan Francés' inicializada correctamente")
            }
        } catch (e: Exception) {
            Log.e("InicializarRecetas", "Error al inicializar recetas: ${e.message}", e)
        }
    }
}