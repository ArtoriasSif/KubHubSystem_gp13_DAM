package com.example.kubhubsystem_gp13_dam.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream


object ImagenUtils {
    fun esImagenValida(bytes: ByteArray?): Boolean {
        if (bytes == null || bytes.size == 0) return false

        try {
            // Verificar magic numbers de JPG/PNG
            if (bytes.size < 4) return false


            // JPG: FF D8 FF
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                return true
            }


            // PNG: 89 50 4E 47
            if (bytes[0] == 0x89.toByte() && bytes[1].toInt() == 0x50 && bytes[2].toInt() == 0x4E && bytes[3].toInt() == 0x47) {
                return true
            }

            return false
        } catch (e: java.lang.Exception) {
            return false
        }
    }
}
/**
 * Utilidades para manejo de imágenes
 * Conversión entre Uri, Base64, ByteArray y compresión
 */
object ImageUtils {

    /**
     * Convierte una Uri de imagen a String Base64
     * Comprime la imagen antes de convertir
     *
     * @param context Contexto de Android
     * @param uri Uri de la imagen
     * @param maxSizeKB Tamaño máximo en KB (default 500KB)
     * @param quality Calidad de compresión JPEG 0-100 (default 80)
     * @return String Base64 de la imagen comprimida, o null si hay error
     */
    fun uriToBase64(
        context: Context,
        uri: Uri,
        maxSizeKB: Int = 500,
        quality: Int = 80
    ): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                println("❌ Error: No se pudo decodificar la imagen")
                return null
            }

            // Comprimir imagen
            val compressedBytes = compressBitmap(bitmap, maxSizeKB, quality)

            // Convertir a Base64
            val base64String = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)

            println("✅ Imagen convertida a Base64: ${base64String.length} caracteres, ${compressedBytes.size / 1024}KB")
            base64String
        } catch (e: Exception) {
            println("❌ Error al convertir Uri a Base64: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte String Base64 a ByteArray
     *
     * @param base64String String en Base64
     * @return ByteArray de la imagen, o null si hay error
     */
    fun base64ToByteArray(base64String: String): ByteArray? {
        return try {
            Base64.decode(base64String, Base64.NO_WRAP)
        } catch (e: Exception) {
            println("❌ Error al decodificar Base64: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte String Base64 a Bitmap
     *
     * @param base64String String en Base64
     * @return Bitmap de la imagen, o null si hay error
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            println("❌ Error al convertir Base64 a Bitmap: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Comprime un Bitmap a ByteArray
     * Reduce el tamaño progresivamente hasta alcanzar el límite deseado
     *
     * @param bitmap Bitmap a comprimir
     * @param maxSizeKB Tamaño máximo en KB
     * @param initialQuality Calidad inicial de compresión (0-100)
     * @return ByteArray comprimido
     */
    fun compressBitmap(
        bitmap: Bitmap,
        maxSizeKB: Int = 500,
        initialQuality: Int = 80
    ): ByteArray {
        var quality = initialQuality
        var outputStream = ByteArrayOutputStream()

        // Comprimir con calidad inicial
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        var compressedBytes = outputStream.toByteArray()

        // Reducir calidad progresivamente si excede el tamaño máximo
        while (compressedBytes.size / 1024 > maxSizeKB && quality > 10) {
            outputStream = ByteArrayOutputStream()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            println("🔄 Recomprimiendo imagen: calidad=$quality, tamaño=${compressedBytes.size / 1024}KB")
        }

        println("✅ Imagen comprimida: calidad=$quality, tamaño=${compressedBytes.size / 1024}KB")
        return compressedBytes
    }

    /**
     * Valida que un String Base64 sea una imagen válida
     *
     * @param base64String String en Base64
     * @return true si es una imagen válida, false en caso contrario
     */
    fun isValidImageBase64(base64String: String?): Boolean {
        if (base64String.isNullOrBlank()) return false

        return try {
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el tamaño en KB de una imagen Base64
     *
     * @param base64String String en Base64
     * @return Tamaño en KB, o 0 si hay error
     */
    fun getBase64SizeKB(base64String: String?): Int {
        if (base64String.isNullOrBlank()) return 0

        return try {
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
            decodedBytes.size / 1024
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Redimensiona un Bitmap manteniendo la relación de aspecto
     *
     * @param bitmap Bitmap original
     * @param maxWidth Ancho máximo
     * @param maxHeight Alto máximo
     * @return Bitmap redimensionado
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    /**
     * Convierte Uri a ByteArray comprimido
     *
     * @param context Contexto de Android
     * @param uri Uri de la imagen
     * @param maxSizeKB Tamaño máximo en KB
     * @return ByteArray comprimido, o null si hay error
     */
    fun uriToByteArray(
        context: Context,
        uri: Uri,
        maxSizeKB: Int = 500
    ): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            compressBitmap(bitmap, maxSizeKB)
        } catch (e: Exception) {
            println("❌ Error al convertir Uri a ByteArray: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}