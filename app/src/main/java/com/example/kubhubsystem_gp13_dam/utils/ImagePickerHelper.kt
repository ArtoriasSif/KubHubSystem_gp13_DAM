package com.example.kubhubsystem_gp13_dam.utils



import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Helper object para manejar la selección de imágenes con permisos
 * Compatible con API 30+ (Android 11+)
 *
 * Maneja los diferentes permisos según la versión de Android:
 * - API 33+ (Android 13+): READ_MEDIA_IMAGES
 * - API 30-32 (Android 11-12): READ_EXTERNAL_STORAGE
 */
object ImagePickerHelper {

    /**
     * Determina qué permiso se necesita según la versión de Android
     *
     * @return String con el permiso necesario
     */
    fun obtenerPermisoNecesario(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // Android 11-12 (API 30-32)
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Verifica si el permiso necesario está otorgado
     *
     * @param context Contexto de la aplicación
     * @return true si el permiso está otorgado
     */
    fun tienePermiso(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(obtenerPermisoNecesario()) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            // Versiones anteriores a Android 6.0 no requieren permisos en runtime
            true
        }
    }

    /**
     * Abre la configuración de la aplicación para que el usuario otorgue permisos manualmente
     * Útil cuando el usuario deniega el permiso permanentemente
     *
     * @param context Contexto de la aplicación
     */
    fun abrirConfiguracionApp(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Obtiene el nombre amigable del permiso para mostrar al usuario
     *
     * @return String descriptivo del permiso
     */
    fun obtenerNombrePermiso(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            "Acceso a fotos e imágenes"
        } else {
            "Acceso al almacenamiento"
        }
    }

    /**
     * Genera un mensaje de explicación personalizado según la versión de Android
     *
     * @return String con explicación del permiso
     */
    fun obtenerMensajeExplicacion(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            "KubHub necesita acceso a tus fotos para permitirte cambiar tu foto de perfil. " +
                    "Solo se accederá a las fotos cuando tú lo solicites."
        } else {
            "KubHub necesita acceso al almacenamiento para permitirte cambiar tu foto de perfil. " +
                    "Solo se accederá a tus archivos cuando tú lo solicites."
        }
    }
}

/**
 * Estado del picker de imágenes con sus launchers
 * Encapsula los launchers de permisos y selección de imagen
 */
data class ImagePickerState(
    val imagePickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    /**
     * Inicia el flujo de selección de imagen
     * Primero solicita permiso, luego abre el selector
     */
    fun solicitarImagen() {
        permissionLauncher.launch(ImagePickerHelper.obtenerPermisoNecesario())
    }

    /**
     * Abre directamente el selector de imágenes
     * Solo usar si ya se tiene el permiso
     */
    fun abrirSelectorDirecto() {
        imagePickerLauncher.launch("image/*")
    }
}

/**
 * Composable que crea y maneja todo el flujo de selección de imagen
 * Incluye solicitud de permisos y selección de imagen de la galería
 *
 * @param onImageSelected Callback cuando se selecciona una imagen exitosamente
 * @param onPermissionDenied Callback cuando se deniega el permiso
 * @return ImagePickerState con los launchers configurados
 *
 * Ejemplo de uso:
 * ```
 * val imagePickerState = rememberImagePickerLauncher(
 *     onImageSelected = { uri ->
 *         viewModel.actualizarFotoPerfil(userId, uri)
 *     },
 *     onPermissionDenied = {
 *         mostrarDialogoPermisos = true
 *     }
 * )
 *
 * Button(onClick = { imagePickerState.solicitarImagen() }) {
 *     Text("Seleccionar imagen")
 * }
 * ```
 */
@Composable
fun rememberImagePickerLauncher(
    onImageSelected: (Uri) -> Unit,
    onPermissionDenied: () -> Unit
): ImagePickerState {

    // Launcher para seleccionar imagen de la galería
    // Usa el contrato GetContent que es compatible con todas las versiones
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Si se seleccionó una imagen válida, notificar al callback
        uri?.let { onImageSelected(it) }
    }

    // Launcher para solicitar permisos en runtime
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso otorgado - abrir selector de imágenes
            imagePickerLauncher.launch("image/*")
        } else {
            // Permiso denegado - notificar al callback
            onPermissionDenied()
        }
    }

    // Retornar estado encapsulado con ambos launchers
    return remember(imagePickerLauncher, permissionLauncher) {
        ImagePickerState(
            imagePickerLauncher = imagePickerLauncher,
            permissionLauncher = permissionLauncher
        )
    }
}

/**
 * Variante del launcher que también maneja cámara
 * Permite elegir entre galería o tomar foto
 *
 * @param onImageSelected Callback cuando se selecciona/toma una imagen
 * @param onPermissionDenied Callback cuando se deniega el permiso
 * @return ImagePickerWithCameraState con launchers de galería y cámara
 */
@Composable
fun rememberImagePickerWithCameraLauncher(
    onImageSelected: (Uri) -> Unit,
    onPermissionDenied: () -> Unit
): ImagePickerWithCameraState {

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        // Si se tomó foto exitosamente, usar el URI temporal
        if (success) {
            // Aquí necesitarías crear un URI temporal antes de llamar al launcher
            // Ver implementación completa más abajo
        }
    }

    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            onPermissionDenied()
        }
        // Si se otorgó, el usuario decidirá si usar galería o cámara
    }

    return remember(galleryLauncher, cameraLauncher, permissionLauncher) {
        ImagePickerWithCameraState(
            galleryLauncher = galleryLauncher,
            cameraLauncher = cameraLauncher,
            permissionLauncher = permissionLauncher
        )
    }
}

/**
 * Estado extendido con soporte para cámara
 */
data class ImagePickerWithCameraState(
    val galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,
    val cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    fun solicitarDesdeGaleria() {
        permissionLauncher.launch(ImagePickerHelper.obtenerPermisoNecesario())
    }

    fun abrirGaleria() {
        galleryLauncher.launch("image/*")
    }

    fun solicitarDesdeCamara() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
}