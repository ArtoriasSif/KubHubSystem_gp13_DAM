package com.example.kubhubsystem_gp13_dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.kubhubsystem_gp13_dam.ui.screens.AppContainer
import com.example.kubhubsystem_gp13_dam.ui.theme.KubHubSystem_gp13_DAMTheme
import com.example.kubhubsystem_gp13_dam.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App() // <-- Aquí llamamos a tu App composable
        }
    }
}

// ================= COMPOSABLE PRINCIPAL =================
@Composable
fun App() {
    var isDarkTheme by remember { mutableStateOf(false) } // Control del tema

    KubHubSystem_gp13_DAMTheme(darkTheme = isDarkTheme) {
        AppContainer(
            onTopButtonClick = {
                // Acción global del botón
                isDarkTheme = !isDarkTheme
            }
        ) {
            MainScreen() // Todo el contenido de tu app
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KubHubSystem_gp13_DAMTheme {
        Greeting("Android")
    }
}