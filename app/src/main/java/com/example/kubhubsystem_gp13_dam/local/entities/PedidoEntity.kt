package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "pedido",
    foreignKeys = [
        ForeignKey(
            entity = EstadoPedidoEntity::class,
            parentColumns = ["idEstadoPedido"],
            childColumns = ["idEstadoPedido"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val idPedido: Int = 0,
    val fechaInicioRango: LocalDateTime,
    val fechaFinRango: LocalDateTime,
    val fechaCreacion: LocalDateTime = LocalDateTime.now(),
    val idEstadoPedido: Int, // FK a EstadoPedidoEntity
    val estaActivo: Boolean = true // Para saber si es el pedido actual o histórico
)