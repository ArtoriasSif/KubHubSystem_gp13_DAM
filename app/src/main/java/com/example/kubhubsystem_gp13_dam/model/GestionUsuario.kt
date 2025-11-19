package com.example.kubhubsystem_gp13_dam.model

import android.util.Patterns
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

/**
 * ===============================================
 * DTOs PARA AUTENTICACIÓN (LOGIN)
 * ===============================================
 */

/**
 * DTO para petición de login
 * Enviado al backend en POST /api/v1/auth/login
 */
data class LoginRequestDTO(
    @SerializedName("email")
    val email: String,

    @SerializedName("contrasena")
    val contrasena: String
) {
    /**
     * Función de validación simple
     */
    fun isValid(): Boolean {
        val emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val passwordValid = contrasena.isNotBlank() && contrasena.length >= 6
        return emailValid && passwordValid
    }
}

/**
 * ===============================================
 * DTOs PARA GESTIÓN DE USUARIOS
 * ===============================================
 */

/**
 * DTO para crear un nuevo usuario
 * POST /api/v1/usuarios
 */
data class UsuarioRequestDTO(
    @SerializedName("idRol")
    val idRol: Int,

    @SerializedName("primerNombre")
    val primerNombre: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("contrasena")
    val contrasena: String,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("segundoNombre")
    val segundoNombre: String? = null,

    @SerializedName("apellidoPaterno")
    val apellidoPaterno: String? = null,

    @SerializedName("apellidoMaterno")
    val apellidoMaterno: String? = null,

    @SerializedName("fotoPerfil")
    val fotoPerfil: String? = null,

    @SerializedName("activo")
    val activo: Boolean? = true
) {
    /**
     * Valida el DTO antes de enviarlo
     */
    fun validate(): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Email inválido"
        if (contrasena.length < 6)
            return "Contraseña demasiado corta (mínimo 6 caracteres)"
        if (primerNombre.isBlank())
            return "El primer nombre es obligatorio"
        return null
    }

    /**
     * Construye el nombre completo
     */
    val nombreCompleto: String
        get() {
            val nombres = listOfNotNull(primerNombre, segundoNombre)
            val apellidos = listOfNotNull(apellidoPaterno, apellidoMaterno)
            return (nombres + apellidos).joinToString(" ")
        }
}

/**
 * DTO de respuesta del servidor con información de usuario
 * ⚠️ NO MODIFICAR - Debe coincidir exactamente con el backend
 */
data class UsuarioResponseDTO(
    @SerializedName("idUsuario")
    var idUsuario: Int? = null,

    @SerializedName("idRol")
    var idRol: Int? = null,

    @SerializedName("nombreRol")
    var nombreRol: String? = null,

    @SerializedName("primerNombre")
    var primerNombre: String? = null,

    @SerializedName("segundoNombre")
    var segundoNombre: String? = null,

    @SerializedName("apellidoPaterno")
    var apellidoPaterno: String? = null,

    @SerializedName("apellidoMaterno")
    var apellidoMaterno: String? = null,

    @SerializedName("nombreCompleto")
    var nombreCompleto: String? = null,

    @SerializedName("email")
    var email: String? = null,

    @SerializedName("username")
    var username: String? = null,

    @SerializedName("fotoPerfil")
    var fotoPerfil: String? = null,

    @SerializedName("activo")
    var activo: Boolean? = true,

    @SerializedName("fechaCreacion")
    var fechaCreacion: LocalDateTime? = null,

    @SerializedName("ultimoAcceso")
    var ultimoAcceso: LocalDateTime? = null
) {
    /**
     * Construye el nombre completo si no existe en la respuesta.
     */
    fun construirNombreCompleto(): String {
        val partes = listOfNotNull(
            primerNombre,
            segundoNombre,
            apellidoPaterno,
            apellidoMaterno
        )
        return partes.joinToString(" ")
    }
}

/**
 * DTO para actualizar usuario existente
 */
data class UsuarioUpdateDTO(
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

    @SerializedName("fotoPerfil")
    val fotoPerfil: String? = null
)

/**
 * DTO para estadísticas de usuarios
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
)

/**
 * ===============================================
 * DTOs PARA GESTIÓN DE ROLES
 * ===============================================
 */

data class RolRequestDTO(
    @SerializedName("nombreRol")
    val nombreRol: String,

    @SerializedName("activo")
    val activo: Boolean = true
)

data class RolResponseDTO(
    @SerializedName("idRol")
    val idRol: Int,

    @SerializedName("nombreRol")
    val nombreRol: String,

    @SerializedName("activo")
    val activo: Boolean = true
)

/**
 * ===============================================
 * MODELO LOCAL DE USUARIO (Para uso en app)
 * ===============================================
 */

/**
 * Modelo de Usuario para uso interno en la app
 */
data class Usuario2(
    val idUsuario: Int = 0,
    val rol: Rol2,
    val primerNombre: String,
    val segundoNombre: String = "",
    val apellidoMaterno: String = "",
    val apellidoPaterno: String = "",
    val email: String,
    val username: String = email.substringBefore("@"),
    val password: String = "",
    val activo: Boolean = true
) {
    val nombreCompleto: String
        get() {
            val partes = listOfNotNull(
                primerNombre.takeIf { it.isNotBlank() },
                segundoNombre.takeIf { it.isNotBlank() },
                apellidoPaterno.takeIf { it.isNotBlank() },
                apellidoMaterno.takeIf { it.isNotBlank() }
            )
            return partes.joinToString(" ")
        }
}

/**
 * ===============================================
 * ENUM DE ROLES (Para uso en app)
 * ===============================================
 */

/**
 * Enum de Roles para uso en la app
 * ⚠️ Los IDs deben coincidir con el backend
 */
enum class Rol2(
    val idRol: Int,
    val nombreRol: String,
    val descripcion: String
) {
    ADMINISTRADOR(1, "Administrador", "Acceso total al sistema"),
    CO_ADMINISTRADOR(2, "Co-Administrador", "Casi todos los permisos"),
    GESTOR_PEDIDOS(3, "Gestor de Pedidos", "Gestión de pedidos"),
    PROFESOR_A_CARGO(4, "Profesor a Cargo", "Gestión de solicitudes de profesores"),
    DOCENTE(5, "Docente", "Solicitudes y consultas"),
    ENCARGADO_BODEGA(6, "Encargado de Bodega", "Control de inventario"),
    ASISTENTE_BODEGA(7, "Asistente de Bodega", "Bodega en tránsito");

    companion object {
        /**
         * Obtiene un Rol2 desde su ID
         */
        fun desdeId(id: Int): Rol2? = values().find { it.idRol == id }

        /**
         * Obtiene un Rol2 desde su nombre
         */
        fun desdeNombre(nombre: String): Rol2? =
            values().find { it.nombreRol.equals(nombre, ignoreCase = true) }

        /**
         * Obtiene todos los roles disponibles
         */
        fun obtenerTodos(): List<Rol2> = values().toList()
    }
}

/**
 * ===============================================
 * FUNCIONES DE CONVERSIÓN
 * ===============================================
 */

/**
 * Construye el nombre completo desde UsuarioResponseDTO
 */
fun UsuarioResponseDTO.construirNombreCompleto(): String {
    val partes = listOfNotNull(
        primerNombre,
        segundoNombre,
        apellidoPaterno,
        apellidoMaterno
    )
    return partes.joinToString(" ")
}

/**
 * Convierte UsuarioResponseDTO a Usuario2 (modelo local)
 */
fun UsuarioResponseDTO.toUsuario2(): Usuario2? {
    val rol = Rol2.desdeId(this.idRol ?: return null) ?: return null

    return Usuario2(
        idUsuario = this.idUsuario ?: 0,
        rol = rol,
        primerNombre = this.primerNombre ?: "",
        segundoNombre = this.segundoNombre ?: "",
        apellidoPaterno = this.apellidoPaterno ?: "",
        apellidoMaterno = this.apellidoMaterno ?: "",
        email = this.email ?: "",
        username = this.username ?: this.email?.substringBefore("@") ?: "",
        activo = this.activo ?: true
    )
}

/**
 * Convierte Usuario2 (modelo local) a UsuarioRequestDTO
 */
fun Usuario2.toUsuarioRequestDTO(contrasena: String): UsuarioRequestDTO {
    return UsuarioRequestDTO(
        idRol = this.rol.idRol,
        primerNombre = this.primerNombre,
        segundoNombre = this.segundoNombre.takeIf { it.isNotBlank() },
        apellidoPaterno = this.apellidoPaterno.takeIf { it.isNotBlank() },
        apellidoMaterno = this.apellidoMaterno.takeIf { it.isNotBlank() },
        email = this.email,
        username = this.username,
        contrasena = contrasena,
        activo = this.activo
    )
}