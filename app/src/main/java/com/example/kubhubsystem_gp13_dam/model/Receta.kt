package com.example.kubhubsystem_gp13_dam.ui.model

import com.example.kubhubsystem_gp13_dam.model.Asignatura
import com.example.kubhubsystem_gp13_dam.model.Producto


data class Receta(
    val idReceta: Int = 0,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val instrucciones: String,
    val observaciones: String? = null,
    val ingredientes: List<IngredienteReceta> = emptyList() // ✅ Se construye desde DetalleRecetaEntity
)

// ✅ Modelo de DOMINIO para representar un ingrediente
// Se construye desde DetalleRecetaEntity + ProductoEntity
data class IngredienteReceta(
    val idDetalle: Int = 0,        // ✅ Mapea desde: idDetalleReceta
    val producto: Producto,         // ✅ Objeto completo del producto
    val cantidad: Double            // ✅ Mapea desde: cantidaUnidadMedida
)