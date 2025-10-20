package com.example.kubhubsystem_gp13_dam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kubhubsystem_gp13_dam.model.PerfilUsuario

/**
 * Componente Avatar que muestra foto de perfil o iniciales con color de fondo
 * Es el componente visual principal para representar usuarios en la UI
 *
 * Comportamiento:
 * - Si el usuario tiene foto (fotoPerfil != null): muestra la imagen
 * - Si no tiene foto: muestra círculo con iniciales y color de fondo
 * - Soporta diferentes tamaños mediante el parámetro size
 * - Opcionalmente clickeable para cambiar foto
 *
 * @param perfil PerfilUsuario con datos del avatar (puede ser null para mostrar placeholder)
 * @param modifier Modificadores opcionales de Compose
 * @param size Tamaño del avatar (diámetro del círculo)
 * @param onClick Callback opcional cuando se hace click (null = no clickeable)
 * @param mostrarBorde Si debe mostrar un borde alrededor del avatar
 * @param colorBorde Color del borde (solo si mostrarBorde = true)
 */
@Composable
fun AvatarUsuario(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null,
    mostrarBorde: Boolean = false,
    colorBorde: Color = MaterialTheme.colorScheme.primary
) {
    val baseModifier = modifier
        .size(size)
        .then(
            if (mostrarBorde) {
                Modifier.border(2.dp, colorBorde, CircleShape)
            } else {
                Modifier
            }
        )
        .clip(CircleShape)
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )

    if (perfil?.fotoPerfil != null) {
        // Mostrar foto de perfil usando Coil
        AsyncImage(
            model = perfil.fotoPerfil,
            contentDescription = "Foto de perfil",
            modifier = baseModifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Mostrar iniciales con color de fondo
        Box(
            modifier = baseModifier
                .background(
                    color = Color(perfil?.colorFondo ?: 0xFFBDBDBD),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = perfil?.iniciales ?: "??",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (size.value / 3).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Variante del avatar con indicador de estado online/offline
 *
 * @param perfil PerfilUsuario con datos del avatar
 * @param enLinea Si el usuario está en línea
 * @param size Tamaño del avatar
 * @param onClick Callback opcional cuando se hace click
 */
@Composable
fun AvatarUsuarioConEstado(
    perfil: PerfilUsuario?,
    enLinea: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        // Avatar principal
        AvatarUsuario(
            perfil = perfil,
            size = size,
            onClick = onClick
        )

        // Indicador de estado en la esquina
        if (enLinea) {
            Box(
                modifier = Modifier
                    .size(size / 5)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)) // Verde
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

/**
 * Avatar pequeño para uso en listas o lugares con espacio reducido
 *
 * @param perfil PerfilUsuario con datos del avatar
 * @param modifier Modificadores opcionales
 * @param onClick Callback opcional cuando se hace click
 */
@Composable
fun AvatarPequeno(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AvatarUsuario(
        perfil = perfil,
        modifier = modifier,
        size = 40.dp,
        onClick = onClick
    )
}

/**
 * Avatar mediano para uso general
 *
 * @param perfil PerfilUsuario con datos del avatar
 * @param modifier Modificadores opcionales
 * @param onClick Callback opcional cuando se hace click
 */
@Composable
fun AvatarMediano(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AvatarUsuario(
        perfil = perfil,
        modifier = modifier,
        size = 56.dp,
        onClick = onClick
    )
}

/**
 * Avatar grande para pantallas de perfil
 *
 * @param perfil PerfilUsuario con datos del avatar
 * @param modifier Modificadores opcionales
 * @param onClick Callback opcional cuando se hace click
 */
@Composable
fun AvatarGrande(
    perfil: PerfilUsuario?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AvatarUsuario(
        perfil = perfil,
        modifier = modifier,
        size = 120.dp,
        onClick = onClick
    )
}

/**
 * Avatar placeholder cuando no hay perfil disponible
 * Muestra un icono genérico de persona
 *
 * @param size Tamaño del avatar
 * @param modifier Modificadores opcionales
 */
@Composable
fun AvatarPlaceholder(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Usuario",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

/**
 * Grupo de avatares apilados (útil para mostrar múltiples usuarios)
 * Por ejemplo: "Juan, María y 3 más"
 *
 * @param perfiles Lista de perfiles a mostrar
 * @param maxVisible Máximo número de avatares visibles
 * @param size Tamaño de cada avatar
 * @param modifier Modificadores opcionales
 */
@Composable
fun GrupoAvatares(
    perfiles: List<PerfilUsuario>,
    maxVisible: Int = 3,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-size * 0.3f)) // Overlap
    ) {
        perfiles.take(maxVisible).forEach { perfil ->
            AvatarUsuario(
                perfil = perfil,
                size = size,
                mostrarBorde = true,
                colorBorde = MaterialTheme.colorScheme.surface
            )
        }

        // Mostrar contador si hay más usuarios
        val restantes = perfiles.size - maxVisible
        if (restantes > 0) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$restantes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Avatar con badge de notificación
 * Útil para mostrar mensajes no leídos o alertas
 *
 * @param perfil PerfilUsuario con datos del avatar
 * @param contadorNotificaciones Número de notificaciones (0 = sin badge)
 * @param size Tamaño del avatar
 * @param onClick Callback opcional cuando se hace click
 */
@Composable
fun AvatarConNotificaciones(
    perfil: PerfilUsuario?,
    contadorNotificaciones: Int = 0,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        AvatarUsuario(
            perfil = perfil,
            size = size,
            onClick = onClick
        )

        // Badge de notificaciones
        if (contadorNotificaciones > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (size * 0.1f), y = -(size * 0.1f)),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = if (contadorNotificaciones > 99) "99+" else contadorNotificaciones.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}