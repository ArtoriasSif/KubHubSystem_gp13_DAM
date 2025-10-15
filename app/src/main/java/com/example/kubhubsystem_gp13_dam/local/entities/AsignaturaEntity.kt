package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignatura")
data class AsignaturaEntity(
    @PrimaryKey(autoGenerate = true) val idAsignatura: Int = 0,
    val nombreAsignatura: String
)