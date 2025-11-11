package com.example.kubhubsystem_gp13_dam.model

import com.google.gson.annotations.SerializedName

data class Inventario(
    val idInventario    : Int,
    val idProducto      : Int,
    val nombreProducto  : String,
    val ubicacion       : String,
    val stock           : Double,
    val estado          : EstadoInventario
)

enum class EstadoInventario {
    DISPONIBLE,
    AGOTADO,
    BAJO_STOCK
}




data class InventoryWithProductoResponseDTO(
    @SerializedName("idInventario")
    val idInventario: Int,

    @SerializedName("idProducto")
    val idProducto: Int,

    @SerializedName("nombreProducto")
    val nombreProducto: String,

    @SerializedName("nombreCategoria")
    val nombreCategoria: String,

    @SerializedName("stock")
    val stock: Double,

    @SerializedName("stockLimitMin")
    val stockMinimo: Double,

    @SerializedName("unidadMedida")
    val unidadMedida: String,

    @SerializedName("estadoStock")
    val estadoStock: String
)

/**
 * Data Transfer Object (DTO) para CREAR o ACTUALIZAR un item de inventario.
 *
 * ✨ SINCRONIZADO CON InventoryWithProductCreateUpdateDTO.java (11-Nov-2025) ✨
 * Basado en el DTO de Spring con 8 campos.
 */
data class InventoryWithProductCreateUpdateDTO(

    @SerializedName("idInventario")
    val idInventario: Int? = null, // ← Nulable para creación

    @SerializedName("idProducto")
    val idProducto: Int? = null,

    @SerializedName("nombreProducto")
    val nombreProducto: String? = null, // Nulable (es 'String' en Java)

    @SerializedName("descripcionProducto")
    val descripcionProducto: String? = "Sin descripción",

    @SerializedName("nombreCategoria")
    val nombreCategoria: String? = null, // Nulable (es 'String' en Java)

    @SerializedName("unidadMedida")
    val unidadMedida: String? = null, // Nulable (es 'String' en Java)

    @SerializedName("stock")
    val stock: Double? = null, // Nulable (es 'Double' en Java)

    @SerializedName("stockMinimo")
    val stockMinimo: Double? = null // Nulable (es 'Double' en Java)
)