package com.example.kubhubsystem_gp13_dam.model

data class DetalleConProducto(
    val idDetalleReceta: Int,
    val idReceta: Int,
    val idProducto: Int,
    val cantidaUnidadMedida: Double,
    val nombreProducto: String,
    val unidad: String,
    val categoria: String
)