package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.UsuarioDao
import com.example.kubhubsystem_gp13_dam.local.entities.UsuarioEntity

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun insertar(usuario: UsuarioEntity): Long = usuarioDao.insertar(usuario)

    suspend fun actualizar(usuario: UsuarioEntity): Int = usuarioDao.actualizar(usuario)

    suspend fun eliminar(usuario: UsuarioEntity): Int = usuarioDao.eliminar(usuario)

    suspend fun eliminarPorId(id: Int): Int = usuarioDao.eliminarPorId(id)

    suspend fun obtenerPorId(id: Int): UsuarioEntity? = usuarioDao.obtenerPorId(id)

    suspend fun iniciarSesion(usuario: String, contraseña: String): UsuarioEntity? =
        usuarioDao.iniciarSesion(usuario, contraseña)

    suspend fun obtenerPorCorreo(correo: String): UsuarioEntity? =
        usuarioDao.obtenerPorCorreo(correo)

    suspend fun obtenerTodos(): List<UsuarioEntity> = usuarioDao.obtenerTodos()

    suspend fun obtenerPorRol(idRol: Int): List<UsuarioEntity> =
        usuarioDao.obtenerPorRol(idRol)

    suspend fun inicializarUsuarios() {
        val usuariosIniciales = listOf(
            // Usuario para cada rol (6 usuarios)
            UsuarioEntity(
                idUsuario = 1,
                idRol = 1, // Admin
                primeroNombre = "Carlos",
                segundoNombre = "Alberto",
                apellidoMaterno = "Gómez",
                apellidoPaterno = "López",
                email = "admin@kubhub.com",
                username = "admin",
                password = "admin123"
            ),
            UsuarioEntity(
                idUsuario = 2,
                idRol = 2, // Co-Admin
                primeroNombre = "María",
                segundoNombre = "Fernanda",
                apellidoMaterno = "Rodríguez",
                apellidoPaterno = "Martínez",
                email = "coadmin@kubhub.com",
                username = "coadmin",
                password = "coadmin123"
            ),
            UsuarioEntity(
                idUsuario = 3,
                idRol = 3, // Gestor de pedidos
                primeroNombre = "Pedro",
                segundoNombre = "Antonio",
                apellidoMaterno = "Silva",
                apellidoPaterno = "García",
                email = "gestor@kubhub.com",
                username = "gestor",
                password = "gestor123"
            ),
            UsuarioEntity(
                idUsuario = 4,
                idRol = 4, // Profesor
                primeroNombre = "Ana",
                segundoNombre = "Isabel",
                apellidoMaterno = "Pérez",
                apellidoPaterno = "Hernández",
                email = "profesor@kubhub.com",
                username = "profesor",
                password = "profesor123"
            ),
            UsuarioEntity(
                idUsuario = 5,
                idRol = 5, // Bodega
                primeroNombre = "Luis",
                segundoNombre = "Miguel",
                apellidoMaterno = "Torres",
                apellidoPaterno = "Ramírez",
                email = "bodega@kubhub.com",
                username = "bodega",
                password = "bodega123"
            ),
            UsuarioEntity(
                idUsuario = 6,
                idRol = 6, // Asistente
                primeroNombre = "Laura",
                segundoNombre = "Patricia",
                apellidoMaterno = "Díaz",
                apellidoPaterno = "Castro",
                email = "asistente@kubhub.com",
                username = "asistente",
                password = "asistente123"
            ),
            // 4 usuarios adicionales como docentes (rol Profesor - idRol 4)
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