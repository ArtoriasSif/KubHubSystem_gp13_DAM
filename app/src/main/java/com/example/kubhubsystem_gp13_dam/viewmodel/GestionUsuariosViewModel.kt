package com.example.kubhubsystem_gp13_dam.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.local.entities.DocenteEntity
import com.example.kubhubsystem_gp13_dam.local.entities.UsuarioEntity
import com.example.kubhubsystem_gp13_dam.model.Rol
import com.example.kubhubsystem_gp13_dam.model.Usuario
import com.example.kubhubsystem_gp13_dam.repository.DocenteRepository
import com.example.kubhubsystem_gp13_dam.repository.RolRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GestionUsuariosViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val rolRepository: RolRepository,
    private val docenteRepository: DocenteRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(GestionUsuariosEstado())
    val estado: StateFlow<GestionUsuariosEstado> = _estado.asStateFlow()

    private var datosInicializados = false

    init {
        cargarDatosCompletos()
    }

    fun inicializarDatosSiEsNecesario() {
        if (!datosInicializados) {
            inicializarDatos()
        }
    }

    fun inicializarDatos() {
        viewModelScope.launch {
            _estado.update { it.copy(
                cargando = true,
                inicializando = true,
                error = null
            ) }

            try {
                // 1. Inicializar roles en la base de datos
                rolRepository.inicializarRoles()

                // 2. Inicializar usuarios en la base de datos
                usuarioRepository.inicializarUsuarios()

                // 3. Obtener todos los usuarios y derivar docentes automáticamente
                val usuariosEntities = usuarioRepository.obtenerTodos()
                docenteRepository.inicializarDocentes(usuariosEntities)

                // 4. Cargar datos completos para la UI
                cargarDatosCompletos()

                datosInicializados = true

                _estado.update { it.copy(
                    inicializando = false,
                    mensajeExito = "Datos inicializados correctamente"
                ) }

            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al inicializar datos: ${e.message}",
                    cargando = false,
                    inicializando = false
                ) }
            }
        }
    }

    fun cargarDatosCompletos() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val usuariosEntities = usuarioRepository.obtenerTodos()
                val usuarios = convertirAUsuarios(usuariosEntities)
                val roles = rolRepository.obtenerTodos()
                val docentes = docenteRepository.obtenerTodos()

                _estado.update { it.copy(
                    usuarios = usuarios,
                    usuariosFiltrados = aplicarFiltros(usuarios),
                    roles = roles,
                    docentes = docentes,
                    totalRoles = roles.size,
                    totalDocentes = docentes.size,
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

    fun cargarUsuarios() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                val usuariosEntities = usuarioRepository.obtenerTodos()
                val usuarios = convertirAUsuarios(usuariosEntities)
                _estado.update { it.copy(
                    usuarios = usuarios,
                    usuariosFiltrados = aplicarFiltros(usuarios),
                    cargando = false
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = e.message ?: "Error desconocido al cargar usuarios",
                    cargando = false
                ) }
            }
        }
    }

    // Función para convertir UsuarioEntity a Usuario
    private fun convertirAUsuarios(entities: List<UsuarioEntity>): List<Usuario> {
        return entities.map { entity ->
            Usuario(
                idUsuario = entity.idUsuario,
                rol = Rol.desdeId(entity.idRol) ?: Rol.DOCENTE, // Valor por defecto si es null
                primeroNombre = entity.primeroNombre,
                segundoNombre = entity.segundoNombre,
                apellidoMaterno = entity.apellidoMaterno,
                apellidoPaterno = entity.apellidoPaterno,
                email = entity.email,
                username = entity.username,
                password = entity.password
            )
        }
    }

    // Función para convertir Usuario a UsuarioEntity
    private fun convertirAUsuarioEntity(usuario: Usuario): UsuarioEntity {
        return UsuarioEntity(
            idUsuario = usuario.idUsuario,
            idRol = usuario.rol.obtenerIdRol(), // Convertir enum a id numérico
            primeroNombre = usuario.primeroNombre,
            segundoNombre = usuario.segundoNombre,
            apellidoMaterno = usuario.apellidoMaterno,
            apellidoPaterno = usuario.apellidoPaterno,
            email = usuario.email,
            username = usuario.username,
            password = usuario.password
        )
    }

    fun onFiltroRolChange(filtroRol: String) {
        _estado.update { it.copy(filtroRol = filtroRol) }
        aplicarFiltros()
    }

    fun onBuscarTextoChange(buscarTexto: String) {
        _estado.update { it.copy(buscarTexto = buscarTexto) }
        aplicarFiltros()
    }

    fun eliminarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                // Primero verificar si es docente y eliminar también esa relación
                val docente = docenteRepository.obtenerPorIdUsuario(usuario.idUsuario)
                if (docente != null) {
                    docenteRepository.eliminar(docente)
                }

                // Luego eliminar el usuario
                usuarioRepository.eliminarPorId(usuario.idUsuario)

                // Recargar datos
                cargarDatosCompletos()

                _estado.update { it.copy(
                    mensajeExito = "Usuario eliminado correctamente"
                ) }

            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al eliminar usuario: ${e.message}"
                ) }
            }
        }
    }

    fun crearUsuario(
        primeroNombre: String,
        segundoNombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        email: String,
        username: String,
        password: String,
        rol: Rol
    ) {
        viewModelScope.launch {
            try {
                val nuevoUsuario = UsuarioEntity(
                    idUsuario = 0,
                    idRol = rol.obtenerIdRol(), // Convertir enum a id
                    primeroNombre = primeroNombre,
                    segundoNombre = segundoNombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    email = email,
                    username = username,
                    password = password
                )

                val usuarioId = usuarioRepository.insertar(nuevoUsuario)

                // Si el rol es DOCENTE, crear automáticamente el docente
                if (rol == Rol.DOCENTE) {
                    val nuevoDocente = DocenteEntity(
                        idDocente = 0,
                        idUsuario = usuarioId.toInt(),
                        seccionesIds = emptyList()
                    )
                    docenteRepository.insertar(nuevoDocente)
                }

                // Recargar datos
                cargarDatosCompletos()

                _estado.update { it.copy(
                    mensajeExito = "Usuario creado correctamente"
                ) }

            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al crear usuario: ${e.message}"
                ) }
            }
        }
    }

    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                val usuarioEntity = convertirAUsuarioEntity(usuario)
                usuarioRepository.actualizar(usuarioEntity)

                // Si cambió el rol, actualizar la relación docente
                val docenteExistente = docenteRepository.obtenerPorIdUsuario(usuario.idUsuario)
                val esDocente = usuario.rol == Rol.DOCENTE

                if (esDocente && docenteExistente == null) {
                    // Crear docente si no existe
                    val nuevoDocente = DocenteEntity(
                        idDocente = 0,
                        idUsuario = usuario.idUsuario,
                        seccionesIds = emptyList()
                    )
                    docenteRepository.insertar(nuevoDocente)
                } else if (!esDocente && docenteExistente != null) {
                    // Eliminar docente si ya no es docente
                    docenteRepository.eliminar(docenteExistente)
                }

                // Recargar datos
                cargarDatosCompletos()

                _estado.update { it.copy(
                    mensajeExito = "Usuario actualizado correctamente"
                ) }

            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al actualizar usuario: ${e.message}"
                ) }
            }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(
            error = null,
            mensajeExito = null
        ) }
    }

    fun obtenerUsuarioPorId(id: Int): Usuario? {
        return estado.value.usuarios.find { it.idUsuario == id }
    }

    fun esUsuarioDocente(idUsuario: Int): Boolean {
        val usuario = estado.value.usuarios.find { it.idUsuario == idUsuario }
        return usuario?.rol == Rol.DOCENTE
    }

    private fun aplicarFiltros() {
        _estado.value.let { estadoActual ->
            val usuariosFiltrados = aplicarFiltros(estadoActual.usuarios)
            _estado.update { estadoActual.copy(usuariosFiltrados = usuariosFiltrados) }
        }
    }

    private fun aplicarFiltros(usuarios: List<Usuario>): List<Usuario> {
        return usuarios.filter { usuario ->
            val coincideBusqueda = estado.value.buscarTexto.isEmpty() ||
                    usuario.primeroNombre.contains(estado.value.buscarTexto, ignoreCase = true) ||
                    usuario.apellidoPaterno.contains(estado.value.buscarTexto, ignoreCase = true) ||
                    usuario.email.contains(estado.value.buscarTexto, ignoreCase = true) ||
                    usuario.username.contains(estado.value.buscarTexto, ignoreCase = true)

            val coincideRol = when (estado.value.filtroRol) {
                "Todos" -> true
                else -> usuario.rol.obtenerNombre() == estado.value.filtroRol
            }

            coincideBusqueda && coincideRol
        }.sortedBy { it.primeroNombre }
    }
}

data class GestionUsuariosEstado(
    val usuarios: List<Usuario> = emptyList(),
    val usuariosFiltrados: List<Usuario> = emptyList(),
    val roles: List<com.example.kubhubsystem_gp13_dam.local.entities.RolEntity> = emptyList(),
    val docentes: List<DocenteEntity> = emptyList(),
    val cargando: Boolean = false,
    val inicializando: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val buscarTexto: String = "",
    val filtroRol: String = "Todos",
    val totalRoles: Int = 0,
    val totalDocentes: Int = 0
)