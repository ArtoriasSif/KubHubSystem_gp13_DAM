package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.model.User

class LoginRepository {

    // Simulamos una base de datos o lista de usuarios registrados
    private val users = listOf(
        User(username = "admin", password = "1234"),
        User(username = "chef", password = "cocina123")
    )

    fun login(username: String, password: String): Boolean {
        return users.any { it.username == username && it.password == password }
    }
}