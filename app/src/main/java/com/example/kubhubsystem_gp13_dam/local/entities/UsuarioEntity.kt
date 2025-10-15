package com.example.kubhubsystem_gp13_dam.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuario",
    foreignKeys = [
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["idRol"],
            childColumns = ["idRol"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val idUsuario: Int = 0,
    val idRol: Int,
    val primeroNombre : String,
    val segundoNombre: String,
    val apellidoMaterno: String,
    val apellidoPaterno: String,
    val email: String,
    val username: String,
    val password: String,
)