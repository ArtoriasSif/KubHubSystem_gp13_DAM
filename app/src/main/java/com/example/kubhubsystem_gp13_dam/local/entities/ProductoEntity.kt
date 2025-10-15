package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

//La entity no hace falta el estado, dado que se calcula auto por metodo, por el stock para visualizar en la app
@Entity(tableName = "producto")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val idProducto: Int = 0,
    val nombreProducto: String,
    val categoria: String,
    val unidad: String
)