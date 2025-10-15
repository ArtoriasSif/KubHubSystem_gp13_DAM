package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.local.entities.MovimientoEntity
import com.example.kubhubsystem_gp13_dam.repository.MovimientoRepository
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.MovimientoViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context.applicationContext) }

    val movimientoRepository = remember {
        MovimientoRepository(
            movimientoDao = database.movimientoDao(),
            inventarioDao = database.inventarioDao()
        )
    }

    val viewModel = remember {
        MovimientoViewModel(movimientoRepository)
    }

    val movimientos by viewModel.movimientos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedInventarioId by remember { mutableStateOf<Int?>(null) }
    var tipoMovimiento by remember { mutableStateOf("ENTRADA") }

    // Mostrar snackbar de errores/éxitos
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            // Mostrar error
            println("Error: $it")
        }
        successMessage?.let {
            // Mostrar éxito
            println("Éxito: $it")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimientos de Inventario") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Movimiento")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (movimientos.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay movimientos registrados")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = movimientos.sortedByDescending { it.fechaMovimiento },
                        key = { it.idMovimiento }
                    ) { movimiento ->
                        MovimientoCard(movimiento)
                    }
                }
            }
        }
    }

    if (showDialog) {
        NuevoMovimientoDialog(
            onDismiss = { showDialog = false },
            onConfirm = { idInventario, cantidad, tipo ->
                when (tipo) {
                    "ENTRADA" -> viewModel.registrarEntrada(idInventario, cantidad)
                    "SALIDA" -> viewModel.registrarSalida(idInventario, cantidad)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun MovimientoCard(movimiento: MovimientoEntity) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val isEntrada = movimiento.tipoMovimiento == "ENTRADA"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = movimiento.tipoMovimiento,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEntrada) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Text(
                    text = "Inventario ID: ${movimiento.idInventario}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = movimiento.fechaMovimiento.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${if (isEntrada) "+" else "-"}${movimiento.cantidadeMovimiento}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isEntrada) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoMovimientoDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, String) -> Unit
) {
    var idInventario by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var tipoMovimiento by remember { mutableStateOf("ENTRADA") }
    var showTipoMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Movimiento") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = idInventario,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                            idInventario = it
                        }
                    },
                    label = { Text("ID Inventario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            cantidad = it
                        }
                    },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = showTipoMenu,
                    onExpandedChange = { showTipoMenu = it }
                ) {
                    OutlinedTextField(
                        value = tipoMovimiento,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Movimiento") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTipoMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showTipoMenu,
                        onDismissRequest = { showTipoMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ENTRADA") },
                            onClick = {
                                tipoMovimiento = "ENTRADA"
                                showTipoMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("SALIDA") },
                            onClick = {
                                tipoMovimiento = "SALIDA"
                                showTipoMenu = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val id = idInventario.toIntOrNull()
                    val cant = cantidad.toDoubleOrNull()
                    if (id != null && cant != null && cant > 0) {
                        onConfirm(id, cant, tipoMovimiento)
                    }
                },
                enabled = idInventario.isNotEmpty() &&
                        cantidad.isNotEmpty() &&
                        cantidad.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}