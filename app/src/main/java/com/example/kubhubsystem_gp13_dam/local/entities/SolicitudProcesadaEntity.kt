package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "solicitud_procesada")
data class SolicitudProcesadaEntity(
    @PrimaryKey(autoGenerate = true) val idSolicitudProcesada: Int = 0,
    val idSolicitud: Int,
    val idPedidoProcesado : Int,
    val idUsuario: Int,
    val idSeccion: Int,
    val fechaProceso: LocalDateTime,
    val fechaSolicitud: LocalDateTime,
    val cantidaPersonas: Int
)