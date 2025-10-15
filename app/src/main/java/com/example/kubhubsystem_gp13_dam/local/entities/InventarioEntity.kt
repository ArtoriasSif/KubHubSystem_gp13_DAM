package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventario",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["idProducto"],
            childColumns = ["idProducto"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InventarioEntity(
    @PrimaryKey(autoGenerate = true) val idInventario: Int = 0,
    val idProducto   : Int, //foranea
    val ubicacion    : String,
    val stock        : Double
)