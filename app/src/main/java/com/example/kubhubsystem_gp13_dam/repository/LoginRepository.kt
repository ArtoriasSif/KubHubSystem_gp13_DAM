package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.User
import com.example.kubhubsystem_gp13_dam.model.UserRole

class LoginRepository {

    // Lista completa de usuarios con roles
    private val users = listOf(
        User(
            username = "admin@kubhub.com",
            password = "admin123",
            role = UserRole.ADMIN,
            displayName = "Administrador"
        ),
        User(
            username = "coadmin@kubhub.com",
            password = "coadmin123",
            role = UserRole.CO_ADMIN,
            displayName = "Co-Administrador"
        ),
        User(
            username = "gestor@kubhub.com",
            password = "gestor123",
            role = UserRole.GESTOR_PEDIDOS,
            displayName = "Gestor de Pedidos"
        ),
        User(
            username = "profesor@kubhub.com",
            password = "profesor123",
            role = UserRole.PROFESOR,
            displayName = "Profesor"
        ),
        User(
            username = "bodega@kubhub.com",
            password = "bodega123",
            role = UserRole.BODEGA,
            displayName = "Bodeguero"
        ),
        User(
            username = "asistente@kubhub.com",
            password = "asistente123",
            role = UserRole.ASISTENTE,
            displayName = "Asistente"
        )
    )

    /**
     * Retorna:
     *  - null → login correcto
     *  - "username" → usuario no existe
     *  - "password" → contraseña incorrecta
     */
    fun login(username: String, password: String): String? {
        val user = users.find { it.username == username }

        return when {
            user == null -> "username"
            user.password != password -> "password"
            else -> null
        }
    }

    /**
     * Obtiene las credenciales demo por rol
     */
    fun getDemoCredentials(role: UserRole): Pair<String, String>? {
        val user = users.find { it.role == role }
        return user?.let { it.username to it.password }
    }

    /**
     * Obtiene el usuario por username
     */
    fun getUserByUsername(username: String): User? {
        return users.find { it.username == username }
    }

    companion object {
        @Volatile
        private var instance: LoginRepository? = null

        fun getInstance(): LoginRepository {
            return instance ?: synchronized(this) {
                instance ?: LoginRepository().also { instance = it }
            }
        }
    }
}