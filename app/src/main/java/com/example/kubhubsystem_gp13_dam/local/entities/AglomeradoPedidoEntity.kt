package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "aglomerado_pedido",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["idPedido"],
            childColumns = ["idPedido"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["idProducto"],
            childColumns = ["idProducto"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["idPedido"]),
        Index(value = ["idProducto"])
    ]
)
data class AglomeradoPedidoEntity(
    @PrimaryKey(autoGenerate = true) val idAglomerado: Int = 0,
    val idPedido: Int,
    val idProducto: Int,
    val cantidadTotal: Double,
    val idAsignatura: Int? = null // Para poder filtrar por asignatura
)