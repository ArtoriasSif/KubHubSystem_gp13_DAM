package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.model.User

class LoginRepository {

    // Tu misma lista de usuarios
    private val users = listOf(
        User(username = "admin", password = "1234"),
        User(username = "chef", password = "cocina123")
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
}