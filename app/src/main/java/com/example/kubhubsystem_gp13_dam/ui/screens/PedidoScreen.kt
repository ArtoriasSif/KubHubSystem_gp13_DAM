package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.model.*

import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidosScreen(  // ✅ Nombre cambiado
    viewModel: PedidoViewModel,
    onNavigateToSolicitud: (Int?) -> Unit
) {
    val pedidoActivo by viewModel.pedidoActivo.collectAsState()
    val solicitudesPedido by viewModel.solicitudesPedido.collectAsState()
    val aglomerado by viewModel.aglomerado.collectAsState()
    val progresoPedido by viewModel.progresoPedido.collectAsState()
    val mostrarPedidoAnterior by viewModel.mostrarPedidoAnterior.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var pestañaSeleccionada by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                actions = {
                    IconButton(onClick = { viewModel.toggleMostrarPedidoAnterior() }) {
                        Icon(
                            if (mostrarPedidoAnterior) Icons.Default.Refresh else Icons.Default.History,
                            "Ver histórico"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!mostrarPedidoAnterior && pestañaSeleccionada == 1) {
                FloatingActionButton(
                    onClick = { onNavigateToSolicitud(null) }
                ) {
                    Icon(Icons.Default.Add, "Nueva Solicitud")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            pedidoActivo?.let { pedido ->
                // Barra de progreso del pedido
                BarraProgresoPedido(
                    estadoActual = pedido.estadoPedido,
                    progreso = progresoPedido
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs
                TabRow(selectedTabIndex = pestañaSeleccionada) {
                    Tab(
                        selected = pestañaSeleccionada == 0,
                        onClick = { pestañaSeleccionada = 0 },
                        text = { Text("Aglomerado") }
                    )
                    Tab(
                        selected = pestañaSeleccionada == 1,
                        onClick = { pestañaSeleccionada = 1 },
                        text = { Text("Solicitudes") }
                    )
                }

                // Contenido según pestaña
                when (pestañaSeleccionada) {
                    0 -> AglomeradoTab(
                        aglomerado = aglomerado,
                        viewModel = viewModel
                    )
                    1 -> SolicitudesTab(
                        solicitudes = solicitudesPedido,
                        viewModel = viewModel,
                        onVerDetalle = onNavigateToSolicitud
                    )
                }
            } ?: run {
                // No hay pedido activo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "No hay pedido activo",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Inicia un nuevo período para comenzar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun BarraProgresoPedido(
    estadoActual: EstadoPedido,
    progreso: Float
) {
    val estados = listOf(
        EstadoPedido.EN_PROCESO,
        EstadoPedido.PENDIENTE_REVISION,
        EstadoPedido.CHECK_INVENTARIO,
        EstadoPedido.ENVIADO_COTIZACION
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Estado del Pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                estados.forEachIndexed { index, estado ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Círculo de estado
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (estado.orden <= estadoActual.orden) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (estado.orden < estadoActual.orden) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    "${index + 1}",
                                    color = if (estado.orden <= estadoActual.orden) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            estado.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (estado == estadoActual) FontWeight.Bold else FontWeight.Normal,
                            color = if (estado.orden <= estadoActual.orden) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }

                    // Línea conectora
                    if (index < estados.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(2.dp)
                                .align(Alignment.CenterVertically)
                                .offset(y = (-20).dp)
                                .background(
                                    if (estado.orden < estadoActual.orden) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }
            }

            // Barra de progreso para solicitudes procesadas
            if (estadoActual == EstadoPedido.PENDIENTE_REVISION) {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Solicitudes Procesadas",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${(progreso * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progreso,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun AglomeradoTab(
    aglomerado: List<AglomeradoPedido>,
    viewModel: PedidoViewModel
) {
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var filtroAsignatura by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filtros y acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total productos: ${aglomerado.size}",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { /* Implementar filtro por asignatura */ }
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtrar")
                }

                Button(onClick = { mostrarDialogoAgregar = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (aglomerado.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No hay productos en el aglomerado",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(aglomerado) { item ->
                    AglomeradoProductoCard(
                        aglomerado = item,
                        onCantidadChange = { nuevaCantidad ->
                            viewModel.actualizarCantidadAglomerado(item.idAglomerado, nuevaCantidad)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AglomeradoProductoCard(
    aglomerado: AglomeradoPedido,
    onCantidadChange: (Double) -> Unit
) {
    var cantidad by remember { mutableStateOf(aglomerado.cantidadTotal.toString()) }
    var editando by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = aglomerado.producto.nombreProducto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = aglomerado.producto.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                aglomerado.asignatura?.let {
                    Text(
                        text = "Asignatura: ${it.nombreAsignatura}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (editando) {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            cantidad.toDoubleOrNull()?.let { nuevaCantidad ->
                                onCantidadChange(nuevaCantidad)
                                editando = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, "Confirmar")
                    }
                } else {
                    Text(
                        text = "${aglomerado.cantidadTotal} ${aglomerado.producto.unidadMedida}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { editando = true }) {
                        Icon(Icons.Default.Edit, "Editar")
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudesTab(
    solicitudes: List<Solicitud>,
    viewModel: PedidoViewModel,
    onVerDetalle: (Int) -> Unit
) {
    var filtroEstado by remember { mutableStateOf("Todos") }
    val estados = listOf("Todos", "Pendiente", "Aprobado", "Rechazado")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filtro por estado
        ScrollableTabRow(
            selectedTabIndex = estados.indexOf(filtroEstado),
            edgePadding = 0.dp
        ) {
            estados.forEach { estado ->
                Tab(
                    selected = filtroEstado == estado,
                    onClick = { filtroEstado = estado },
                    text = { Text(estado) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val solicitudesFiltradas = if (filtroEstado == "Todos") {
            solicitudes
        } else {
            // Necesitarías agregar el estado a la solicitud
            solicitudes
        }

        if (solicitudesFiltradas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No hay solicitudes",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(solicitudesFiltradas) { solicitud ->
                    SolicitudCard(
                        solicitud = solicitud,
                        onAprobar = { viewModel.aprobarSolicitud(solicitud.idSolicitud) },
                        onRechazar = { viewModel.rechazarSolicitud(solicitud.idSolicitud) },
                        onVerDetalle = { onVerDetalle(solicitud.idSolicitud) }
                    )
                }
            }
        }
    }
}

@Composable
fun SolicitudCard(
    solicitud: Solicitud,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onVerDetalle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = solicitud.seccion.nombreSeccion,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Docente: ${solicitud.docenteSeccion.primeroNombre} ${solicitud.docenteSeccion.apellidoPaterno}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Sala: ${solicitud.reservaSala.sala.codigoSala} - ${solicitud.reservaSala.diaSemana.nombreMostrar} Bloque ${solicitud.reservaSala.bloqueHorario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${solicitud.detalleSolicitud.size} productos - ${solicitud.cantidadPersonas} personas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Badge de estado (necesitarías agregar estado a Solicitud)
                AssistChip(
                    onClick = { },
                    label = { Text("Pendiente") }, // Cambiar según estado real
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onVerDetalle,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver")
                }

                Button(
                    onClick = onAprobar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aprobar")
                }

                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar")
                }
            }
        }
    }
}