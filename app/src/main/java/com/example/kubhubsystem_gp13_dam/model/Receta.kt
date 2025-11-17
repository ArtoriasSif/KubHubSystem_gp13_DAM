package com.example.kubhubsystem_gp13_dam.ui.model

import com.example.kubhubsystem_gp13_dam.model.Producto
import com.google.gson.annotations.SerializedName


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


data class RecipeWithDetailsCreateDTO(

    @SerializedName("nombreReceta")
    val nombreReceta: String? = null,

    @SerializedName("descripcionReceta")
    val descripcionReceta: String? = null,

    @SerializedName("listaItems")
    val listaItems: List<RecipeItemDTO>? = null,

    @SerializedName("instrucciones")
    val instrucciones: String? = null,

    @SerializedName("estadoReceta")
    val estadoReceta: EstadoRecetaType? = null
)

data class RecipeWithDetailsAnswerUpdateDTO(

    @SerializedName("idReceta")
    val idReceta: Int? = null,

    @SerializedName("nombreReceta")
    val nombreReceta: String? = null,

    @SerializedName("descripcionReceta")
    val descripcionReceta: String? = null,

    @SerializedName("listaItems")
    val listaItems: List<RecipeItemDTO>? = null,

    @SerializedName("instrucciones")
    val instrucciones: String? = null,

    @SerializedName("estadoReceta")
    val estadoReceta: EstadoRecetaType? = null,

    @SerializedName("cambioReceta")
    val cambioReceta: Boolean = false,

    @SerializedName("cambioDetalles")
    val cambioDetalles: Boolean = false
)

data class RecipeItemDTO(

    @SerializedName("idProducto")
    val idProducto: Int? = null,

    @SerializedName("nombreProducto")
    val nombreProducto: String? = null,

    @SerializedName("unidadMedida")
    val unidadMedida: String? = null,

    @SerializedName("cantUnidadMedida")
    val cantUnidadMedida: Double? = null,

    @SerializedName("activo")
    val activo: Boolean? = null
)

enum class EstadoRecetaType {
    @SerializedName("ACTIVO")
    ACTIVO,
    @SerializedName("INACTIVO")
    INACTIVO
}

// 🔥 OPTIMIZACIÓN: Data class para estadísticas (inmutable)
data class RecetasStats(
    val total: Int,
    val activas: Int,
    val inactivas: Int
)







