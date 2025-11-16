package com.example.kubhubsystem_gp13_dam.local.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para petición de login
 * Mapea exactamente con LoginRequestDTO.java del backend
 */
data class LoginRequestDTO(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("contrasena")
    val contrasena: String
)