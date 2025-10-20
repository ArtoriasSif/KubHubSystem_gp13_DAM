package com.example.kubhubsystem_gp13_dam.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper object con funciones utilitarias
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

// Clase que maneja el estado del picker
class ImagePickerWithCameraState(
    private val context: Context,
    private val onImageSelected: (Uri) -> Unit,
    private val onGalleryPermissionDenied: () -> Unit,
    private val onCameraPermissionDenied: () -> Unit
) {
    // Variables mutables para mantener el estado
    var photoUri by mutableStateOf<Uri?>(null)
    var photoFile by mutableStateOf<File?>(null)

    // Launchers (se asignarán después de crear el composable)
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
            println("📸 Lanzando cámara con URI: $photoUri")
            cameraLauncher.launch(photoUri!!)
        } catch (e: Exception) {
            println("❌ Error al abrir cámara: ${e.message}")
            e.printStackTrace()
        }
    }

    fun procesarResultadoCamara(success: Boolean) {
        println("📸 Resultado cámara: success=$success, uri=$photoUri")
        if (success && photoUri != null && photoFile?.exists() == true) {
            println("✅ Foto capturada exitosamente")
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

// Composable principal
@Composable
fun rememberImagePickerWithCameraLauncher(
    onImageSelected: (Uri) -> Unit,
    onGalleryPermissionDenied: () -> Unit,
    onCameraPermissionDenied: () -> Unit
): ImagePickerWithCameraState {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Crear el estado
    val state = remember(context) {
        ImagePickerWithCameraState(
            context = context,
            onImageSelected = onImageSelected,
            onGalleryPermissionDenied = onGalleryPermissionDenied,
            onCameraPermissionDenied = onCameraPermissionDenied
        )
    }

    // Launcher para galería
    state.galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Launcher para cámara
    state.cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        state.procesarResultadoCamara(success)
    }

    // Launcher para permiso de galería
    state.galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.procesarPermisoGaleria(granted)
    }

    // Launcher para permiso de cámara
    state.cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.procesarPermisoCamara(granted)
    }

    return state
}