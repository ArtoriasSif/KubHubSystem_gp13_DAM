package com.example.kubhubsystem_gp13_dam.manager



import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.compose.material3.MaterialTheme
import com.example.kubhubsystem_gp13_dam.model.PerfilUsuario
import com.example.kubhubsystem_gp13_dam.model.PerfilHelper
import com.example.kubhubsystem_gp13_dam.model.PerfilHelper.generarIniciales
import com.example.kubhubsystem_gp13_dam.model.Usuario2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Gestor centralizado para los perfiles de usuario (sin persistencia)
 * Implementado como Singleton para mantener un único estado en toda la app
 *
 * Este manager:
 * - Mantiene las fotos de perfil en memoria durante la sesión de la app
 * - NO persiste datos en base de datos
 * - Los datos se pierden al cerrar la app (comportamiento deseado)
 * - Permite actualizar fotos de perfil sin modificar UsuarioEntity
 */
class PerfilUsuarioManager private constructor() {

    // Mapa de perfiles indexado por idUsuario para acceso rápido O(1)
    private val _perfiles = MutableStateFlow<Map<Int, PerfilUsuario>>(emptyMap())
    val perfiles: StateFlow<Map<Int, PerfilUsuario>> = _perfiles.asStateFlow()

    companion object {
        @Volatile
        private var instance: PerfilUsuarioManager? = null

        /**
         * Obtiene la instancia única del manager (Singleton thread-safe)
         */
        fun getInstance(): PerfilUsuarioManager {
            return instance ?: synchronized(this) {
                instance ?: PerfilUsuarioManager().also { instance = it }
            }
        }

        /**
         * Reinicia la instancia (útil para testing o reset completo)
         * USAR CON PRECAUCIÓN - elimina todos los perfiles en memoria
         */
        fun resetInstance() {
            synchronized(this) {
                instance = null
            }
        }
    }

    /**
     * Inicializa perfiles por defecto para todos los usuarios
     * Se debe llamar al cargar la lista de usuarios desde la BD
     *
     * @param usuarios Lista de usuarios existentes en la base de datos
     */

    /**
     * Inicializa perfiles para una lista de usuarios
     * ✅ ACTUALIZADO: Ahora carga las fotos de perfil desde Usuario2.fotoPerfil
     */
    fun inicializarPerfiles(usuarios: List<Usuario2>) {
        val nuevosPerfiles = usuarios.associate { usuario ->
            val nombreCompleto = buildString {
                append(usuario.primerNombre)
                if (!usuario.segundoNombre.isNullOrBlank()) append(" ${usuario.segundoNombre}")
                if (!usuario.apellidoPaterno.isNullOrBlank()) append(" ${usuario.apellidoPaterno}")
                if (!usuario.apellidoMaterno.isNullOrBlank()) append(" ${usuario.apellidoMaterno}")
            }.trim()

            // ✅ CRÍTICO: Usar fotoPerfilToUri() que soporta URLs HTTP
            val fotoUri = PerfilHelper.fotoPerfilToUri(usuario.fotoPerfil)

            usuario.idUsuario to PerfilUsuario(
                idUsuario = usuario.idUsuario,
                fotoPerfil = fotoUri,
                iniciales = PerfilHelper.generarIniciales(usuario),
                colorFondo = PerfilHelper.obtenerColorPorId(usuario.idUsuario)
            )
        }

        _perfiles.value = nuevosPerfiles
        Log.d(TAG, "✅ Perfiles inicializados: ${nuevosPerfiles.size} usuarios")

        // Log detallado para debug
        val conFoto = nuevosPerfiles.values.count { it.fotoPerfil != null }
        Log.d(TAG, "📸 Usuarios con foto de perfil: $conFoto de ${nuevosPerfiles.size}")

        // ✅ Log para ver las URLs de las fotos
        nuevosPerfiles.forEach { (id, perfil) ->
            if (perfil.fotoPerfil != null) {
                Log.d(TAG, "👤 Usuario $id tiene foto: ${perfil.fotoPerfil}")
            }
        }
    }


    /**
     * ✅ NUEVO: Sincroniza el perfil de un usuario específico con los datos del backend
     * Útil cuando se actualiza un usuario individualmente
     */
    fun sincronizarPerfil(usuario: Usuario2) {
        val fotoUri = PerfilHelper.fotoPerfilToUri(usuario.fotoPerfil)

        Log.d(TAG, "🔄 Sincronizando perfil usuario ${usuario.idUsuario}")
        Log.d(TAG, "   fotoPerfil backend: ${usuario.fotoPerfil?.take(100)}")
        Log.d(TAG, "   URI generada: ${fotoUri?.toString()?.take(100)}")
        val nombreCompleto = buildString {
            append(usuario.primerNombre)
            if (!usuario.segundoNombre.isNullOrBlank()) append(" ${usuario.segundoNombre}")
            if (!usuario.apellidoPaterno.isNullOrBlank()) append(" ${usuario.apellidoPaterno}")
            if (!usuario.apellidoMaterno.isNullOrBlank()) append(" ${usuario.apellidoMaterno}")
        }.trim()

        // ✅ CRÍTICO: Usar fotoPerfilToUri() que soporta URLs HTTP
        PerfilHelper.fotoPerfilToUri(usuario.fotoPerfil)

        val perfilActualizado = PerfilUsuario(
            idUsuario = usuario.idUsuario,
            fotoPerfil = fotoUri,
            iniciales = PerfilHelper.generarIniciales(usuario),
            colorFondo = PerfilHelper.obtenerColorPorId(usuario.idUsuario)
        )

        _perfiles.update { perfilesActuales ->
            perfilesActuales + (usuario.idUsuario to perfilActualizado)
        }

        Log.d(
            TAG,
            "✅ Perfil sincronizado para usuario ${usuario.idUsuario}: " +
                    if (perfilActualizado.fotoPerfil != null) "Con foto: ${perfilActualizado.fotoPerfil}" else "Sin foto"
        )
    }




    /**
     * Actualiza la foto de perfil de un usuario específico
     *
     * @param idUsuario ID del usuario a actualizar
     * @param fotoUri URI de la nueva foto (null para eliminar)
     * @throws IllegalArgumentException si el usuario no existe
     */
    fun actualizarFotoPerfil(idUsuario: Int, fotoUri: Uri?) {
        val perfilesActuales = _perfiles.value.toMutableMap()
        val perfilActual = perfilesActuales[idUsuario]
            ?: throw IllegalArgumentException("Usuario con ID $idUsuario no tiene perfil inicializado")

        perfilesActuales[idUsuario] = perfilActual.copy(fotoPerfil = fotoUri)
        _perfiles.value = perfilesActuales
    }

    /**
     * Obtiene el perfil de un usuario específico
     *
     * @param idUsuario ID del usuario
     * @return PerfilUsuario o null si no existe
     */
    fun obtenerPerfil(idUsuario: Int): PerfilUsuario? {
        return _perfiles.value[idUsuario]
    }

    /**
     * Verifica si un usuario tiene perfil inicializado
     *
     * @param idUsuario ID del usuario
     * @return true si existe el perfil, false en caso contrario
     */
    fun existePerfil(idUsuario: Int): Boolean {
        return _perfiles.value.containsKey(idUsuario)
    }

    /**
     * Verifica si un usuario tiene foto de perfil personalizada
     *
     * @param idUsuario ID del usuario
     * @return true si tiene foto, false si usa iniciales o no existe
     */
    fun tieneFotoPerfil(idUsuario: Int): Boolean {
        return _perfiles.value[idUsuario]?.fotoPerfil != null
    }

    /**
     * Elimina el perfil cuando se elimina un usuario
     * Debe llamarse cuando se elimina un usuario de la BD
     *
     * @param idUsuario ID del usuario a eliminar
     */
    fun eliminarPerfil(idUsuario: Int) {
        _perfiles.value = _perfiles.value.toMutableMap().apply {
            remove(idUsuario)
        }
    }

    /**
     * Agrega un nuevo perfil cuando se crea un usuario
     * Debe llamarse después de crear un usuario en la BD
     *
     * @param usuario Usuario nuevo al cual crear perfil
     */
    fun agregarPerfil(usuario: Usuario2) {
        val perfilesActuales = _perfiles.value.toMutableMap()

        if (!perfilesActuales.containsKey(usuario.idUsuario)) {
            // Crear perfil desde usuario (convierte Base64 a Uri si existe)
            perfilesActuales[usuario.idUsuario] = PerfilHelper.crearPerfilDesdeUsuario(usuario)
            _perfiles.value = perfilesActuales
        }
    }

    /**
     * Actualiza el perfil cuando cambian los datos del usuario
     * Regenera iniciales y color si cambiaron nombre/apellido
     * Actualiza foto desde Base64 o URL si viene del backend
     *
     * @param usuario Usuario con datos actualizados del backend
     */
    fun actualizarDatosUsuario(usuario: Usuario2) {
        val perfilesActuales = _perfiles.value.toMutableMap()
        val perfilActual = perfilesActuales[usuario.idUsuario]

        if (perfilActual != null) {
            val nuevasIniciales = PerfilHelper.generarIniciales(usuario)
            val nuevoColor = PerfilHelper.obtenerColorPorId(usuario.idUsuario)

            // ✅ Actualizar foto desde Backend (URL o Base64)
            val nuevaFoto = PerfilHelper.fotoPerfilToUri(usuario.fotoPerfil)

            perfilesActuales[usuario.idUsuario] = perfilActual.copy(
                iniciales = nuevasIniciales,
                colorFondo = nuevoColor,
                fotoPerfil = nuevaFoto
            )
            _perfiles.value = perfilesActuales
        }
    }

    /**
     * Obtiene todos los perfiles como lista (útil para mostrar en UI)
     *
     * @return Lista de todos los perfiles
     */
    fun obtenerTodosLosPerfiles(): List<PerfilUsuario> {
        return _perfiles.value.values.toList()
    }

    /**
     * Obtiene la cantidad total de perfiles
     *
     * @return Número de perfiles en memoria
     */
    fun obtenerCantidadPerfiles(): Int {
        return _perfiles.value.size
    }

    /**
     * Obtiene la cantidad de perfiles con foto personalizada
     *
     * @return Número de usuarios con foto de perfil
     */
    fun obtenerCantidadConFoto(): Int {
        return _perfiles.value.values.count { it.fotoPerfil != null }
    }

    /**
     * Obtiene la cantidad de perfiles sin foto (usando iniciales)
     *
     * @return Número de usuarios usando iniciales
     */
    fun obtenerCantidadSinFoto(): Int {
        return _perfiles.value.values.count { it.fotoPerfil == null }
    }

    /**
     * Limpia todos los perfiles en memoria
     * USAR CON PRECAUCIÓN - elimina todas las fotos de perfil
     * Útil al cerrar sesión o reiniciar la app
     */
    fun limpiarPerfiles() {
        _perfiles.value = emptyMap()
    }

    /**
     * Exporta los IDs de usuarios que tienen foto de perfil
     * Útil para estadísticas o debugging
     *
     * @return Lista de IDs de usuarios con foto
     */
    fun obtenerIdsConFoto(): List<Int> {
        return _perfiles.value
            .filter { it.value.fotoPerfil != null }
            .keys
            .toList()
    }
}