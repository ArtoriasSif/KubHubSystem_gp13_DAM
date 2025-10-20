package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.kubhubsystem_gp13_dam.model.Producto
import com.example.kubhubsystem_gp13_dam.model.EstadoInventario
import com.example.kubhubsystem_gp13_dam.model.Inventario
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.repository.MovimientoRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.InventarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.obtener(context.applicationContext) }

    val productoRepository = remember { ProductoRepository(database.productoDao()) }
    val inventarioRepository = remember {
        InventarioRepository(
            invDao = database.inventarioDao(),
            proDao = database.productoDao()
        )
    }
    val movimientoRepository = remember {
        MovimientoRepository(
            movimientoDao = database.movimientoDao(),
            inventarioDao = database.inventarioDao()
        )
    }

    val viewModel = remember {
        InventarioViewModel(
            productoRepository = productoRepository,
            inventarioRepository = inventarioRepository,
            movimientoRepository = movimientoRepository
        )
    }

    val productosFiltrados by viewModel.productosFiltrados.collectAsState()
    val categoriasFromDb by viewModel.categorias.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val inventarios by viewModel.inventarios.collectAsState()

    val categoriasConTodos by remember(categoriasFromDb) {
        derivedStateOf { listOf("Todos") + categoriasFromDb }
    }

    var showDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            InventarioHeader()

            InventarioToolbar(
                searchQuery = searchQuery,
                selectedCategoria = selectedCategoria,
                categorias = categoriasConTodos,
                showCategoryMenu = showCategoryMenu,
                onSearchChange = viewModel::updateSearchQuery,
                onCategoryMenuToggle = { showCategoryMenu = it },
                onCategorySelected = { categoria ->
                    viewModel.updateSelectedCategoria(categoria)
                    showCategoryMenu = false
                },
                onNewProducto = {
                    productoEditando = null
                    showDialog = true
                }
            )

            ProductoCounter(count = productosFiltrados.size)

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            ProductosTable(
                productos = productosFiltrados,
                inventarios = inventarios,
                isLoading = isLoading,
                onEdit = { producto ->
                    productoEditando = producto
                    showDialog = true
                },
                onDelete = { id ->
                    viewModel.eliminarProducto(id)
                },
                onUpdateStock = { idInventario, stock ->
                    viewModel.actualizarStock(idInventario, stock)
                },
                onRegistrarEntrada = { idInventario, cantidad ->
                    viewModel.registrarEntrada(idInventario, cantidad)
                },
                onRegistrarSalida = { idInventario, cantidad ->
                    viewModel.registrarSalida(idInventario, cantidad)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            InventarioFooter()
        }
    }

    if (showDialog) {
        ProductoDialog(
            producto = productoEditando,
            onDismiss = {
                showDialog = false
                productoEditando = null
            },
            onSave = { producto, stockInicial, ubicacion ->
                if (productoEditando != null) {
                    // Modo EDICIÓN: solo actualizar producto
                    viewModel.actualizarProducto(producto)
                } else {
                    // Modo CREACIÓN: crear producto + inventario
                    viewModel.agregarProductoConInventario(
                        producto = producto,
                        stockInicial = stockInicial,
                        ubicacion = ubicacion
                    )
                }
                showDialog = false
                productoEditando = null
            }
        )
    }
}

@Composable
private fun ProductosTable(
    productos: List<Producto>,
    inventarios: List<Inventario>,
    isLoading: Boolean,
    onEdit: (Producto) -> Unit,
    onDelete: (Int) -> Unit,
    onUpdateStock: (Int, Double) -> Unit,
    onRegistrarEntrada: (Int, Double) -> Unit,
    onRegistrarSalida: (Int, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ProductosTableHeader()
            HorizontalDivider()

            if (productos.isEmpty() && !isLoading) {
                EmptyProductosPlaceholder()
            } else {
                LazyColumn {
                    items(
                        items = productos,
                        key = { it.idProducto }
                    ) { producto ->
                        val inventario = inventarios.find { it.idProducto == producto.idProducto }

                        if (inventario != null) {
                            ProductoRow(
                                producto = producto,
                                inventario = inventario,
                                onEdit = { onEdit(producto) },
                                onDelete = { onDelete(producto.idProducto) },
                                onUpdateStock = { newStock ->
                                    onUpdateStock(inventario.idInventario, newStock)
                                },
                                onRegistrarEntrada = { cantidad ->
                                    onRegistrarEntrada(inventario.idInventario, cantidad)
                                },
                                onRegistrarSalida = { cantidad ->
                                    onRegistrarSalida(inventario.idInventario, cantidad)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoRow(
    producto: Producto,
    inventario: Inventario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Double) -> Unit,
    onRegistrarEntrada: (Double) -> Unit,
    onRegistrarSalida: (Double) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenuOptions by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }
    var showMovimientoDialog by remember { mutableStateOf(false) }
    var tipoMovimiento by remember { mutableStateOf("ENTRADA") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(inventario.nombreProducto, modifier = Modifier.weight(2f))
        Text(producto.categoria, modifier = Modifier.weight(1.5f))

        TextButton(
            onClick = { showStockDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (inventario.stock % 1 != 0.0) {
                    inventario.stock.toString()
                } else {
                    inventario.stock.toInt().toString()
                },
                fontWeight = FontWeight.Bold,
                color = when (inventario.estado) {
                    EstadoInventario.DISPONIBLE -> Color(0xFF4CAF50)
                    EstadoInventario.AGOTADO -> Color(0xFFF44336)
                    EstadoInventario.BAJO_STOCK -> Color(0xFFFF9800)
                }
            )
        }

        Text(producto.unidadMedida, modifier = Modifier.weight(1f))

        EstadoBadge(
            estado = inventario.estado,
            modifier = Modifier.weight(1.5f)
        )

        ProductoActions(
            onEdit = onEdit,
            onMoreOptions = { showMenuOptions = true },
            showMenu = showMenuOptions,
            onDismissMenu = { showMenuOptions = false },
            onAjustarStock = {
                showMenuOptions = false
                showStockDialog = true
            },
            onRegistrarEntrada = {
                showMenuOptions = false
                tipoMovimiento = "ENTRADA"
                showMovimientoDialog = true
            },
            onRegistrarSalida = {
                showMenuOptions = false
                tipoMovimiento = "SALIDA"
                showMovimientoDialog = true
            },
            onDelete = {
                showMenuOptions = false
                showDeleteDialog = true
            },
            modifier = Modifier.weight(1f)
        )
    }

    if (showStockDialog) {
        StockAdjustDialog(
            productoNombre = inventario.nombreProducto,
            stockActual = inventario.stock,
            unidad = producto.unidadMedida,
            onDismiss = { showStockDialog = false },
            onConfirm = { nuevoStock ->
                onUpdateStock(nuevoStock)
                showStockDialog = false
            }
        )
    }

    if (showMovimientoDialog) {
        MovimientoDialog(
            productoNombre = inventario.nombreProducto,
            stockActual = inventario.stock,
            unidad = producto.unidadMedida,
            tipoMovimiento = tipoMovimiento,
            onDismiss = { showMovimientoDialog = false },
            onConfirm = { cantidad ->
                when (tipoMovimiento) {
                    "ENTRADA" -> onRegistrarEntrada(cantidad)
                    "SALIDA" -> onRegistrarSalida(cantidad)
                }
                showMovimientoDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            productoNombre = inventario.nombreProducto,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun ProductoActions(
    onEdit: () -> Unit,
    onMoreOptions: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onAjustarStock: () -> Unit,
    onRegistrarEntrada: () -> Unit,
    onRegistrarSalida: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color(0xFFFFC107)
                )
            }

            IconButton(onClick = onMoreOptions) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Más opciones"
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text("Registrar Entrada") },
                onClick = onRegistrarEntrada,
                leadingIcon = {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF4CAF50))
                }
            )
            DropdownMenuItem(
                text = { Text("Registrar Salida") },
                onClick = onRegistrarSalida,
                leadingIcon = {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color(0xFFF44336))
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Ajustar Stock Manual") },
                onClick = onAjustarStock,
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Eliminar") },
                onClick = onDelete,
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            )
        }
    }
}

@Composable
private fun MovimientoDialog(
    productoNombre: String,
    stockActual: Double,
    unidad: String,
    tipoMovimiento: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var cantidad by remember { mutableStateOf("") }
    val isEntrada = tipoMovimiento == "ENTRADA"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "${if (isEntrada) "Registrar Entrada" else "Registrar Salida"} - $productoNombre"
            )
        },
        text = {
            Column {
                Text("Stock actual: ${if (stockActual % 1 != 0.0) stockActual else stockActual.toInt()} $unidad")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            cantidad = it
                        }
                    },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    supportingText = {
                        if (!isEntrada) {
                            val cant = cantidad.toDoubleOrNull() ?: 0.0
                            if (cant > stockActual) {
                                Text(
                                    "Stock insuficiente",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "La fecha y hora se registrarán automáticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cant = cantidad.toDoubleOrNull() ?: 0.0
                    if (cant > 0) {
                        onConfirm(cant)
                    }
                },
                enabled = cantidad.toDoubleOrNull()?.let {
                    it > 0 && (isEntrada || it <= stockActual)
                } == true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEntrada) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
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

// Mantener los demás composables sin cambios (InventarioHeader, InventarioToolbar, etc.)
// ... (resto del código igual al anterior)

@Composable
private fun InventarioHeader() {
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
}

@Composable
private fun InventarioToolbar(
    searchQuery: String,
    selectedCategoria: String,
    categorias: List<String>,
    showCategoryMenu: Boolean,
    onSearchChange: (String) -> Unit,
    onCategoryMenuToggle: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    onNewProducto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Buscar productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        CategoryFilterButton(
            selectedCategoria = selectedCategoria,
            categorias = categorias,
            showMenu = showCategoryMenu,
            onToggleMenu = onCategoryMenuToggle,
            onSelectCategoria = { categoria ->
                onCategorySelected(categoria)
                onCategoryMenuToggle(false)
            }
        )

        OutlinedButton(
            onClick = { /* TODO: Implementar realizar pedido */ },
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Realizar Pedido")
        }

        Button(
            onClick = onNewProducto,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Producto", color = Color.Black)
        }
    }
}

@Composable
private fun CategoryFilterButton(
    selectedCategoria: String,
    categorias: List<String>,
    showMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onSelectCategoria: (String) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = { onToggleMenu(true) },
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(selectedCategoria)
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onToggleMenu(false) }
        ) {
            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria) },
                    onClick = {
                        onSelectCategoria(categoria)
                        onToggleMenu(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductoCounter(count: Int) {
    Text(
        text = "$count producto(s) encontrado(s)",
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProductosTable(
    productos: List<Producto>,
    inventarios: List<Inventario>,
    isLoading: Boolean,
    onEdit: (Producto) -> Unit,
    onDelete: (Int) -> Unit,
    onUpdateStock: (Int, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ProductosTableHeader()
            HorizontalDivider()

            if (productos.isEmpty() && !isLoading) {
                EmptyProductosPlaceholder()
            } else {
                LazyColumn {
                    items(
                        items = productos,
                        key = { it.idProducto }
                    ) { producto ->
                        val inventario = inventarios.find { it.idProducto == producto.idProducto }

                        if (inventario != null) {
                            ProductoRow(
                                producto = producto,
                                inventario = inventario,
                                onEdit = { onEdit(producto) },
                                onDelete = { onDelete(producto.idProducto) },
                                onUpdateStock = { newStock ->
                                    onUpdateStock(inventario.idInventario, newStock)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            ProductosPagination()
        }
    }
}

@Composable
private fun ProductosTableHeader() {
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
}

@Composable
private fun EmptyProductosPlaceholder() {
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
}

@Composable
private fun ProductosPagination() {
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

@Composable
private fun InventarioFooter() {
    Text(
        text = "© 2025 KuHub System | Version 0.1",
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProductoRow(
    producto: Producto,
    inventario: Inventario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Double) -> Unit
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
        Text(inventario.nombreProducto, modifier = Modifier.weight(2f))
        Text(producto.categoria, modifier = Modifier.weight(1.5f))

        TextButton(
            onClick = { showStockDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (inventario.stock % 1 != 0.0) {
                    inventario.stock.toString()
                } else {
                    inventario.stock.toInt().toString()
                },
                fontWeight = FontWeight.Bold,
                color = when (inventario.estado) {
                    EstadoInventario.DISPONIBLE -> Color(0xFF4CAF50)
                    EstadoInventario.AGOTADO -> Color(0xFFF44336)
                    EstadoInventario.BAJO_STOCK -> Color(0xFFFF9800)
                }
            )
        }

        Text(producto.unidadMedida, modifier = Modifier.weight(1f))

        EstadoBadge(
            estado = inventario.estado,
            modifier = Modifier.weight(1.5f)
        )

        ProductoActions(
            onEdit = onEdit,
            onMoreOptions = { showMenuOptions = true },
            showMenu = showMenuOptions,
            onDismissMenu = { showMenuOptions = false },
            onAdjustStock = {
                showMenuOptions = false
                showStockDialog = true
            },
            onDelete = {
                showMenuOptions = false
                showDeleteDialog = true
            },
            modifier = Modifier.weight(1f)
        )
    }

    if (showStockDialog) {
        StockAdjustDialog(
            productoNombre = inventario.nombreProducto,
            stockActual = inventario.stock,
            unidad = producto.unidadMedida,
            onDismiss = { showStockDialog = false },
            onConfirm = { nuevoStock ->
                onUpdateStock(nuevoStock)
                showStockDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            productoNombre = inventario.nombreProducto,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun EstadoBadge(
    estado: EstadoInventario,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (estado) {
        EstadoInventario.DISPONIBLE -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF2E7D32),
            "Disponible"
        )
        EstadoInventario.AGOTADO -> Triple(
            Color(0xFFF44336).copy(alpha = 0.2f),
            Color(0xFFC62828),
            "Agotado"
        )
        EstadoInventario.BAJO_STOCK -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.2f),
            Color(0xFFE65100),
            "Bajo Stock"
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Composable
private fun ProductoActions(
    onEdit: () -> Unit,
    onMoreOptions: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onAdjustStock: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color(0xFFFFC107)
                )
            }

            IconButton(onClick = onMoreOptions) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Más opciones"
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text("Ajustar Stock") },
                onClick = onAdjustStock,
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Eliminar") },
                onClick = onDelete,
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            )
        }
    }
}

@Composable
private fun StockAdjustDialog(
    productoNombre: String,
    stockActual: Double,
    unidad: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var nuevoStock by remember {
        mutableStateOf(
            if (stockActual % 1 != 0.0) stockActual.toString()
            else stockActual.toInt().toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Stock - $productoNombre") },
        text = {
            Column {
                Text("Stock actual: ${if (stockActual % 1 != 0.0) stockActual else stockActual.toInt()} $unidad")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nuevoStock,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            nuevoStock = it
                        }
                    },
                    label = { Text("Nuevo stock") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stock = nuevoStock.toDoubleOrNull() ?: 0.0
                    onConfirm(stock)
                }
            ) {
                Text("Actualizar")
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
private fun DeleteConfirmDialog(
    productoNombre: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Producto") },
        text = { Text("¿Está seguro de que desea eliminar $productoNombre?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Eliminar", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductoDialog(
    producto: Producto?,
    onDismiss: () -> Unit,
    onSave: (Producto, stockInicial: Double, ubicacion: String) -> Unit
) {
    // Datos del Producto
    var nombre by remember { mutableStateOf(producto?.nombreProducto ?: "") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "Secos") }
    var unidad by remember { mutableStateOf(producto?.unidadMedida ?: "kg") }

    // Datos del Inventario (solo para productos nuevos)
    var stockInicial by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("Bodega Principal") }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnidadMenu by remember { mutableStateOf(false) }
    var showUbicacionMenu by remember { mutableStateOf(false) }

    val categorias = listOf("Secos", "Líquidos", "Lácteos", "Frescos", "Congelados", "Enlatados")
    val unidades = listOf("kg", "l", "unidad", "g", "ml", "caja")
    val ubicaciones = listOf("Bodega Principal", "Bodega Secundaria", "Refrigerador A", "Refrigerador B", "Congelador")

    val isEditMode = producto != null

    val isFormValid by remember {
        derivedStateOf {
            if (isEditMode) {
                nombre.isNotEmpty()
            } else {
                nombre.isNotEmpty() &&
                        stockInicial.isNotEmpty() &&
                        (stockInicial.toDoubleOrNull() ?: -1.0) >= 0
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditMode) "Editar Producto" else "Nuevo Producto")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECCIÓN: Información del Producto
                Text(
                    text = "Información del Producto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto *") },
                    placeholder = { Text("Ej: Harina 0000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nombre.isEmpty()
                )

                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                        },
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

                ExposedDropdownMenuBox(
                    expanded = showUnidadMenu,
                    onExpandedChange = { showUnidadMenu = it }
                ) {
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad de medida *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnidadMenu)
                        },
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

                // SECCIÓN: Inventario Inicial (solo para productos nuevos)
                if (!isEditMode) {
                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Inventario Inicial",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Configure el stock inicial del producto en el inventario",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = stockInicial,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                stockInicial = it
                            }
                        },
                        label = { Text("Stock inicial *") },
                        placeholder = { Text("Ej: 100") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = stockInicial.isEmpty() || (stockInicial.toDoubleOrNull() ?: -1.0) < 0,
                        supportingText = {
                            if (stockInicial.isNotEmpty()) {
                                val stock = stockInicial.toDoubleOrNull() ?: 0.0
                                Text(
                                    text = if (stock >= 0) {
                                        "Stock: ${if (stock % 1 != 0.0) stock else stock.toInt()} $unidad"
                                    } else {
                                        "Stock inválido"
                                    },
                                    color = if (stock >= 0)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    ExposedDropdownMenuBox(
                        expanded = showUbicacionMenu,
                        onExpandedChange = { showUbicacionMenu = it }
                    ) {
                        OutlinedTextField(
                            value = ubicacion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ubicación *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUbicacionMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = showUbicacionMenu,
                            onDismissRequest = { showUbicacionMenu = false }
                        ) {
                            ubicaciones.forEach { ubi ->
                                DropdownMenuItem(
                                    text = { Text(ubi) },
                                    onClick = {
                                        ubicacion = ubi
                                        showUbicacionMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Información adicional
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "El estado del inventario se calculará automáticamente según el stock inicial.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        val productoToSave = Producto(
                            idProducto = producto?.idProducto ?: 0,
                            nombreProducto = nombre.trim(),
                            categoria = categoria,
                            unidadMedida = unidad
                        )

                        val stock = if (isEditMode) 0.0 else (stockInicial.toDoubleOrNull() ?: 0.0)
                        val ubi = if (isEditMode) "" else ubicacion

                        onSave(productoToSave, stock, ubi)
                    }
                },
                enabled = isFormValid
            ) {
                Icon(
                    if (isEditMode) Icons.Default.Save else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditMode) "Actualizar" else "Crear Producto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}