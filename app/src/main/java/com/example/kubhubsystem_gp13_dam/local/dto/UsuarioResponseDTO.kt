/*

package com.example.kubhubsystem_gp13_dam.local.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para respuesta de Usuario
 * NO incluye la contraseña por seguridad
 * Mapea exactamente con UsuarioResponseDTO.java del backend
 */
data class UsuarioResponseDTO(
    @SerializedName("idUsuario")
    val idUsuario: Int,
    
    @SerializedName("idRol")
    val idRol: Int,
    
    @SerializedName("nombreRol")
    val nombreRol: String, // Viene convertido del backend (ej: "Administrador")
    
    @SerializedName("primerNombre")
    val primerNombre: String?,
    
    @SerializedName("segundoNombre")
    val segundoNombre: String?,
    
    @SerializedName("apellidoPaterno")
    val apellidoPaterno: String?,
    
    @SerializedName("apellidoMaterno")
    val apellidoMaterno: String?,
    
    @SerializedName("nombreCompleto")
    val nombreCompleto: String,
    
    @SerializedName("email")
    val email: String,

    @SerializedName("contrasena")
    val contrasena: String,

    @SerializedName("username")
    val username: String?,
    
    @SerializedName("fotoPerfil")
    val fotoPerfil: String?, // TODO: Implementar cuando se requiera manejo de imágenes
    
    @SerializedName("activo")
    val activo: Boolean,
    
    @SerializedName("fechaCreacion")
    val fechaCreacion: String?, // ISO 8601 format - ignorado por ahora
    
    @SerializedName("ultimoAcceso")
    val ultimoAcceso: String? // ISO 8601 format - ignorado por ahora
)**/