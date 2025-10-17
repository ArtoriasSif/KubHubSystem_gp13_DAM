package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reserva_sala",
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
data class ReservaSalaEntity(
    @PrimaryKey(autoGenerate = true) val idReservaSala: Int = 0,
    val idSeccion : Int,
    val idSala : Int,
    val codigoTaller: String,
    val bloque: Int,
    val diaSemana: String
)