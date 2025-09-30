package com.example.kubhubsystem_gp13_dam.ui.theme.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kubhubsystem_gp13_dam.ui.theme.repository.LoginRepository

class LoginViewModel(
    private val repository: LoginRepository = LoginRepository()
) : ViewModel() {

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Por favor, completa ambos campos"
            return
        }

        val success = repository.login(username, password)
        if (success) {
            errorMessage = null
            // TODO: Acción cuando el login es exitoso
        } else {
            errorMessage = "Usuario o contraseña incorrectos"
        }
    }
}