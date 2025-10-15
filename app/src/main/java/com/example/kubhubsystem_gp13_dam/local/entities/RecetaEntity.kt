package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receta")
data class RecetaEntity(
    @PrimaryKey(autoGenerate = true) val idReceta: Int = 0,
    val nombreReceta: String,
    val descripcionReceta: String
)