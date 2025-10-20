package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.UsuarioDao
import com.example.kubhubsystem_gp13_dam.local.entities.UsuarioEntity
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun insertar(usuario: UsuarioEntity): Long = usuarioDao.insertar(usuario)

    suspend fun actualizar(usuario: UsuarioEntity): Int = usuarioDao.actualizar(usuario)

    suspend fun eliminar(usuario: UsuarioEntity): Int = usuarioDao.eliminar(usuario)

    suspend fun eliminarPorId(id: Int): Int = usuarioDao.eliminarPorId(id)

    suspend fun obtenerPorId(id: Int): UsuarioEntity? = usuarioDao.obtenerPorId(id)

    suspend fun obtenerUsuarioPorId(id: Int): Usuario? {
        val entity = usuarioDao.obtenerPorId(id) ?: return null

        return Usuario(
            idUsuario = entity.idUsuario,
            rol = Rol.desdeId(entity.idRol) ?: Rol.GESTOR_PEDIDOS,
            primeroNombre = entity.primeroNombre,
            segundoNombre = entity.segundoNombre,
            apellidoMaterno = entity.apellidoMaterno,
            apellidoPaterno = entity.apellidoPaterno,
            email = entity.email,
            username = entity.username,
            password = entity.password
        )
    }

    suspend fun iniciarSesion(usuario: String, contraseña: String): UsuarioEntity? =
        usuarioDao.iniciarSesion(usuario, contraseña)

    suspend fun obtenerPorCorreo(correo: String): UsuarioEntity? =
        usuarioDao.obtenerPorCorreo(correo)

    suspend fun obtenerTodos(): List<UsuarioEntity> = usuarioDao.obtenerTodos()

    suspend fun obtenerPorRol(idRol: Int): List<UsuarioEntity> =
        usuarioDao.obtenerPorRol(idRol)

    suspend fun inicializarUsuarios() {
        val usuariosIniciales = listOf(
            // ADMIN - debe coincidir con admin@kubhub.com
            UsuarioEntity(
                idUsuario = 1,
                idRol = 1, // Admin
                primeroNombre = "Administrador", // Cambiado para coincidir con displayName
                segundoNombre = "",
                apellidoMaterno = "Sistema",
                apellidoPaterno = "KubHub",
                email = "admin@kubhub.com", // COINCIDE con username de la lista demo
                username = "admin@kubhub.com", // COINCIDE exactamente
                password = "admin123" // COINCIDE exactamente
            ),
            // CO-ADMIN - debe coincidir con coadmin@kubhub.com
            UsuarioEntity(
                idUsuario = 2,
                idRol = 2, // Co-Admin
                primeroNombre = "Co-Administrador", // Cambiado para coincidir
                segundoNombre = "",
                apellidoMaterno = "Sistema",
                apellidoPaterno = "KubHub",
                email = "coadmin@kubhub.com", // COINCIDE
                username = "coadmin@kubhub.com", // COINCIDE
                password = "coadmin123" // COINCIDE
            ),
            // GESTOR_PEDIDOS - debe coincidir con gestor@kubhub.com
            UsuarioEntity(
                idUsuario = 3,
                idRol = 3, // Gestor de pedidos
                primeroNombre = "Gestor",
                segundoNombre = "de Pedidos",
                apellidoMaterno = "Operaciones",
                apellidoPaterno = "KubHub",
                email = "gestor@kubhub.com", // COINCIDE
                username = "gestor@kubhub.com", // COINCIDE
                password = "gestor123" // COINCIDE
            ),
            // PROFESOR - debe coincidir con profesor@kubhub.com
            UsuarioEntity(
                idUsuario = 4,
                idRol = 4, // Profesor
                primeroNombre = "Profesor",
                segundoNombre = "Principal",
                apellidoMaterno = "Académico",
                apellidoPaterno = "KubHub",
                email = "profesor@kubhub.com", // COINCIDE
                username = "profesor@kubhub.com", // COINCIDE
                password = "profesor123" // COINCIDE
            ),
            // BODEGA - debe coincidir con bodega@kubhub.com
            UsuarioEntity(
                idUsuario = 5,
                idRol = 5, // Bodega
                primeroNombre = "Bodeguero",
                segundoNombre = "",
                apellidoMaterno = "Inventario",
                apellidoPaterno = "KubHub",
                email = "bodega@kubhub.com", // COINCIDE
                username = "bodega@kubhub.com", // COINCIDE
                password = "bodega123" // COINCIDE
            ),
            // ASISTENTE - debe coincidir con asistente@kubhub.com
            UsuarioEntity(
                idUsuario = 6,
                idRol = 6, // Asistente
                primeroNombre = "Asistente",
                segundoNombre = "",
                apellidoMaterno = "Apoyo",
                apellidoPaterno = "KubHub",
                email = "asistente@kubhub.com", // COINCIDE
                username = "asistente@kubhub.com", // COINCIDE
                password = "asistente123" // COINCIDE
            ),
            // LOS SIGUIENTES 4 USUARIOS SE MANTIENEN IGUAL (profesores adicionales)
            UsuarioEntity(
                idUsuario = 7,
                idRol = 4, // Profesor
                primeroNombre = "Roberto",
                segundoNombre = "Carlos",
                apellidoMaterno = "Mendoza",
                apellidoPaterno = "Vargas",
                email = "roberto.vargas@kubhub.com",
                username = "r.vargas",
                password = "docente123"
            ),
            UsuarioEntity(
                idUsuario = 8,
                idRol = 4, // Profesor
                primeroNombre = "Sofia",
                segundoNombre = "Elena",
                apellidoMaterno = "Rojas",
                apellidoPaterno = "Morales",
                email = "sofia.morales@kubhub.com",
                username = "s.morales",
                password = "docente123"
            ),
            UsuarioEntity(
                idUsuario = 9,
                idRol = 4, // Profesor
                primeroNombre = "Javier",
                segundoNombre = "Andrés",
                apellidoMaterno = "Ortega",
                apellidoPaterno = "Fuentes",
                email = "javier.fuentes@kubhub.com",
                username = "j.fuentes",
                password = "docente123"
            ),
            UsuarioEntity(
                idUsuario = 10,
                idRol = 4, // Profesor
                primeroNombre = "Carmen",
                segundoNombre = "Rosa",
                apellidoMaterno = "Navarro",
                apellidoPaterno = "Jiménez",
                email = "carmen.jimenez@kubhub.com",
                username = "c.jimenez",
                password = "docente123"
            )
        )

        usuariosIniciales.forEach { usuario ->
            val existe = usuarioDao.obtenerPorId(usuario.idUsuario)
            if (existe == null) {
                usuarioDao.insertar(usuario)
            }
        }
    }
}