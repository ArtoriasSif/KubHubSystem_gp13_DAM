package com.example.kubhubsystem_gp13_dam.data.repository

import android.content.Context
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.local.dto.LoginRequestDTO
import com.example.kubhubsystem_gp13_dam.local.dto.LoginResponseDTO
import com.example.kubhubsystem_gp13_dam.utils.TokenManager
import kotlinx.coroutines.delay

/**
 * Repositorio de autenticación
 * ✅ ACTUALIZADO: Ahora se conecta al backend Spring Boot vía Retrofit
 * ❌ ELIMINADO: Ya no usa DAOs ni base de datos local
 */
class LoginRepository private constructor(
    private val context: Context
) {

    private val authService = RetrofitClient.authService
    private val tokenManager = TokenManager.getInstance(context)

    /**
     * Realiza el login contra el backend
     *
     * @param email Correo del usuario
     * @param password Contraseña ingresada
     * @return String? → Devuelve:
     *  - "email" si el usuario no existe
     *  - "password" si la contraseña es incorrecta
     *  - "error" si hay un error de conexión
     *  - null si la autenticación es exitosa
     */
    suspend fun login(email: String, password: String): String? {
        return try {
            // Simular delay de red
            delay(1000)

            // Crear DTO de request
            val loginRequest = LoginRequestDTO(
                email = email.trim().lowercase(),
                contrasena = password
            )

            // Llamar al backend
            val response = authService.login(loginRequest)

            when {
                response.isSuccessful && response.body() != null -> {
                    // ✅ Login exitoso
                    val loginResponse = response.body()!!

                    // Guardar sesión en SharedPreferences
                    guardarSesion(loginResponse)

                    null // Login exitoso
                }
                response.code() == 401 -> {
                    // Credenciales inválidas
                    "password"
                }
                response.code() == 404 -> {
                    // Usuario no encontrado
                    "email"
                }
                else -> {
                    // Error genérico
                    "error"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "error" // Error de conexión o servidor
        }
    }

    /**
     * Guarda la información de sesión en SharedPreferences
     */
    private fun guardarSesion(loginResponse: LoginResponseDTO) {
        val usuario = loginResponse.usuario

        tokenManager.guardarSesion(
            token = loginResponse.token,
            userId = usuario.idUsuario,
            userEmail = usuario.email,
            userRol = usuario.nombreRol,
            userName = usuario.nombreCompleto
        )
    }

    /**
     * Cierra la sesión del usuario
     */
    suspend fun logout() {
        try {
            // Llamar al endpoint de logout (opcional, ya que el backend lo maneja en frontend)
            authService.logout()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Siempre limpiar la sesión local
            tokenManager.limpiarSesion()
        }
    }

    /**
     * Verifica si hay una sesión activa
     */
    fun tieneSesionActiva(): Boolean {
        return tokenManager.tieneSesionActiva()
    }

    /**
     * Obtiene el usuario logueado desde SharedPreferences
     */
    fun obtenerUsuarioLogueado(): Usuario? {
        if (!tokenManager.tieneSesionActiva()) return null

        val userId = tokenManager.obtenerUserId()
        val email = tokenManager.obtenerUserEmail() ?: return null
        val rolNombre = tokenManager.obtenerUserRol() ?: return null
        val nombreCompleto = tokenManager.obtenerUserName() ?: return null

        // Parsear el rol
        val rol = Rol.desdeNombre(rolNombre) ?: Rol.DOCENTE

        return Usuario(
            idUsuario = userId,
            rol = rol,
            primerNombre = nombreCompleto.split(" ").firstOrNull() ?: "",
            email = email,
            activo = true
        )
    }

    /**
     * Obtiene las credenciales demo para testing
     * (mantener para compatibilidad con LoginViewModel actual)
     */
    fun getDemoCredentials(rol: Rol): Pair<String, String>? {
        return when (rol) {
            Rol.ADMINISTRADOR -> "admin@kubhub.com" to "admin123"
            Rol.CO_ADMINISTRADOR -> "coadmin@kubhub.com" to "coadmin123"
            Rol.GESTOR_PEDIDOS -> "gestor@kubhub.com" to "gestor123"
            Rol.PROFESOR_A_CARGO -> "profesor@kubhub.com" to "profesor123"
            Rol.DOCENTE -> "docente@kubhub.com" to "docente123"
            Rol.ENCARGADO_BODEGA -> "bodega@kubhub.com" to "bodega123"
            Rol.ASISTENTE_BODEGA -> "asistente@kubhub.com" to "asistente123"
        }
    }

    companion object {
        @Volatile
        private var instance: LoginRepository? = null

        fun getInstance(context: Context): LoginRepository {
            return instance ?: synchronized(this) {
                instance ?: LoginRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}