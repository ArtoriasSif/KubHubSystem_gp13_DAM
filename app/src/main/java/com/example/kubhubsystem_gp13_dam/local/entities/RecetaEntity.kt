package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// ✅ RESPETANDO LA REGLA #1: Entity original SIN modificaciones
@Entity(tableName = "receta")
data class RecetaEntity(
    @PrimaryKey(autoGenerate = true) val idReceta: Int = 0,
    val nombreReceta: String,
    val descripcionReceta: String,
    val categoriaReceta: String,
    val instrucciones: String,
    val observaciones: String? = null
)