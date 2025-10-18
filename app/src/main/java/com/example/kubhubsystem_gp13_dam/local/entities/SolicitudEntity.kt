package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "solicitud")
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val idSolicitud: Int = 0,
    val idUsuario: Int, // Gestor de pedidos que crea la solicitud
    val idSeccion: Int,
    val idReservaSala: Int, // Para obtener sala, día, bloque
    val cantidadPersonas: Int,
    val estadoSolicitud: String = "Pendiente", // Pendiente/Aprobado/Rechazado
    val fechaSolicitudPlanificada: LocalDateTime, // Fecha para la que es la solicitud (día de clase)
    val fechaCreacion: LocalDateTime = LocalDateTime.now() // Cuando se creó
)