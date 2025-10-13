package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.model.EstadoSolicitud
import com.example.kubhubsystem_gp13_dam.model.Solicitud
import com.example.kubhubsystem_gp13_dam.model.formatear
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidosScreen(
    viewModel: SolicitudViewModel = viewModel()
) {
    val solicitudes by viewModel.solicitudesFiltradas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedEstado by viewModel.selectedEstado.collectAsState()

    var showEstadoMenu by remember { mutableStateOf(false) }
    var solicitudSeleccionada by remember { mutableStateOf<Solicitud?>(null) }
    var showDetalleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gestión de Pedidos",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Administre los pedidos de insumos realizados por los profesores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Barra de búsqueda y filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar pedidos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Box {
                OutlinedButton(
                    onClick = { showEstadoMenu = true },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedEstado?.displayName ?: "Todos los estados")
                }

                DropdownMenu(
                    expanded = showEstadoMenu,
                    onDismissRequest = { showEstadoMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los estados") },
                        onClick = {
                            viewModel.updateSelectedEstado(null)
                            showEstadoMenu = false
                        }
                    )
                    EstadoSolicitud.values().forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.displayName) },
                            onClick = {
                                viewModel.updateSelectedEstado(estado)
                                showEstadoMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Contador
        Text(
            text = "${solicitudes.size} pedido(s) encontrado(s)",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tabla de pedidos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Encabezados
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ASIGNATURA", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("PROFESOR", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                    Text("FECHA SOLICITUD", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold)
                    Text("FECHA CLASE", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold)
                    Text("ESTADO", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("ACCIONES", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                if (solicitudes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron pedidos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn {
                        items(solicitudes) { solicitud ->
                            PedidoRow(
                                solicitud = solicitud,
                                onVerDetalle = {
                                    solicitudSeleccionada = solicitud
                                    showDetalleDialog = true
                                },
                                onCambiarEstado = { nuevoEstado ->
                                    viewModel.cambiarEstado(solicitud.idSolicitud, nuevoEstado)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "© 2025 KuHub System | Version 0.1",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDetalleDialog && solicitudSeleccionada != null) {
        DetallePedidoDialog(
            solicitud = solicitudSeleccionada!!,
            onDismiss = { showDetalleDialog = false }
        )
    }
}

@Composable
fun PedidoRow(
    solicitud: Solicitud,
    onVerDetalle: () -> Unit,
    onCambiarEstado: (EstadoSolicitud) -> Unit
) {
    var showMenuOptions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = solicitud.asignatura.nombreRamo,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = solicitud.profesor,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = solicitud.fechaSolicitud.formatear(),
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = solicitud.fechaClase.formatear(),
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodySmall
        )

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color(solicitud.estado.color).copy(alpha = 0.2f)
        ) {
            Text(
                text = solicitud.estado.displayName,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(solicitud.estado.color)
            )
        }

        Box(modifier = Modifier.weight(0.8f)) {
            Row {
                IconButton(onClick = onVerDetalle) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Ver detalle",
                        tint = Color(0xFFFFC107)
                    )
                }

                IconButton(onClick = { showMenuOptions = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }
            }

            DropdownMenu(
                expanded = showMenuOptions,
                onDismissRequest = { showMenuOptions = false }
            ) {
                EstadoSolicitud.values().forEach { estado ->
                    if (estado != solicitud.estado) {
                        DropdownMenuItem(
                            text = { Text("Marcar como ${estado.displayName}") },
                            onClick = {
                                onCambiarEstado(estado)
                                showMenuOptions = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetallePedidoDialog(
    solicitud: Solicitud,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle del Pedido") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Información de asignatura
                Text("Asignatura", fontWeight = FontWeight.Bold)
                Text("${solicitud.asignatura.codigoRamo} - ${solicitud.asignatura.nombreRamo}")

                // Información de profesor
                Text("Profesor", fontWeight = FontWeight.Bold)
                Text(solicitud.profesor)

                // Fechas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Fecha de Solicitud", fontWeight = FontWeight.Bold)
                        Text(solicitud.fechaSolicitud.formatear())
                    }
                    Column {
                        Text("Fecha de Clase", fontWeight = FontWeight.Bold)
                        Text(solicitud.fechaClase.formatear())
                    }
                }

                // Estado
                Text("Estado", fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(solicitud.estado.color).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = solicitud.estado.displayName,
                        modifier = Modifier.padding(8.dp),
                        color = Color(solicitud.estado.color)
                    )
                }

                // Productos solicitados
                Text("Productos Solicitados", fontWeight = FontWeight.Bold)
                if (solicitud.productos.isEmpty()) {
                    Text("Sin productos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column {
                        solicitud.productos.forEach { producto ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(producto.producto.nombre)
                                Text("${producto.cantidadSolicitada} ${producto.unidad}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}