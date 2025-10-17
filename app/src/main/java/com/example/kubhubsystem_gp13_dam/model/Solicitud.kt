package com.example.kubhubsystem_gp13_dam.model

import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
/*
data class Solicitud(
    val idSolicitud: Int = 0,
    val asignatura: Asignatura,
    val seccion: Seccion,
    val profesor: String,
    val fechaSolicitud: LocalDateTime = LocalDateTime.now(),
    val fechaClase: LocalDateTime,
    val productos: List<ProductoSolicitado> = emptyList(),
    val recetaBase: Receta? = null,
    val observaciones: String = "",
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE
)
*/
data class ProductoSolicitado(
    val idProductoSolicitado: Int = 0,
    val producto: Producto,
    val cantidadSolicitada: Double,
    val unidad: String
)

enum class EstadoSolicitud(val displayName: String, val color: Long) {
    PENDIENTE("Pendiente", 0xFFFFC107),
    APROBADO("Aprobado", 0xFFFF9800),
    ENTREGADO("Entregado", 0xFF4CAF50),
    RECHAZADO("Rechazado", 0xFFF44336)
}

// Extensión para formatear fechas
fun LocalDateTime.formatear(): String {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, hh:mm a")
    return this.format(formatter)
}