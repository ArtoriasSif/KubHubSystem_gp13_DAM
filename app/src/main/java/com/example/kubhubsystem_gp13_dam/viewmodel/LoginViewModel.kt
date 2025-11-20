package com.example.kubhubsystem_gp13_dam.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.model.Rol2
import com.example.kubhubsystem_gp13_dam.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para Login
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberSession: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRole: Rol2? = null,
    val forgotPasswordRequested: Boolean = false,
    val loginSuccessful: Boolean = false
)

/**
 * ViewModel para Login - Versión 2 Refactorizado
 * ✅ Maneja toda la lógica de autenticación
 * ✅ Usa LoginRepository para comunicación con backend
 * ✅ Proporciona estado reactivo para la UI
 */
class LoginViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {

    constructor(context: Context) : this(LoginRepository.getInstance(context))

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        println("✅ LoginViewModel2 inicializado")
        verificarSesionActiva()
    }

    override fun onCleared() {
        super.onCleared()
        println("🧹 LoginViewModel2: Limpiando recursos")
    }

    /**
     * Verifica si existe una sesión activa al iniciar
     */
    private fun verificarSesionActiva() {
        val tieneSesion = loginRepository.tieneSesionActiva()
        if (tieneSesion) {
            println("✅ Sesión activa detectada")
            val usuario = loginRepository.obtenerUsuarioLogueado()
            println("   Usuario: ${usuario?.email} - Rol: ${usuario?.rol?.nombreRol}")
        } else {
            println("ℹ️ No hay sesión activa")
        }
    }

    /**
     * Actualiza el email ingresado
     */
    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                errorMessage = null
            )
        }
    }

    /**
     * Actualiza la contraseña ingresada
     */
    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                errorMessage = null
            )
        }
    }

    /**
     * Actualiza el estado de "recordar sesión"
     */
    fun updateRememberSession(remember: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(rememberSession = remember)
        }
    }

    /**
     * Marca que se solicitó recuperación de contraseña
     */
    fun updateForgotPasswordRequest(requested: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(forgotPasswordRequested = requested)
        }
    }

    /**
     * Selecciona un rol demo y autocompleta credenciales
     */
    fun selectDemoRole(role: Rol2) {
        val credentials = loginRepository.getDemoCredentials(role)
        if (credentials != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    email = credentials.first,
                    password = credentials.second,
                    selectedRole = role,
                    errorMessage = null
                )
            }
            println("✅ Rol demo seleccionado: ${role.nombreRol}")
        } else {
            println("⚠️ No se encontraron credenciales para el rol: ${role.nombreRol}")
        }
    }

    /**
     * Limpia la selección de cuenta demo
     */
    fun clearDemoSelection() {
        _uiState.update { currentState ->
            currentState.copy(
                email = "",
                password = "",
                selectedRole = null,
                errorMessage = null
            )
        }
        println("🧹 Selección de demo limpiada")
    }

    /**
     * Valida los campos de email y contraseña
     */
    private fun validarCampos(): String? {
        val currentState = _uiState.value

        if (currentState.email.isEmpty()) {
            return "Por favor ingrese su correo electrónico"
        }

        if (currentState.password.isEmpty()) {
            return "Por favor ingrese su contraseña"
        }

        if (!loginRepository.validarEmail(currentState.email)) {
            return "El formato del correo electrónico es inválido"
        }

        if (!loginRepository.validarPassword(currentState.password)) {
            return "La contraseña debe tener al menos 6 caracteres"
        }

        return null
    }

    /**
     * Realiza el login con las credenciales actuales
     */
    fun login(onSuccess: () -> Unit) {
        // Validar campos primero
        val validationError = validarCampos()
        if (validationError != null) {
            _uiState.update {
                it.copy(errorMessage = validationError)
            }
            println("❌ Validación fallida: $validationError")
            return
        }

        val currentState = _uiState.value

        // Mostrar loading
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }

        println("🔄 Intentando login para: ${currentState.email}")

        viewModelScope.launch {
            try {
                val result = loginRepository.login(currentState.email, currentState.password)

                when (result) {
                    null -> {
                        // Login exitoso
                        println("✅ Login exitoso para: ${currentState.email}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginSuccessful = true,
                                errorMessage = null
                            )
                        }
                        onSuccess()
                    }
                    "email" -> {
                        println("❌ Usuario no encontrado: ${currentState.email}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "El correo electrónico no está registrado en el sistema"
                            )
                        }
                    }
                    "password" -> {
                        println("❌ Contraseña incorrecta para: ${currentState.email}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "La contraseña ingresada es incorrecta"
                            )
                        }
                    }
                    "inactive" -> {
                        println("⚠️ Usuario inactivo: ${currentState.email}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Su cuenta está inactiva. Contacte al administrador."
                            )
                        }
                    }
                    "invalid_format" -> {
                        println("❌ Formato inválido")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "El formato de las credenciales es inválido"
                            )
                        }
                    }
                    "error" -> {
                        println("❌ Error de conexión")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error de conexión. Verifique su red e intente nuevamente."
                            )
                        }
                    }
                    else -> {
                        println("❌ Error desconocido: $result")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error inesperado. Por favor intente más tarde."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Excepción en login: ${e.message}")
                e.printStackTrace()
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
     * Cierra la sesión del usuario
     */
    fun logout() {
        viewModelScope.launch {
            try {
                loginRepository.logout()
                _uiState.update {
                    LoginUiState()
                }
                println("✅ Logout completado")
            } catch (e: Exception) {
                println("❌ Error en logout: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Limpia el mensaje de error actual
     */
    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    /**
     * Obtiene el usuario actualmente logueado
     */
    fun obtenerUsuarioLogueado() = loginRepository.obtenerUsuarioLogueado()

    /**
     * Verifica si hay sesión activa
     */
    fun tieneSesionActiva() = loginRepository.tieneSesionActiva()
}