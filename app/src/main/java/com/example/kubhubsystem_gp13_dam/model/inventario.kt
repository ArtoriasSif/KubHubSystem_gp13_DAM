package com.example.kubhubsystem_gp13_dam.model

import com.google.gson.annotations.SerializedName


data class InventoryWithProductCreateDTO(
    @SerializedName("idInventario")
    val idInventario: Int,

    @SerializedName("idProducto")
    val idProducto: Int,

    @SerializedName("nombreProducto")
    val nombreProducto: String? = null,

    @SerializedName("descripcionProducto")
    val descripcionProducto: String ,

    @SerializedName("nombreCategoria")
    val nombreCategoria: String? = null,

    @SerializedName("unidadMedida")
    val unidadMedida: String? = null,

    @SerializedName("stock")
    val stock: Double? = null,

    @SerializedName("stockLimitMin")
    val stockLimitMin: Double? = null
)

/**
 * Data Transfer Object (DTO) para LISTAR o ACTUALIZAR un item de inventario.
 *
 * ✨ SINCRONIZADO CON InventoryWithProductCreateUpdateDTO.java (11-Nov-2025) ✨
 * Basado en el DTO de Spring con 8 campos.
 */
data class InventoryWithProductResponseAnswerUpdateDTO(

    @SerializedName("idInventario")
    val idInventario: Int? = null,

    @SerializedName("idProducto")
    val idProducto: Int? = null,

    @SerializedName("nombreProducto")
    val nombreProducto: String? = null,

    @SerializedName("descripcionProducto")
    val descripcionProducto: String?,

    @SerializedName("nombreCategoria")
    val nombreCategoria: String? = null,

    @SerializedName("unidadMedida")
    val unidadMedida: String? = null,

    @SerializedName("stock")
    val stock: Double? = null,

    @SerializedName("stockLimitMin")
    val stockLimitMin: Double? = null,

    @SerializedName("estadoStock")
    val estadoStock: String
)


/**
 * Representa la entidad Producto, mapeada desde la base de datos.
 *
 * NOTA: Los tipos se definen según las restricciones de nulidad (@Column(nullable = false))
 */
data class ProductoEntityDTO(
    // Clave primaria, generada automáticamente.
    val idProducto: Int,

    // Código de producto, único. Puede ser nulo en algunos contextos si se está creando.
    val codProducto: String? = null,

    // Descripción del producto. Es opcional (puede ser nulo en la BD).
    val descripcionProducto: String? = null,

    // Nombre del producto. No puede ser nulo (@Column(nullable = false)).
    val nombreProducto: String,

    // Nombre de la categoría. No puede ser nulo (@Column(nullable = false)).
    val nombreCategoria: String,

    // Unidad de medida. No puede ser nulo (@Column(nullable = false)).
    val unidadMedida: String,

    // Estado activo/inactivo. Por defecto es true y no puede ser nulo.
    val activo: Boolean = true,

    // Array de bytes para la foto. Es opcional (puede ser nulo en la BD).
    val fotoProducto: ByteArray? = null
)


// Modelo que usa el diálogo — mutable o inmutable según prefieras
data class InventoryForm(
    val idInventario: Int? = null,
    val idProducto: Int? = null,
    val nombreProducto: String? = null,
    val descripcionProducto: String = "",
    val nombreCategoria: String? = null,
    val unidadMedida: String? = null,
    val stock: Double? = null,
    val stockLimitMin: Double? = null
)

// mapear para crear (tu DTO Create tiene id non-null Int, así que usamos 0 cuando UI trae null)
fun InventoryForm.toCreateDTO(): InventoryWithProductCreateDTO {
    return InventoryWithProductCreateDTO(
        idInventario = this.idInventario ?: 0,
        idProducto = this.idProducto ?: 0,
        nombreProducto = this.nombreProducto,
        descripcionProducto = this.descripcionProducto,
        nombreCategoria = this.nombreCategoria,
        unidadMedida = this.unidadMedida,
        stock = this.stock,
        stockLimitMin = this.stockLimitMin
    )
}

// mapear para actualizar (usa el DTO de respuesta/update)
fun InventoryForm.toUpdateDTO(estadoStock: String): InventoryWithProductResponseAnswerUpdateDTO {
    return InventoryWithProductResponseAnswerUpdateDTO(
        idInventario = this.idInventario,
        idProducto = this.idProducto,
        nombreProducto = this.nombreProducto,
        descripcionProducto = this.descripcionProducto,
        nombreCategoria = this.nombreCategoria,
        unidadMedida = this.unidadMedida,
        stock = this.stock,
        stockLimitMin = this.stockLimitMin,
        estadoStock = estadoStock
    )
}
