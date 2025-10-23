package com.example.kubhubsystem_gp13_dam.utils

object SpanishSingularConverter {

    /**
     * Convierte una palabra en español a su forma singular
     * para comparación en base de datos
     * @param palabra Palabra a convertir
     * @return Palabra en singular (lowercase)
     */
    fun convertirASingular(palabra: String): String {
        if (palabra.isBlank()) return palabra.lowercase()

        val palabraLower = palabra.lowercase().trim()

        // Si no termina en 's', ya está en singular
        if (!palabraLower.endsWith("s")) {
            return palabraLower
        }

        // Regla 1: Palabras que terminan en vocal + s -> quitar 's'
        // Ejemplos: tomates -> tomate, papas -> papa
        if (palabraLower.length >= 2 && esVocal(palabraLower[palabraLower.length - 2])) {
            return palabraLower.dropLast(1)
        }

        // Regla 2: Palabras que terminan en 'ces' -> cambiar a 'z'
        // Ejemplos: peces -> pez, nueces -> nuez
        if (palabraLower.endsWith("ces") && palabraLower.length > 3) {
            return palabraLower.dropLast(3) + "z"
        }

        // Regla 3: Palabras que terminan en 'es' después de consonante -> quitar 'es'
        // Ejemplos: limones -> limón, camiones -> camión
        if (palabraLower.endsWith("es") && palabraLower.length > 2) {
            val baseWord = palabraLower.dropLast(2)

            // Casos especiales: recuperar acentos perdidos
            if (baseWord.endsWith("on")) {
                return baseWord.dropLast(2) + "ón"
            }
            if (baseWord.endsWith("an")) {
                return baseWord.dropLast(2) + "án"
            }
            if (baseWord.endsWith("in")) {
                return baseWord.dropLast(2) + "ín"
            }

            return baseWord
        }

        // Regla 4: Palabras invariables que terminan en 's'
        // Ejemplos: lunes, martes, crisis
        if (esPalabraInvariable(palabraLower)) {
            return palabraLower
        }

        // Si termina en 's' pero no coincide con ninguna regla, dejar como está
        return palabraLower
    }

    /**
     * Normaliza un texto completo (varias palabras) a singular
     * @param texto Texto con una o más palabras
     * @return Texto con todas las palabras en singular
     */
    fun normalizarTexto(texto: String): String {
        if (texto.isBlank()) return texto.lowercase()

        return texto.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { convertirASingular(it) }
    }

    /**
     * Verifica si dos productos son iguales ignorando singular/plural
     * @param producto1 Nombre del primer producto
     * @param producto2 Nombre del segundo producto
     * @return true si son iguales en forma singular
     */
    fun sonIgualesIgnorandoPlural(producto1: String, producto2: String): Boolean {
        return normalizarTexto(producto1) == normalizarTexto(producto2)
    }

    private fun esVocal(c: Char): Boolean {
        return c in "aeiouáéíóú"
    }

    private fun esPalabraInvariable(palabra: String): Boolean {
        val invariables = setOf(
            "lunes", "martes", "miércoles", "jueves", "viernes",
            "crisis", "tesis", "análisis", "síntesis", "dosis",
            "virus", "campus", "atlas"
        )
        return palabra in invariables
    }
}

// Funciones de extensión para uso más simple
fun String.asSingular(): String {
    return SpanishSingularConverter.convertirASingular(this)
}

fun String.normalizarParaBusqueda(): String {
    return SpanishSingularConverter.normalizarTexto(this)
}