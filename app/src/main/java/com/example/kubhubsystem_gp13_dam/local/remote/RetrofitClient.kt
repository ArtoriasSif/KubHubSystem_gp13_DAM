package com.example.kubhubsystem_gp13_dam.local.remote

import android.content.Context
import com.example.kubhubsystem_gp13_dam.BuildConfig
import com.example.kubhubsystem_gp13_dam.utils.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit centralizado para todas las comunicaciones con el backend
 * ✅ ACTUALIZADO: Ahora incluye todos los servicios necesarios
 */
object RetrofitClient {

    // ✅ Base URL leída desde build.gradle.kts (BuildConfig)
    // Ejemplo: "http://54.242.76.7/"
    private val BASE_URL: String = BuildConfig.BASE_URL
    private lateinit var tokenManager: TokenManager

    /**
     * ⭐ NUEVO: Inicializar con contexto para TokenManager
     */
    fun init(context: Context) {
        tokenManager = TokenManager.getInstance(context)
        println("✅ RetrofitClient inicializado con TokenManager")
    }

    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    // ✅ Interceptor para logging (útil en desarrollo)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * ⭐ ACTUALIZADO: Cliente HTTP con AuthInterceptor
     */
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager)) // ⭐ Agregar interceptor de auth
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ✅ Instancia única de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ========================================
    // SERVICIOS DISPONIBLES
    // ========================================

    /**
     * Servicio de autenticación (login/logout)
     */
    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    /**
     * Servicio de gestión de usuarios
     */
    val usuarioService: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    /**
     * Servicio de gestión de roles
     */
    val rolService: RolApiService by lazy {
        retrofit.create(RolApiService::class.java)
    }

    /**
     * Servicio de inventario (ya existente)
     */
    val inventarioService: InventarioApiService by lazy {
        retrofit.create(InventarioApiService::class.java)
    }

    // ✅ Método genérico (mantener por compatibilidad)
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    // ✅ Proporciona acceso a Retrofit (mantener por compatibilidad)
    val apiService: Retrofit
        get() = retrofit
}