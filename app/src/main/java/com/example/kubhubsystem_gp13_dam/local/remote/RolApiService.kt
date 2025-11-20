package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.model.RolRequestDTO
import com.example.kubhubsystem_gp13_dam.model.RolResponseDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para gestión de Roles
 * Mapea los endpoints de RolController.java del backend
 * 
 * Base URL: configurada en RetrofitClient
 * Endpoints: /api/v1/roles
 */
interface RolApiService {

    /**
     * GET /api/v1/roles
     * Obtiene todos los roles
     */
    @GET("api/v1/roles")
    suspend fun obtenerTodos(): Response<List<RolResponseDTO>>

    /**
     * GET /api/v1/roles/activos
     * Obtiene solo los roles activos
     */
    @GET("api/v1/roles/activos")
    suspend fun obtenerActivos(): Response<List<RolResponseDTO>>

    /**
     * GET /api/v1/roles/{id}
     * Obtiene un rol por su ID
     */
    @GET("api/v1/roles/{id}")
    suspend fun obtenerPorId(@Path("id") id: Int): Response<RolResponseDTO>

    /**
     * GET /api/v1/roles/nombre/{nombre}
     * Obtiene un rol por su nombre
     */
    @GET("api/v1/roles/nombre/{nombre}")
    suspend fun obtenerPorNombre(@Path("nombre") nombre: String): Response<RolResponseDTO>

    /**
     * POST /api/v1/roles
     * Crea un nuevo rol
     */
    @POST("api/v1/roles")
    suspend fun crear(@Body rolRequest: RolRequestDTO): Response<RolResponseDTO>

    /**
     * PUT /api/v1/roles/{id}
     * Actualiza un rol existente
     */
    @PUT("api/v1/roles/{id}")
    suspend fun actualizar(
        @Path("id") id: Int,
        @Body rolRequest: RolRequestDTO
    ): Response<RolResponseDTO>

    /**
     * PATCH /api/v1/roles/{id}/desactivar
     * Desactiva un rol
     */
    @PATCH("api/v1/roles/{id}/desactivar")
    suspend fun desactivar(@Path("id") id: Int): Response<Unit>

    /**
     * PATCH /api/v1/roles/{id}/activar
     * Activa un rol
     */
    @PATCH("api/v1/roles/{id}/activar")
    suspend fun activar(@Path("id") id: Int): Response<Unit>

    /**
     * DELETE /api/v1/roles/{id}
     * Elimina un rol permanentemente
     */
    @DELETE("api/v1/roles/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>

    /**
     * GET /api/v1/roles/existe/{nombre}
     * Verifica si existe un rol con ese nombre
     */
    @GET("api/v1/roles/existe/{nombre}")
    suspend fun existePorNombre(@Path("nombre") nombre: String): Response<Boolean>
}