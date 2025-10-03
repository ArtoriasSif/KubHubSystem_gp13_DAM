package com.example.kubhubsystem_gp13_dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kubhubsystem_gp13_dam.ui.screens.AppContainer
import com.example.kubhubsystem_gp13_dam.ui.theme.KubHubSystem_gp13_DAMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KubHubSystem_gp13_DAMTheme {
                AppContainer()
            }
        }
    }
}