package com.example.kubhubsystem_gp13_dam.model

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