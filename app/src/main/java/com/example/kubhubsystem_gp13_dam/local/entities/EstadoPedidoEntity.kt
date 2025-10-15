package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estado_pedido")
data class EstadoPedidoEntity(
    @PrimaryKey(autoGenerate = true) val idEstadoPedido: Int = 0,
    val tipoEstado : String
)