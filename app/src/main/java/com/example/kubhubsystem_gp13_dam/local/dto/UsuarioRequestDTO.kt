package com.example.kubhubsystem_gp13_dam.local.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para crear un nuevo Usuario
 * Mapea exactamente con UsuarioRequestDTO.java del backend
 */
data class UsuarioRequestDTO(
    @SerializedName("idRol")
    val idRol: Int,
    
    @SerializedName("primerNombre")
    val primerNombre: String,
    
    @SerializedName("segundoNombre")
    val segundoNombre: String? = null,
    
    @SerializedName("apellidoPaterno")
    val apellidoPaterno: String? = null,
    
    @SerializedName("apellidoMaterno")
    val apellidoMaterno: String? = null,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("contrasena")
    val contrasena: String,
    
    @SerializedName("fotoPerfil")
    val fotoPerfil: String? = null, // TODO: Implementar cuando se requiera
    
    @SerializedName("activo")
    val activo: Boolean = true
)