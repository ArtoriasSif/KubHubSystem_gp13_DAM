package com.example.kubhubsystem_gp13_dam.model

import java.time.LocalDateTime

data class Pedido(
    val idPedido: Int = 0,
    val fechaInicioRango: LocalDateTime,
    val fechaFinRango: LocalDateTime,
    val fechaCreacion: LocalDateTime = LocalDateTime.now(),
    val estadoPedido: EstadoPedido,
    val solicitudes: List<Solicitud> = emptyList(),
    val aglomerado: List<AglomeradoPedido> = emptyList(),
    val estaActivo: Boolean = true
)

data class AglomeradoPedido(
    val idAglomerado: Int = 0,
    val producto: Producto,
    val cantidadTotal: Double,
    val asignatura: Asignatura? = null // Opcional para filtrar
)

enum class EstadoPedido(val displayName: String, val orden: Int) {
    EN_PROCESO("En Proceso", 1),
    PENDIENTE_REVISION("Pendiente a Revisión", 2),
    CHECK_INVENTARIO("Check de Inventario", 3),
    ENVIADO_COTIZACION("Enviado a Cotización", 4);

    companion object {
        fun desdeNombre(nombre: String): EstadoPedido? {
            return values().find { it.name == nombre }
        }
    }
}