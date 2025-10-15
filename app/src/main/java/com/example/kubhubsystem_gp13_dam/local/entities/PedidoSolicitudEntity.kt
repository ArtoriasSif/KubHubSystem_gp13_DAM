package com.example.kubhubsystem_gp13_dam.local.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import java.time.LocalDateTime

@Entity(
    tableName = "pedido_solicitud",
    primaryKeys = ["idPedido", "idSolicitud"],
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["idPedido"],
            childColumns = ["idPedido"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SolicitudEntity::class,
            parentColumns = ["idSolicitud"],
            childColumns = ["idSolicitud"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PedidoSolicitudEntity(
    val idPedido: Int = 0,
    val idSolicitud: Int,
    val fechaSolicitudPlanificada : LocalDateTime
)