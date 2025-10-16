package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.RolDao
import com.example.kubhubsystem_gp13_dam.local.entities.RolEntity

class RolRepository (private val rolDao: RolDao) {

    suspend fun insertar(rol: RolEntity): Long = rolDao.insertar(rol)

    suspend fun actualizar(rol: RolEntity): Int = rolDao.actualizar(rol)

    suspend fun eliminar(rol: RolEntity): Int = rolDao.eliminar(rol)

    suspend fun eliminarPorId(id: Int): Int = rolDao.eliminarPorId(id)

    suspend fun obtenerPorId(id: Int): RolEntity? = rolDao.obtenerPorId(id)

    suspend fun obtenerPorNombre(nombre: String): RolEntity? = rolDao.obtenerPorNombre(nombre)

    suspend fun obtenerTodos(): List<RolEntity> = rolDao.obtenerTodos()

    suspend fun inicializarRoles() {
        val rolesIniciales = listOf(
            RolEntity(1, "Admin"),
            RolEntity(2, "Co-Admin"),
            RolEntity(3, "Gestor de pedidos"),
            RolEntity(4, "Docente"),
            RolEntity(5, "Bodega"),
            RolEntity(6, "Asistente")
        )

        rolesIniciales.forEach { rol ->
            val existe = rolDao.obtenerPorId(rol.idRol)
            if (existe == null) {
                rolDao.insertar(rol)
            }
        }
    }
}