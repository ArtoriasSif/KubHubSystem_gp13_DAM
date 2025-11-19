

package com.example.kubhubsystem_gp13_dam.model

/**
 * Modelo simple para User (usado en login demo)
 * Mantener como está - este es diferente al modelo Usuario del sistema
 */
data class User(
    val username: String,
    val password: String,
    val role: UserRole,
    val displayName: String
)

/**
 * Enum de roles para el modelo User (login demo)
 * ✅ ACTUALIZADO: Ahora coincide con los 7 roles del backend
 */
enum class UserRole(
    val displayName: String,
    val description: String
) {
    ADMINISTRADOR("Administrador", "Acceso total al sistema"),
    CO_ADMINISTRADOR("Co-Administrador", "Casi todos los permisos"),
    GESTOR_PEDIDOS("Gestor de Pedidos", "Gestión de pedidos"),
    PROFESOR_A_CARGO("Profesor a Cargo", "Gestión académica"),
    DOCENTE("Docente", "Solicitudes y consultas"),
    ENCARGADO_BODEGA("Encargado de Bodega", "Control de inventario"),
    ASISTENTE_BODEGA("Asistente de Bodega", "Bodega en tránsito");

    companion object {
        /**
         * Convierte desde el enum Rol del sistema al UserRole de login
         */
        fun desdeRol(rol: Rol): UserRole {
            return when (rol) {
                Rol.ADMINISTRADOR -> ADMINISTRADOR
                Rol.CO_ADMINISTRADOR -> CO_ADMINISTRADOR
                Rol.GESTOR_PEDIDOS -> GESTOR_PEDIDOS
                Rol.PROFESOR_A_CARGO -> PROFESOR_A_CARGO
                Rol.DOCENTE -> DOCENTE
                Rol.ENCARGADO_BODEGA -> ENCARGADO_BODEGA
                Rol.ASISTENTE_BODEGA -> ASISTENTE_BODEGA
            }
        }
    }
}