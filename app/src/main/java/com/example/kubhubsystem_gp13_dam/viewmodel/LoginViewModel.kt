package com.example.kubhubsystem_gp13_dam.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.LoginRepository
import com.example.kubhubsystem_gp13_dam.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para Login
 * ✅ ACTUALIZADO: Ahora usa el nuevo LoginRepository que se conecta al backend
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRole: Rol? = null,
    val forgotPasswordRequested: Boolean = false
)

class LoginViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {

    constructor(context: Context) : this(LoginRepository.getInstance(context))

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        println("🧹 LoginViewModel: Limpiando recursos")
    }

    // ========================================
    // MÉTODOS DE ACTUALIZACIÓN DE ESTADO
    // ========================================

    fun updateEmail(email: String) {
        if (_uiState.value.email != email) {
            _uiState.update { currentState ->
                currentState.copy(email = email, errorMessage = null)
            }
        }
    }

    fun updatePassword(password: String) {
        if (_uiState.value.password != password) {
            _uiState.update { currentState ->
                currentState.copy(password = password, errorMessage = null)
            }
        }
    }

    fun updateRememberSession(remember: Boolean) {
        if (_uiState.value.rememberSession != remember) {
            _uiState.update { currentState ->
                currentState.copy(rememberSession = remember)
            }
        }
    }

    fun updateForgotPasswordRequest(requested: Boolean) {
        if (_uiState.value.forgotPasswordRequested != requested) {
            _uiState.update { currentState ->
                currentState.copy(forgotPasswordRequested = requested)
            }
        }
    }

    // ========================================
    // SELECCIÓN DE ROLES DEMO
    // ========================================

    /**
     * Selecciona un rol demo y llena automáticamente email y password
     * Útil para testing
     */
    fun selectDemoRole(role: Rol) {
        val credentials = loginRepository.getDemoCredentials(role)
        if (credentials != null) {
            val currentState = _uiState.value
            if (currentState.email != credentials.first ||
                currentState.password != credentials.second ||
                currentState.selectedRole != role) {

                _uiState.update {
                    it.copy(
                        email = credentials.first,
                        password = credentials.second,
                        selectedRole = role,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun clearDemoSelection() {
        val currentState = _uiState.value
        if (currentState.email.isNotEmpty() ||
            currentState.password.isNotEmpty() ||
            currentState.selectedRole != null) {

            _uiState.update {
                it.copy(
                    email = "",
                    password = "",
                    selectedRole = null,
                    errorMessage = null
                )
            }
        }
    }

    // ========================================
    // LOGIN
    // ========================================

    /**
     * Ejecuta el login contra el backend
     * ✅ ACTUALIZADO: Ahora se conecta al backend Spring Boot
     */
    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validaciones básicas
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Por favor complete todos los campos obligatorios")
            }
            return
        }

        // Mostrar indicador de carga
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                // ✅ LLAMADA AL BACKEND
                val result = loginRepository.login(currentState.email, currentState.password)

                when (result) {
                    null -> {
                        // ✅ Login exitoso
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        onSuccess()
                    }
                    "email" -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "El usuario no existe"
                            )
                        }
                    }
                    "password" -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Contraseña incorrecta"
                            )
                        }
                    }
                    "error" -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error de conexión. Verifique su red."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
}