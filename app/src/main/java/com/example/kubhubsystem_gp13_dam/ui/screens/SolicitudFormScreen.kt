package com.example.kubhubsystem_gp13_dam.ui.screens
/*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudScreen(
    viewModel: SolicitudViewModel = viewModel()
) {
    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var seccionSeleccionada by remember { mutableStateOf<Seccion?>(null) }
    var fechaClase by remember { mutableStateOf<LocalDate?>(null) }
    var recetaSeleccionada by remember { mutableStateOf<Receta?>(null) }
    var productosSeleccionados by remember { mutableStateOf<List<ProductoSolicitado>>(emptyList()) }
    var observaciones by remember { mutableStateOf("") }

    var showAsignaturaMenu by remember { mutableStateOf(false) }
    var showSeccionMenu by remember { mutableStateOf(false) }
    var showRecetaMenu by remember { mutableStateOf(false) }
    var showAgregarProductoDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val asignaturas by viewModel.asignaturas.collectAsState()
    val recetas by viewModel.recetas.collectAsState()
    val productos by viewModel.productos.collectAsState()

    val seccionesDisponibles = remember(asignaturaSeleccionada) {
        asignaturaSeleccionada?.secciones ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "Solicitud de Insumos",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Seleccione una receta para cargar sus ingredientes y realice su pedido.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Selector de Asignatura
        Box {
            OutlinedTextField(
                value = asignaturaSeleccionada?.let { "${it.codigoRamo} - ${it.nombreRamo}" } ?: "",
                onValueChange = {},
                label = { Text("Asignatura *") },
                placeholder = { Text("Seleccione una asignatura") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showAsignaturaMenu = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Seleccionar")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = showAsignaturaMenu,
                onDismissRequest = { showAsignaturaMenu = false }
            ) {
                asignaturas.forEach { asignatura ->
                    DropdownMenuItem(
                        text = { Text("${asignatura.codigoRamo} - ${asignatura.nombreRamo}") },
                        onClick = {
                            asignaturaSeleccionada = asignatura
                            seccionSeleccionada = null
                            showAsignaturaMenu = false
                        }
                    )
                }
            }
        }

        // Selector de Sección
        if (asignaturaSeleccionada != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = seccionSeleccionada?.let { "Sección ${it.numeroSeccion} - ${it.docente}" } ?: "",
                        onValueChange = {},
                        label = { Text("Sección *") },
                        placeholder = { Text("Seleccione una sección") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showSeccionMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Seleccionar")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = showSeccionMenu,
                        onDismissRequest = { showSeccionMenu = false }
                    ) {
                        seccionesDisponibles.forEach { seccion ->
                            DropdownMenuItem(
                                text = { Text("Sección ${seccion.numeroSeccion} - ${seccion.docente}") },
                                onClick = {
                                    seccionSeleccionada = seccion
                                    showSeccionMenu = false
                                }
                            )
                        }
                    }
                }

                // Fecha de Clase
                OutlinedTextField(
                    value = fechaClase?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Fecha de Clase *") },
                    placeholder = { Text("dd-mm-aaaa") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider()

        // Cargar Receta Base
        Text(
            text = "Cargar Receta Base",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Box {
            OutlinedTextField(
                value = recetaSeleccionada?.nombre ?: "",
                onValueChange = {},
                label = { Text("Receta") },
                placeholder = { Text("Seleccione una receta para cargar sus ingredientes") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showRecetaMenu = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Seleccionar")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = showRecetaMenu,
                onDismissRequest = { showRecetaMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Ninguna (agregar manualmente)") },
                    onClick = {
                        recetaSeleccionada = null
                        showRecetaMenu = false
                    }
                )
                recetas.forEach { receta ->
                    DropdownMenuItem(
                        text = { Text(receta.nombre) },
                        onClick = {
                            recetaSeleccionada = receta
                            // Cargar ingredientes de la receta
                            productosSeleccionados = receta.ingredientes.map { ingrediente ->
                                ProductoSolicitado(
                                    producto = ingrediente.producto,
                                    cantidadSolicitada = ingrediente.cantidad,
                                    unidad = ingrediente.unidad
                                )
                            }
                            showRecetaMenu = false
                        }
                    )
                }
            }
        }

        HorizontalDivider()

        // Ingredientes del Pedido
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredientes del Pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAgregarProductoDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Ingrediente", color = Color.Black)
            }
        }

        // Tabla de ingredientes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text("PRODUCTO", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("CANTIDAD", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("ACCIONES", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                if (productosSeleccionados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Seleccione una receta para ver sus ingredientes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn {
                        items(productosSeleccionados.size) { index ->
                            val producto = productosSeleccionados[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = producto.producto.nombreProducto,
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    text = "${producto.cantidadSolicitada} ${producto.unidad}",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        productosSeleccionados = productosSeleccionados.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier.weight(0.5f)
                                ) {
                                    Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                                }
                            }
                            if (index < productosSeleccionados.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        // Observaciones
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            placeholder = { Text("Añada aquí modificaciones o productos extra (ej: 'Agregar 2kg de sal')") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    // Limpiar formulario
                    asignaturaSeleccionada = null
                    seccionSeleccionada = null
                    fechaClase = null
                    recetaSeleccionada = null
                    productosSeleccionados = emptyList()
                    observaciones = ""
                }
            ) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (asignaturaSeleccionada != null &&
                        seccionSeleccionada != null &&
                        fechaClase != null) {

                        val nuevaSolicitud = Solicitud(
                            asignatura = asignaturaSeleccionada!!,
                            seccion = seccionSeleccionada!!,
                            profesor = seccionSeleccionada!!.docente,
                            fechaClase = LocalDateTime.of(fechaClase, LocalTime.of(8, 0)),
                            productos = productosSeleccionados,
                            recetaBase = recetaSeleccionada,
                            observaciones = observaciones
                        )

                        viewModel.agregarSolicitud(nuevaSolicitud)

                        // Limpiar formulario
                        asignaturaSeleccionada = null
                        seccionSeleccionada = null
                        fechaClase = null
                        recetaSeleccionada = null
                        productosSeleccionados = emptyList()
                        observaciones = ""
                    }
                },
                enabled = asignaturaSeleccionada != null &&
                        seccionSeleccionada != null &&
                        fechaClase != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Text("Enviar Solicitud", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Diálogo para agregar producto manualmente
    if (showAgregarProductoDialog) {
        AgregarProductoSolicitudDialog(
            productos = productos,
            onDismiss = { showAgregarProductoDialog = false },
            onAdd = { producto ->
                productosSeleccionados = productosSeleccionados + producto
                showAgregarProductoDialog = false
            }
        )
    }

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaClase = java.time.Instant.ofEpochMilli(millis)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoSolicitudDialog(
    productos: List<Producto>,
    onDismiss: () -> Unit,
    onAdd: (ProductoSolicitado) -> Unit
) {
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("kg") }
    var showProductoMenu by remember { mutableStateOf(false) }
    var showUnidadMenu by remember { mutableStateOf(false) }

    val unidades = listOf("kg", "g", "l", "ml", "unidad", "taza", "cucharada")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = showProductoMenu,
                    onExpandedChange = { showProductoMenu = it }
                ) {
                    OutlinedTextField(
                        value = productoSeleccionado?.nombreProducto ?: "Seleccione producto",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductoMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showProductoMenu,
                        onDismissRequest = { showProductoMenu = false }
                    ) {
                        productos.forEach { producto ->
                            DropdownMenuItem(
                                text = { Text(producto.nombreProducto) },
                                onClick = {
                                    productoSeleccionado = producto
                                    unidad = producto.unidadMedida
                                    showProductoMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = showUnidadMenu,
                    onExpandedChange = { showUnidadMenu = it }
                ) {
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnidadMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showUnidadMenu,
                        onDismissRequest = { showUnidadMenu = false }
                    ) {
                        unidades.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unidad = u
                                    showUnidadMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productoSeleccionado != null && cantidad.isNotEmpty()) {
                        onAdd(
                            ProductoSolicitado(
                                producto = productoSeleccionado!!,
                                cantidadSolicitada = cantidad.toDoubleOrNull() ?: 0.0,
                                unidad = unidad
                            )
                        )
                    }
                },
                enabled = productoSeleccionado != null && cantidad.isNotEmpty()
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


 */