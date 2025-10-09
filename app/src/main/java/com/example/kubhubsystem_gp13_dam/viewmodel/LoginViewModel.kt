package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.LoginRepository
import com.example.kubhubsystem_gp13_dam.model.loginUsers.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
// DATA CLASS: ESTADO DE LA INTERFAZ DE LOGIN
// =====================================================================
/**
 * Representa el estado de la UI de login.
 *
 * Contiene todos los valores que afectan la interfaz:
 * - email, password
 * - rememberSession: checkbox
 * - isLoading: indicador de carga
 * - errorMessage: mensajes de error a mostrar
 * - selectedRole: rol demo seleccionado
 * - forgotPasswordRequested: estado de solicitud de recuperación de contraseña
 */
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


// =====================================================================
// VIEWMODEL: LOGIN
// =====================================================================
/**
 * Maneja toda la lógica de login, controlando:
 * - Actualización de campos (email, password, checkbox)
 * - Selección de roles demo
 * - Login real a través del [LoginRepository]
 * - Recuperación de contraseña
 *
 * Expone un estado [uiState] inmutable que la UI puede observar.
 */
class LoginViewModel(
    // Repositorio para interactuar con la fuente de datos de login, se utiliza el patrón Singleton para que solo haya una instancia de LoginRepository
    private val repository: LoginRepository = LoginRepository.getInstance()
) : ViewModel() {

    // =================================================================
    // CICLO DE VIDA DEL VIEWMODEL
    // =================================================================
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

    // =================================================================
    // MÉTODOS DE ACTUALIZACIÓN DE ESTADO
    // =================================================================

    // ✅ Actualizar email - OPTIMIZADO: Solo actualiza si realmente cambió
    fun updateEmail(email: String) {
        /**
         * Para actualizar se realiza una copia del estado actual y se actualiza el campo correspondiente
         * Verificamos primero si el valor es diferente para evitar recomposiciones innecesarias
         * Actualiza el email si cambió, limpia errores previos
         * */
        if (_uiState.value.email != email) {
            _uiState.update { currentState ->
                currentState.copy(email = email, errorMessage = null)
            }
        }
    }

    /** Actualiza la contraseña si cambió, limpia errores previos */
    fun updatePassword(password: String) {
        if (_uiState.value.password != password) {
            _uiState.update { currentState ->
                currentState.copy(password = password, errorMessage = null)
            }
        }
    }

    /** Actualiza el estado del checkbox de recordar sesión */
    fun updateRememberSession(remember: Boolean) {
        if (_uiState.value.rememberSession != remember) {
            _uiState.update { currentState ->
                currentState.copy(rememberSession = remember)
            }
        }
    }

    /** Actualiza el estado de solicitud de recuperación de contraseña */
    fun updateForgotPasswordRequest(requested: Boolean) {
        if (_uiState.value.forgotPasswordRequested != requested) {
            _uiState.update { currentState ->
                currentState.copy(forgotPasswordRequested = requested)
            }
        }
    }

    // =================================================================
    // SELECCIÓN DE ROLES DEMO
    // =================================================================
    /**
     * Selecciona un rol demo y llena automáticamente email y password.
     * Evita recomposiciones innecesarias verificando cambios.
     */
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

    /** Limpia la selección de rol demo y los campos de credenciales */
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

    // =================================================================
    // LOGIN
    // =================================================================
    /**
     * Ejecuta el login real usando [LoginRepository].
     *
     * Pasos:
     * 1. Valida que email y password no estén vacíos.
     * 2. Muestra indicador de carga.
     * 3. Llama al repositorio para verificar credenciales.
     * 4. Actualiza el estado según el resultado:
     *    - Login exitoso -> llama a `onSuccess()` es una lambda que se ejecutará si el login es exitoso
     *    - Usuario no existe -> mensaje de error
     *    - Contraseña incorrecta -> mensaje de error
     * 5. Manejo de errores inesperados con try-catch.
     */
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
     */
}