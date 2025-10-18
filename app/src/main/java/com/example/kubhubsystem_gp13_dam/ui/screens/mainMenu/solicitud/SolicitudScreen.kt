package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudScreen(
    viewModel: SolicitudViewModel,
    onNavigateBack: () -> Unit
) {
    val solicitudActual by viewModel.solicitudActual.collectAsState()
    val detallesTemp by viewModel.detallesTemp.collectAsState()
    val productosDisponibles by viewModel.productosDisponibles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var mostrarDialogoProducto by remember { mutableStateOf(false) }
    var mostrarDialogoReceta by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (solicitudActual?.idSolicitud == 0) "Nueva Solicitud"
                        else "Editar Solicitud"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (solicitudActual?.idSolicitud == 0) {
                                viewModel.guardarSolicitud()
                            } else {
                                viewModel.actualizarSolicitud()
                            }
                        },
                        enabled = detallesTemp.isNotEmpty() && !isLoading
                    ) {
                        Icon(Icons.Default.Check, "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información de la solicitud
            solicitudActual?.let { solicitud ->
                InfoSolicitudCard(solicitud)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botones para agregar productos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mostrarDialogoReceta = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desde Receta")
                }

                Button(
                    onClick = { mostrarDialogoProducto = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manual")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de productos
            Text(
                "Productos Solicitados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (detallesTemp.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay productos agregados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(detallesTemp) { index, detalle ->
                        DetalleProductoCard(
                            detalle = detalle,
                            onCantidadChange = { nuevaCantidad ->
                                viewModel.actualizarCantidadDetalle(index, nuevaCantidad)
                            },
                            onEliminar = { viewModel.eliminarDetalleTemp(index) }
                        )
                    }
                }
            }

            // Mostrar loading
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        // Diálogos
        if (mostrarDialogoProducto) {
            DialogoAgregarProducto(
                productos = productosDisponibles,
                onDismiss = { mostrarDialogoProducto = false },
                onAgregar = { producto, cantidad ->
                    viewModel.agregarProductoManual(producto, cantidad)
                    mostrarDialogoProducto = false
                }
            )
        }

        if (mostrarDialogoReceta) {
            DialogoSeleccionarReceta(
                viewModel = viewModel, // Pasar el ViewModel completo
                onDismiss = { mostrarDialogoReceta = false }
            )
        }

        // Snackbars para mensajes
        errorMessage?.let { mensaje ->
            LaunchedEffect(mensaje) {
                // Mostrar snackbar o toast
                viewModel.clearError()
            }
        }

        successMessage?.let { mensaje ->
            LaunchedEffect(mensaje) {
                // Mostrar snackbar o toast
                viewModel.clearSuccess()
                if (mensaje.contains("creada") || mensaje.contains("actualizada")) {
                    onNavigateBack()
                }
            }
        }
    }
}

@Composable
fun InfoSolicitudCard(solicitud: Solicitud) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Información de la Solicitud",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("Sección", solicitud.seccion.nombreSeccion)
            InfoRow("Docente", solicitud.docenteSeccion.let {
                "${it.primeroNombre} ${it.apellidoPaterno}"
            })
            InfoRow("Sala", solicitud.reservaSala.sala.codigoSala)
            InfoRow("Día", solicitud.reservaSala.diaSemana.nombreMostrar)
            InfoRow("Bloque", solicitud.reservaSala.bloqueHorario.toString())
            InfoRow("Personas", solicitud.cantidadPersonas.toString())
            InfoRow(
                "Fecha Solicitud",
                solicitud.fechaSolicitud.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun DetalleProductoCard(
    detalle: DetalleSolicitud,
    onCantidadChange: (Double) -> Unit,
    onEliminar: () -> Unit
) {
    var cantidad by remember { mutableStateOf(detalle.cantidadUnidadMedida.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detalle.producto.nombreProducto,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detalle.producto.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        cantidad = it
                        it.toDoubleOrNull()?.let { nuevaCantidad ->
                            onCantidadChange(nuevaCantidad)
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = detalle.producto.unidadMedida,
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAgregarProducto(
    productos: List<Producto>,
    onDismiss: () -> Unit,
    onAgregar: (Producto, Double) -> Unit
) {
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = !expandido }
                ) {
                    OutlinedTextField(
                        value = productoSeleccionado?.nombreProducto ?: "Seleccionar producto",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        productos.forEach { producto ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(producto.nombreProducto)
                                        Text(
                                            producto.categoria,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                },
                                onClick = {
                                    productoSeleccionado = producto
                                    expandido = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad") },
                    suffix = { Text(productoSeleccionado?.unidadMedida ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    productoSeleccionado?.let { producto ->
                        cantidad.toDoubleOrNull()?.let { cant ->
                            if (cant > 0) {
                                onAgregar(producto, cant)
                            }
                        }
                    }
                },
                enabled = productoSeleccionado != null && cantidad.toDoubleOrNull() != null
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogoSeleccionarReceta(
    viewModel: SolicitudViewModel,
    onDismiss: () -> Unit
) {
    val recetas by viewModel.recetasFiltradas.collectAsState()
    val busqueda by viewModel.busquedaReceta.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Receta") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                // Buscador
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { viewModel.actualizarBusquedaReceta(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar receta...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (recetas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay recetas disponibles",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recetas) { receta ->
                            RecetaItemCard(
                                receta = receta,
                                onClick = {
                                    viewModel.cargarProductosDesdeReceta(receta.idReceta)
                                    onDismiss()
                                }
                            )
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

@Composable
private fun RecetaItemCard(
    receta: com.example.kubhubsystem_gp13_dam.ui.model.Receta,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receta.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = receta.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text(receta.categoria) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${receta.ingredientes.size} ingredientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}