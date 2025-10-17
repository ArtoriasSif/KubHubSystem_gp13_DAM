package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sala")
data class SalaEntity(
    @PrimaryKey(autoGenerate = true) val idSala: Int = 0,
    val codigoSala: String
)