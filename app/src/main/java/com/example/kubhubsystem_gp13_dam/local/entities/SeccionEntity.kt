package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "seccion",
    foreignKeys = [
        ForeignKey(
            entity = AsignaturaEntity::class,
            parentColumns = ["idAsignatura"],
            childColumns = ["idAsignatura"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SeccionEntity(
    @PrimaryKey(autoGenerate = true)
    val idSeccion: Int = 0,
    val idAsignatura: Int,
    val nombreSeccion: String,
    val idDocente: Int? = null
)