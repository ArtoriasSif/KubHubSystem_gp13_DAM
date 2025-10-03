package com.example.kubhubsystem_gp13_dam.model

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val stock: Int,
    val unidad: String,
    val estado: EstadoProducto
)

enum class EstadoProducto {
    DISPONIBLE,
    AGOTADO,
    BAJO_STOCK
}

enum class CategoriaProducto(val displayName: String) {
    TODOS("Todas las categorías"),
    SECOS("Secos"),
    LIQUIDOS("Líquidos"),
    LACTEOS("Lácteos"),
    FRESCOS("Frescos")
}