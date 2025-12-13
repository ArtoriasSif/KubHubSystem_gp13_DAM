package com.example.kubhubsystem_gp13_dam.manager

import android.content.Context
import android.content.SharedPreferences

class TokenManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "kuhub_session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROL = "user_rol"
        private const val KEY_USER_NAME = "user_name"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Guarda la sesión completa
     */
    fun guardarSesion(
        token: String,
        userId: Int,
        userEmail: String,
        userRol: String,
        userName: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_ROL, userRol)
            putString(KEY_USER_NAME, userName)
            apply()
        }
        println("✅ Sesión guardada - Token: ${token.take(20)}...")
    }

    /**
     * Obtiene el token JWT
     */
    fun obtenerToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    /**
     * Obtiene el ID del usuario
     */
    fun obtenerUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, 0)
    }

    /**
     * Obtiene el email del usuario
     */
    fun obtenerUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Obtiene el rol del usuario
     */
    fun obtenerUserRol(): String? {
        return sharedPreferences.getString(KEY_USER_ROL, null)
    }

    /**
     * Obtiene el nombre del usuario
     */
    fun obtenerUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    /**
     * Verifica si hay sesión activa
     */
    fun tieneSesionActiva(): Boolean {
        val token = obtenerToken()
        return !token.isNullOrBlank()
    }

    /**
     * Limpia la sesión
     */
    fun limpiarSesion() {
        sharedPreferences.edit().clear().apply()
        println("🧹 Sesión limpiada")
    }
}