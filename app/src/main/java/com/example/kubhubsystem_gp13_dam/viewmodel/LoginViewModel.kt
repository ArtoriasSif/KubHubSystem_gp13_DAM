package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kubhubsystem_gp13_dam.model.UserError
import com.example.kubhubsystem_gp13_dam.repository.LoginRepository


class LoginViewModel(
    private val repository: LoginRepository = LoginRepository()
) : ViewModel() {

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    // AHORA SE USA UserError EN VEZ DE errorMessage
    var userError by mutableStateOf<UserError?>(null)
        private set

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            userError = UserError(message = "Por favor, completa ambos campos")
            return
        }

        val success = repository.login(username, password)

        if (success) {
            userError = null
        } else {
            userError = UserError(message = "Usuario o contraseña incorrectos")
        }
    }

    fun clearFields() {
        username = ""
        password = ""
    }
}