package com.example.kubhubsystem_gp13_dam.local.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para crear o actualizar un Rol
 * Mapea exactamente con RolRequestDTO.java del backend
 */
data class RolRequestDTO(
    @SerializedName("nombreRol")
    val nombreRol: String,
    
    @SerializedName("activo")
    val activo: Boolean = true
)