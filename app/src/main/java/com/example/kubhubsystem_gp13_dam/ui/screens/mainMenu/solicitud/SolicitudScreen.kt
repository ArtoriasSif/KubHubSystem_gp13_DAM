package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.SolicitudViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla de solicitud optimizada:
 * - Usa LazyColumn como contenedor principal (pantalla deslizable)
 * - Usa remember / rememberSaveable para estados locales donde aplica
 * - Usa derivedStateOf para evitar cálculos en cada recomposición
 * - No cambia la lógica del ViewModel; mantiene collectAsState() para flujos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudScreen(
    viewModel: SolicitudViewModel,
    onNavigateBack: () -> Unit
) {
    // --- Estados desde ViewModel (flows) ---
    // Se mantienen como collectAsState() (vienen del ViewModel)
    val detallesTemp by viewModel.detallesTemp.collectAsState()
    val productosDisponibles by viewModel.productosDisponibles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val asignaturas by viewModel.asignaturas.collectAsState()
    val asignaturaSeleccionada by viewModel.asignaturaSeleccionada.collectAsState()
    val secciones by viewModel.secciones.collectAsState()
    val seccionSeleccionada by viewModel.seccionSeleccionada.collectAsState()
    val nombreDocente by viewModel.nombreDocente.collectAsState()
    val recetas by viewModel.recetas.collectAsState()
    val busquedaReceta by viewModel.busquedaReceta.collectAsState()

    // --- Estados locales (UI) ---
    // Uso rememberSaveable cuando el tipo lo permite (Strings, Booleans)
    var cantidadPersonas by rememberSaveable { mutableStateOf("20") }
    var observaciones by rememberSaveable { mutableStateOf("") }

    // LocalDateTime no es saveable por defecto -> usar remember
    var fechaSeleccionada by remember { mutableStateOf(LocalDateTime.now()) }

    // Dialogos y expansiones
    var mostrarDialogoProducto by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoReceta by rememberSaveable { mutableStateOf(false) }
    var expandidoAsignatura by rememberSaveable { mutableStateOf(false) }
    var expandidoSeccion by rememberSaveable { mutableStateOf(false) }

    // Estados derivados: evitan re-evaluar la lógica en cada recomposición
    val cantidadPersonasError by remember(cantidadPersonas) {
        derivedStateOf {
            // true si vacío o no es número válido > 0
            cantidadPersonas.isEmpty() || cantidadPersonas.toIntOrNull() == null || cantidadPersonas.toInt() <= 0
        }
    }

    // ---------- UI: usamos LazyColumn para toda la pantalla (deslizable) ----------
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
        // LazyColumn principal: cada sección como un ítem para permitir scroll eficiente
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- Información básica (card con fecha y cantidad) ----
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }

                        // Cantidad Personas
                        OutlinedTextField(
                            value = cantidadPersonas,
                            onValueChange = { newValue ->
                                // Sólo permitir dígitos
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    cantidadPersonas = newValue
                                    // Actualizar ViewModel sólo cuando sea número válido > 0
                                    newValue.toIntOrNull()?.let { cantidad ->
                                        if (cantidad > 0) viewModel.actualizarCantidadPersonas(cantidad)
                                    }
                                }
                            },
                            label = { Text("Cantidad de Personas") },
                            leadingIcon = { Icon(Icons.Default.People, null) },
                            isError = cantidadPersonasError,
                            supportingText = {
                                if (cantidadPersonasError) {
                                    Text("Ingrese un número válido mayor a 0", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // ---- Información Académica ----
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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

                        // Asignatura (dropdown)
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
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoAsignatura) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
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
                                                Text(asignatura.codigoAsignatura, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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

                        // Sección (dropdown)
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
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoSeccion) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
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

                        // Docente (editable)
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
            }

            // ---- Botones para agregar productos ----
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { mostrarDialogoReceta = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Restaurant, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desde Receta")
                    }

                    Button(onClick = { mostrarDialogoProducto = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manual")
                    }
                }
            }

            // ---- Título productos ----
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Productos Solicitados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---- Lista de productos: si vacío mostrar mensaje, sino itemsIndexed ----
            if (detallesTemp.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // espacio para que no se vea muy pequeño
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay productos agregados", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                itemsIndexed(detallesTemp) { index, detalle ->
                    DetalleProductoCard(
                        detalle = detalle,
                        onCantidadChange = { nuevaCantidad -> viewModel.actualizarCantidadDetalle(index, nuevaCantidad) },
                        onEliminar = { viewModel.eliminarDetalleTemp(index) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ---- Observaciones ----
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
            }

            // ---- Botón Enviar y progreso ----
            item {
                Button(
                    onClick = { if (detallesTemp.isNotEmpty()) viewModel.guardarSolicitud() },
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

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ---- Diálogos ----
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

        // ---- Snackbars / efectos (mantengo la lógica original usando LaunchedEffect) ----
        errorMessage?.let { mensaje ->
            LaunchedEffect(mensaje) {
                // Aquí podrías mostrar un Snackbar desde un ScaffoldState si lo deseas
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

/**
 * Componente DetalleProductoCard optimizado:
 * - Usa rememberSaveable para cantidad si aplica
 * - Valida cantidad y notifica al onCantidadChange sólo cuando es válida
 */
@Composable
fun DetalleProductoCard(
    detalle: DetalleSolicitud,
    onCantidadChange: (Double) -> Unit,
    onEliminar: () -> Unit
) {
    // guardamos el valor de texto localmente para editar sin afectar inmediatamente al ViewModel
    var cantidad by rememberSaveable { mutableStateOf(detalle.cantidadUnidadMedida.toString()) }
    var cantidadError by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = detalle.producto.nombreProducto, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = detalle.producto.categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { newValue ->
                            // Permitir números y punto decimal
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                cantidad = newValue
                                val doubleValue = newValue.toDoubleOrNull()
                                cantidadError = doubleValue == null || doubleValue <= 0
                                if (doubleValue != null && doubleValue > 0) {
                                    // Notificamos sólo si el número es válido
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
                        Text("Inválido", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Text(text = detalle.producto.unidadMedida, style = MaterialTheme.typography.bodyMedium)

                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** DialogoAgregarProducto y DialogoSeleccionarReceta se mantienen prácticamente iguales,
 *  sólo con comentarios añadidos y uso de rememberSaveable donde aplica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAgregarProducto(
    productos: List<Producto>,
    onDismiss: () -> Unit,
    onAgregar: (Producto, Double) -> Unit
) {
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by rememberSaveable { mutableStateOf("") }
    var cantidadError by remember { mutableStateOf(false) }
    var expandido by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                    OutlinedTextField(
                        value = productoSeleccionado?.nombreProducto ?: "Seleccionar producto",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        productos.forEach { producto ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(producto.nombreProducto)
                                        Text(producto.categoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
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
                    onValueChange = { newValue ->
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
                            Text("Ingrese un número válido mayor a 0", color = MaterialTheme.colorScheme.error)
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
                            if (cant > 0) onAgregar(producto, cant)
                        }
                    }
                },
                enabled = productoSeleccionado != null && !cantidadError && cantidad.isNotEmpty()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
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
            Column(modifier = Modifier.fillMaxWidth().height(500.dp)) {
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay recetas disponibles", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(recetas) { _, receta ->
                            Card(modifier = Modifier.fillMaxWidth(), onClick = {
                                viewModel.cargarProductosDesdeReceta(receta.idReceta)
                                onDismiss()
                            }) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = receta.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(text = receta.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                        }

                                        AssistChip(onClick = { }, label = { Text(receta.categoria) }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer))
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(text = "${receta.ingredientes.size} ingredientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
