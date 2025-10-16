package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_receta",
    foreignKeys = [
        ForeignKey(
            entity = RecetaEntity::class,
            parentColumns = ["idReceta"],
            childColumns = ["idReceta"],
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
        Index(value = ["idReceta"]),
        Index(value = ["idProducto"])
    ]
)
data class DetalleRecetaEntity(
    @PrimaryKey(autoGenerate = true) val idDetalleReceta: Int = 0,
    val idReceta: Int,
    val idProducto: Int,
    val cantidaUnidadMedida: Double
)