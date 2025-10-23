package com.example.kubhubsystem_gp13_dam.utils

object SpanishTextValidator  {

    private val PREPOSICIONES = setOf("de", "para", "con", "sin", "en", "sobre", "por", "la")

    /**
     * Valida y formatea texto en español
     * @param texto Texto a validar y formatear
     * @return String capitalizado si es válido, null si contiene errores
     */
    fun validarYFormatearTexto(texto: String): String? {
        if (texto.isBlank()) return null

        // Normalizar: eliminar espacios duplicados
        val textoNormalizado = texto.trim().replace("\\s+".toRegex(), " ")

        // Validar cada palabra
        val palabras = textoNormalizado.split(" ")

        palabras.forEach { palabra ->
            val palabraLimpia = palabra.lowercase()
            if (!validarPalabra(palabraLimpia)) {
                return null
            }
        }

        // Si todas las palabras son válidas, formatear y retornar
        return formatearTexto(textoNormalizado)
    }

    private fun validarPalabra(palabra: String): Boolean {
        return !tieneCaracteresNoEspanoles(palabra) &&
                !tieneSecuenciasVocalesInvalidas(palabra) &&
                !tieneSecuenciasConsonantesInvalidas(palabra) &&
                !tieneCombinacionesInvalidas(palabra)
    }

    private fun tieneCaracteresNoEspanoles(palabra: String): Boolean {
        val caracteresValidos = Regex("[a-záéíóúñü]+")
        return !palabra.matches(caracteresValidos)
    }

    private fun tieneSecuenciasVocalesInvalidas(palabra: String): Boolean {
        // 1. Tres vocales iguales seguidas
        if (Regex("([aeiouáéíóú])\\1\\1").containsMatchIn(palabra)) return true

        // 2. Dos vocales débiles iguales seguidas (excepto en palabras específicas)
        if (Regex("(ii|uu)").containsMatchIn(palabra)) return true

        // 3. Combinaciones de tres vocales que no existen
        val combinacionesInvalidas = setOf(
            "aei", "aeu", "aoi", "aou",
            "eai", "eau", "eoi", "eou",
            "iai", "ieu", "ioi", "iou",
            "oai", "oau", "oei", "oeu",
            "uai", "uau", "uei", "ueu",
            "aea", "aea", "oeo", "oeo"
        )

        return combinacionesInvalidas.any { it in palabra }
    }

    private fun tieneSecuenciasConsonantesInvalidas(palabra: String): Boolean {
        // Buscar 3 o más consonantes seguidas
        val consonantesRegex = Regex("[bcdfghjklmnñpqrstvwxyz]{3,}")
        val matches = consonantesRegex.findAll(palabra)

        // Excepciones válidas en español
        val excepcionesValidas = setOf(
            "str", "ntr", "mpr", "mpl", "nstr", "nscr",
            "rst", "rstr", "bstr", "trans", "constr"
        )

        matches.forEach { match ->
            val secuencia = match.value
            // Si la secuencia no está en las excepciones, es inválida
            if (!excepcionesValidas.any { secuencia.contains(it) }) {
                return true
            }
        }

        return false
    }

    private fun tieneCombinacionesInvalidas(palabra: String): Boolean {
        val combinacionesInvalidas = setOf(
            // Consonantes dobles no válidas
            "kk", "ww", "xx", "yy", "vv", "jj", "hh",
            // Combinaciones imposibles
            "kj", "kq", "qw", "wq", "zx", "xz", "wk",
            "bk", "bp", "bq", "db", "dp", "dq", "fb", "fp", "fq",
            "gb", "gp", "gq", "jb", "jp", "jq", "kb", "kp", "kq",
            "pb", "pq", "qb", "qp", "vb", "vp", "vq", "wb", "wp", "wq",
            "xb", "xp", "xq", "zb", "zp", "zq","xd",
            // Q sin u
            "qa", "qe", "qi", "qo",
            // H después de consonantes específicas
            "kh", "qh", "wh"
        )

        return combinacionesInvalidas.any { it in palabra }
    }

    private fun formatearTexto(texto: String): String {
        val palabras = texto.split(" ")

        return palabras.mapIndexed { index, palabra ->
            val palabraLower = palabra.lowercase()

            // Si es la primera palabra, siempre capitalizar
            if (index == 0) {
                palabraLower.replaceFirstChar { it.uppercase() }
            }
            // Si es preposición (y no es la primera palabra), mantener en minúsculas
            else if (palabraLower in PREPOSICIONES) {
                palabraLower
            }
            // Resto de palabras, capitalizar
            else {
                palabraLower.replaceFirstChar { it.uppercase() }
            }
        }.joinToString(" ")
    }
}

// Función de extensión para uso más simple
fun String.validarYFormatearEspanol(): String? {
    return SpanishTextValidator .validarYFormatearTexto(this)
}