package com.example.kubhubsystem_gp13_dam.model

data class Usuario(
    val idUsuario: Int = 0,
    val rol: Rol,
    val primeroNombre: String,
    val segundoNombre: String,
    val apellidoMaterno: String,
    val apellidoPaterno: String,
    val email: String,
    val username: String,
    val password: String
)

data class Docente(
    val idDocente: Int = 0,
    val idUsuario: Int,
    val seccionesIds: List<Int>
)

enum class Rol(
    val displayName: String,
    val description: String
) {
    ADMIN("Admin", "Acceso total al sistema"),
    CO_ADMIN("Co-Admin", "Casi todos los permisos"),
    GESTOR_PEDIDOS("Gestor de pedidos", "Gestión de pedidos"),
    DOCENTE("Docente", "Solicitudes y consultas"),
    BODEGA("Bodega", "Control de inventario"),
    ASISTENTE("Asistente", "Bodega en tránsito");

    fun obtenerNombre(): String {
        return displayName
    }

    fun obtenerIdRol(): Int {
        return this.ordinal + 1
    }

    companion object {
        fun desdeId(id: Int): Rol? {
            return values().find { it.obtenerIdRol() == id }
        }

        fun desdeNombre(nombre: String): Rol? {
            return values().find { it.displayName.equals(nombre, ignoreCase = true) }
        }
    }
}