package com.example.kubhubsystem_gp13_dam.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import com.example.kubhubsystem_gp13_dam.model.Usuario2
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * ✅ PERFILHELPER SIMPLIFICADO
 * Ya no convierte a URI - mantiene el Base64 como String
 */
object PerfilHelper {

    private val coloresPaleta = listOf(
        0xFFE57373, 0xFFF06292, 0xFFBA68C8, 0xFF9575CD,
        0xFF7986CB, 0xFF64B5F6, 0xFF4FC3F7, 0xFF4DD0E1,
        0xFF4DB6AC, 0xFF81C784, 0xFFAED581, 0xFFFFD54F,
        0xFFFFB74D, 0xFFFF8A65, 0xFFA1887F, 0xFF90A4AE
    )

    /**
     * Genera iniciales desde Usuario2
     */
    fun generarIniciales(usuario: Usuario2): String {
        val primerLetra = usuario.primerNombre.firstOrNull()?.uppercaseChar() ?: 'U'
        val segundaLetra = (usuario.apellidoPaterno?.firstOrNull()
            ?: usuario.primerNombre.getOrNull(1))?.uppercaseChar() ?: 'S'
        return "$primerLetra$segundaLetra"
    }

    /**
     * Obtiene color consistente por ID
     */
    fun obtenerColorPorId(idUsuario: Int): Long {
        return coloresPaleta[idUsuario % coloresPaleta.size]
    }

    /**
     * ✅ FUNCIÓN CRÍTICA SIMPLIFICADA
     * Ya no convierte a URI - devuelve el Base64 tal cual viene del backend
     */
    fun procesarFotoPerfil(fotoPerfil: String?): String? {
        if (fotoPerfil.isNullOrBlank()) {
            Log.d("PerfilHelper", "📭 fotoPerfil vacío")
            return null
        }

        val limpio = fotoPerfil.trim()

        Log.d("PerfilHelper", "📥 Foto recibida:")
        Log.d("PerfilHelper", "   Preview: ${limpio.take(50)}")
        Log.d("PerfilHelper", "   Length: ${limpio.length}")

        // ✅ DEVOLVER TAL CUAL
        // El componente AvatarUsuario se encargará de decodificar
        return limpio
    }

    /**
     * Crea perfil desde Usuario2
     */
    fun crearPerfilDesdeUsuario(usuario: Usuario2): PerfilUsuario {
        val foto = procesarFotoPerfil(usuario.fotoPerfil)

        Log.d("PerfilHelper", "🆕 Perfil usuario ${usuario.idUsuario}")
        Log.d("PerfilHelper", "   Foto presente: ${foto != null}")

        return PerfilUsuario(
            idUsuario = usuario.idUsuario,
            fotoPerfil = foto, // ✅ Ahora es String, no Uri
            iniciales = generarIniciales(usuario),
            colorFondo = obtenerColorPorId(usuario.idUsuario)
        )
    }

    /**
     * Limpia Base64 para enviar al backend
     * Remueve prefijos data:image si existen
     */
    fun limpiarBase64ParaBackend(base64: String?): String? {
        if (base64.isNullOrBlank()) return null

        val limpio = base64.trim()

        return when {
            limpio.contains("base64,") -> limpio.substringAfter("base64,")
            else -> limpio
        }
    }

    /**
     * Detecta tipo MIME según header Base64
     */
    fun detectarMimeType(base64: String): String {
        val clean = if (base64.contains("base64,")) {
            base64.substringAfter("base64,")
        } else {
            base64
        }

        return when {
            clean.startsWith("/9j/") -> "image/jpeg"
            clean.startsWith("iVBOR") -> "image/png"
            clean.startsWith("R0lG") -> "image/gif"
            clean.startsWith("UklG") -> "image/webp"
            else -> "image/jpeg"
        }
    }
}

/**
 * ✅ PERFILUSUARIO ACTUALIZADO
 * fotoPerfil ahora es String? (Base64) en lugar de Uri?
 */
data class PerfilUsuario(
    val idUsuario: Int,
    val fotoPerfil: String? = null, // ✅ Cambio: String en lugar de Uri
    val iniciales: String,
    val colorFondo: Long
)

/**
 * Helper para permisos y selección de imágenes
 */
object ImagePickerHelper {

    fun obtenerPermisoGaleria(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    fun tienePermisoGaleria(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(obtenerPermisoGaleria()) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun tienePermisoCamara(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(Manifest.permission.CAMERA) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun abrirConfiguracionApp(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun crearArchivoFotoTemporal(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Pictures")
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    fun obtenerUriParaArchivo(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun obtenerMensajeExplicacion(esCamara: Boolean): String {
        return if (esCamara) {
            "KubHub necesita acceso a la cámara para tomar fotos de perfil."
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "KubHub necesita acceso a tus fotos para cambiar tu foto de perfil."
            } else {
                "KubHub necesita acceso al almacenamiento para cambiar tu foto de perfil."
            }
        }
    }
}


/**
 * Estado del image picker con cámara
 */
class ImagePickerWithCameraState(
    private val context: Context,
    private val onImageSelected: (Uri) -> Unit,
    private val onGalleryPermissionDenied: () -> Unit,
    private val onCameraPermissionDenied: () -> Unit
) {
    var photoUri by mutableStateOf<Uri?>(null)
    var photoFile by mutableStateOf<File?>(null)

    lateinit var galleryLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>
    lateinit var cameraLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Uri, Boolean>
    lateinit var galleryPermissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>
    lateinit var cameraPermissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>

    fun solicitarDesdeGaleria() {
        if (ImagePickerHelper.tienePermisoGaleria(context)) {
            galleryLauncher.launch("image/*")
        } else {
            galleryPermissionLauncher.launch(ImagePickerHelper.obtenerPermisoGaleria())
        }
    }

    fun solicitarDesdeCamara() {
        if (ImagePickerHelper.tienePermisoCamara(context)) {
            abrirCamara()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun abrirCamara() {
        try {
            photoFile = ImagePickerHelper.crearArchivoFotoTemporal(context)
            photoUri = ImagePickerHelper.obtenerUriParaArchivo(context, photoFile!!)
            Log.d("ImagePicker", "📸 Lanzando cámara con URI: $photoUri")
            cameraLauncher.launch(photoUri!!)
        } catch (e: Exception) {
            Log.e("ImagePicker", "❌ Error al abrir cámara: ${e.message}")
            e.printStackTrace()
        }
    }

    fun procesarResultadoCamara(success: Boolean) {
        Log.d("ImagePicker", "📸 Resultado cámara: success=$success, uri=$photoUri")
        if (success && photoUri != null && photoFile?.exists() == true) {
            Log.d("ImagePicker", "✅ Foto capturada exitosamente")
            onImageSelected(photoUri!!)
        }
        photoUri = null
        photoFile = null
    }

    fun procesarPermisoGaleria(granted: Boolean) {
        if (granted) {
            galleryLauncher.launch("image/*")
        } else {
            onGalleryPermissionDenied()
        }
    }

    fun procesarPermisoCamara(granted: Boolean) {
        if (granted) {
            abrirCamara()
        } else {
            onCameraPermissionDenied()
        }
    }
}

/**
 * Composable para recordar el estado del image picker
 */
@Composable
fun rememberImagePickerWithCameraLauncher(
    onImageSelected: (Uri) -> Unit,
    onGalleryPermissionDenied: () -> Unit,
    onCameraPermissionDenied: () -> Unit
): ImagePickerWithCameraState {
    val context = androidx.compose.ui.platform.LocalContext.current

    val state = remember(context) {
        ImagePickerWithCameraState(
            context = context,
            onImageSelected = onImageSelected,
            onGalleryPermissionDenied = onGalleryPermissionDenied,
            onCameraPermissionDenied = onCameraPermissionDenied
        )
    }

    state.galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    state.cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        state.procesarResultadoCamara(success)
    }

    state.galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.procesarPermisoGaleria(granted)
    }

    state.cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.procesarPermisoCamara(granted)
    }

    return state
}