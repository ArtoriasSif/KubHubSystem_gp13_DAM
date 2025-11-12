package com.example.kubhubsystem_gp13_dam.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.local.remote.InventarioApiService
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductCreateUpdateDTO
import com.example.kubhubsystem_gp13_dam.model.InventoryWithProductoResponseDTO
import com.example.kubhubsystem_gp13_dam.repository.InventarioRepository
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.InventarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen() {
    val viewModel = remember {
        val apiService = RetrofitClient.createService(InventarioApiService::class.java)
        val repository = InventarioRepository(apiService)
        InventarioViewModel(repository)
    }

    val inventariosPaginados by viewModel.inventariosPaginados.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var itemEditando by remember { mutableStateOf<InventoryWithProductoResponseDTO?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val categoriasConTodos by remember(categorias) {
        derivedStateOf { listOf("Todas las categorías") + categorias }
    }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Inventario",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Gestione los productos del inventario, vea movimientos y actualice existencias.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar productos...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategoria,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                        },
                        modifier = Modifier
                            .width(220.dp)
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categoriasConTodos.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    viewModel.updateSelectedCategoria(cat)
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Realizar Pedido")
                }

                Button(
                    onClick = {
                        itemEditando = null
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA500)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Producto", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableHeaderText("NOMBRE", 0.25f)
                        TableHeaderText("CATEGORÍA", 0.15f)
                        TableHeaderText("STOCK", 0.12f)
                        TableHeaderText("UNIDAD", 0.12f)
                        TableHeaderText("ESTADO", 0.15f)
                        TableHeaderText("ACCIONES", 0.15f)
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (inventariosPaginados.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay productos disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(inventariosPaginados) { item ->
                                InventarioRow(
                                    item = item,
                                    onEdit = {
                                        itemEditando = item
                                        showDialog = true
                                    },
                                    onDelete = {
                                        viewModel.eliminarInventario(item.idInventario, item.nombreProducto)
                                    }
                                )
                                if (item != inventariosPaginados.last()) {
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.previousPage() },
                    enabled = currentPage > 1
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = if (currentPage > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = currentPage.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                IconButton(
                    onClick = { viewModel.nextPage() },
                    enabled = currentPage < totalPages
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    if (showDialog) {
        InventarioDialog(
            item = itemEditando,
            categorias = categorias,
            onDismiss = {
                showDialog = false
                itemEditando = null
            },
            onSave = { dto ->
                // ✅ Mucho más simple y seguro
                viewModel.guardarInventario(dto)
                showDialog = false
                itemEditando = null
            }
        )
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
    item: InventoryWithProductoResponseDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.nombreProducto,
            modifier = Modifier.weight(0.25f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.nombreCategoria,
            modifier = Modifier.weight(0.15f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.stock.toInt().toString(),
            modifier = Modifier.weight(0.12f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )

        Text(
            text = item.unidadMedida,
            modifier = Modifier.weight(0.12f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )

        Surface(
            modifier = Modifier.weight(0.15f),
            color = Color(0xFF00C853),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Disponible",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.weight(0.15f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color(0xFFFFA500),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Detalles",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Está seguro que desea eliminar '${item.nombreProducto}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
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
    item: InventoryWithProductoResponseDTO?,
    categorias: List<String>,
    onDismiss: () -> Unit,
    onSave: (dto: InventoryWithProductCreateUpdateDTO) -> Unit // ✅ Simplificado
) {
    val isEditMode = item != null

    var nombre by remember { mutableStateOf(item?.nombreProducto ?: "") }
    var descripcion by remember { mutableStateOf(if (isEditMode) "Descripción no disponible" else "") }
    var categoria by remember { mutableStateOf(item?.nombreCategoria ?: "") }
    var unidad by remember { mutableStateOf(item?.unidadMedida ?: "") }
    var stock by remember { mutableStateOf(item?.stock?.toString() ?: "0.0") }
    var stockMinimo by remember { mutableStateOf(item?.stockMinimo?.toString() ?: "0.0") }

    // ✅ Estados para errores de validación
    var stockError by remember { mutableStateOf(false) }
    var stockMinimoError by remember { mutableStateOf(false) }

    var showCategoriaMenu by remember { mutableStateOf(false) }
    var showUnidadMenu by remember { mutableStateOf(false) }

    val unidades = listOf("kg", "l", "unidad", "g", "ml", "caja")
    val categoriasDisponibles = if (categorias.isEmpty()) {
        listOf("Secos", "Líquidos", "Lácteos", "Frescos")
    } else categorias

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Editar Producto" else "Nuevo Producto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = showCategoriaMenu,
                            onExpandedChange = { showCategoriaMenu = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = categoria.ifEmpty { "Seleccione categoría" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showCategoriaMenu
                                    )
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
                                            categoria = cat
                                            showCategoriaMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = showUnidadMenu,
                            onExpandedChange = { showUnidadMenu = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unidad.ifEmpty { "Seleccione unidad" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidad de medida") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = showUnidadMenu
                                    )
                                },
                                modifier = Modifier.menuAnchor()
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
                                { Text("Ingrese un número válido", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            )
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
                                { Text("Ingrese un número válido", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            // ✅ Validación y construcción del DTO
                            val stockDouble = stock.toDoubleOrNull()
                            val stockMinimoDouble = stockMinimo.toDoubleOrNull()

                            if (stockDouble == null || stockMinimoDouble == null) {
                                stockError = stockDouble == null
                                stockMinimoError = stockMinimoDouble == null
                                return@Button
                            }

                            val dto = InventoryWithProductCreateUpdateDTO(
                                idInventario = item?.idInventario,
                                idProducto = item?.idProducto,
                                nombreProducto = nombre,
                                descripcionProducto = descripcion.ifBlank { "Sin descripción" },
                                nombreCategoria = categoria,
                                unidadMedida = unidad,
                                stock = stockDouble,
                                stockMinimo = stockMinimoDouble
                            )

                            onSave(dto)
                        },
                        enabled = nombre.isNotBlank() &&
                                categoria.isNotBlank() &&
                                unidad.isNotBlank() &&
                                !stockError &&
                                !stockMinimoError,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (isEditMode) "Guardar Cambios" else "Crear Producto",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}