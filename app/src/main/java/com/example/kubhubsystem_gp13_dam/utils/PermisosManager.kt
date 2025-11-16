package com.example.kubhubsystem_gp13_dam.utils

import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.ui.navigation.MenuRoutes

/**
 * Gestor de permisos del sistema
 * Define qué rutas/pantallas puede acceder cada rol
 * 
 * ⚠️ PERMISOS HARDCODEADOS - Modificar aquí para cambiar accesos
 * 
 * Por ahora, todos los roles tienen acceso a todas las rutas.
 * TODO: Personalizar permisos según rol cuando sea necesario
 */
object PermisosManager {

    /**
     * Mapeo de roles y sus rutas permitidas
     * TODO: Personalizar este mapeo según los requerimientos del sistema
     */
    private val permisosRol: Map<Rol, List<String>> = mapOf(
        // ADMINISTRADOR - Acceso total (por ahora)
        Rol.ADMINISTRADOR to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // CO_ADMINISTRADOR - Acceso total (por ahora)
        Rol.CO_ADMINISTRADOR to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // GESTOR_PEDIDOS - Acceso total (por ahora)
        Rol.GESTOR_PEDIDOS to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // PROFESOR_A_CARGO - Acceso total (por ahora)
        Rol.PROFESOR_A_CARGO to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // DOCENTE - Acceso total (por ahora)
        Rol.DOCENTE to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // ENCARGADO_BODEGA - Acceso total (por ahora)
        Rol.ENCARGADO_BODEGA to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // ASISTENTE_BODEGA - Acceso total (por ahora)
        Rol.ASISTENTE_BODEGA to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        )
    )

    /**
     * Verifica si un rol tiene acceso a una ruta específica
     * 
     * @param rol Rol del usuario
     * @param ruta Ruta a la que se quiere acceder
     * @return true si tiene permiso, false en caso contrario
     */
    fun tieneAcceso(rol: Rol, ruta: String): Boolean {
        return permisosRol[rol]?.contains(ruta) ?: false
    }

    /**
     * Obtiene todas las rutas permitidas para un rol
     * 
     * @param rol Rol del usuario
     * @return Lista de rutas permitidas
     */
    fun obtenerRutasPermitidas(rol: Rol): List<String> {
        return permisosRol[rol] ?: emptyList()
    }

    /**
     * Verifica si un rol tiene acceso a gestión de usuarios
     * (útil para mostrar/ocultar opciones en el drawer)
     */
    fun puedeGestionarUsuarios(rol: Rol): Boolean {
        return tieneAcceso(rol, MenuRoutes.Usuarios.route)
    }

    /**
     * Verifica si un rol tiene acceso a gestión de inventario
     */
    fun puedeGestionarInventario(rol: Rol): Boolean {
        return tieneAcceso(rol, MenuRoutes.Inventario.route)
    }

    /**
     * Verifica si un rol tiene acceso a gestión de pedidos
     */
    fun puedeGestionarPedidos(rol: Rol): Boolean {
        return tieneAcceso(rol, MenuRoutes.GestionPedidos.route)
    }
}