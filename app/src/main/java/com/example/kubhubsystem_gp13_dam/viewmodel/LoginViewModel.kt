package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.LoginRepository
import com.example.kubhubsystem_gp13_dam.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class LoginUiState(
    // Representan el estado actual de la interfaz de login
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRole: UserRole? = null,
    val forgotPasswordRequested: Boolean = false
)

// ViewModel se encarga de manejar la lógica y actualizar el estado de la interfaz de login
class LoginViewModel(
    // Repositorio para interactuar con la fuente de datos de login, se utiliza el patrón Singleton para que solo haya una instancia de LoginRepository
    private val repository: LoginRepository = LoginRepository.getInstance()
) : ViewModel() {

    // ✅ Limpiar recursos cuando el ViewModel se destruye
    override fun onCleared() {
        super.onCleared()
        // Cancelar todas las coroutines pendientes automáticamente con viewModelScope
        println("🧹 LoginViewModel: Limpiando recursos")
    }
    // MutableStateFlow Estado mutable del ViewModel que guarda el estado actual de la interfaz cada vez que su contenido cambia.
    private val _uiState = MutableStateFlow(LoginUiState())
    //.asStateFlow() Estado inmutable del ViewModel que expone el estado actual de la interfaz a los observadores (solo lectura)
    //.asStateFlow() convierte un MutableStateFlow en un StateFlow. El viewmodel puede modificar el estado (porque usa _uiState, que es una MutableStateFlow)
    /**
    Básicamente, lo que hace esta parte de la lógica es tomar el estado actual del ViewModel,
    que es mutable y privado (controlado solo dentro del propio ViewModel),
    y transformarlo en un estado inmutable.
    De esta forma, la interfaz solo puede leer y observar los cambios del estado,
    mientras que la lógica de modificación queda protegida dentro del ViewModel.
     */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow() //_uiState declarado de esta forma para indicar que es privado

    // ✅ Actualizar email - OPTIMIZADO: Solo actualiza si realmente cambió
    fun updateEmail(email: String) {
        // Para actualizar se realiza una copia del estado actual y se actualiza el campo correspondiente
        // ✅ Verificamos primero si el valor es diferente para evitar recomposiciones innecesarias
        if (_uiState.value.email != email) {
            _uiState.update { currentState ->
                currentState.copy(email = email, errorMessage = null)
            }
        }
    }

    // ✅ Actualizar password - OPTIMIZADO: Solo actualiza si realmente cambió
    fun updatePassword(password: String) {
        if (_uiState.value.password != password) {
            _uiState.update { currentState ->
                currentState.copy(password = password, errorMessage = null)
            }
        }
    }

    // ✅ Actualizar checkbox de recordar sesión - OPTIMIZADO
    fun updateRememberSession(remember: Boolean) {
        if (_uiState.value.rememberSession != remember) {
            _uiState.update { currentState ->
                currentState.copy(rememberSession = remember)
            }
        }
    }

    // ✅ Actualizar estado de solicitud de recuperación de contraseña - OPTIMIZADO
    fun updateForgotPasswordRequest(requested: Boolean) {
        if (_uiState.value.forgotPasswordRequested != requested) {
            _uiState.update { currentState ->
                currentState.copy(forgotPasswordRequested = requested)
            }
        }
    }

    // ✅ Seleccionar rol demo - OPTIMIZADO
    fun selectDemoRole(role: UserRole) {
        val credentials = repository.getDemoCredentials(role)
        if (credentials != null) {
            // Solo actualiza si las credenciales o el rol son diferentes
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

    // ✅ Limpiar selección de rol demo y campos de credenciales - OPTIMIZADO
    fun clearDemoSelection() {
        val currentState = _uiState.value
        // Solo limpia si hay algo que limpiar
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

    // Realizar login
    // onSuccess es una lambda que se ejecutará si el login es exitoso
    fun login(onSuccess: () -> Unit) {
        // Obtener una copia del estado actual del ViewModel
        val currentState = _uiState.value

        // Validaciones básicas de campos obligatorios
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Por favor complete todos los campos obligatorios")
            }
            return
        }

        // Si no hay errores, se realiza el login, actualiza el estado para mostrar un indicador de carga y limpia error previo
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }

        // viewModelScope.launch ejecuta el código sin bloquear la interfaz.
        viewModelScope.launch {
            try {
                val result = repository.login(currentState.email, currentState.password)

                when (result) {
                    null -> {
                        //✅ Login exitoso, Detiene el indicador de carga, Llama a la función onSuccess() (navegar a siguiente screen).
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                        onSuccess()
                    }
                    //❌ Errores: Detiene la carga, Muestra el mensaje de error correspondiente.
                    "username" -> {
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
                }
            } catch (e: Exception) {
                // ✅ Manejo de errores inesperados
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
    /**
     * Básicamente, esta función se encarga de manejar todo el proceso de inicio de sesión:
     *
     * 1. Toma el estado actual del ViewModel (email y contraseña).
     * 2. Valida que ambos campos no estén vacíos; si alguno falta, muestra un mensaje de error y detiene el proceso.
     * 3. Cambia el estado para mostrar un indicador de carga mientras se realiza el login.
     * 4. Llama al repositorio para intentar iniciar sesión de manera asíncrona.
     * 5. Según la respuesta del repositorio:
     *      - Si el login es exitoso, detiene la carga y llama a `onSuccess()` para que la UI pueda continuar (por ejemplo, navegar a otra pantalla).
     *      - Si hay un error, detiene la carga y muestra un mensaje específico de error en la interfaz.
     *
     * En pocas palabras: controla la lógica del login, protege el estado interno y notifica a la UI
     * de cualquier cambio de forma segura y reactiva.
     *
     * ✅ OPTIMIZACIONES APLICADAS:
     * - Uso de .update {} en lugar de .value = para actualizaciones thread-safe
     * - Verificación de cambios antes de actualizar el estado (evita recomposiciones innecesarias)
     * - Try-catch para manejar errores inesperados
     * - Menos asignaciones de memoria al reutilizar el estado actual
     */
}