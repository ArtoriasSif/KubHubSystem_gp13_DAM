package com.example.kubhubsystem_gp13_dam.model

/**
 * Enum que representa los roles del sistema
 * ✅ ACTUALIZADO: Ahora coincide EXACTAMENTE con el backend Java Spring Boot
 *
 * Mapeo:
 * Backend ENUM (BD) → Kotlin ENUM
 * ADMINISTRADOR → ADMINISTRADOR
 * CO_ADMINISTRADOR → CO_ADMINISTRADOR
 * GESTOR_PEDIDOS → GESTOR_PEDIDOS
 * PROFESOR_A_CARGO → PROFESOR_A_CARGO
 * DOCENTE → DOCENTE
 * ENCARGADO_BODEGA → ENCARGADO_BODEGA
 * ASISTENTE_BODEGA → ASISTENTE_BODEGA
 */
enum class Rol(
    private val id: Int,
    private val nombre: String,
    private val nombreEnum: String // Nombre como aparece en BD
) {
    ADMINISTRADOR(1, "Administrador", "ADMINISTRADOR"),
    CO_ADMINISTRADOR(2, "Co-Administrador", "CO_ADMINISTRADOR"),
    GESTOR_PEDIDOS(3, "Gestor de Pedidos", "GESTOR_PEDIDOS"),
    PROFESOR_A_CARGO(4, "Profesor a Cargo", "PROFESOR_A_CARGO"),
    DOCENTE(5, "Docente", "DOCENTE"),
    ENCARGADO_BODEGA(6, "Encargado de Bodega", "ENCARGADO_BODEGA"),
    ASISTENTE_BODEGA(7, "Asistente de Bodega", "ASISTENTE_BODEGA");

    /**
     * Obtiene el ID del rol (para comunicación con backend)
     */
    fun obtenerIdRol(): Int = id

    /**
     * Obtiene el nombre legible del rol (para UI)
     */
    fun obtenerNombre(): String = nombre

    /**
     * Obtiene el nombre ENUM (para enviar al backend si es necesario)
     */
    fun obtenerNombreEnum(): String = nombreEnum

    companion object {
        /**
         * Obtiene un Rol desde su ID
         * Útil para mapear respuestas del backend
         */
        fun desdeId(id: Int): Rol? {
            return entries.find { it.id == id }
        }

        /**
         * Obtiene un Rol desde su nombre legible
         * Ejemplo: "Administrador" → ADMINISTRADOR
         */
        fun desdeNombre(nombre: String): Rol? {
            return entries.find { it.nombre.equals(nombre, ignoreCase = true) }
        }

        /**
         * Obtiene un Rol desde su nombre ENUM (como viene del backend)
         * Ejemplo: "ADMINISTRADOR" → ADMINISTRADOR
         * Ejemplo: "CO_ADMINISTRADOR" → CO_ADMINISTRADOR
         */
        fun desdeNombreEnum(nombreEnum: String): Rol? {
            return entries.find { it.nombreEnum.equals(nombreEnum, ignoreCase = true) }
        }

        /**
         * Obtiene todos los roles disponibles
         */
        fun obtenerTodos(): List<Rol> = entries.toList()
    }
}