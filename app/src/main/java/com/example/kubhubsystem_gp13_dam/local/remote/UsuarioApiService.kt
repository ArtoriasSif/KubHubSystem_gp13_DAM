package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioEstadisticasDTO
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioRequestDTO
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioResponseDTO
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioUpdateDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para gestión de Usuarios
 * Mapea los endpoints de UsuarioController.java del backend
 * 
 * Base URL: configurada en RetrofitClient
 * Endpoints: /api/v1/usuarios
 */
interface UsuarioApiService {

    /**
     * GET /api/v1/usuarios
     * Obtiene todos los usuarios
     */
    @GET("api/v1/usuarios")
    suspend fun obtenerTodos(): Response<List<UsuarioResponseDTO>>

    /**
     * GET /api/v1/usuarios/activos
     * Obtiene solo los usuarios activos
     */
    @GET("api/v1/usuarios/activos")
    suspend fun obtenerActivos(): Response<List<UsuarioResponseDTO>>

    /**
     * GET /api/v1/usuarios/{id}
     * Obtiene un usuario por su ID
     */
    @GET("api/v1/usuarios/{id}")
    suspend fun obtenerPorId(@Path("id") id: Int): Response<UsuarioResponseDTO>

    /**
     * GET /api/v1/usuarios/email/{email}
     * Obtiene un usuario por su email
     */
    @GET("api/v1/usuarios/email/{email}")
    suspend fun obtenerPorEmail(@Path("email") email: String): Response<UsuarioResponseDTO>

    /**
     * GET /api/v1/usuarios/buscar?q=termino
     * Busca usuarios por nombre o email
     */
    @GET("api/v1/usuarios/buscar")
    suspend fun buscar(@Query("q") query: String): Response<List<UsuarioResponseDTO>>

    /**
     * GET /api/v1/usuarios/rol/{idRol}
     * Obtiene usuarios por rol
     */
    @GET("api/v1/usuarios/rol/{idRol}")
    suspend fun obtenerPorRol(@Path("idRol") idRol: Int): Response<List<UsuarioResponseDTO>>

    /**
     * POST /api/v1/usuarios
     * Crea un nuevo usuario
     */
    @POST("api/v1/usuarios")
    suspend fun crear(@Body usuarioRequest: UsuarioRequestDTO): Response<UsuarioResponseDTO>

    /**
     * PUT /api/v1/usuarios/{id}
     * Actualiza un usuario existente
     */
    @PUT("api/v1/usuarios/{id}")
    suspend fun actualizar(
        @Path("id") id: Int,
        @Body usuarioUpdate: UsuarioUpdateDTO
    ): Response<UsuarioResponseDTO>

    /**
     * PATCH /api/v1/usuarios/{id}/desactivar
     * Desactiva un usuario
     */
    @PATCH("api/v1/usuarios/{id}/desactivar")
    suspend fun desactivar(@Path("id") id: Int): Response<Unit>

    /**
     * PATCH /api/v1/usuarios/{id}/activar
     * Activa un usuario
     */
    @PATCH("api/v1/usuarios/{id}/activar")
    suspend fun activar(@Path("id") id: Int): Response<Unit>

    /**
     * DELETE /api/v1/usuarios/{id}
     * Elimina un usuario permanentemente
     */
    @DELETE("api/v1/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>

    /**
     * PATCH /api/v1/usuarios/{id}/cambiar-contrasena
     * Cambia la contraseña de un usuario
     */
    @PATCH("api/v1/usuarios/{id}/cambiar-contrasena")
    suspend fun cambiarContrasena(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Unit>

    /**
     * PATCH /api/v1/usuarios/{id}/foto-perfil
     * Actualiza la foto de perfil
     * TODO: Implementar cuando se requiera manejo de imágenes
     */
    @PATCH("api/v1/usuarios/{id}/foto-perfil")
    suspend fun actualizarFotoPerfil(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<UsuarioResponseDTO>

    /**
     * GET /api/v1/usuarios/estadisticas
     * Obtiene estadísticas de usuarios
     */
    @GET("api/v1/usuarios/estadisticas")
    suspend fun obtenerEstadisticas(): Response<UsuarioEstadisticasDTO>
}