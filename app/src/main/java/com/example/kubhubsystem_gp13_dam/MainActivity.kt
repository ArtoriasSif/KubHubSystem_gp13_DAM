package com.example.kubhubsystem_gp13_dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome.AppContainer
import com.example.kubhubsystem_gp13_dam.ui.theme.KubHubSystem_gp13_DAMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐ PASO CLAVE: Inicializar Retrofit aquí
        // Usamos 'applicationContext' para que el SessionManager viva mientras la app viva
        RetrofitClient.init(applicationContext)
        println("✅ RetrofitClient inicializado desde MainActivity")

        enableEdgeToEdge()
        setContent {
            KubHubSystem_gp13_DAMTheme {
                AppContainer()
            }
        }
    }
}