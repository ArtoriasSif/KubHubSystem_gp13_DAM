package com.example.kubhubsystem_gp13_dam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun loginTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        // Texto
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
        errorTextColor = MaterialTheme.colorScheme.error,

        // Contenedor (fondo dentro del outline)
        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        errorContainerColor = MaterialTheme.colorScheme.error, // opcional

        // Cursor
        cursorColor = MaterialTheme.colorScheme.onSurface,
        errorCursorColor = MaterialTheme.colorScheme.error,

        // Bordes / indicador
        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        errorBorderColor = MaterialTheme.colorScheme.error,

        // Labels / placeholder mínimos
        focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f)
    )
}
