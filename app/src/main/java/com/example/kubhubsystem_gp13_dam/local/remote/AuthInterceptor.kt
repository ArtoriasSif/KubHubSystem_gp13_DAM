package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que agrega el token JWT a todas las peticiones autenticadas
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Si la petición es a /login, no agregar token
        if (originalRequest.url.encodedPath.contains("/login")) {
            return chain.proceed(originalRequest)
        }

        // Obtener token actual
        val token = tokenManager.obtenerToken()

        // Si no hay token, continuar sin agregarlo
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Agregar token al header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        println("🔐 Token agregado a request: ${originalRequest.url}")

        return chain.proceed(authenticatedRequest)
    }
}