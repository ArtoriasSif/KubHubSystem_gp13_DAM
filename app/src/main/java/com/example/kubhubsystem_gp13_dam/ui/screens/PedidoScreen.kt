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

/**
 * Pantalla principal de gestión de pedidos.
 * Muestra el estado del pedido activo, aglomerado de productos y solicitudes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPedidosScreen(
    viewModel: PedidoViewModel,
    onNavigateToSolicitud: (Int?) -> Unit
) {
    // Observar estados del ViewModel usando collectAsState
    val pedidoActivo by viewModel.pedidoActivo.collectAsState()
    val solicitudesPedido by viewModel.solicitudesPedido.collectAsState()
    val aglomerado by viewModel.aglomerado.collectAsState()
    val progresoPedido by viewModel.progresoPedido.collectAsState()
    val mostrarPedidoAnterior by viewModel.mostrarPedidoAnterior.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado local para la pestaña seleccionada
    var pestañaSeleccionada by remember { mutableStateOf(0) }

    // Monitorear tiempo de composición y detectar errores
    DisposableEffect(Unit) {
        val startTime = System.currentTimeMillis()

        onDispose {
            val compositionTime = System.currentTimeMillis() - startTime
            // Si la composición tarda más de 3 segundos, podría haber un problema
            if (compositionTime > 3000) {
                println("⚠️ WARNING: GestionPedidosScreen tomó ${compositionTime}ms en componerse")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                actions = {
                    // Botón para alternar entre pedido actual e histórico
                    IconButton(onClick = { viewModel.toggleMostrarPedidoAnterior() }) {
                        Icon(
                            if (mostrarPedidoAnterior) Icons.Default.Refresh else Icons.Default.History,
                            contentDescription = "Ver histórico"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Mostrar FAB solo si no está en modo histórico y está en pestaña de solicitudes
            if (!mostrarPedidoAnterior && pestañaSeleccionada == 1) {
                FloatingActionButton(
                    onClick = { onNavigateToSolicitud(null) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Solicitud")
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

                // Pestañas: Aglomerado y Solicitudes
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

                // Contenido según pestaña seleccionada
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
                // No hay pedido activo - Mostrar estado vacío
                EmptyPedidoState()
            }

            // Indicador de carga
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * Componente que muestra el estado vacío cuando no hay pedido activo.
 */
@Composable
private fun EmptyPedidoState() {
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

/**
 * Barra de progreso visual que muestra el estado actual del pedido.
 * Incluye 4 estados: En Proceso, Pendiente Revisión, Check Inventario, Enviado Cotización.
 */
@Composable
fun BarraProgresoPedido(
    estadoActual: EstadoPedido,
    progreso: Float
) {
    // Lista de estados en orden secuencial
    val estados = remember {
        listOf(
            EstadoPedido.EN_PROCESO,
            EstadoPedido.PENDIENTE_REVISION,
            EstadoPedido.CHECK_INVENTARIO,
            EstadoPedido.ENVIADO_COTIZACION
        )
    }

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

            // Fila de estados con indicadores visuales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                estados.forEachIndexed { index, estado ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Círculo indicador de estado
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
                            // Check si está completado, número si no
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

                        // Nombre del estado
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

                    // Línea conectora entre estados
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

            // Barra de progreso de solicitudes (solo visible en estado PENDIENTE_REVISION)
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

/**
 * Pestaña que muestra el aglomerado de productos del pedido.
 * Permite filtrar, agregar y editar cantidades.
 */
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
        // Barra de acciones: contador, filtro y botón agregar
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
                // Botón de filtro por asignatura
                FilledTonalButton(
                    onClick = {
                        // <- Implementar filtro por asignatura
                        // Mostrar diálogo con lista de asignaturas disponibles
                        // y aplicar filtro según selección del usuario
                        viewModel.aplicarFiltroAsignatura(filtroAsignatura)
                    }
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtrar")
                }

                // Botón para agregar producto manual
                Button(onClick = { mostrarDialogoAgregar = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de productos o estado vacío
        if (aglomerado.isEmpty()) {
            EmptyAglomeradoState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = aglomerado,
                    key = { it.idAglomerado } // Clave única para optimizar recomposición
                ) { item ->
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

/**
 * Estado vacío para cuando no hay productos en el aglomerado.
 */
@Composable
private fun EmptyAglomeradoState() {
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
}

/**
 * Card individual de producto en el aglomerado.
 * Permite editar la cantidad de forma inline.
 */
@Composable
fun AglomeradoProductoCard(
    aglomerado: AglomeradoPedido,
    onCantidadChange: (Double) -> Unit
) {
    // Estado local para edición de cantidad
    var cantidad by remember(aglomerado.cantidadTotal) {
        mutableStateOf(aglomerado.cantidadTotal.toString())
    }
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
            // Información del producto
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
                // Mostrar asignatura si está asignado
                aglomerado.asignatura?.let {
                    Text(
                        text = "Asignatura: ${it.nombreAsignatura}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Cantidad editable
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (editando) {
                    // Modo edición: TextField + botón confirmar
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
                        Icon(Icons.Default.Check, contentDescription = "Confirmar")
                    }
                } else {
                    // Modo lectura: cantidad + botón editar
                    Text(
                        text = "${aglomerado.cantidadTotal} ${aglomerado.producto.unidadMedida}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { editando = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            }
        }
    }
}

/**
 * Pestaña que muestra todas las solicitudes del pedido.
 * Permite filtrar por estado y aprobar/rechazar solicitudes.
 */
@Composable
fun SolicitudesTab(
    solicitudes: List<Solicitud>,
    viewModel: PedidoViewModel,
    onVerDetalle: (Int) -> Unit
) {
    var filtroEstado by remember { mutableStateOf("Todos") }
    var solicitudExpandida by remember { mutableStateOf<Int?>(null) }

    // Lista de estados posibles
    val estados = remember { listOf("Todos", "Pendiente", "Aprobado", "Rechazado") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tabs de filtro por estado
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

        // Filtrar solicitudes según estado seleccionado
        val solicitudesFiltradas = remember(solicitudes, filtroEstado) {
            if (filtroEstado == "Todos") {
                solicitudes
            } else {
                solicitudes.filter { it.estado == filtroEstado }
            }
        }

        // Lista de solicitudes o estado vacío
        if (solicitudesFiltradas.isEmpty()) {
            EmptySolicitudesState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = solicitudesFiltradas,
                    key = { it.idSolicitud } // Clave única para optimizar recomposición
                ) { solicitud ->
                    SolicitudCard(
                        solicitud = solicitud,
                        estaExpandida = solicitudExpandida == solicitud.idSolicitud,
                        onToggleExpansion = {
                            solicitudExpandida = if (solicitudExpandida == solicitud.idSolicitud) {
                                null
                            } else {
                                solicitud.idSolicitud
                            }
                        },
                        onAprobar = {
                            viewModel.aprobarSolicitud(solicitud.idSolicitud)
                        },
                        onRechazar = {
                            viewModel.rechazarSolicitud(solicitud.idSolicitud)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Estado vacío para cuando no hay solicitudes.
 */
@Composable
private fun EmptySolicitudesState() {
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
}

/**
 * Card de solicitud individual.
 * Muestra información resumida y permite expandir para ver detalles completos.
 * Incluye botones de aprobar/rechazar para solicitudes pendientes.
 */
@Composable
fun SolicitudCard(
    solicitud: Solicitud,
    estaExpandida: Boolean,
    onToggleExpansion: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado: información básica + badge de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // ID de solicitud formateado
                    Text(
                        text = String.format("%03d", solicitud.idSolicitud),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    // Información del docente
                    Text(
                        text = "Docente: ${solicitud.docenteSeccion.primerNombre} ${solicitud.docenteSeccion.apellidoPaterno}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Información de sala y horario
                    Text(
                        text = "Sala: ${solicitud.reservaSala.sala.codigoSala} - ${solicitud.reservaSala.diaSemana.nombreMostrar} Bloque ${solicitud.reservaSala.bloqueHorario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    // Resumen de productos y personas
                    Text(
                        text = "${solicitud.detalleSolicitud.size} productos - ${solicitud.cantidadPersonas} personas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Badge de estado con color según estado
                AssistChip(
                    onClick = { },
                    label = { Text(solicitud.estado) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (solicitud.estado) {
                            "Aprobado" -> MaterialTheme.colorScheme.tertiaryContainer
                            "Rechazado" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detalles expandibles: lista de productos
            if (estaExpandida) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Productos solicitados:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Listar todos los productos de la solicitud
                solicitud.detalleSolicitud.forEach { detalle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detalle.producto.nombreProducto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = detalle.producto.categoria,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "${detalle.cantidadUnidadMedida} ${detalle.producto.unidadMedida}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para expandir/contraer detalles
                FilledTonalButton(
                    onClick = onToggleExpansion,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (estaExpandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (estaExpandida) "Ocultar" else "Ver")
                }

                // Botones de aprobar/rechazar solo para solicitudes pendientes
                if (solicitud.estado == "Pendiente") {
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}