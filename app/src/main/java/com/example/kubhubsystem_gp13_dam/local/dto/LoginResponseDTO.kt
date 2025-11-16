package com.example.kubhubsystem_gp13_dam.local.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para respuesta de login exitoso
 * Mapea exactamente con LoginResponseDTO.java del backend
 */
data class LoginResponseDTO(
    @SerializedName("usuario")
    val usuario: UsuarioResponseDTO,
    
    @SerializedName("token")
    val token: String,
    
    @SerializedName("mensaje")
    val mensaje: String
)