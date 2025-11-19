package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.local.dto.LoginResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API Service para autenticación
 * Mapea los endpoints de AuthController.java del backend
 * 
 * Base URL: configurada en RetrofitClient
 * Endpoints: /api/v1/auth
 */
interface AuthApiService {

    /**
     * POST /api/v1/auth/login
     * Realiza el login de un usuario
     * 
     * @param loginRequest DTO con email y contraseña
     * @return LoginResponseDTO con usuario, token y mensaje
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body loginRequest: com.example.kubhubsystem_gp13_dam.model.LoginRequestDTO): Response<LoginResponseDTO>

    /**
     * POST /api/v1/auth/logout
     * Cierra la sesión (placeholder - el logout es manejado en el frontend)
     * 
     * En una implementación real con JWT, aquí se invalidaría el token
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>
}