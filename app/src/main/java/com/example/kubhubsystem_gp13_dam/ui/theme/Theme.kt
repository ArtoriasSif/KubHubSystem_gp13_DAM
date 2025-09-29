package com.example.kubhubsystem_gp13_dam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// === COLOR SCHEME OSCURO ===
private val DarkColorScheme = darkColorScheme(
    primary = Primary,                  // Color principal
    onPrimary = OnPrimary,              // Texto sobre primary
    primaryContainer = PrimaryContainer,
    secondary = Secondary,              // Color secundario
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceDim,
    inverseSurface = InverseSurface,
    outline = Outline
)

// === COLOR SCHEME CLARO ===
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceDim,
    inverseSurface = InverseSurface,
    outline = Outline
)

// === THEME COMPOSABLE ===
@Composable
fun KubHubSystem_gp13_DAMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // MaterialTheme aplica colores, tipografía y formas a todos los composables
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}