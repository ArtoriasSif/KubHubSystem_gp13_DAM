package com.example.kubhubsystem_gp13_dam.local.remote

import com.example.kubhubsystem_gp13_dam.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val BASE_URL: String = BuildConfig.BASE_URL

    // ✅ Configurar Gson para serializar correctamente los Enums
    private val gson = GsonBuilder()
        .setLenient() // Permite JSON más flexible
        .serializeNulls() // Incluye campos null en el JSON
        .create()

    // ✅ Cliente HTTP con logging (opcional pero útil para debug)
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // ✅ Instancia única de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)) // 🔥 Usar el Gson configurado
            .build()
    }

    val apiService: Retrofit
        get() = retrofit

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}