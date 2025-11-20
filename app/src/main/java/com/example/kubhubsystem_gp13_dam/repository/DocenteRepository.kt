package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.DocenteDao
import com.example.kubhubsystem_gp13_dam.local.entities.DocenteEntity
import com.example.kubhubsystem_gp13_dam.local.entities.UsuarioEntity
import com.example.kubhubsystem_gp13_dam.model.Rol2

class DocenteRepository(private val docenteDao: DocenteDao) {

    suspend fun insertar(docente: DocenteEntity): Long = docenteDao.insertar(docente)

    suspend fun actualizar(docente: DocenteEntity): Int = docenteDao.actualizar(docente)

    suspend fun eliminar(docente: DocenteEntity): Int = docenteDao.eliminar(docente)

    suspend fun eliminarPorId(id: Int): Int = docenteDao.eliminarPorId(id)

    suspend fun obtenerPorId(id: Int): DocenteEntity? = docenteDao.obtenerPorId(id)

    suspend fun obtenerPorIdUsuario(idUsuario: Int): DocenteEntity? =
        docenteDao.obtenerPorIdUsuario(idUsuario)

    suspend fun obtenerTodos(): List<DocenteEntity> = docenteDao.obtenerTodos()

    suspend fun obtenerPorIdSeccion(idSeccion: Int): List<DocenteEntity> =
        docenteDao.obtenerPorIdSeccion(idSeccion)

    // Extensión para derivar docentes usando el enum Rol
    suspend fun derivarDocentesDesdeUsuarios(usuarios: List<UsuarioEntity>) {
        // Filtrar usuarios que tienen el rol PROFESOR del enum
        val usuariosDocentes = usuarios.filter { usuario ->
            // Buscar el rol en el enum usando el idRol del usuario
            Rol2.desdeId(usuario.idRol) == Rol2.DOCENTE
        }

        usuariosDocentes.forEach { usuario ->
            // Verificar si ya existe un docente para este usuario
            val docenteExistente = docenteDao.obtenerPorIdUsuario(usuario.idUsuario)

            if (docenteExistente == null) {
                // Crear nueva entidad Docente
                val nuevoDocente = DocenteEntity(
                    idDocente = 0, // Auto-generado
                    idUsuario = usuario.idUsuario,
                    seccionesIds = emptyList() // Inicialmente sin secciones asignadas
                )
                docenteDao.insertar(nuevoDocente)
            }
        }
    }

    // Método para obtener solo los IDs de usuarios que son docentes según el enum
    suspend fun obtenerIdsUsuariosDocentes(usuarios: List<UsuarioEntity>): List<Int> {
        return usuarios.filter { usuario ->
            Rol2.desdeId(usuario.idRol) == Rol2.DOCENTE
        }.map { it.idUsuario }
    }

    // Método para inicializar docentes usando el enum
    suspend fun inicializarDocentes(usuarios: List<UsuarioEntity>) {
        derivarDocentesDesdeUsuarios(usuarios)
    }
}