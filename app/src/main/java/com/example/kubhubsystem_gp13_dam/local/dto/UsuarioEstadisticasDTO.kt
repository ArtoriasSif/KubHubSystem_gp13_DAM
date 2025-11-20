package com.example.kubhubsystem_gp13_dam.local.dto
/**
import com.google.gson.annotations.SerializedName

/**
 * DTO para estadísticas de usuarios
 * Mapea exactamente con UsuarioEstadisticasDTO.java del backend
 */
data class UsuarioEstadisticasDTO(
    @SerializedName("totalUsuarios")
    val totalUsuarios: Long,
    
    @SerializedName("usuariosActivos")
    val usuariosActivos: Long,
    
    @SerializedName("usuariosInactivos")
    val usuariosInactivos: Long,
    
    @SerializedName("totalRoles")
    val totalRoles: Long
)*/