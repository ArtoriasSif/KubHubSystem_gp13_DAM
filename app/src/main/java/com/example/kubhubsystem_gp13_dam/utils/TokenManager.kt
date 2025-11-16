package com.example.kubhubsystem_gp13_dam.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de tokens de autenticación usando SharedPreferences
 * 
 * Almacena:
 * - Token de sesión (simulado, generado por el backend)
 * - ID del usuario logueado
 * - Rol del usuario
 * - Email del usuario
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "kubhub_auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROL = "user_rol"
        private const val KEY_USER_NAME = "user_name"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Guarda la información de sesión después del login
     */
    fun guardarSesion(
        token: String,
        userId: Int,
        userEmail: String,
        userRol: String,
        userName: String
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_ROL, userRol)
            putString(KEY_USER_NAME, userName)
            apply()
        }
    }

    /**
     * Obtiene el token guardado
     */
    fun obtenerToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Obtiene el ID del usuario logueado
     */
    fun obtenerUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * Obtiene el email del usuario logueado
     */
    fun obtenerUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Obtiene el rol del usuario logueado
     */
    fun obtenerUserRol(): String? {
        return prefs.getString(KEY_USER_ROL, null)
    }

    /**
     * Obtiene el nombre del usuario logueado
     */
    fun obtenerUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Verifica si hay una sesión activa
     */
    fun tieneSesionActiva(): Boolean {
        return obtenerToken() != null && obtenerUserId() != -1
    }

    /**
     * Limpia toda la información de sesión (logout)
     */
    fun limpiarSesion() {
        prefs.edit().clear().apply()
    }

    /**
     * Actualiza solo el token (si fuera necesario renovarlo)
     */
    fun actualizarToken(nuevoToken: String) {
        prefs.edit().putString(KEY_TOKEN, nuevoToken).apply()
    }
}