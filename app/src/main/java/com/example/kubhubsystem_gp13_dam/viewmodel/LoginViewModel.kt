package com.example.kubhubsystem_gp13_dam.viewmodel

import android.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kubhubsystem_gp13_dam.model.ErrorType
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
        // Si algún campo está vacío
        if (username.isBlank() && password.isBlank()) {
            userError = UserError(
                type = ErrorType.BOTH,
                message = "Por favor, completa ambos campos"
            )
            return
        } else if (username.isBlank()) {
            userError = UserError(
                type = ErrorType.USERNAME,
                message = "Por favor, ingresa el usuario"
            )
            return
        } else if (password.isBlank()) {
            userError = UserError(
                type = ErrorType.PASSWORD,
                message = "Por favor, ingresa la contraseña"
            )
            return
        }

        // Llamada al repositorio
        val errorType = repository.login(username, password)

        // Asignar UserError según lo que retorne el repositorio
        userError = when (errorType) {
            null -> null // login correcto
            "username" -> UserError(
                type = ErrorType.USERNAME,
                message = "El usuario no existe"
            )
            "password" -> UserError(
                type = ErrorType.PASSWORD,
                message = "Contraseña incorrecta"
            )
            else -> UserError(
                type = ErrorType.BOTH,
                message = "Usuario o contraseña incorrectos"
            )
        }
    }

    fun clearFields() {
        username = ""
        password = ""
    }
}