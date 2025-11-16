package com.example.kubhubsystem_gp13_dam.model

import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioResponseDTO


/**
 * Modelo de dominio para Usuario
 * ✅ ACTUALIZADO: Ahora mapea desde UsuarioResponseDTO del backend
 *
 * Este modelo se usa internamente en la app.
 * Los DTOs se usan para comunicación con el backend.
 */
data class Usuario(
    val idUsuario: Int = 0,
    val rol: Rol,
    val primerNombre: String,
    val segundoNombre: String? = null,
    val apellidoPaterno: String? = null,
    val apellidoMaterno: String? = null,
    val email: String,
    val username: String? = null,
    val password: String = "", // Solo se usa al crear/actualizar, nunca viene del backend
    val fotoPerfil: String? = null, // TODO: Implementar cuando se requiera
    val activo: Boolean = true
) {
    /**
     * Obtiene el nombre completo del usuario
     * Replicando la lógica del backend
     */
    fun obtenerNombreCompleto(): String {
        return buildString {
            if (primerNombre.isNotBlank()) append(primerNombre).append(" ")
            if (!segundoNombre.isNullOrBlank()) append(segundoNombre).append(" ")
            if (!apellidoPaterno.isNullOrBlank()) append(apellidoPaterno).append(" ")
            if (!apellidoMaterno.isNullOrBlank()) append(apellidoMaterno)
        }.trim()
    }

    companion object {
        /**
         * Convierte un UsuarioResponseDTO (del backend) a Usuario (modelo de dominio)
         */
        fun desdeDTO(dto: UsuarioResponseDTO): Usuario {
            return Usuario(
                idUsuario = dto.idUsuario,
                rol = Rol.desdeId(dto.idRol) ?: Rol.DOCENTE, // Fallback a DOCENTE si no encuentra
                primerNombre = dto.primerNombre ?: "",
                segundoNombre = dto.segundoNombre,
                apellidoPaterno = dto.apellidoPaterno,
                apellidoMaterno = dto.apellidoMaterno,
                email = dto.email,
                username = dto.username,
                password = "", // Nunca viene del backend por seguridad
                fotoPerfil = dto.fotoPerfil,
                activo = dto.activo
            )
        }

        /**
         * Convierte una lista de DTOs a lista de Usuarios
         */
        fun desdeDTOs(dtos: List<UsuarioResponseDTO>): List<Usuario> {
            return dtos.map { desdeDTO(it) }
        }
    }
}