package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudScreen(
    viewModel: SolicitudViewModel,
    onNavigateBack: () -> Unit
) {
    val detallesTemp by viewModel.detallesTemp.collectAsState()
    val productosDisponibles by viewModel.productosDisponibles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var cantidadPersonasError by remember { mutableStateOf(false) }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Estados para los campos del formulario
    var cantidadPersonas by remember { mutableStateOf("20") }
    var observaciones by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf(LocalDateTime.now()) }

    var mostrarDialogoProducto by remember { mutableStateOf(false) }
    var mostrarDialogoReceta by remember { mutableStateOf(false) }

    val asignaturas by viewModel.asignaturas.collectAsState()
    val asignaturaSeleccionada by viewModel.asignaturaSeleccionada.collectAsState()
    var expandidoAsignatura by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
            // Información básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información de la Solicitud",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Fecha
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Fecha de Solicitud",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cantidadPersonas,
                        onValueChange = { newValue ->
                            // Solo permitir números
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                cantidadPersonas = newValue
                                cantidadPersonasError =
                                    newValue.isEmpty() || newValue.toIntOrNull() == null || newValue.toInt() <= 0

                                // Actualizar en el ViewModel si es válido
                                newValue.toIntOrNull()?.let { cantidad ->
                                    if (cantidad > 0) {
                                        viewModel.actualizarCantidadPersonas(cantidad)
                                    }
                                }
                            }
                        },
                        label = { Text("Cantidad de Personas") },
                        leadingIcon = { Icon(Icons.Default.People, null) },
                        isError = cantidadPersonasError,
                        supportingText = {
                            if (cantidadPersonasError) {
                                Text(
                                    "Ingrese un número válido mayor a 0",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información Académica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandidoAsignatura,
                        onExpandedChange = { expandidoAsignatura = !expandidoAsignatura }
                    ) {
                        OutlinedTextField(
                            value = asignaturaSeleccionada?.nombreAsignatura ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Asignatura") },
                            placeholder = { Text("Seleccione una asignatura") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoAsignatura)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expandidoAsignatura,
                            onDismissRequest = { expandidoAsignatura = false }
                        ) {
                            asignaturas.forEach { asignatura ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(asignatura.nombreAsignatura)
                                            Text(
                                                asignatura.codigoAsignatura,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.seleccionarAsignatura(asignatura)
                                        expandidoAsignatura = false
                                    }
                                )
                            }
                        }
                    }

                    // ✅ NUEVO: Selector de Sección
                    val secciones by viewModel.secciones.collectAsState()
                    val seccionSeleccionada by viewModel.seccionSeleccionada.collectAsState()
                    var expandidoSeccion by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandidoSeccion,
                        onExpandedChange = { expandidoSeccion = !expandidoSeccion }
                    ) {
                        OutlinedTextField(
                            value = seccionSeleccionada?.nombreSeccion ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sección") },
                            placeholder = { Text("Seleccione una sección") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoSeccion)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = asignaturaSeleccionada != null
                        )

                        ExposedDropdownMenu(
                            expanded = expandidoSeccion,
                            onDismissRequest = { expandidoSeccion = false }
                        ) {
                            secciones.forEach { seccion ->
                                DropdownMenuItem(
                                    text = { Text(seccion.nombreSeccion) },
                                    onClick = {
                                        viewModel.seleccionarSeccion(seccion)
                                        expandidoSeccion = false
                                    }
                                )
                            }
                        }
                    }

                    // ✅ NUEVO: Campo de Docente (editable, se llena automáticamente)
                    val nombreDocente by viewModel.nombreDocente.collectAsState()

                    OutlinedTextField(
                        value = nombreDocente,
                        onValueChange = { viewModel.actualizarNombreDocente(it) },
                        label = { Text("Docente") },
                        placeholder = { Text("Nombre del docente") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = seccionSeleccionada != null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Productos Solicitados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de productos
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

            Spacer(modifier = Modifier.height(16.dp))

            // Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                placeholder = { Text("Comentarios adicionales...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón enviar
            Button(
                onClick = {
                    if (detallesTemp.isNotEmpty()) {
                        viewModel.guardarSolicitud()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = detallesTemp.isNotEmpty() && !isLoading
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar Solicitud")
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
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
                viewModel = viewModel,
                onDismiss = { mostrarDialogoReceta = false }
            )
        }

        // Snackbars
        errorMessage?.let { mensaje ->
            LaunchedEffect(mensaje) {
                // Aquí podrías mostrar un Snackbar
                viewModel.clearError()
            }
        }

        successMessage?.let { mensaje ->
            LaunchedEffect(mensaje) {
                viewModel.clearSuccess()
                if (mensaje.contains("creada") || mensaje.contains("exitosa")) {
                    onNavigateBack()
                }
            }
        }
    }
}

// ============================================
// COMPONENTES
// ============================================

@Composable
fun DetalleProductoCard(
    detalle: DetalleSolicitud,
    onCantidadChange: (Double) -> Unit,
    onEliminar: () -> Unit
) {
    var cantidad by remember { mutableStateOf(detalle.cantidadUnidadMedida.toString()) }
    var cantidadError by remember { mutableStateOf(false) }  // ✅ NUEVO

    Card(modifier = Modifier.fillMaxWidth()) {
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
                // ✅ CAMPO DE CANTIDAD VALIDADO
                Column {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { newValue ->
                            // Permitir números y un punto decimal
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                cantidad = newValue
                                val doubleValue = newValue.toDoubleOrNull()
                                cantidadError = doubleValue == null || doubleValue <= 0

                                if (doubleValue != null && doubleValue > 0) {
                                    onCantidadChange(doubleValue)
                                }
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        isError = cantidadError
                    )
                    if (cantidadError && cantidad.isNotEmpty()) {
                        Text(
                            "Inválido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

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
    var cantidadError by remember { mutableStateOf(false) }  // ✅ NUEVO
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

                // ✅ CAMPO DE CANTIDAD VALIDADO
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { newValue ->
                        // Permitir números y un punto decimal
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            cantidad = newValue
                            val doubleValue = newValue.toDoubleOrNull()
                            cantidadError = doubleValue == null || doubleValue <= 0
                        }
                    },
                    label = { Text("Cantidad") },
                    suffix = { Text(productoSeleccionado?.unidadMedida ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = cantidadError,
                    supportingText = {
                        if (cantidadError && cantidad.isNotEmpty()) {
                            Text(
                                "Ingrese un número válido mayor a 0",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
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
                enabled = productoSeleccionado != null && !cantidadError && cantidad.isNotEmpty()  // ✅ Validar
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
    val recetas by viewModel.recetas.collectAsState()
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.cargarProductosDesdeReceta(receta.idReceta)
                                    onDismiss()
                                }
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