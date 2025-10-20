package com.example.kubhubsystem_gp13_dam.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class para gestionar la obtención de ubicación del dispositivo
 *
 * Características:
 * - Verifica permisos automáticamente
 * - Obtiene la última ubicación conocida
 * - Solicita actualizaciones de ubicación si es necesario
 * - Maneja errores y timeouts
 */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la ubicación actual del dispositivo
     *
     * @return Location object con lat/lng o null si no se pudo obtener
     */
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            // Primero intentar obtener la última ubicación conocida
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        println("📍 Ubicación obtenida (última conocida): ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        // Si no hay última ubicación, solicitar una actualización
                        requestLocationUpdate(continuation)
                    }
                }
                .addOnFailureListener { exception ->
                    println("❌ Error al obtener ubicación: ${exception.message}")
                    continuation.resumeWithException(exception)
                }
        } catch (e: SecurityException) {
            println("❌ Error de seguridad al obtener ubicación: ${e.message}")
            continuation.resume(null)
        }

        // Cancelar la solicitud si se cancela la coroutine
        continuation.invokeOnCancellation {
            println("🚫 Solicitud de ubicación cancelada")
        }
    }

    /**
     * Solicita una actualización de ubicación en tiempo real
     */
    @Suppress("MissingPermission")
    private fun requestLocationUpdate(
        continuation: kotlinx.coroutines.CancellableContinuation<Location?>
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 segundos
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 segundos
            setMaxUpdates(1) // Solo una actualización
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    println("📍 Ubicación obtenida (nueva): ${location.latitude}, ${location.longitude}")
                    continuation.resume(location)
                } else {
                    continuation.resume(null)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            println("❌ Error de seguridad al solicitar actualización: ${e.message}")
            continuation.resume(null)
        }
    }

    /**
     * Formatea la ubicación en formato legible
     */
    fun formatLocation(location: Location?): String {
        return if (location != null) {
            "Lat: %.4f, Lng: %.4f".format(location.latitude, location.longitude)
        } else {
            "Ubicación no disponible"
        }
    }

    /**
     * Obtiene solo la latitud
     */
    fun getLatitude(location: Location?): Double? = location?.latitude

    /**
     * Obtiene solo la longitud
     */
    fun getLongitude(location: Location?): Double? = location?.longitude
}