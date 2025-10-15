package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sala_seccion",
    foreignKeys = [
        ForeignKey(
            entity = SeccionEntity::class,
            parentColumns = ["idSeccion"],
            childColumns = ["idSeccion"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
//no esta de todo claro los atributos
data class SalaSeccionEntity(
    @PrimaryKey(autoGenerate = true) val idSalaSeccion: Int = 0,
    val idSeccion : Int,
    val codigoTaller: String,
    val bloque: Int,
    val dia: String //formato fecha?
)