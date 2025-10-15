package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.model.CategoriaReceta
import com.example.kubhubsystem_gp13_dam.ui.model.IngredienteReceta
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecetaRepository {

    private val _recetas = MutableStateFlow<List<Receta>>(
        listOf(
            Receta(
                idReceta = 1,
                nombre = "Pan Francés",
                descripcion = "Pan clásico francés de corteza crujiente",
                categoria = CategoriaReceta.PANADERIA,
                asignaturaRelacionada = null,
                ingredientes = listOf(
                    IngredienteReceta(
                        idIngrediente = 1,
                        producto = Producto(
                            idProducto = 1,
                            nombreProducto = "Harina",
                            categoria = "Secos",
                            unidadMedida = "kg"
                        ),
                        cantidad = 1.0,
                        unidad = "kg"
                    ),
                    IngredienteReceta(
                        idIngrediente = 2,
                        producto = Producto(
                            idProducto = 3,
                            nombreProducto = "Azúcar",
                            categoria = "Secos",
                            unidadMedida = "kg"
                        ),
                        cantidad = 0.05,
                        unidad = "kg"
                    )
                ),
                instrucciones = "1. Mezclar ingredientes secos\n2. Agregar agua\n3. Amasar\n4. Fermentar\n5. Hornear",
                tiempoPreparacion = 180,
                porciones = 4,
                estaActiva = true
            ),
            Receta(
                idReceta = 2,
                nombre = "Croissant",
                descripcion = "Masa hojaldrada francesa",
                categoria = CategoriaReceta.PASTELERIA,
                asignaturaRelacionada = null,
                ingredientes = listOf(
                    IngredienteReceta(
                        idIngrediente = 3,
                        producto = Producto(
                            idProducto = 1,
                            nombreProducto = "Harina",
                            categoria = "Secos",
                            unidadMedida = "kg"
                        ),
                        cantidad = 0.5,
                        unidad = "kg"
                    )
                ),
                instrucciones = "1. Preparar masa\n2. Laminar\n3. Plegar\n4. Formar\n5. Hornear",
                tiempoPreparacion = 240,
                porciones = 12,
                estaActiva = true
            )
        )
    )

    val recetas: StateFlow<List<Receta>> = _recetas.asStateFlow()

    fun agregarReceta(receta: Receta) {
        val nuevoId = (_recetas.value.maxOfOrNull { it.idReceta } ?: 0) + 1
        val nuevaReceta = receta.copy(idReceta = nuevoId)
        _recetas.value = _recetas.value + nuevaReceta
    }

    fun actualizarReceta(receta: Receta) {
        _recetas.value = _recetas.value.map {
            if (it.idReceta == receta.idReceta) receta else it
        }
    }

    fun eliminarReceta(idReceta: Int) {
        _recetas.value = _recetas.value.filter { it.idReceta != idReceta }
    }

    fun getRecetaById(idReceta: Int): Receta? {
        return _recetas.value.find { it.idReceta == idReceta }
    }

    fun getRecetasPorCategoria(categoria: CategoriaReceta): List<Receta> {
        return _recetas.value.filter { it.categoria == categoria }
    }

    fun getRecetasPorAsignatura(idAsignatura: Int): List<Receta> {
        return _recetas.value.filter { it.asignaturaRelacionada?.idRamo == idAsignatura }
    }

    companion object {
        @Volatile
        private var instance: RecetaRepository? = null

        fun getInstance(): RecetaRepository {
            return instance ?: synchronized(this) {
                instance ?: RecetaRepository().also { instance = it }
            }
        }
    }
}