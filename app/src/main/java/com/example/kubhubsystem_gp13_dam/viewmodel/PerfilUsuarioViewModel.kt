package com.example.kubhubsystem_gp13_dam.viewmodel


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.manager.PerfilUsuarioManager
import com.example.kubhubsystem_gp13_dam.model.PerfilUsuario
import com.example.kubhubsystem_gp13_dam.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel dedicado exclusivamente a la gestión de perfiles de usuario
 * No interactúa con la base de datos, solo maneja datos en memoria
 */
class PerfilUsuarioViewModel(
    private val perfilManager: PerfilUsuarioManager = PerfilUsuarioManager.getInstance()
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUsuarioEstado())
    val estado: StateFlow<PerfilUsuarioEstado> = _estado.asStateFlow()

    // Exponer los perfiles desde el manager
    val perfiles: StateFlow<Map<Int, PerfilUsuario>> = perfilManager.perfiles

    init {
        observarPerfiles()
    }

    /**
     * Observa cambios en los perfiles del manager
     */
    private fun observarPerfiles() {
        viewModelScope.launch {
            perfilManager.perfiles.collect { perfilesMap ->
                _estado.update { it.copy(
                    perfilesActivos = perfilesMap.values.toList(),
                    totalPerfiles = perfilesMap.size,
                    perfilesConFoto = perfilesMap.values.count { perfil -> perfil.fotoPerfil != null }
                ) }
            }
        }
    }

    /**
     * Inicializa perfiles por defecto para una lista de usuarios
     */
    fun inicializarPerfiles(usuarios: List<Usuario>) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            try {
                perfilManager.inicializarPerfiles(usuarios)
                _estado.update { it.copy(
                    cargando = false,
                    mensajeExito = "Perfiles inicializados correctamente"
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al inicializar perfiles: ${e.message}",
                    cargando = false
                ) }
            }
        }
    }

    /**
     * Actualiza la foto de perfil de un usuario específico
     */
    fun actualizarFotoPerfil(idUsuario: Int, fotoUri: Uri) {
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }
            try {
                perfilManager.actualizarFotoPerfil(idUsuario, fotoUri)
                _estado.update { it.copy(
                    procesando = false,
                    mensajeExito = "Foto de perfil actualizada",
                    ultimoIdModificado = idUsuario
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al actualizar foto: ${e.message}",
                    procesando = false
                ) }
            }
        }
    }

    /**
     * Elimina la foto de perfil (vuelve al icono por defecto)
     */
    fun eliminarFotoPerfil(idUsuario: Int) {
        viewModelScope.launch {
            _estado.update { it.copy(procesando = true, error = null) }
            try {
                perfilManager.actualizarFotoPerfil(idUsuario, null)
                _estado.update { it.copy(
                    procesando = false,
                    mensajeExito = "Foto de perfil eliminada",
                    ultimoIdModificado = idUsuario
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al eliminar foto: ${e.message}",
                    procesando = false
                ) }
            }
        }
    }

    /**
     * Obtiene el perfil de un usuario específico
     */
    fun obtenerPerfil(idUsuario: Int): PerfilUsuario? {
        return perfilManager.obtenerPerfil(idUsuario)
    }

    /**
     * Agrega un nuevo perfil cuando se crea un usuario
     */
    fun agregarPerfil(usuario: Usuario) {
        viewModelScope.launch {
            try {
                perfilManager.agregarPerfil(usuario)
                _estado.update { it.copy(
                    mensajeExito = "Perfil creado para ${usuario.primeroNombre}"
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al crear perfil: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Elimina un perfil cuando se elimina un usuario
     */
    fun eliminarPerfil(idUsuario: Int) {
        viewModelScope.launch {
            try {
                perfilManager.eliminarPerfil(idUsuario)
                _estado.update { it.copy(
                    mensajeExito = "Perfil eliminado"
                ) }
            } catch (e: Exception) {
                _estado.update { it.copy(
                    error = "Error al eliminar perfil: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Verifica si un usuario tiene foto de perfil personalizada
     */
    fun tieneFotoPerfil(idUsuario: Int): Boolean {
        return perfilManager.obtenerPerfil(idUsuario)?.fotoPerfil != null
    }

    /**
     * Obtiene estadísticas de perfiles
     */
    fun obtenerEstadisticas(): EstadisticasPerfiles {
        val perfilesMap = perfiles.value
        return EstadisticasPerfiles(
            totalPerfiles = perfilesMap.size,
            perfilesConFoto = perfilesMap.values.count { it.fotoPerfil != null },
            perfilesSinFoto = perfilesMap.values.count { it.fotoPerfil == null }
        )
    }

    /**
     * Limpia mensajes de error y éxito
     */
    fun limpiarMensajes() {
        _estado.update { it.copy(
            error = null,
            mensajeExito = null,
            ultimoIdModificado = null
        ) }
    }

    /**
     * Selecciona un perfil para edición
     */
    fun seleccionarPerfil(idUsuario: Int) {
        _estado.update { it.copy(perfilSeleccionado = idUsuario) }
    }

    /**
     * Deselecciona el perfil actual
     */
    fun deseleccionarPerfil() {
        _estado.update { it.copy(perfilSeleccionado = null) }
    }
}

/**
 * Estado del ViewModel de perfiles
 */
data class PerfilUsuarioEstado(
    val perfilesActivos: List<PerfilUsuario> = emptyList(),
    val perfilSeleccionado: Int? = null,
    val totalPerfiles: Int = 0,
    val perfilesConFoto: Int = 0,
    val cargando: Boolean = false,
    val procesando: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val ultimoIdModificado: Int? = null
)

/**
 * Estadísticas de perfiles
 */
data class EstadisticasPerfiles(
    val totalPerfiles: Int,
    val perfilesConFoto: Int,
    val perfilesSinFoto: Int
) {
    val porcentajeConFoto: Float
        get() = if (totalPerfiles > 0) (perfilesConFoto.toFloat() / totalPerfiles) * 100 else 0f
}