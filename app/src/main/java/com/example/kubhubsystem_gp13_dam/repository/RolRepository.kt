package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.model.RolRequestDTO
import com.example.kubhubsystem_gp13_dam.model.RolResponseDTO

/**
 * Repositorio de Roles
 * ✅ ACTUALIZADO: Ahora se conecta al backend Spring Boot vía Retrofit
 * ❌ ELIMINADO: Ya no usa DAOs ni base de datos local
 */
class RolRepository {

    private val rolService = RetrofitClient.rolService

    /**
     * Obtiene todos los roles
     */
    suspend fun obtenerTodos(): List<RolResponseDTO> {
        return try {
            val response = rolService.obtenerTodos()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene solo roles activos
     */
    suspend fun obtenerActivos(): List<RolResponseDTO> {
        return try {
            val response = rolService.obtenerActivos()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene un rol por su ID
     */
    suspend fun obtenerPorId(id: Int): RolResponseDTO? {
        return try {
            val response = rolService.obtenerPorId(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene un rol por su nombre
     */
    suspend fun obtenerPorNombre(nombre: String): RolResponseDTO? {
        return try {
            val response = rolService.obtenerPorNombre(nombre)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea un nuevo rol
     */
    suspend fun crear(nombreRol: String, activo: Boolean = true): RolResponseDTO? {
        return try {
            val requestDTO = RolRequestDTO(nombreRol, activo)
            val response = rolService.crear(requestDTO)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Actualiza un rol existente
     */
    suspend fun actualizar(id: Int, nombreRol: String, activo: Boolean): RolResponseDTO? {
        return try {
            val requestDTO = RolRequestDTO(nombreRol, activo)
            val response = rolService.actualizar(id, requestDTO)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Desactiva un rol
     */
    suspend fun desactivar(id: Int): Boolean {
        return try {
            val response = rolService.desactivar(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Activa un rol
     */
    suspend fun activar(id: Int): Boolean {
        return try {
            val response = rolService.activar(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Elimina un rol permanentemente
     */
    suspend fun eliminar(id: Int): Boolean {
        return try {
            val response = rolService.eliminar(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Verifica si existe un rol con ese nombre
     */
    suspend fun existePorNombre(nombre: String): Boolean {
        return try {
            val response = rolService.existePorNombre(nombre)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * ❌ ELIMINADO: inicializarRoles()
     * Ya no es necesario porque los datos vienen del backend
     */
}