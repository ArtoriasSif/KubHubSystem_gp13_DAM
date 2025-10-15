package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "pedido_procesado")
data class PedidoProcesadoEntity(
    @PrimaryKey(autoGenerate = true) val idPedidoProcesado: Int = 0,
    val idPedido: Int ,
    val fechaInicioRango: LocalDateTime,
    val fechaFinRango: LocalDateTime,
    val fechaProcesoPedido: LocalDateTime
)