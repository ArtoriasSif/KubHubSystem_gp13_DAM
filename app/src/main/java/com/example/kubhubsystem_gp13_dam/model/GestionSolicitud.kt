package com.example.kubhubsystem_gp13_dam.model

import java.time.LocalDateTime

data class Solicitud(
    val idSolicitud: Int = 0,
    val detalleSolicitud: List<DetalleSolicitud> = emptyList(),
    val gestorPedidos: Usuario,
    val seccion: Seccion,
    val docenteSeccion: Usuario,
    val reservaSala: ReservaSala,
    val cantidadPersonas: Int,
    val fechaSolicitud: LocalDateTime,                      // Fecha de la solicitud
    val fechaCreacion: LocalDateTime = LocalDateTime.now()  // Momento actual del proceso
)

data class DetalleSolicitud(
    val idDetalleSolicitud: Int = 0,
    val idSolicitud: Int,
    val producto: Producto,
    val cantidadUnidadMedida: Double
)