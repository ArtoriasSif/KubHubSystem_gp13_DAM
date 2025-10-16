package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kubhubsystem_gp13_dam.model.*
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
    var categoria by remember { mutableStateOf(receta?.categoria ?: "") }
    var nuevaCategoria by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf(receta?.ingredientes ?: emptyList()) }
    var instrucciones by remember { mutableStateOf(receta?.instrucciones ?: "") }
    var observaciones by remember { mutableStateOf(receta?.observaciones ?: "") }

    var showCategoriaMenu by remember { mutableStateOf(false) }
    var showNuevaCategoriaDialog by remember { mutableStateOf(false) }
    var showIngredienteDialog by remember { mutableStateOf(false) }

    val productos by viewModel.productos.collectAsState()
    val categoriasRecetas by viewModel.categoriasRecetas.collectAsState()

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
                        text = if (receta != null) "Editar Receta" else "Nueva Receta",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B6914)
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

                    // Categoría (sin asignatura)
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = categoria,
                                onValueChange = {},
                                label = { Text("Categoría") },
                                readOnly = true,
                                placeholder = { Text("Seleccione categoría") },
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
                                // Opción para crear nueva categoría
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "Crear nueva categoría",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        showCategoriaMenu = false
                                        showNuevaCategoriaDialog = true
                                    }
                                )

                                if (categoriasRecetas.isNotEmpty()) {
                                    HorizontalDivider()
                                }

                                // Categorías existentes
                                categoriasRecetas.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            categoria = cat
                                            showCategoriaMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Ingredientes - Título
                    item {
                        Text(
                            text = "Ingredientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B6914)
                        )
                    }

                    // Lista de ingredientes o mensaje vacío
                    if (ingredientes.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${ingrediente.cantidad} ${ingrediente.producto.unidadMedida}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            ingredientes = ingredientes.filterIndexed { i, _ -> i != index }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botón agregar ingrediente
                    item {
                        OutlinedButton(
                            onClick = { showIngredienteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = productos.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Ingrediente")
                        }

                        if (productos.isEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ No hay productos disponibles. Debe crear productos primero.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
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

                    // Observaciones
                    item {
                        OutlinedTextField(
                            value = observaciones,
                            onValueChange = { observaciones = it },
                            label = { Text("Observaciones (opcional)") },
                            placeholder = { Text("Notas adicionales, sustituciones, etc.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }

                HorizontalDivider()

                // Footer con botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombre.isNotEmpty() && categoria.isNotEmpty() && instrucciones.isNotEmpty()) {
                                onSave(
                                    Receta(
                                        idReceta = receta?.idReceta ?: 0,
                                        nombre = nombre.trim(),
                                        descripcion = descripcion.trim(),
                                        categoria = categoria.trim(),
                                        instrucciones = instrucciones.trim(),
                                        observaciones = observaciones.trim().ifEmpty { null },
                                        ingredientes = ingredientes
                                    )
                                )
                            }
                        },
                        enabled = nombre.isNotEmpty() &&
                                categoria.isNotEmpty() &&
                                instrucciones.isNotEmpty() &&
                                ingredientes.isNotEmpty()
                    ) {
                        Icon(
                            if (receta != null) Icons.Default.Edit else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (receta != null) "Actualizar" else "Crear Receta")
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

    // Diálogo para crear nueva categoría
    if (showNuevaCategoriaDialog) {
        AlertDialog(
            onDismissRequest = { showNuevaCategoriaDialog = false },
            icon = {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Nueva Categoría") },
            text = {
                OutlinedTextField(
                    value = nuevaCategoria,
                    onValueChange = { nuevaCategoria = it },
                    label = { Text("Nombre de la categoría") },
                    placeholder = { Text("Ej: Repostería, Panadería, Pastelería, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaCategoria.isNotEmpty()) {
                            categoria = nuevaCategoria.trim()
                            nuevaCategoria = ""
                            showNuevaCategoriaDialog = false
                        }
                    },
                    enabled = nuevaCategoria.isNotEmpty()
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        nuevaCategoria = ""
                        showNuevaCategoriaDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
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
    var showProductoMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
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
                        value = productoSeleccionado?.nombreProducto ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto") },
                        placeholder = { Text("Seleccione un producto") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductoMenu)
                        },
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
                                text = {
                                    Column {
                                        Text(
                                            text = producto.nombreProducto,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Categoría: ${producto.categoria} • Unidad: ${producto.unidadMedida}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    productoSeleccionado = producto
                                    showProductoMenu = false
                                }
                            )
                        }
                    }
                }

                // Cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        // Permitir números decimales
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            cantidad = it
                        }
                    },
                    label = { Text("Cantidad") },
                    placeholder = { Text("Ej: 1.5") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        productoSeleccionado?.let {
                            Text("Unidad de medida: ${it.unidadMedida}")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productoSeleccionado != null && cantidad.isNotEmpty()) {
                        val cantidadDouble = cantidad.toDoubleOrNull()
                        if (cantidadDouble != null && cantidadDouble > 0) {
                            onAdd(
                                IngredienteReceta(
                                    idDetalle = 0,
                                    producto = productoSeleccionado!!,
                                    cantidad = cantidadDouble
                                )
                            )
                        }
                    }
                },
                enabled = productoSeleccionado != null &&
                        cantidad.isNotEmpty() &&
                        cantidad.toDoubleOrNull()?.let { it > 0 } == true
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