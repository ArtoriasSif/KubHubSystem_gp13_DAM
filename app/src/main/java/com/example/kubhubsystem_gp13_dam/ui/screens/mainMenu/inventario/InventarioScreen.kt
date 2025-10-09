package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.inventario

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.model.CategoriaProducto
import com.example.kubhubsystem_gp13_dam.model.EstadoProducto
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.InventarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    viewModel: InventarioViewModel = viewModel()
) {
    // Estados del ViewModel
    val productosFiltrados by viewModel.productosFiltrados.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()

    // Estados locales del UI
    var showDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<Producto?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
        ) {
            Text(
                text = "Inventario",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gestione los productos del inventario, vea movimientos y actualice existencias.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Barra de búsqueda y filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar productos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filtro de categoría
            Box {
                OutlinedButton(
                    onClick = { showCategoryMenu = true },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedCategoria.displayName)
                }

                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    CategoriaProducto.values().forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.displayName) },
                            onClick = {
                                viewModel.updateSelectedCategoria(categoria)
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Botón realizar pedido
            OutlinedButton(
                onClick = { /* TODO: Implementar realizar pedido */ },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Realizar Pedido")
            }

            // Botón nuevo producto
            Button(
                onClick = {
                    productoEditando = null
                    showDialog = true
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Producto", color = Color.Black)
            }
        }

        // Contador de productos
        Text(
            text = "${productosFiltrados.size} producto(s) encontrado(s)",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tabla de productos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Encabezados de tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("NOMBRE", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("CATEGORÍA", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("STOCK", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("UNIDAD", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("ESTADO", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("ACCIONES", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                // Lista de productos
                if (productosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron productos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn {
                        items(productosFiltrados) { producto ->
                            ProductoRow(
                                producto = producto,
                                onEdit = {
                                    productoEditando = producto
                                    showDialog = true
                                },
                                onDelete = {
                                    viewModel.eliminarProducto(producto.id)
                                },
                                onUpdateStock = { nuevoStock ->
                                    viewModel.actualizarStock(producto.id, nuevoStock)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }

                // Paginación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO: Página anterior */ }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                    }
                    Text(
                        text = "1",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(onClick = { /* TODO: Página siguiente */ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Text(
            text = "© 2025 KuHub System | Version 0.1",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Diálogo para agregar/editar producto
    if (showDialog) {
        ProductoDialog(
            producto = productoEditando,
            onDismiss = { showDialog = false },
            onSave = { producto ->
                if (productoEditando != null) {
                    viewModel.actualizarProducto(producto)
                } else {
                    viewModel.agregarProducto(producto)
                }
                showDialog = false
                productoEditando = null
            }
        )
    }
}

@Composable
fun ProductoRow(
    producto: Producto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenuOptions by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(producto.nombre, modifier = Modifier.weight(2f))
        Text(producto.categoria, modifier = Modifier.weight(1.5f))

        // Stock clickeable
        TextButton(
            onClick = { showStockDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = producto.stock.toString(),
                fontWeight = FontWeight.Bold,
                color = when (producto.estado) {
                    EstadoProducto.DISPONIBLE -> Color(0xFF4CAF50)
                    EstadoProducto.AGOTADO -> Color(0xFFF44336)
                    EstadoProducto.BAJO_STOCK -> Color(0xFFFF9800)
                }
            )
        }

        Text(producto.unidad, modifier = Modifier.weight(1f))

        // Badge de estado
        Surface(
            modifier = Modifier.weight(1.5f),
            shape = RoundedCornerShape(16.dp),
            color = when (producto.estado) {
                EstadoProducto.DISPONIBLE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                EstadoProducto.AGOTADO -> Color(0xFFF44336).copy(alpha = 0.2f)
                EstadoProducto.BAJO_STOCK -> Color(0xFFFF9800).copy(alpha = 0.2f)
            }
        ) {
            Text(
                text = when (producto.estado) {
                    EstadoProducto.DISPONIBLE -> "Disponible"
                    EstadoProducto.AGOTADO -> "Agotado"
                    EstadoProducto.BAJO_STOCK -> "Bajo Stock"
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = when (producto.estado) {
                    EstadoProducto.DISPONIBLE -> Color(0xFF2E7D32)
                    EstadoProducto.AGOTADO -> Color(0xFFC62828)
                    EstadoProducto.BAJO_STOCK -> Color(0xFFE65100)
                }
            )
        }

        // Acciones
        Box(modifier = Modifier.weight(1f)) {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFFFFC107)
                    )
                }

                IconButton(onClick = { showMenuOptions = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }
            }

            DropdownMenu(
                expanded = showMenuOptions,
                onDismissRequest = { showMenuOptions = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Ajustar Stock") },
                    onClick = {
                        showMenuOptions = false
                        showStockDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {
                        showMenuOptions = false
                        showDeleteDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                )
            }
        }
    }

    // Diálogo de ajuste de stock
    if (showStockDialog) {
        var nuevoStock by remember { mutableStateOf(producto.stock.toString()) }

        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = { Text("Ajustar Stock - ${producto.nombre}") },
            text = {
                Column {
                    Text("Stock actual: ${producto.stock} ${producto.unidad}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nuevoStock,
                        onValueChange = { if (it.all { char -> char.isDigit() }) nuevoStock = it },
                        label = { Text("Nuevo stock") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stock = nuevoStock.toIntOrNull() ?: 0
                        onUpdateStock(stock)
                        showStockDialog = false
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStockDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Está seguro de que desea eliminar ${producto.nombre}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDialog(
    producto: Producto?,
    onDismiss: () -> Unit,
    onSave: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "Secos") }
    var stock by remember { mutableStateOf(producto?.stock?.toString() ?: "") }
    var unidad by remember { mutableStateOf(producto?.unidad ?: "kg") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnidadMenu by remember { mutableStateOf(false) }

    val categorias = listOf("Secos", "Líquidos", "Lácteos", "Frescos")
    val unidades = listOf("kg", "l", "unidad", "g", "ml")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (producto != null) "Editar Producto" else "Nuevo Producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Selector de categoría
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = stock,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) stock = it },
                    label = { Text("Stock inicial") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Selector de unidad
                ExposedDropdownMenuBox(
                    expanded = showUnidadMenu,
                    onExpandedChange = { showUnidadMenu = it }
                ) {
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad de medida") },
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
                    if (nombre.isNotEmpty() && stock.isNotEmpty()) {
                        val stockInt = stock.toIntOrNull() ?: 0
                        val estado = when {
                            stockInt == 0 -> EstadoProducto.AGOTADO
                            stockInt < 20 -> EstadoProducto.BAJO_STOCK
                            else -> EstadoProducto.DISPONIBLE
                        }
                        onSave(
                            Producto(
                                id = producto?.id ?: 0,
                                nombre = nombre,
                                categoria = categoria,
                                stock = stockInt,
                                unidad = unidad,
                                estado = estado
                            )
                        )
                    }
                },
                enabled = nombre.isNotEmpty() && stock.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}