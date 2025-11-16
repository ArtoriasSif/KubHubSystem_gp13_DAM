package com.example.kubhubsystem_gp13_dam.model

import android.net.Uri

/**
 * Data class para representar el perfil de un usuario
 * NO se persiste en la base de datos - solo existe en memoria
 * ✅ ACTUALIZADO: Los nombres de atributos coinciden con Usuario del backend
 */
data class PerfilUsuario(
    val idUsuario: Int,
    val fotoPerfil: Uri? = null, // null = usar icono por defecto con iniciales
    val iniciales: String, // Ej: "AK" para Administrador KubHub
    val colorFondo: Long // Color del círculo de fondo cuando no hay foto (formato Long para Color(long))
)

/**
 * Helper object para generar iniciales y colores de perfil
 */
object PerfilHelper {

    // Paleta de colores Material Design para avatares (como Long para Color(long))
    private val coloresPaleta = listOf(
        0xFFE57373, // Rojo claro
        0xFFF06292, // Rosa
        0xFFBA68C8, // Púrpura claro
        0xFF9575CD, // Púrpura profundo
        0xFF7986CB, // Índigo
        0xFF64B5F6, // Azul claro
        0xFF4FC3F7, // Azul claro brillante
        0xFF4DD0E1, // Cyan
        0xFF4DB6AC, // Teal
        0xFF81C784, // Verde claro
        0xFFAED581, // Verde lima
        0xFFFFD54F, // Ámbar
        0xFFFFB74D, // Naranja claro
        0xFFFF8A65, // Naranja profundo
        0xFFA1887F, // Marrón claro
        0xFF90A4AE  // Gris azulado
    )

    /**
     * Genera las iniciales de un usuario basado en su nombre y apellido
     * ✅ CORREGIDO: Usa primerNombre y apellidoPaterno (backend)
     *
     * @param usuario Usuario del cual generar iniciales
     * @return String con 2 letras en mayúsculas (ej: "AK")
     */
    fun generarIniciales(usuario: Usuario): String {
        val primerLetra = usuario.primerNombre.firstOrNull()?.uppercaseChar() ?: 'U'
        val segundaLetra = (usuario.apellidoPaterno?.firstOrNull() ?: usuario.primerNombre.getOrNull(1))?.uppercaseChar() ?: 'S'
        return "$primerLetra$segundaLetra"
    }

    /**
     * Genera iniciales desde strings individuales (alternativa)
     *
     * @param primerNombre Primer nombre del usuario
     * @param apellidoPaterno Apellido paterno del usuario (nullable)
     * @return String con 2 letras en mayúsculas
     */
    fun generarIniciales(primerNombre: String, apellidoPaterno: String?): String {
        val primerLetra = primerNombre.firstOrNull()?.uppercaseChar() ?: 'U'
        val segundaLetra = (apellidoPaterno?.firstOrNull() ?: primerNombre.getOrNull(1))?.uppercaseChar() ?: 'S'
        return "$primerLetra$segundaLetra"
    }

    /**
     * Obtiene un color consistente basado en el ID del usuario
     * El mismo ID siempre generará el mismo color
     *
     * @param idUsuario ID único del usuario
     * @return Color en formato Long (compatible con Color(long))
     */
    fun obtenerColorPorId(idUsuario: Int): Long {
        return coloresPaleta[idUsuario % coloresPaleta.size]
    }

    /**
     * Obtiene un color basado en el nombre del usuario (alternativa)
     * Útil si no se tiene el ID disponible
     *
     * @param nombre Nombre completo del usuario
     * @return Color en formato Long (compatible con Color(long))
     */
    fun obtenerColorPorNombre(nombre: String): Long {
        val hash = nombre.hashCode()
        return coloresPaleta[Math.abs(hash) % coloresPaleta.size]
    }

    /**
     * Crea un perfil por defecto para un usuario
     *
     * @param usuario Usuario para el cual crear el perfil
     * @return PerfilUsuario con valores por defecto (sin foto)
     */
    fun crearPerfilPorDefecto(usuario: Usuario): PerfilUsuario {
        return PerfilUsuario(
            idUsuario = usuario.idUsuario,
            fotoPerfil = null,
            iniciales = generarIniciales(usuario),
            colorFondo = obtenerColorPorId(usuario.idUsuario)
        )
    }

    /**
     * Valida que una URI de foto sea válida
     *
     * @param uri URI a validar
     * @return true si la URI es válida, false en caso contrario
     */
    fun esUriValida(uri: Uri?): Boolean {
        return uri != null && uri.toString().isNotEmpty()
    }
}