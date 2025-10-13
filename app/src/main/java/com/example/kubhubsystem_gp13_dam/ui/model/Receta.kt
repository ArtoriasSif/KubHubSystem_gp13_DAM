package com.example.kubhubsystem_gp13_dam.ui.model

import com.example.kubhubsystem_gp13_dam.model.Asignatura
import com.example.kubhubsystem_gp13_dam.model.Producto


data class Receta(
    val idReceta: Int = 0,
    val nombre: String,
    val descripcion: String,
    val categoria: CategoriaReceta,
    val asignaturaRelacionada: Asignatura?,
    val ingredientes: List<IngredienteReceta> = emptyList(),
    val instrucciones: String,
    val tiempoPreparacion: Int = 0, // en minutos
    val porciones: Int = 1,
    val estaActiva: Boolean = true
)

data class IngredienteReceta(
    val idIngrediente: Int = 0,
    val producto: Producto,  // Referencia al producto del inventario
    val cantidad: Double,
    val unidad: String  // kg, l, unidades, etc.
)

enum class CategoriaReceta(val displayName: String) {
    PANADERIA("Panadería"),
    PASTELERIA("Pastelería"),
    COCINA_CALIENTE("Cocina Caliente"),
    COCINA_FRIA("Cocina Fría"),
    REPOSTERIA("Repostería"),
    POSTRES("Postres"),
    BEBIDAS("Bebidas"),
    SALSAS("Salsas y Bases"),
    OTROS("Otros")
}