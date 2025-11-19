package com.example.kubhubsystem_gp13_dam.ui.screens.startAndHome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.data.repository.PeriodoRepository
import com.example.kubhubsystem_gp13_dam.viewmodel.PedidoViewModel

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeInternalScreen(
    pedidoViewModel: PedidoViewModel? = null,
    onNavigateToPedidos: () -> Unit = {}
) {
    val periodoRepository = remember { PeriodoRepository.getInstance() }
    val periodoActual by periodoRepository.periodoActual.collectAsState()

    // Observar el pedido activo desde la BD
    val pedidoActivo by (pedidoViewModel?.pedidoActivo?.collectAsState() ?: remember { mutableStateOf(null) })

    val aglomerado by (pedidoViewModel?.aglomerado?.collectAsState() ?: remember { mutableStateOf(emptyList()) })

    var showIniciarPeriodoDialog by remember { mutableStateOf(false) }
    var showCerrarPeriodoDialog by remember { mutableStateOf(false) }

    // 🔄 AÑADIDO: ScrollState para hacer el contenido scrolleable
    val scrollState = rememberScrollState()

    LaunchedEffect(pedidoActivo) {
        pedidoActivo?.let { pedido ->
            if (pedido.estaActivo) {
                periodoRepository.sincronizarConPedido(
                    idPedido = pedido.idPedido,
                    fechaInicio = pedido.fechaInicioRango.toLocalDate(),
                    fechaFin = pedido.fechaFinRango.toLocalDate(),
                    estaActivo = true
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Título
        Text(
            text = "Bienvenido a Kubhub System",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sistema de gestión de inventario y pedidos para instituciones educativas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card de Periodo de Recolección
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (periodoActual != null && pedidoActivo != null)
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = if (periodoActual != null && pedidoActivo != null)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column {
                            Text(
                                text = "Periodo de Recolección",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            if (periodoActual != null && pedidoActivo != null) {
                                Surface(
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "ACTIVO",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "INACTIVO",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                if (periodoActual != null && pedidoActivo != null) {
                    // Información del periodo activo
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoRow(
                            label = "Fecha de Inicio",
                            value = periodoActual!!.fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        )

                        InfoRow(
                            label = "Fecha de Cierre",
                            value = periodoActual!!.fechaCierre.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        )

                        InfoRow(
                            label = "Solicitudes Recibidas",
                            value = "${periodoActual!!.solicitudesIds.size}"
                        )

                        InfoRow(
                            label = "Estado del Pedido",
                            value = pedidoActivo!!.estadoPedido.displayName,
                            valueColor = Color(0xFF2196F3)
                        )

                        val diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            periodoActual!!.fechaCierre
                        )

                        InfoRow(
                            label = "Días Restantes",
                            value = "$diasRestantes días",
                            valueColor = if (diasRestantes <= 2) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showCerrarPeriodoDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Periodo")
                    }
                } else {
                    // No hay periodo activo
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "No hay un periodo de recolección activo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Inicie un nuevo periodo para que los profesores puedan realizar solicitudes de insumos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showIniciarPeriodoDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107)
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Periodo", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        //  Resumen del Pedido Actual
        if (pedidoActivo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF2196F3)
                        )
                        Text(
                            text = "Resumen del Pedido Actual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider()

                    //Usar el aglomerado observado en tiempo real
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total de productos:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${aglomerado.size}",  // ✅ CAMBIO PRINCIPAL
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }

                    // Botón para ir a Gestión de Pedidos
                    Button(
                        onClick = onNavigateToPedidos,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver más detalles en Gestión de Pedidos")
                    }
                }
            }
        }

        // Cards de acceso rápido
        Text(
            text = "Acceso Rápido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickAccessCard(
                title = "Inventario",
                icon = Icons.Default.Inventory,
                description = "Gestionar productos",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )

            QuickAccessCard(
                title = "Solicitudes",
                icon = Icons.Default.Assignment,
                description = "Ver pedidos",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )

            QuickAccessCard(
                title = "Recetas",
                icon = Icons.Default.MenuBook,
                description = "Gestionar recetas",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Diálogo para iniciar periodo
    if (showIniciarPeriodoDialog) {
        IniciarPeriodoDialog(
            onDismiss = { showIniciarPeriodoDialog = false },
            onConfirm = { fechaCierre ->
                periodoRepository.iniciarPeriodo(fechaCierre)
                pedidoViewModel?.let { vm ->
                    val fechaInicio = LocalDate.now().atStartOfDay()
                    val fechaFin = fechaCierre.atTime(23, 59, 59)
                    vm.iniciarNuevoPeriodo(fechaInicio, fechaFin)
                }
                showIniciarPeriodoDialog = false
            }
        )
    }

    // Diálogo para cerrar periodo
    if (showCerrarPeriodoDialog) {
        AlertDialog(
            onDismissRequest = { showCerrarPeriodoDialog = false },
            title = { Text("Cerrar Periodo") },
            text = {
                Text("¿Está seguro de que desea cerrar el periodo actual? Los profesores no podrán realizar más solicitudes hasta que se inicie un nuevo periodo.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        periodoActual?.let { periodoRepository.cerrarPeriodo(it.idPeriodo) }
                        pedidoActivo?.let { pedido ->
                            pedidoViewModel?.cerrarPedidoActual(pedido.idPedido)
                        }
                        showCerrarPeriodoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Cerrar Periodo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCerrarPeriodoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Resto de funciones Composable (sin cambios)
@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IniciarPeriodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var fechaCierre by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Iniciar Periodo de Recolección") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Seleccione la fecha de cierre para el nuevo periodo de recolección de solicitudes.")

                OutlinedTextField(
                    value = fechaCierre?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                    onValueChange = {},
                    label = { Text("Fecha de Cierre") },
                    placeholder = { Text("Seleccione una fecha") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (fechaCierre != null) {
                    val diasDuracion = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        fechaCierre
                    )
                    Text(
                        text = "El periodo durará $diasDuracion días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    fechaCierre?.let { onConfirm(it) }
                },
                enabled = fechaCierre != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Text("Iniciar Periodo", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.now().plusDays(7)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaCierre = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}