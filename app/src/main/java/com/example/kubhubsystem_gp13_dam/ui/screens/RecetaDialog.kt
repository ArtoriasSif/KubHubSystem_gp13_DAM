package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.model.CategoriaReceta
import com.example.kubhubsystem_gp13_dam.ui.model.IngredienteReceta
import com.example.kubhubsystem_gp13_dam.ui.model.Receta
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetaDialog(
    receta: Receta?,
    viewModel: RecetasViewModel,
    onDismiss: () -> Unit,
    onSave: (Receta) -> Unit
) {
    var nombre by remember { mutableStateOf(receta?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(receta?.descripcion ?: "") }
    var categoria by remember { mutableStateOf(receta?.categoria ?: CategoriaReceta.PANADERIA) }
    var asignaturaSeleccionada by remember { mutableStateOf(receta?.asignaturaRelacionada) }
    var ingredientes by remember { mutableStateOf(receta?.ingredientes ?: emptyList()) }
    var instrucciones by remember { mutableStateOf(receta?.instrucciones ?: "") }
    var tiempoPreparacion by remember { mutableStateOf(receta?.tiempoPreparacion?.toString() ?: "") }
    var porciones by remember { mutableStateOf(receta?.porciones?.toString() ?: "1") }

    var showCategoriaMenu by remember { mutableStateOf(false) }
    var showAsignaturaMenu by remember { mutableStateOf(false) }
    var showIngredienteDialog by remember { mutableStateOf(false) }

    val productos by viewModel.productos.collectAsState()
    val asignaturas by viewModel.asignaturas.collectAsState()
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (receta != null) "Editar Receta" else "Nueva Receta",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider()

                // Contenido
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre
                    item {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            placeholder = { Text("Nombre de la receta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // Descripción
                    item {
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            placeholder = { Text("Descripción breve de la receta") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3
                        )
                    }

                    // Fila: Categoría y Asignatura
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Categoría
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = categoria.displayName,
                                    onValueChange = {},
                                    label = { Text("Categoría") },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showCategoriaMenu = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "Seleccionar")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                DropdownMenu(
                                    expanded = showCategoriaMenu,
                                    onDismissRequest = { showCategoriaMenu = false }
                                ) {
                                    CategoriaReceta.values().forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.displayName) },
                                            onClick = {
                                                categoria = cat
                                                showCategoriaMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Asignatura
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = asignaturaSeleccionada?.nombreRamo ?: "Sin asignar",
                                    onValueChange = {},
                                    label = { Text("Asignatura") },
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
                                    DropdownMenuItem(
                                        text = { Text("Sin asignar") },
                                        onClick = {
                                            asignaturaSeleccionada = null
                                            showAsignaturaMenu = false
                                        }
                                    )
                                    asignaturas.forEach { asig ->
                                        DropdownMenuItem(
                                            text = { Text("${asig.codigoRamo} - ${asig.nombreRamo}") },
                                            onClick = {
                                                asignaturaSeleccionada = asig
                                                showAsignaturaMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Ingredientes
                    item {
                        Text(
                            text = "Ingredientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (ingredientes.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No hay ingredientes agregados",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(ingredientes.size) { index ->
                            val ingrediente = ingredientes[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
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
                                            text = ingrediente.producto.nombreProducto,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${ingrediente.cantidad} ${ingrediente.unidad}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            ingredientes = ingredientes.filterIndexed { i, _ -> i != index }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = { showIngredienteDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Ingrediente")
                        }
                    }

                    // Instrucciones
                    item {
                        OutlinedTextField(
                            value = instrucciones,
                            onValueChange = { instrucciones = it },
                            label = { Text("Instrucciones") },
                            placeholder = { Text("Instrucciones paso a paso para preparar la receta") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            maxLines = 10
                        )
                    }

                    // Fila: Tiempo y Porciones
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = tiempoPreparacion,
                                onValueChange = { if (it.all { char -> char.isDigit() }) tiempoPreparacion = it },
                                label = { Text("Tiempo (min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = porciones,
                                onValueChange = { if (it.all { char -> char.isDigit() }) porciones = it },
                                label = { Text("Porciones") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombre.isNotEmpty()) {
                                onSave(
                                    Receta(
                                        idReceta = receta?.idReceta ?: 0,
                                        nombre = nombre,
                                        descripcion = descripcion,
                                        categoria = categoria,
                                        asignaturaRelacionada = asignaturaSeleccionada,
                                        ingredientes = ingredientes,
                                        instrucciones = instrucciones,
                                        tiempoPreparacion = tiempoPreparacion.toIntOrNull() ?: 0,
                                        porciones = porciones.toIntOrNull() ?: 1,
                                        estaActiva = true
                                    )
                                )
                            }
                        },
                        enabled = nombre.isNotEmpty() && ingredientes.isNotEmpty()
                    ) {
                        Text("Crear Receta")
                    }
                }
            }
        }
    }

    // Diálogo para agregar ingrediente
    if (showIngredienteDialog) {
        AgregarIngredienteDialog(
            productos = productos,
            onDismiss = { showIngredienteDialog = false },
            onAdd = { ingrediente ->
                ingredientes = ingredientes + ingrediente
                showIngredienteDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteDialog(
    productos: List<Producto>,
    onDismiss: () -> Unit,
    onAdd: (IngredienteReceta) -> Unit
) {
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("kg") }
    var showProductoMenu by remember { mutableStateOf(false) }
    var showUnidadMenu by remember { mutableStateOf(false) }

    val unidades = listOf("kg", "g", "l", "ml", "unidad", "taza", "cucharada", "cucharadita")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Ingrediente") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de producto
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            IngredienteReceta(
                                producto = productoSeleccionado!!,
                                cantidad = cantidad.toDoubleOrNull() ?: 0.0,
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