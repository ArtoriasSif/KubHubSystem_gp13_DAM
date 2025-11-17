package com.example.kubhubsystem_gp13_dam.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.local.remote.InventarioApiService
import com.example.kubhubsystem_gp13_dam.local.remote.ProductoApiService
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductResponseAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.InventarioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen() {
    // --- Servicios y Repositorios ---
    val inventarioApiService = remember { RetrofitClient.createService(InventarioApiService::class.java) }
    val productoApiService = remember { RetrofitClient.createService(ProductoApiService::class.java) }
    val inventarioRepository = remember { InventarioRepository(inventarioApiService) }
    val productoRepository = remember { ProductoRepository(productoApiService) }
    val factory = remember { InventarioRepository.InventarioViewModelFactory(inventarioRepository, productoRepository) }
    val viewModel: InventarioViewModel = viewModel(factory = factory)

    // Estados del ViewModel
    val inventariosPaginados by viewModel.inventoryPaginated.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val unidadesMedida by viewModel.unidadesMedida.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // Estados locales
    var showDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InventoryWithProductResponseAnswerUpdateDTO?>(null) }
    var expandedCategoriaFilter by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val categoriasConTodos by remember {
        derivedStateOf {
            listOf("Todas las categorías") + categorias.map {
                it.lowercase().replaceFirstChar { char -> char.uppercase() }
            }
        }
    }

    // Normalizamos la lista para que nunca haya nulls en campos críticos
    val safeInventarios = remember(inventariosPaginados) {
        inventariosPaginados.map { dto ->
            dto.copy(
                descripcionProducto = dto.descripcionProducto ?: "",
                estadoStock = dto.estadoStock ?: ""
            )
        }
    }

    // Mostrar mensajes (snackbar)
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            if (showDialog) {
                showDialog = false
                itemToEdit = null
            }
            viewModel.clearSuccess()
        }
    }

    // Cerrar diálogo cuando hay success
    LaunchedEffect(successMessage) {
        successMessage?.let {
            if (showDialog) {
                showDialog = false
                itemToEdit = null
            }
            viewModel.clearSuccess()
        }
    }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            item {
                // ========== ENCABEZADO ==========
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Inventario",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Gestione los productos del inventario, vea movimientos y actualice existencias.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            // ========== BARRA DE BÚSQUEDA Y FILTROS ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BUSCADOR
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar productos...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpiar búsqueda"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // FILTRO POR CATEGORÍA
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoriaFilter,
                        onExpandedChange = { expandedCategoriaFilter = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoria.ifEmpty { "Todas las categorías" },
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoriaFilter)
                            },
                            modifier = Modifier
                                .width(240.dp)
                                .menuAnchor(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedCategoriaFilter,
                            onDismissRequest = { expandedCategoriaFilter = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas las categorías") },
                                onClick = {
                                    viewModel.updateCategoriaFilter("Todos")
                                    expandedCategoriaFilter = false
                                }
                            )
                            categorias.forEach { categoria ->
                                DropdownMenuItem(
                                    text = { Text(categoria.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.updateCategoriaFilter(categoria)
                                        expandedCategoriaFilter = false
                                    }
                                )
                            }
                        }
                    }

                    // BOTÓN REALIZAR PEDIDO
                    OutlinedButton(
                        onClick = { /* Acción de pedido */ },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Inventarios Por Volumes")
                    }

                    // BOTÓN NUEVO PRODUCTO
                    Button(
                        onClick = {
                            itemToEdit = null
                            showDialog = true
                        },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuevo Producto", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                // ========== TABLA DE INVENTARIOS (SCROLLEABLE) ==========
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    tonalElevation = 1.dp
                ) {
                    Column {
                        // ========== ENCABEZADO DE TABLA CORREGIDO ==========
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NOMBRE",
                                modifier = Modifier.weight(0.20f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )

                            Text(
                                text = "CATEGORÍA",
                                modifier = Modifier.weight(0.18f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )

                            Text(
                                text = "STOCK",
                                modifier = Modifier.weight(0.08f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center  // ✅ CAMBIO: de Start a Center
                            )

                            Text(
                                text = "UNIDAD",
                                modifier = Modifier.weight(0.22f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center  // ✅ CAMBIO: de Start a Center
                            )

                            Text(
                                text = "ESTADO",
                                modifier = Modifier.weight(0.20f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "ACCIONES",
                                modifier = Modifier.weight(0.12f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
            // LOADING STATE
            if (isLoading) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            // EMPTY STATE
            else if (safeInventarios.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay productos en el inventario",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            // LISTA DE PRODUCTOS (SCROLLEABLE) - ✅ MIGRACIÓN INTEGRADA
            else {
                items(
                    items = safeInventarios,
                    key = { it.idInventario ?: 0 }
                ) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Column {
                            InventarioRow(
                                item = item,
                                viewModel = viewModel,
                                coroutineScope = coroutineScope,
                                onEdit = { idInventario ->
                                    val inventario = viewModel.getInventory(idInventario)
                                    itemToEdit = inventario
                                    showDialog = true
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        viewModel.deleteInventory(
                                            item.idInventario ?: 0,
                                            item.nombreProducto ?: "Producto"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            }

            // ========== CIERRE VISUAL DE LA TABLA ==========
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    tonalElevation = 1.dp
                ) {
                    Spacer(modifier = Modifier.height(1.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ========== PAGINACIÓN ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousPage() },
                        enabled = currentPage > 1
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior")
                    }

                    Text(
                        text = "Página $currentPage de $totalPages",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { viewModel.nextPage() },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente")
                    }
                }
            }
        }

        // ========== DIÁLOGO DE CREAR/EDITAR ==========
        if (showDialog) {
            InventarioDialog(
                item = itemToEdit,
                categorias = categorias,
                unidadesMedida = unidadesMedida,
                isSaving = isSaving,
                onDismiss = {
                    showDialog = false
                    itemToEdit = null
                },
                onCreate = { dto ->
                    viewModel.createInventoryWithProduct(dto)
                },
                onUpdate = { dto ->
                    viewModel.updateInventoryWithProduct(dto)
                }
            )
        }
    }
}

// ========== COMPONENTE InventarioRow MIGRADO CON TODA LA LÓGICA ==========
@Composable
private fun InventarioRow(
    item: InventoryWithProductResponseAnswerUpdateDTO,
    viewModel: InventarioViewModel,
    coroutineScope: CoroutineScope,
    onEdit: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenuOptions by remember { mutableStateOf(false) }
    var showStockDialog by remember { mutableStateOf(false) }
    var showMovimientoDialog by remember { mutableStateOf(false) }
    var tipoMovimiento by remember { mutableStateOf("ENTRADA") }

    // ✅ Valores seguros con protección contra null y strings vacíos
    // ✅ OPTIMIZACIÓN: Cálculos pesados con remember
    val safeData = remember(item.idInventario) {
        object {
            val nombre = item.nombreProducto?.trim().takeIf { !it.isNullOrBlank() } ?: "Sin nombre"
            val categoria = item.nombreCategoria?.trim()?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "Sin categoría"
            val unidad = item.unidadMedida?.trim()?.takeIf { it.isNotBlank() }?.uppercase() ?: "N/A"
            val stock = item.stock ?: 0.0
            val stockLimitMin = item.stockLimitMin
            val idInventario = item.idInventario ?: 0
        }
    }

    // ✅ OPTIMIZACIÓN: Estado calculado con remember
    val estadoInfo = remember(safeData.stock, safeData.stockLimitMin) {
        when {
            safeData.stockLimitMin == null || safeData.stockLimitMin == 0.0 ->
                "NO ASIGNADO" to Color(0xFF9E9E9E)
            safeData.stock == 0.0 ->
                "AGOTADO" to Color(0xFFFF5252)
            safeData.stock < safeData.stockLimitMin ->
                "BAJO STOCK" to Color(0xFFFFA500)
            else ->
                "DISPONIBLE" to Color(0xFF00C853)
        }
    }


    /**Cálculo del estado (seguro frente a nulls)
    val estadoCalculado = when {
        safeStockLimitMin == null || safeStockLimitMin == 0.0 -> "NO ASIGNADO"
        safeStock == 0.0 -> "AGOTADO"
        safeStock < safeStockLimitMin -> "BAJO STOCK"
        else -> "DISPONIBLE"
    }

    val estadoColor = when (estadoCalculado) {
        "AGOTADO" -> Color(0xFFFF5252)
        "BAJO STOCK" -> Color(0xFFFFA500)
        "DISPONIBLE" -> Color(0xFF00C853)
        else -> Color(0xFF9E9E9E)
    }*/

    // ========== CUERPO DE TABLA CORREGIDO ==========
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NOMBRE (0.20f)
        Text(
            text = safeData.nombre,
            modifier = Modifier.weight(0.20f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // CATEGORÍA (0.18f)
        Text(
            text = safeData.categoria,
            modifier = Modifier.weight(0.18f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // STOCK (0.08f) - Clickeable para ajustar
        Box(
            modifier = Modifier.weight(0.08f),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = { showStockDialog = true },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (safeData.stock % 1 != 0.0) {
                        safeData.stock.toString()
                    } else {
                        safeData.stock.toInt().toString()
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = estadoInfo.second
                )
            }
        }

        // UNIDAD (0.22f)
        Text(
            text = safeData.unidad,
            modifier = Modifier.weight(0.22f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // ESTADO (0.20f)
        Surface(
            modifier = Modifier.weight(0.20f),
            color = estadoInfo.second,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = estadoInfo.first,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ACCIONES (0.12f)
        Box(modifier = Modifier.weight(0.12f)) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            )  {
                // Botón Editar
                IconButton(
                    onClick = {
                        Log.d("EDIT_BTN", "Editar inventario ID=${safeData.idInventario}")
                        onEdit(safeData.idInventario)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón Más opciones (menú)
                IconButton(
                    onClick = { showMenuOptions = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "Más opciones",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Menú desplegable con todas las opciones
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
                        text = { Text("Registrar Entrada") },
                        onClick = {
                            showMenuOptions = false
                            tipoMovimiento = "ENTRADA"
                            showMovimientoDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Registrar Salida") },
                        onClick = {
                            showMenuOptions = false
                            tipoMovimiento = "SALIDA"
                            showMovimientoDialog = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color(0xFFF44336)) },
                        onClick = {
                            showMenuOptions = false
                            showDeleteDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFF44336)
                            )
                        }
                    )
                }
            }
        }
    }

    // ========== DIÁLOGOS ==========

    // Diálogo de ajuste de stock
    if (showStockDialog) {
        StockAdjustDialog(
            productoNombre = safeData.nombre,
            stockActual = safeData.stock,
            unidad = safeData.unidad,
            onDismiss = { showStockDialog = false },
            onConfirm = { nuevoStock ->
                coroutineScope.launch {
                    TODO("Implementar updateStock en el ViewModel")
                }
                showStockDialog = false
            }
        )
    }


    // Diálogo de movimiento (entrada/salida)
    if (showMovimientoDialog) {
        MovimientoDialog(
            productoNombre = safeData.nombre,
            stockActual = safeData.stock,
            unidad = safeData.unidad,
            tipoMovimiento = tipoMovimiento,
            onDismiss = { showMovimientoDialog = false },
            onConfirm = { cantidad ->
                coroutineScope.launch {
                    when (tipoMovimiento) {
                        "ENTRADA" -> TODO("Implementar registrarEntrada en el ViewModel")
                        "SALIDA" -> TODO("Implementar registrarSalida en el ViewModel")
                    }
                }
                showMovimientoDialog = false
            }
        )
    }


    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Está seguro de que desea eliminar '${safeData.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Eliminar")
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


/**
@Composable
private fun RowScope.TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioDialog(
    item: InventoryWithProductResponseAnswerUpdateDTO?,
    categorias: List<String>,
    unidadesMedida: List<String>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onCreate: (dto: InventoryWithProductCreateDTO) -> Unit,
    onUpdate: (dto: InventoryWithProductResponseAnswerUpdateDTO) -> Unit
) {
    val isEditMode = item != null

    // Campos del formulario (rememberSaveable para sobrevivir rotaciones)
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var unidad by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var stockMinimo by rememberSaveable { mutableStateOf("") }

    var stockError by rememberSaveable { mutableStateOf(false) }
    var stockMinimoError by rememberSaveable { mutableStateOf(false) }

    var showCategoriaMenu by rememberSaveable { mutableStateOf(false) }
    var showUnidadMenu by rememberSaveable { mutableStateOf(false) }

    // Normalizar listas
    val categoriasDisponibles = remember(categorias) {
        categorias.filter { it.isNotBlank() }
            .map { it.trim().lowercase().replaceFirstChar { c -> c.uppercase() } }
            .distinct()
    }

    val unidadesDisponibles = remember(unidadesMedida) {
        unidadesMedida.filter { it.isNotBlank() }
            .map { it.trim().uppercase() }
            .distinct()
    }

    // Poblar campos cuando cambie el item
    LaunchedEffect(item?.idInventario) {
        if (isEditMode && categoria.isBlank() && unidad.isBlank()) {
            val data = item
            nombre = data?.nombreProducto ?: ""
            descripcion = data?.descripcionProducto ?: ""
            categoria = data?.nombreCategoria ?: ""
            unidad = data?.unidadMedida ?: ""
            stock = data?.stock?.toString() ?: ""
            stockMinimo = data?.stockLimitMin?.toString() ?: ""
        }
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // ========== HEADER CON DISEÑO MEJORADO ==========
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isEditMode) Color(0xFFFFA726) else Color(0xFFFFC107),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = if (isEditMode) "Editar Producto" else "Nuevo Producto",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isEditMode)
                                    "Actualiza la información del producto en el inventario"
                                else
                                    "Agrega un nuevo producto al inventario",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = { if (!isSaving) onDismiss() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // ========== FORMULARIO CON MEJOR ESPACIADO ==========
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // SECCIÓN: INFORMACIÓN BÁSICA
                    Text(
                        text = "Información del Producto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // NOMBRE
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del producto") },
                        placeholder = { Text("Ej: Harina de trigo") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    // DESCRIPCIÓN CON LÍMITE DE 100 CARACTERES
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            if (it.length <= 100) {
                                descripcion = it
                            }
                        },
                        label = { Text("Descripción") },
                        placeholder = { Text("Describe las características del producto...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        supportingText = {
                            Text(
                                text = "${descripcion.length}/100 caracteres",
                                fontSize = 12.sp,
                                color = if (descripcion.length >= 100)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // SECCIÓN: CLASIFICACIÓN
                    Text(
                        text = "Clasificación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // CATEGORÍA + UNIDAD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // CATEGORÍA
                        ExposedDropdownMenuBox(
                            expanded = showCategoriaMenu,
                            onExpandedChange = { showCategoriaMenu = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = categoria,
                                onValueChange = { categoria = it },
                                readOnly = true,
                                label = { Text("Categoría") },
                                placeholder = { Text("Seleccionar") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoriaMenu)
                                },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoriaMenu,
                                onDismissRequest = { showCategoriaMenu = false }
                            ) {
                                categoriasDisponibles.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            categoria = cat
                                            Log.d("DIALOG_EDIT", "Categoría seleccionada: $categoria")
                                            showCategoriaMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Label,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // UNIDAD
                        ExposedDropdownMenuBox(
                            expanded = showUnidadMenu,
                            onExpandedChange = { showUnidadMenu = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unidad,
                                onValueChange = { unidad = it },
                                readOnly = true,
                                label = { Text("Unidad") },
                                placeholder = { Text("Seleccionar") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Scale,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnidadMenu)
                                },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = showUnidadMenu,
                                onDismissRequest = { showUnidadMenu = false }
                            ) {
                                unidadesDisponibles.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unidad = u
                                            Log.d("DIALOG_EDIT", "Unidad seleccionada: $unidad")
                                            showUnidadMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Straighten,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // SECCIÓN: CANTIDADES
                    Text(
                        text = "Control de Stock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // STOCK + STOCK MINIMO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = {
                                stock = it
                                stockError = it.toDoubleOrNull() == null && it.isNotBlank()
                            },
                            label = { Text("Stock actual") },
                            placeholder = { Text("0") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = if (stockError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = stockError,
                            supportingText = if (stockError) {
                                { Text("Ingrese un número válido") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = stockMinimo,
                            onValueChange = {
                                stockMinimo = it
                                stockMinimoError = it.toDoubleOrNull() == null && it.isNotBlank()
                            },
                            label = { Text("Stock mínimo") },
                            placeholder = { Text("0") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (stockMinimoError) MaterialTheme.colorScheme.error
                                    else Color(0xFFFFA726)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = stockMinimoError,
                            supportingText = if (stockMinimoError) {
                                { Text("Ingrese un número válido") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // INFO CARD
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "El stock mínimo se usa para alertas de reposición automática",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // ========== BOTONES CON MEJOR DISEÑO ==========
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Cancelar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (isEditMode) {
                                val stockValue = stock.toDoubleOrNull() ?: 0.0
                                val stockMinValue = stockMinimo.toDoubleOrNull() ?: 0.0

                                val estado = when {
                                    stockMinValue == 0.0 -> "NO ASIGNADO"
                                    stockValue == 0.0 -> "AGOTADO"
                                    stockValue < stockMinValue -> "BAJO STOCK"
                                    else -> "DISPONIBLE"
                                }

                                val updateDto = InventoryWithProductResponseAnswerUpdateDTO(
                                    idInventario = item!!.idInventario,
                                    idProducto = item.idProducto,
                                    nombreProducto = nombre,
                                    descripcionProducto = descripcion,
                                    nombreCategoria = categoria,
                                    unidadMedida = unidad,
                                    stock = stockValue,
                                    stockLimitMin = stockMinValue,
                                    estadoStock = estado
                                )
                                onUpdate(updateDto)
                            } else {
                                val createDto = InventoryWithProductCreateDTO(
                                    idInventario = 0,
                                    idProducto = 0,
                                    nombreProducto = nombre,
                                    descripcionProducto = descripcion,
                                    nombreCategoria = categoria,
                                    unidadMedida = unidad,
                                    stock = stock.toDoubleOrNull() ?: 0.0,
                                    stockLimitMin = stockMinimo.toDoubleOrNull() ?: 0.0
                                )
                                onCreate(createDto)
                            }
                        },
                        enabled = nombre.isNotBlank() &&
                                categoria.isNotBlank() &&
                                unidad.isNotBlank() &&
                                !stockError &&
                                !stockMinimoError &&
                                !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditMode) Color(0xFFFFA726) else Color(0xFFFFC107),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.Black
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Guardando...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Icon(
                                if (isEditMode) Icons.Default.Save else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isEditMode) "Guardar Cambios" else "Crear Producto",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
// ========== DIÁLOGOS AUXILIARES ==========

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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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