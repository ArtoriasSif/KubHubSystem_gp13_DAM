package com.example.kubhubsystem_gp13_dam.local.dto
/**
import com.google.gson.annotations.SerializedName

/**
 * DTO para actualizar un Usuario existente
 * La contraseña es opcional en actualizaciones
 * Mapea exactamente con UsuarioUpdateDTO.java del backend
 */
data class UsuarioUpdateDTO(
    @SerializedName("idRol")
    val idRol: Int? = null,
    
    @SerializedName("primerNombre")
    val primerNombre: String? = null,
    
    @SerializedName("segundoNombre")
    val segundoNombre: String? = null,
    
    @SerializedName("apellidoPaterno")
    val apellidoPaterno: String? = null,
    
    @SerializedName("apellidoMaterno")
    val apellidoMaterno: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("contrasena")
    val contrasena: String? = null,
    
    @SerializedName("fotoPerfil")
    val fotoPerfil: String? = null, // TODO: Implementar cuando se requiera
    
    @SerializedName("activo")
    val activo: Boolean? = null
)*/