package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "solicitud")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val idSolicitud: Int = 0,
    val idUsuario: Int,
    val idSeccion: Int,
    val cantidaPersonas: Int
    // Las fechas se asignata a tabla intermedia Pedido solicitud
)