package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.LoginRepository
import com.example.kubhubsystem_gp13_dam.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRole: UserRole? = null
)

class LoginViewModel(
    private val repository: LoginRepository = LoginRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Actualizar email
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    // Actualizar password
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    // Actualizar checkbox de recordar sesión
    fun updateRememberSession(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberSession = remember)
    }

    // Seleccionar rol demo
    fun selectDemoRole(role: UserRole) {
        val credentials = repository.getDemoCredentials(role)
        if (credentials != null) {
            _uiState.value = _uiState.value.copy(
                email = credentials.first,
                password = credentials.second,
                selectedRole = role,
                errorMessage = null
            )
        }
    }

    // Realizar login
    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validaciones básicas
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Por favor complete todos los campos"
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.login(currentState.email, currentState.password)

            when (result) {
                null -> {
                    // Login exitoso
                    _uiState.value = currentState.copy(isLoading = false)
                    onSuccess()
                }
                "username" -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "El usuario no existe"
                    )
                }
                "password" -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Contraseña incorrecta"
                    )
                }
            }
        }
    }
}