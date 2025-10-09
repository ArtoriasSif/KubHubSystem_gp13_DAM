package com.example.kubhubsystem_gp13_dam.model.loginUsers

data class User(
    val username: String,
    val password: String,
    val role: UserRole,
    val displayName: String
)

enum class UserRole(
    val displayName: String,
    val description: String
) {
    ADMIN("Admin", "Acceso total al sistema"),
    CO_ADMIN("Co-Admin", "Casi todos los permisos"),
    GESTOR_PEDIDOS("Gestor de pedidos", "Gestión de pedidos"),
    PROFESOR("Profesor", "Solicitudes y consultas"),
    BODEGA("Bodega", "Control de inventario"),
    ASISTENTE("Asistente", "Bodega en tránsito")
}