package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioEstadisticasDTO
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioRequestDTO
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioUpdateDTO

/**
 * Repositorio de Usuarios
 * ✅ ACTUALIZADO: Ahora se conecta al backend Spring Boot vía Retrofit
 * ❌ ELIMINADO: Ya no usa DAOs ni base de datos local
 */
class UsuarioRepository {

    private val usuarioService = RetrofitClient.usuarioService

    /**
     * Obtiene todos los usuarios
     */
    suspend fun obtenerTodos(): List<Usuario> {
        return try {
            val response = usuarioService.obtenerTodos()
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTOs(response.body()!!)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene solo usuarios activos
     */
    suspend fun obtenerActivos(): List<Usuario> {
        return try {
            val response = usuarioService.obtenerActivos()
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTOs(response.body()!!)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene un usuario por su ID
     */
    suspend fun obtenerPorId(id: Int): Usuario? {
        return try {
            val response = usuarioService.obtenerPorId(id)
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTO(response.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene un usuario por su email
     */
    suspend fun obtenerPorEmail(email: String): Usuario? {
        return try {
            val response = usuarioService.obtenerPorEmail(email)
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTO(response.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Busca usuarios por término (nombre o email)
     */
    suspend fun buscar(query: String): List<Usuario> {
        return try {
            val response = usuarioService.buscar(query)
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTOs(response.body()!!)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene usuarios por rol
     */
    suspend fun obtenerPorRol(rol: Rol): List<Usuario> {
        return try {
            val response = usuarioService.obtenerPorRol(rol.obtenerIdRol())
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTOs(response.body()!!)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Crea un nuevo usuario
     */
    suspend fun crear(usuario: Usuario): Usuario? {
        return try {
            val requestDTO = UsuarioRequestDTO(
                idRol = usuario.rol.obtenerIdRol(),
                primerNombre = usuario.primerNombre,
                segundoNombre = usuario.segundoNombre,
                apellidoPaterno = usuario.apellidoPaterno,
                apellidoMaterno = usuario.apellidoMaterno,
                email = usuario.email,
                username = usuario.username,
                contrasena = usuario.password,
                fotoPerfil = usuario.fotoPerfil,
                activo = usuario.activo
            )

            val response = usuarioService.crear(requestDTO)
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTO(response.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Actualiza un usuario existente
     */
    suspend fun actualizar(id: Int, usuario: Usuario): Usuario? {
        return try {
            val updateDTO = UsuarioUpdateDTO(
                idRol = usuario.rol.obtenerIdRol(),
                primerNombre = usuario.primerNombre,
                segundoNombre = usuario.segundoNombre,
                apellidoPaterno = usuario.apellidoPaterno,
                apellidoMaterno = usuario.apellidoMaterno,
                email = usuario.email,
                username = usuario.username,
                contrasena = if (usuario.password.isNotBlank()) usuario.password else null,
                fotoPerfil = usuario.fotoPerfil,
                activo = usuario.activo
            )

            val response = usuarioService.actualizar(id, updateDTO)
            if (response.isSuccessful && response.body() != null) {
                Usuario.desdeDTO(response.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Desactiva un usuario
     */
    suspend fun desactivar(id: Int): Boolean {
        return try {
            val response = usuarioService.desactivar(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Activa un usuario
     */
    suspend fun activar(id: Int): Boolean {
        return try {
            val response = usuarioService.activar(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Elimina un usuario permanentemente
     */
    suspend fun eliminar(id: Int): Boolean {
        return try {
            val response = usuarioService.eliminar(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Cambia la contraseña de un usuario
     */
    suspend fun cambiarContrasena(id: Int, nuevaContrasena: String): Boolean {
        return try {
            val body = mapOf("nuevaContrasena" to nuevaContrasena)
            val response = usuarioService.cambiarContrasena(id, body)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene estadísticas de usuarios
     */
    suspend fun obtenerEstadisticas(): UsuarioEstadisticasDTO? {
        return try {
            val response = usuarioService.obtenerEstadisticas()
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
     * ❌ ELIMINADO: inicializarUsuarios()
     * Ya no es necesario porque los datos vienen del backend
     */
}