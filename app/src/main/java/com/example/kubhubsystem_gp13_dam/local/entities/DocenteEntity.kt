package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "docente",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["idUsuario"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DocenteEntity(
    @PrimaryKey(autoGenerate = true) val idDocente: Int = 0,
    val idUsuario: Int, // Relación con Usuario
    val seccionesIds: List<Int> // ✅ Aquí se guardan las IDs de secciones
)