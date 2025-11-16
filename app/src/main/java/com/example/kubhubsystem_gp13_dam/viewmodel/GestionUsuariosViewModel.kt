package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.local.dto.RolResponseDTO
import com.example.kubhubsystem_gp13_dam.local.dto.UsuarioEstadisticasDTO
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para Gestión de Usuarios
 * ✅ ACTUALIZADO: Ahora usa los nuevos repositorios que se conectan al backend
 * ❌ ELIMINADO: Ya no maneja DocenteRepository ni inicialización de datos locales
 */
data class GestionUsuariosEstado(
    val usuarios: List<Usuario> = emptyList(),
    val usuariosFiltrados: List<Usuario> = emptyList(),
    val roles: List<RolResponseDTO> = emptyList(),
    val estadisticas: UsuarioEstadisticasDTO? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val buscarTexto: String = "",
    val filtroRol: String = "Todos"
)

class GestionUsuariosViewModel : ViewModel() {

    private val usuarioRepository = UsuarioRepository()
    private val rolRepository = RolRepository()

    private val _estado = MutableStateFlow(GestionUsuariosEstado())
    val estado: StateFlow<GestionUsuariosEstado> = _estado.asStateFlow()

    init {
        cargarDatosCompletos()
    }

    /**
     * Carga todos los datos necesarios para la pantalla
     * ✅ ACTUALIZADO: Ahora consulta el backend
     */
    fun cargarDatosCompletos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            try {
                // Cargar usuarios
                val usuarios = usuarioRepository.obtenerTodos()

                // Cargar roles
                val roles = rolRepository.obtenerTodos()

                // Cargar estadísticas
                val estadisticas = usuarioRepository.obtenerEstadisticas()

                _estado.update { it.copy(
                    usuarios = usuarios,
                    usuariosFiltrados = aplicarFiltros(usuarios),
                    roles = roles,
                    estadisticas = estadisticas,
                    cargando = false
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al cargar datos: ${e.message}",
                    cargando = false
                ) }
            }
        }
    }

    /**
     * Carga solo la lista de usuarios
     */
    fun cargarUsuarios() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }

            try {
                val usuarios = usuarioRepository.obtenerTodos()
                _estado.update { it.copy(
                    usuarios = usuarios,
                    usuariosFiltrados = aplicarFiltros(usuarios),
                    cargando = false
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al cargar usuarios: ${e.message}",
                    cargando = false
                ) }
            }
        }
    }

    /**
     * Actualiza el filtro por rol
     */
    fun onFiltroRolChange(filtroRol: String) {
        _estado.update { it.copy(filtroRol = filtroRol) }
        aplicarFiltros()
    }

    /**
     * Actualiza el texto de búsqueda
     */
    fun onBuscarTextoChange(buscarTexto: String) {
        _estado.update { it.copy(buscarTexto = buscarTexto) }
        aplicarFiltros()
    }

    /**
     * Elimina un usuario
     * ✅ ACTUALIZADO: Ahora elimina vía API
     */
    fun eliminarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val exitoso = usuarioRepository.eliminar(usuario.idUsuario)

                if (exitoso) {
                    cargarDatosCompletos()
                    _estado.update { it.copy(
                        mensajeExito = "Usuario eliminado correctamente"
                    ) }
                } else {
                    _estado.update { it.copy(
                        error = "No se pudo eliminar el usuario"
                    ) }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al eliminar usuario: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Crea un nuevo usuario
     * ✅ ACTUALIZADO: Ahora crea vía API
     */
    fun crearUsuario(
        primerNombre: String,
        segundoNombre: String?,
        apellidoPaterno: String?,
        apellidoMaterno: String?,
        email: String,
        username: String?,
        password: String,
        rol: Rol
    ) {
        viewModelScope.launch {
            try {
                val nuevoUsuario = Usuario(
                    idUsuario = 0,
                    rol = rol,
                    primerNombre = primerNombre,
                    segundoNombre = segundoNombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    email = email,
                    username = username,
                    password = password
                )

                val usuarioCreado = usuarioRepository.crear(nuevoUsuario)

                if (usuarioCreado != null) {
                    cargarDatosCompletos()
                    _estado.update { it.copy(
                        mensajeExito = "Usuario creado correctamente"
                    ) }
                } else {
                    _estado.update { it.copy(
                        error = "No se pudo crear el usuario"
                    ) }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al crear usuario: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Actualiza un usuario existente
     * ✅ ACTUALIZADO: Ahora actualiza vía API
     */
    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val usuarioActualizado = usuarioRepository.actualizar(usuario.idUsuario, usuario)

                if (usuarioActualizado != null) {
                    cargarDatosCompletos()
                    _estado.update { it.copy(
                        mensajeExito = "Usuario actualizado correctamente"
                    ) }
                } else {
                    _estado.update { it.copy(
                        error = "No se pudo actualizar el usuario"
                    ) }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al actualizar usuario: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Desactiva un usuario
     */
    fun desactivarUsuario(id: Int) {
        viewModelScope.launch {
            try {
                val exitoso = usuarioRepository.desactivar(id)
                if (exitoso) {
                    cargarDatosCompletos()
                    _estado.update { it.copy(
                        mensajeExito = "Usuario desactivado correctamente"
                    ) }
                } else {
                    _estado.update { it.copy(
                        error = "No se pudo desactivar el usuario"
                    ) }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al desactivar usuario: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Activa un usuario
     */
    fun activarUsuario(id: Int) {
        viewModelScope.launch {
            try {
                val exitoso = usuarioRepository.activar(id)
                if (exitoso) {
                    cargarDatosCompletos()
                    _estado.update { it.copy(
                        mensajeExito = "Usuario activado correctamente"
                    ) }
                } else {
                    _estado.update { it.copy(
                        error = "No se pudo activar el usuario"
                    ) }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al activar usuario: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Limpia los mensajes de error y éxito
     */
    fun limpiarMensajes() {
        _estado.update { it.copy(
            error = null,
            mensajeExito = null
        ) }
    }

    /**
     * Obtiene un usuario por su ID
     */
    fun obtenerUsuarioPorId(id: Int): Usuario? {
        return estado.value.usuarios.find { it.idUsuario == id }
    }

    /**
     * Verifica si un usuario es docente
     */
    fun esUsuarioDocente(idUsuario: Int): Boolean {
        val usuario = estado.value.usuarios.find { it.idUsuario == idUsuario }
        return usuario?.rol == Rol.DOCENTE
    }

    /**
     * Aplica los filtros actuales y actualiza la lista filtrada
     */
    private fun aplicarFiltros() {
        _estado.value.let { estadoActual ->
            val usuariosFiltrados = aplicarFiltros(estadoActual.usuarios)
            _estado.update { estadoActual.copy(usuariosFiltrados = usuariosFiltrados) }
        }
    }

    /**
     * Aplica filtros a una lista de usuarios
     */
    private fun aplicarFiltros(usuarios: List<Usuario>): List<Usuario> {
        return usuarios.filter { usuario ->
            val coincideBusqueda = estado.value.buscarTexto.isEmpty() ||
                    usuario.primerNombre.contains(estado.value.buscarTexto, ignoreCase = true) ||
                    usuario.apellidoPaterno?.contains(estado.value.buscarTexto, ignoreCase = true) == true ||
                    usuario.email.contains(estado.value.buscarTexto, ignoreCase = true) ||
                    usuario.username?.contains(estado.value.buscarTexto, ignoreCase = true) == true

            val coincideRol = when (estado.value.filtroRol) {
                "Todos" -> true
                else -> usuario.rol.obtenerNombre() == estado.value.filtroRol
            }

            coincideBusqueda && coincideRol
        }.sortedBy { it.primerNombre }
    }
}