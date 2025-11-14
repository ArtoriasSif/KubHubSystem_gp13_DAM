package com.example.kubhubsystem_gp13_dam.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen() {
    // --- crear una vez los servicios y repos (usar remember para no recrearlos en recomposiciones) ---
    val inventarioApiService = remember { RetrofitClient.createService(InventarioApiService::class.java) }
    val productoApiService = remember { RetrofitClient.createService(ProductoApiService::class.java) }

    val inventarioRepository = remember { InventarioRepository(inventarioApiService) }
    val productoRepository = remember { ProductoRepository(productoApiService) }

    // Crear la factory usando la clase que añadimos al repo archivo
    val factory = remember {
        // si pegaste la clase en el mismo archivo como InventarioViewModelFactory:
        InventarioRepository.InventarioViewModelFactory(inventarioRepository, productoRepository)
        // si implementaste como anidada dentro de InventarioRepository (inner class), usar:
        // InventarioRepository.InventarioViewModelFactory(inventarioRepository, productoRepository)
    }

    // Pedimos el ViewModel al framework con la factory (esto evita el NoSuchMethodException)
    val viewModel: InventarioViewModel = viewModel(factory = factory)

    // Estados del ViewModel
    val inventariosPaginados by viewModel.inventoryPaginated.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val unidadesMedida by viewModel.unidadesMedida.collectAsState()
    val estados by viewModel.estados.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()
    val selectedEstado by viewModel.selectedEstado.collectAsState()
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

    // Función para cargar item desde el caché por ID
    fun loadItemForEdit(idInventario: Int) {
        itemToEdit = viewModel.getInventory(idInventario)
        showDialog = true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val categoriasConTodos by remember(categorias) {
        derivedStateOf {
            listOf("Todas las categorías") + categorias.map { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
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
            // clearSuccess se hace en el efecto que cierra el dialogo para evitar cerrar antes
        }
    }

    // Cerrar diálogo cuando hay success (evita cerrar inmediatamente en onSave)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
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

            // ========== BARRA DE BÚSQUEDA Y FILTROS ==========
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
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
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

                // BOTÓN REALIZAR PEDIDO (opcional)
                OutlinedButton(
                    onClick = { /* Acción de pedido */ },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear Inventario Por Volumes")
                }

                // BOTÓN NUEVO PRODUCTO
                Button(
                    onClick = {
                        itemToEdit = null
                        showDialog = true
                    },
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107) // Amarillo como en la imagen
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Producto", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== TABLA DE INVENTARIOS (SCROLLEABLE) ==========
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Ocupar todo el espacio disponible
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 1.dp
            ) {
                Column {
                    // ENCABEZADO DE TABLA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // NOMBRE (0.20f)
                        Text(
                            text = "NOMBRE",
                            modifier = Modifier.weight(0.20f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start // Alinear a la izquierda
                        )

                        // CATEGORÍA (0.18f)
                        Text(
                            text = "CATEGORÍA",
                            modifier = Modifier.weight(0.18f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start // Alinear a la izquierda
                        )

                        // STOCK (0.08f) - CORREGIDO
                        Text(
                            text = "STOCK",
                            // CAMBIO: Peso reducido (números cortos)
                            modifier = Modifier.weight(0.08f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start // Alinear a la izquierda
                        )

                        // UNIDAD (0.22f) - CORREGIDO
                        Text(
                            text = "UNIDAD",
                            // CAMBIO: Peso aumentado (para "CHAT MENTIEA")
                            modifier = Modifier.weight(0.22f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start // Alinear a la izquierda
                        )

                        // ESTADO (0.20f)
                        Text(
                            text = "ESTADO",
                            modifier = Modifier.weight(0.20f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center // Centrado
                        )

                        // ACCIONES (0.12f)
                        Text(
                            text = "ACCIONES",
                            modifier = Modifier.weight(0.12f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End // Alinear al final (derecha)
                        )
                    }

                    // LOADING STATE
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    // EMPTY STATE
                    else if (safeInventarios.isEmpty()) {
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
                    // LISTA DE PRODUCTOS (SCROLLEABLE)
                    else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(
                                items = safeInventarios,
                                key = { it.idInventario ?: 0 }
                            ) { item ->
                                InventarioRow(
                                    item = item,
                                    onEdit = { idInventario ->
                                        val inventario = viewModel.getInventory(idInventario)
                                        itemToEdit = inventario
                                        showDialog = true
                                    },
                                    onViewDetails = {
                                        // Acción para ver detalles
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== PAGINACIÓN ==========
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
}

@Composable
private fun InventarioRow(
    item: InventoryWithProductResponseAnswerUpdateDTO,
    onEdit: (Int) -> Unit,   // 👈 ahora recibe el ID del inventario
    onViewDetails: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ Valores seguros con protección contra null y strings vacíos
    val safeNombre = item.nombreProducto?.trim().takeIf { !it.isNullOrBlank() } ?: "Sin nombre"

    // IMPORTANT: proteger trim() con ?.
    val safeDescripcion = item.descripcionProducto
        ?.trim()
        ?.ifBlank { "Sin descripción" }
        ?: "Sin descripción"

    val safeCategoria = item.nombreCategoria
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.replaceFirstChar { it.uppercase() }
        ?: "Sin categoría"

    val safeUnidad = item.unidadMedida
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.uppercase()
        ?: "N/A"

    val safeStock = item.stock ?: 0.0
    val safeStockLimitMin = item.stockLimitMin // lo tratamos abajo

    // Cálculo del estado (seguro frente a nulls)
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
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NOMBRE (15%)
        Text(
            text = safeNombre,
            modifier = Modifier.weight(0.15f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(20.dp))

        // CATEGORÍA (15%)
        Text(
            text = safeCategoria,
            modifier = Modifier.weight(0.15f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(20.dp))

        // STOCK (8%)
        Text(
            text = safeStock.toInt().toString(),
            modifier = Modifier.weight(0.08f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(20.dp))

        // UNIDAD (18%)
        Text(
            text = safeUnidad,
            modifier = Modifier.weight(0.18f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(20.dp))

        // ESTADO (24%)
        Surface(
            modifier = Modifier.weight(0.18f),
            color = estadoColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = estadoCalculado,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(24.dp))

        // ACCIONES (20%)
        Row(
            modifier = Modifier.weight(0.20f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Editar
            IconButton(
                onClick = {
                    Log.d("EDIT_BTN", "Editar inventario ID=${item.idInventario}")
                    onEdit(item.idInventario!!)
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

            // Botón Ver detalles
            IconButton(
                onClick = onViewDetails,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = "Ver detalles",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón Eliminar
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Está seguro de que desea eliminar '${item.nombreProducto}'?") },
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
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TÍTULO
                    Text(
                        text = if (isEditMode) "Editar Producto" else "Nuevo Producto",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { if (!isSaving) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // FORMULARIO
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // NOMBRE
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del producto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    // DESCRIPCIÓN
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    // CATEGORÍA + UNIDAD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CATEGORÍA
                        ExposedDropdownMenuBox(
                            expanded = showCategoriaMenu,
                            onExpandedChange = { showCategoriaMenu = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = categoria,
                                onValueChange = { categoria = it },   // 👈 OBLIGATORIO
                                readOnly = true,
                                label = { Text("Categoría") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoriaMenu)
                                },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoriaMenu,
                                onDismissRequest = { showCategoriaMenu = false }
                            ) {
                                categoriasDisponibles.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            categoria = cat   // 👈 ACTUALIZA ESTADO REAL OK
                                            Log.d("DIALOG_EDIT", "Categoría seleccionada: $categoria")
                                            showCategoriaMenu = false
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
                                onValueChange = { unidad = it },   // 👈 OBLIGATORIO
                                readOnly = true,
                                label = { Text("Unidad de medida") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnidadMenu)
                                },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showUnidadMenu,
                                onDismissRequest = { showUnidadMenu = false }
                            ) {
                                unidadesDisponibles.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unidad = u    // 👈 ACTUALIZA ESTADO REAL OK
                                            Log.d("DIALOG_EDIT", "Unidad seleccionada: $unidad")
                                            showUnidadMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // STOCK + STOCK MINIMO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = {
                                stock = it
                                stockError = it.toDoubleOrNull() == null && it.isNotBlank()
                            },
                            label = { Text("Stock") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = stockError,
                            supportingText = if (stockError) {
                                { Text("Ingrese un número válido") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = stockMinimo,
                            onValueChange = {
                                stockMinimo = it
                                stockMinimoError = it.toDoubleOrNull() == null && it.isNotBlank()
                            },
                            label = { Text("Stock mínimo") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = stockMinimoError,
                            supportingText = if (stockMinimoError) {
                                { Text("Ingrese un número válido") }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                // BOTONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
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
                            }
                            else {
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
                        enabled =
                            nombre.isNotBlank() &&
                                    categoria.isNotBlank() &&
                                    unidad.isNotBlank() &&
                                    !stockError &&
                                    !stockMinimoError &&
                                    !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Guardando…")
                        } else {
                            Text(if (isEditMode) "Guardar Cambios" else "Crear Producto")
                        }
                    }
                }
            }
        }
    }
}
