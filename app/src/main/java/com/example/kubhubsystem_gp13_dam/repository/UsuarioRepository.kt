package com.example.kubhubsystem_gp13_dam.repository

import android.content.Context
import android.net.Uri
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient.apiService
import com.example.kubhubsystem_gp13_dam.model.Rol2
import com.example.kubhubsystem_gp13_dam.model.Usuario2
import com.example.kubhubsystem_gp13_dam.model.UsuarioEstadisticasDTO
import com.example.kubhubsystem_gp13_dam.model.UsuarioRequestDTO
import com.example.kubhubsystem_gp13_dam.model.UsuarioUpdateDTO
import com.example.kubhubsystem_gp13_dam.model.toUsuario2
import com.example.kubhubsystem_gp13_dam.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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
    suspend fun obtenerTodos(): List<Usuario2> {
        return try {
            val response = usuarioService.obtenerTodos()
            if (response.isSuccessful && response.body() != null) {
                Usuario2.desdeDTOs(response.body()!!)
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
    suspend fun obtenerActivos(): List<Usuario2> {
        return try {
            val response = usuarioService.obtenerActivos()
            if (response.isSuccessful && response.body() != null) {
                Usuario2.desdeDTOs(response.body()!!)
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
    suspend fun obtenerPorId(idUsuario: Int): Usuario2? {
        return withContext(Dispatchers.IO) {
            try {
                val response = usuarioService.obtenerPorId(idUsuario)

                if (response.isSuccessful) {
                    val dto = response.body()
                    if (dto != null) {
                        println("✅ DTO recibido del backend para usuario $idUsuario:")
                        println("   fotoPerfil: ${dto.fotoPerfil?.take(100) ?: "null"}")

                        val usuario = dto.toUsuario2()
                        println("✅ Usuario2 convertido:")
                        println("   fotoPerfil: ${usuario?.fotoPerfil?.take(100) ?: "null"}")

                        usuario
                    } else {
                        println("⚠️ Response body es null para usuario $idUsuario")
                        null
                    }
                } else {
                    println("❌ Error HTTP ${response.code()}: ${response.message()}")
                    null
                }
            } catch (e: Exception) {
                println("❌ Excepción al obtener usuario $idUsuario: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Obtiene un usuario por su email
     */
    suspend fun obtenerPorEmail(email: String): Usuario2? {
        return try {
            val response = usuarioService.obtenerPorEmail(email)
            if (response.isSuccessful && response.body() != null) {
                Usuario2.desdeDTO(response.body()!!)
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
    suspend fun buscar(query: String): List<Usuario2> {
        return try {
            val response = usuarioService.buscar(query)
            if (response.isSuccessful && response.body() != null) {
                Usuario2.desdeDTOs(response.body()!!)
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
    suspend fun obtenerPorRol(rol: Rol2): List<Usuario2> {
        return try {
            val response = usuarioService.obtenerPorRol(rol.obtenerIdRol())
            if (response.isSuccessful && response.body() != null) {
                Usuario2.desdeDTOs(response.body()!!)
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
    suspend fun crear(usuario: Usuario2): Usuario2? {
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
                Usuario2.desdeDTO(response.body()!!)
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
    suspend fun actualizar(id: Int, usuario: Usuario2): Usuario2? {
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
                Usuario2.desdeDTO(response.body()!!)
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
     * Actualiza la foto de perfil de un usuario
     *
     * @param context Contexto de Android para leer la Uri
     * @param idUsuario ID del usuario
     * @param imageUri Uri de la imagen seleccionada
     * @return Usuario2 actualizado con la nueva foto, o null si hay error
     */
    suspend fun actualizarFotoPerfil(
        context: Context,
        idUsuario: Int,
        imageUri: Uri
    ): Usuario2? {
        return try {
            // Convertir Uri a ByteArray comprimido
            val imageBytes = ImageUtils.uriToByteArray(context, imageUri, maxSizeKB = 800)

            if (imageBytes == null) {
                println("❌ Error: No se pudo convertir la imagen a ByteArray")
                return null
            }

            // Crear RequestBody para la imagen
            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            // Crear MultipartBody.Part
            val fotoPart = MultipartBody.Part.createFormData(
                "foto",
                "profile_image.jpg",
                requestBody
            )

            // Llamar al endpoint
            val response = usuarioService.actualizarFotoPerfil(idUsuario, fotoPart)

            if (response.isSuccessful && response.body() != null) {
                println("✅ Foto de perfil actualizada exitosamente")
                Usuario2.desdeDTO(response.body()!!)
            } else {
                println("❌ Error en respuesta del servidor: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            println("❌ Error al actualizar foto de perfil: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina la foto de perfil de un usuario (estableciéndola en null)
     *
     * @param idUsuario ID del usuario
     * @return true si se eliminó correctamente, false en caso contrario
     */
    suspend fun eliminarFotoPerfil(idUsuario: Int): Boolean {
        return try {
            val updateDTO = UsuarioUpdateDTO(fotoPerfil = null)
            val response = usuarioService.actualizar(idUsuario, updateDTO)
            response.isSuccessful
        } catch (e: Exception) {
            println("❌ Error al eliminar foto de perfil: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * ❌ ELIMINADO: inicializarUsuarios()
     * Ya no es necesario porque los datos vienen del backend
     */
}