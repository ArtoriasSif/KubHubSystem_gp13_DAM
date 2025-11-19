package com.example.kubhubsystem_gp13_dam.utils

import com.example.kubhubsystem_gp13_dam.model.Rol2
import com.example.kubhubsystem_gp13_dam.ui.navigation.MenuRoutes

/**
 * Gestor de permisos del sistema - Versión 2
 * Define qué rutas/pantallas puede acceder cada rol
 *
 * ✅ ACTUALIZADO PARA Rol2
 * ⚠️ PERMISOS CONFIGURABLES - Modificar aquí para cambiar accesos
 *
 * Permisos personalizados según rol del sistema
 */
object PermisosManager {

    /**
     * Mapeo de roles y sus rutas permitidas
     * Cada rol tiene acceso específico según su función en el sistema
     */
    private val permisosRol: Map<Rol2, List<String>> = mapOf(
        // ADMINISTRADOR - Acceso total al sistema
        Rol2.ADMINISTRADOR to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Solicitud.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Usuarios.route
        ),

        // CO_ADMINISTRADOR - Casi todos los permisos excepto gestión de usuarios
        Rol2.CO_ADMINISTRADOR to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route,
            MenuRoutes.Solicitud.route
        ),

        // GESTOR_PEDIDOS - Enfocado en pedidos y solicitudes
        Rol2.GESTOR_PEDIDOS to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Solicitud.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route
        ),

        // PROFESOR_A_CARGO - Gestión académica y solicitudes
        Rol2.PROFESOR_A_CARGO to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.Solicitud.route,
            MenuRoutes.Asignaturas.route,
            MenuRoutes.Recetas.route,
            MenuRoutes.GestionPedidos.route
        ),

        // DOCENTE - Solo solicitudes y consultas básicas
        Rol2.DOCENTE to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Solicitud.route
        ),

        // ENCARGADO_BODEGA - Gestión de inventario y pedidos
        Rol2.ENCARGADO_BODEGA to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route,
            MenuRoutes.GestionPedidos.route
        ),

        // ASISTENTE_BODEGA - Solo inventario básico
        Rol2.ASISTENTE_BODEGA to listOf(
            MenuRoutes.Home.route,
            MenuRoutes.MainMenu.route,
            MenuRoutes.Dashboard.route,
            MenuRoutes.Inventario.route
        )
    )

    /**
     * Verifica si un rol tiene acceso a una ruta específica
     *
     * @param rol Rol del usuario (Rol2)
     * @param ruta Ruta a la que se quiere acceder
     * @return true si tiene permiso, false en caso contrario
     */
    fun tieneAcceso(rol: Rol2, ruta: String): Boolean {
        val permisos = permisosRol[rol] ?: emptyList()
        val tieneAcceso = permisos.contains(ruta)

        // Debug logging
        if (!tieneAcceso) {
            println("⚠️ PermisosManager: ${rol.nombreRol} NO tiene acceso a '$ruta'")
        }

        return tieneAcceso
    }

    /**
     * Obtiene todas las rutas permitidas para un rol
     *
     * @param rol Rol del usuario (Rol2)
     * @return Lista de rutas permitidas
     */
    fun obtenerRutasPermitidas(rol: Rol2): List<String> {
        return permisosRol[rol] ?: emptyList()
    }

    /**
     * Verifica si un rol tiene acceso a gestión de usuarios
     * Solo ADMINISTRADOR puede gestionar usuarios
     */
    fun puedeGestionarUsuarios(rol: Rol2): Boolean {
        return rol == Rol2.ADMINISTRADOR
    }

    /**
     * Verifica si un rol tiene acceso a gestión de inventario
     */
    fun puedeGestionarInventario(rol: Rol2): Boolean {
        return tieneAcceso(rol, MenuRoutes.Inventario.route)
    }

    /**
     * Verifica si un rol tiene acceso a gestión de pedidos
     */
    fun puedeGestionarPedidos(rol: Rol2): Boolean {
        return tieneAcceso(rol, MenuRoutes.GestionPedidos.route)
    }

    /**
     * Verifica si un rol tiene acceso al dashboard
     */
    fun puedeVerDashboard(rol: Rol2): Boolean {
        return tieneAcceso(rol, MenuRoutes.Dashboard.route)
    }

    /**
     * Verifica si un rol tiene acceso a gestión de asignaturas
     */
    fun puedeGestionarAsignaturas(rol: Rol2): Boolean {
        return tieneAcceso(rol, MenuRoutes.Asignaturas.route)
    }

    /**
     * Verifica si un rol tiene acceso a gestión de recetas
     */
    fun puedeGestionarRecetas(rol: Rol2): Boolean {
        return tieneAcceso(rol, MenuRoutes.Recetas.route)
    }

    /**
     * Verifica si un rol puede crear solicitudes
     */
    fun puedeCrearSolicitudes(rol: Rol2): Boolean {
        return tieneAcceso(rol, MenuRoutes.Solicitud.route)
    }

    /**
     * Verifica si un rol es de tipo administrativo
     */
    fun esRolAdministrativo(rol: Rol2): Boolean {
        return rol in listOf(
            Rol2.ADMINISTRADOR,
            Rol2.CO_ADMINISTRADOR,
            Rol2.GESTOR_PEDIDOS
        )
    }

    /**
     * Verifica si un rol es de tipo bodega
     */
    fun esRolBodega(rol: Rol2): Boolean {
        return rol in listOf(
            Rol2.ENCARGADO_BODEGA,
            Rol2.ASISTENTE_BODEGA
        )
    }

    /**
     * Verifica si un rol es de tipo docente
     */
    fun esRolDocente(rol: Rol2): Boolean {
        return rol in listOf(
            Rol2.DOCENTE,
            Rol2.PROFESOR_A_CARGO
        )
    }

    /**
     * Obtiene descripción de permisos para un rol
     * Útil para mostrar en UI o para debugging
     */
    fun obtenerDescripcionPermisos(rol: Rol2): String {
        val rutas = obtenerRutasPermitidas(rol)
        return buildString {
            appendLine("Permisos para ${rol.nombreRol}:")
            if (rutas.isEmpty()) {
                appendLine("  - Sin permisos asignados")
            } else {
                rutas.forEach { ruta ->
                    appendLine("  ✓ $ruta")
                }
            }
        }
    }

    /**
     * Valida si un rol puede realizar una acción específica
     * Útil para validaciones más granulares
     */
    fun puedeRealizarAccion(rol: Rol2, accion: AccionSistema): Boolean {
        return when (accion) {
            AccionSistema.CREAR_USUARIO -> rol == Rol2.ADMINISTRADOR
            AccionSistema.EDITAR_USUARIO -> rol == Rol2.ADMINISTRADOR
            AccionSistema.ELIMINAR_USUARIO -> rol == Rol2.ADMINISTRADOR
            AccionSistema.VER_USUARIOS -> puedeGestionarUsuarios(rol)

            AccionSistema.CREAR_PEDIDO -> puedeGestionarPedidos(rol)
            AccionSistema.APROBAR_PEDIDO -> esRolAdministrativo(rol)
            AccionSistema.RECHAZAR_PEDIDO -> esRolAdministrativo(rol)
            AccionSistema.VER_PEDIDOS -> puedeGestionarPedidos(rol)

            AccionSistema.CREAR_SOLICITUD -> puedeCrearSolicitudes(rol)
            AccionSistema.EDITAR_SOLICITUD -> puedeCrearSolicitudes(rol)
            AccionSistema.VER_SOLICITUDES -> puedeCrearSolicitudes(rol)

            AccionSistema.GESTIONAR_INVENTARIO -> puedeGestionarInventario(rol)
            AccionSistema.VER_INVENTARIO -> puedeGestionarInventario(rol)

            AccionSistema.GESTIONAR_ASIGNATURAS -> puedeGestionarAsignaturas(rol)
            AccionSistema.VER_DASHBOARD -> puedeVerDashboard(rol)
        }
    }
}

/**
 * Enum de acciones del sistema para validación granular de permisos
 */
enum class AccionSistema {
    // Usuarios
    CREAR_USUARIO,
    EDITAR_USUARIO,
    ELIMINAR_USUARIO,
    VER_USUARIOS,

    // Pedidos
    CREAR_PEDIDO,
    APROBAR_PEDIDO,
    RECHAZAR_PEDIDO,
    VER_PEDIDOS,

    // Solicitudes
    CREAR_SOLICITUD,
    EDITAR_SOLICITUD,
    VER_SOLICITUDES,

    // Inventario
    GESTIONAR_INVENTARIO,
    VER_INVENTARIO,

    // Otros
    GESTIONAR_ASIGNATURAS,
    VER_DASHBOARD
}