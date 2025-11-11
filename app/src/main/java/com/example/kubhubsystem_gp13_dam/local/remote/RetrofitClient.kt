package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ✅ Base URL leída desde build.gradle.kts (BuildConfig)
    // Ejemplo: "http://54.82.10.87/api/v1/"
    private val BASE_URL: String = BuildConfig.BASE_URL

    // ✅ Cliente HTTP configurado (puedes añadir interceptores si lo deseas)
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    // ✅ Instancia única de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ Proporciona acceso a Retrofit para crear tus servicios
    val apiService: Retrofit
        get() = retrofit

    // ✅ Alternativa genérica
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
