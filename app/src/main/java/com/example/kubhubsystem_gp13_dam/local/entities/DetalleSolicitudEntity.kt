package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.PrimaryKey

data class DetalleSolicitudEntity(
    @PrimaryKey(autoGenerate = true) val idDetalleSolicitud: Int = 0,
    val idSolicitud: Int,
    val idProducto: Int,
    val cantidaUnidadMedida: Double
)