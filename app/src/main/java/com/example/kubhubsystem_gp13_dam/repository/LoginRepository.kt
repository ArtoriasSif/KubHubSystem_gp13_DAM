package com.example.kubhubsystem_gp13_dam.repository

import android.content.Context
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.utils.TokenManager
import kotlinx.coroutines.delay

/**
 * Repositorio de autenticación - Versión 2 Refactorizado
 * ✅ Conectado directamente con API Services
 * ✅ Sin dependencias de contexto local innecesarias
 * ✅ Usa DTOs correctos del backend
 */
class LoginRepository private constructor(
    private val context: Context
) {

    private val authService = RetrofitClient.authService
    private val tokenManager = TokenManager.getInstance(context)

    /**
     * Realiza el login contra el backend
     * @return null si es exitoso, o código de error como String
     */
    suspend fun login(email: String, password: String): String? {
        println("🔥 PISTA 1: Entrando a la función login en Repository")
        return try {
            // Delay simulado para mejor UX
            delay(500)

            // Crear DTO de request
            val loginRequest = LoginRequestDTO(
                email = email.trim().lowercase(),
                contrasena = password
            )

            // Validación básica antes de enviar
            if (!loginRequest.isValid()) {
                return "invalid_format"
            }

            // Llamada al backend
            val response = authService.login(loginRequest)
            println("🔥 PISTA 2: Respuesta recibida: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val loginResponse = response.body()!!

                    println("🔥 PISTA 3: ¡Login exitoso! Intentando guardar sesión...")
                    val usuario = loginResponse.usuario

                    // Verificar si el usuario está activo
                    if (usuario.activo == false) {
                        return "inactive"
                    }

                    // Guardar sesión
                    guardarSesion(loginResponse)

                    println("✅ Login exitoso: ${usuario.email} - Rol: ${usuario.nombreRol}")
                    null // Éxito
                }
                response.code() == 401 -> {
                    println("❌ Credenciales incorrectas")
                    "password"
                }
                response.code() == 404 -> {
                    println("❌ Usuario no encontrado")
                    "email"
                }
                response.code() == 403 -> {
                    println("⚠️ Usuario inactivo")
                    "inactive"
                }
                else -> {
                    println("⚠️ Error en login: ${response.code()} - ${response.message()}")
                    "error"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Excepción en login: ${e.message}")
            "error"
        }
    }

    /**
     * Guarda la sesión del usuario en TokenManager
     */
    private fun guardarSesion(loginResponse: LoginResponseDTO) {
        println("🔥 PISTA 4: DENTRO DE GUARDAR SESIÓN")
        val usuarioResponse = loginResponse.usuario

        // Construir nombre completo
        val nombreCompleto = if (!usuarioResponse.nombreCompleto.isNullOrBlank()) {
            usuarioResponse.nombreCompleto!!
        } else {
            // Construir manualmente desde las partes
            val partes = listOfNotNull(
                usuarioResponse.primerNombre,
                usuarioResponse.segundoNombre,
                usuarioResponse.apellidoPaterno,
                usuarioResponse.apellidoMaterno
            )
            partes.joinToString(" ")
        }

        // Obtener nombre del rol
        val rolNombre = usuarioResponse.nombreRol ?: "Desconocido"

        // Guardar en TokenManager
        tokenManager.guardarSesion(
            token = loginResponse.token,
            userId = usuarioResponse.idUsuario ?: 0,
            userEmail = usuarioResponse.email ?: "",
            userRol = rolNombre,
            userName = nombreCompleto
        )
        println("✅ PISTA 5: TokenManager terminó de guardar")
        println("✅ Sesión guardada: ${usuarioResponse.email} - Rol: $rolNombre")
    }

    /**
     * Cierra sesión del usuario
     */
    suspend fun logout() {
        try {
            // Intentar logout en backend
            authService.logout()
            println("✅ Logout exitoso en backend")
        } catch (e: Exception) {
            e.printStackTrace()
            println("⚠️ Error al hacer logout en backend: ${e.message}")
        } finally {
            // Siempre limpiar sesión local
            tokenManager.limpiarSesion()
            println("✅ Sesión local limpiada")
        }
    }

    /**
     * Verifica si hay una sesión activa
     */
    fun tieneSesionActiva(): Boolean {
        return tokenManager.tieneSesionActiva()
    }

    /**
     * Obtiene el usuario actualmente logueado
     */
    fun obtenerUsuarioLogueado(): Usuario2? {
        if (!tokenManager.tieneSesionActiva()) return null

        val userId = tokenManager.obtenerUserId()
        val email = tokenManager.obtenerUserEmail() ?: return null
        val rolNombre = tokenManager.obtenerUserRol() ?: return null
        val nombreCompleto = tokenManager.obtenerUserName() ?: return null

        // Convertir nombre de rol a enum
        val rol = Rol2.desdeNombre(rolNombre) ?: Rol2.DOCENTE

        // Dividir nombre completo
        val partes = nombreCompleto.split(" ")
        val primerNombre = partes.firstOrNull() ?: ""
        val segundoNombre = if (partes.size > 2) partes[1] else ""
        val apellidoPaterno = partes.getOrNull(if (partes.size > 2) 2 else 1) ?: ""
        val apellidoMaterno = partes.lastOrNull() ?: ""

        return Usuario2(
            idUsuario = userId,
            rol = rol,
            primerNombre = primerNombre,
            segundoNombre = segundoNombre,
            apellidoPaterno = apellidoPaterno,
            apellidoMaterno = apellidoMaterno,
            email = email,
            username = email.substringBefore("@"),
            password = "", // No almacenamos la contraseña
            activo = true
        )
    }

    /**
     * Obtiene credenciales demo para un rol específico
     */
    fun getDemoCredentials(rol: Rol2): Pair<String, String>? {
        return when (rol) {
            Rol2.ADMINISTRADOR -> "admin@kuhub.cl" to "admin123"
            Rol2.CO_ADMINISTRADOR -> "ma.delara@kuhub.cl" to "matheusmago123"
            Rol2.GESTOR_PEDIDOS -> "gestor@kuhub.cl" to "gestor123"
            Rol2.PROFESOR_A_CARGO -> "profesorCargo@kuhub.cl" to "profesor123"
            Rol2.DOCENTE -> "carmen.jimenez@kuhub.cl" to "docente123"
            Rol2.ENCARGADO_BODEGA -> "bodega@kuhub.cl" to "bodega123"
            Rol2.ASISTENTE_BODEGA -> "asistente@kuhub.cl" to "asistente123"
        }
    }

    /**
     * Valida formato de email
     */
    fun validarEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valida longitud de contraseña
     */
    fun validarPassword(password: String): Boolean {
        return password.length >= 6
    }

    companion object {
        @Volatile
        private var instance: LoginRepository? = null

        fun getInstance(context: Context): LoginRepository {
            return instance ?: synchronized(this) {
                instance ?: LoginRepository(context.applicationContext).also {
                    instance = it
                    println("✅ LoginRepository inicializado")
                }
            }
        }
    }
}