package com.example.kubhubsystem_gp13_dam.model

import android.util.Log
import android.util.Patterns
import com.google.gson.annotations.SerializedName
import com.example.kubhubsystem_gp13_dam.utils.PerfilHelper

/**
 * ===============================================
 * DTOs PARA AUTENTICACIÓN (LOGIN)
 * ===============================================
 */

/**
 * DTO para petición de login
 * Mapea exactamente con LoginRequestDTO.java del backend
 */
data class LoginRequestDTO(
    @SerializedName("email")
    val email: String,

    @SerializedName("contrasena")
    val contrasena: String
){
    /**
     * Valida que el email y contraseña tengan formato correcto
     */
    fun isValid(): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                contrasena.isNotBlank() &&
                contrasena.length >= 6
    }
}


data class LoginResponseDTO(
    @SerializedName("usuario")
    val usuario: UsuarioResponseDTO,

    @SerializedName("token")
    val token: String,

    @SerializedName("mensaje")
    val mensaje: String
)

/**
 * ===============================================
 * DTOs PARA GESTIÓN DE USUARIOS
 * ===============================================
 */

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
    val fotoPerfil: String? = null, // Base64 string de la imagen

    @SerializedName("activo")
    val activo: Boolean = true
){
    /**
     * Valida que los datos del usuario sean correctos
     */
    fun isValid(): Boolean {
        return primerNombre.isNotBlank() &&
                email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                contrasena.isNotBlank() &&
                contrasena.length >= 6 &&
                idRol > 0
    }
}


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

    @SerializedName("username")
    val username: String?,

    @SerializedName("fotoPerfil")
    val fotoPerfil: String?, // Base64 string de la imagen (recibida del backend)

    @SerializedName("activo")
    val activo: Boolean,

    @SerializedName("fechaCreacion")
    val fechaCreacion: String?, // ISO 8601 format - ignorado por ahora

    @SerializedName("ultimoAcceso")
    val ultimoAcceso: String? // ISO 8601 format - ignorado por ahora
)

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
    val fotoPerfil: String? = null, // Base64 string de la imagen

    @SerializedName("activo")
    val activo: Boolean? = null
){
    /**
     * Valida que al menos un campo esté presente para actualizar
     */
    fun hasChanges(): Boolean {
        return idRol != null ||
                primerNombre != null ||
                segundoNombre != null ||
                apellidoPaterno != null ||
                apellidoMaterno != null ||
                email != null ||
                username != null ||
                contrasena != null ||
                fotoPerfil != null ||
                activo != null
    }

    /**
     * Valida que los datos sean correctos si están presentes
     */
    fun isValid(): Boolean {
        // Si el email está presente, debe ser válido
        if (email != null && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

        // Si la contraseña está presente, debe tener al menos 6 caracteres
        if (contrasena != null && contrasena.length < 6) {
            return false
        }

        // Si el primer nombre está presente, no debe estar vacío
        if (primerNombre != null && primerNombre.isBlank()) {
            return false
        }

        return true
    }
}

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
)


/**
 * ===============================================
 * DTOs PARA GESTIÓN DE ROLES
 * ===============================================
 */

/**
 * DTO para crear o actualizar un Rol
 * Mapea exactamente con RolRequestDTO.java del backend
 */
data class RolRequestDTO(
    @SerializedName("nombreRol")
    val nombreRol: String,

    @SerializedName("activo")
    val activo: Boolean = true
){
    /**
     * Valida que el nombre del rol no esté vacío
     */
    fun isValid(): Boolean {
        return nombreRol.isNotBlank()
    }
}

/**
 * DTO para respuesta de Rol
 * Mapea exactamente con RolResponseDTO.java del backend
 */
data class RolResponseDTO(
    @SerializedName("idRol")
    val idRol: Int,

    @SerializedName("nombreRol")
    val nombreRol: String,

    @SerializedName("activo")
    val activo: Boolean
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
    val fotoPerfil: String? = null,
    val activo: Boolean = true
) {

    init {
        // ✅ Log mejorado para debugging
        if (fotoPerfil != null) {
            println("🔍 Usuario $idUsuario ($primerNombre) fotoPerfil presente:")
            println("   Tipo: ${when {
                fotoPerfil.startsWith("http") -> "URL HTTP/HTTPS"
                fotoPerfil.startsWith("data:image") -> "Data URI Base64"
                else -> "Formato desconocido"
            }}")
            println("   Primeros 100 chars: ${fotoPerfil.take(100)}...")
        } else {
            println("🔍 Usuario $idUsuario ($primerNombre) sin fotoPerfil")
        }
    }

    fun obtenerNombreCompleto(): String {
        return buildString {
            if (primerNombre.isNotBlank()) append(primerNombre).append(" ")
            if (segundoNombre.isNotBlank()) append(segundoNombre).append(" ")
            if (apellidoPaterno.isNotBlank()) append(apellidoPaterno).append(" ")
            if (apellidoMaterno.isNotBlank()) append(apellidoMaterno)
        }.trim()
    }

    companion object {

        /**
         * Convierte UN SOLO DTO en Usuario2
         */
        fun desdeDTO(dto: UsuarioResponseDTO): Usuario2? {
            return dto.toUsuario2()
        }

        /**
         * Convierte lista de DTOs
         */
        fun desdeDTOs(lista: List<UsuarioResponseDTO>): List<Usuario2> {
            return lista.mapNotNull { it.toUsuario2() }
        }
    }

    val nombreCompleto: String
        get() = listOfNotNull(
            primerNombre.takeIf { it.isNotBlank() },
            segundoNombre.takeIf { it.isNotBlank() },
            apellidoPaterno.takeIf { it.isNotBlank() },
            apellidoMaterno.takeIf { it.isNotBlank() }
        ).joinToString(" ")
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

    /**
     * Obtiene el ID del rol (para comunicación con backend)
     */
    fun obtenerIdRol(): Int = idRol

    /**
     * Obtiene el nombre legible del rol (para UI)
     */
    fun obtenerNombre(): String = nombreRol

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
/**
 * ✅ CONVERSIÓN CORREGIDA
 * Ya no normaliza a DataURI - mantiene el Base64 puro del backend
 */
fun UsuarioResponseDTO.toUsuario2(): Usuario2? {
    val rolEnum = Rol2.desdeId(this.idRol)
    if (rolEnum == null) {
        println("⚠️ Rol desconocido: $idRol desde backend")
        return null
    }

    // ✅ CRÍTICO: NO normalizar aquí
    // Dejar el fotoPerfil tal cual viene del backend (Base64 puro)
    val fotoLimpia = this.fotoPerfil?.trim()?.takeIf { it.isNotBlank() }

    if (fotoLimpia != null) {
        Log.d("Usuario2.toUsuario2", "📸 Foto para usuario $idUsuario:")
        Log.d("Usuario2.toUsuario2", "   Preview: ${fotoLimpia.take(50)}")
        Log.d("Usuario2.toUsuario2", "   Length: ${fotoLimpia.length}")
    }

    return Usuario2(
        idUsuario = this.idUsuario,
        rol = rolEnum,
        primerNombre = this.primerNombre ?: "",
        segundoNombre = this.segundoNombre ?: "",
        apellidoPaterno = this.apellidoPaterno ?: "",
        apellidoMaterno = this.apellidoMaterno ?: "",
        email = this.email,
        username = this.username ?: this.email.substringBefore("@"),
        fotoPerfil = fotoLimpia, // ✅ Base64 puro, sin conversión
        activo = this.activo
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