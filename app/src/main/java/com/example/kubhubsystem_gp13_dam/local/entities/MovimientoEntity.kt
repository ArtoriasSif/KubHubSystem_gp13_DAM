package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "movimiento",
    foreignKeys = [
        ForeignKey(
            entity = InventarioEntity::class,
            parentColumns = ["idInventario"],
            childColumns = ["idInventario"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val idMovimiento: Int = 0,
    val idInventario: Int,
    val fechaMovimiento: LocalDateTime,
    val cantidadeMovimiento: Double,
    val tipoMovimiento: String
)