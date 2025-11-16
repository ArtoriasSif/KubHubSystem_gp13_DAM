package com.example.kubhubsystem_gp13_dam.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.local.remote.ProductoApiService
import com.example.kubhubsystem_gp13_dam.local.remote.RecetaApiService
import com.example.kubhubsystem_gp13_dam.local.remote.RetrofitClient
import com.example.kubhubsystem_gp13_dam.model.ProductoEntityDTO
import com.example.kubhubsystem_gp13_dam.repository.RecetaRepository2
import com.example.kubhubsystem_gp13_dam.ui.model.EstadoRecetaType
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeItemDTO
import com.example.kubhubsystem_gp13_dam.ui.model.RecipeWithDetailsAnswerUpdateDTO
import com.example.kubhubsystem_gp13_dam.viewmodel.RecetasViewModel2


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen2() {
    // ---CONFIGURACION E INYECCION DEL VIEWMODEL ---
    // Crea el servicio de API para Recetas
    val recetaApiService = remember { RetrofitClient.createService(RecetaApiService::class.java) }
    // Crea el servicio de API para Productos
    val productoApiService = remember { RetrofitClient.createService(ProductoApiService::class.java) }
    // Crea el Repositorio, inyectándole los servicios API
    val repository = remember { RecetaRepository2(recetaApiService, productoApiService) }
    // Crea la "Fábrica" (Factory) que sabe cómo construir el ViewModel con el repositorio
    val factory = remember { repository.createViewModelFactory() }
    // Obtiene la instancia del ViewModel usando la fábrica
    val viewModel: RecetasViewModel2 = viewModel(factory = factory)


    // OBSERVACIÓN DEL ESTADO DEL VIEWMODEL ---
    // (ESTAS VARIABLES "ESCUCHAN" LOS FLOW/STATEFLOW DEL VIEWMODEL.
    // CUANDO UN VALOR CAMBIA EN EL VIEWMODEL, LA UI SE REDIBUJA AUTOMÁTICAMENTE)
    // La lista principal de recetas a mostrar (ya filtrada)
    val statusChangeResult by viewModel.statusChangeResult.collectAsState()
    val recetas by viewModel.recetasFiltradas.collectAsState()
    // Indicadores de estado para mostrar (ej: CircularProgressIndicator)
    val isLoading by viewModel.isLoading.collectAsState() // Para cargas generales (GET)
    val isSaving by viewModel.isSaving.collectAsState()   // Para (POST, PUT, DELETE)
    // Mensajes para mostrar al usuario (ej: en un Snackbar)
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    //--ESTADOS DE DIALOGOS--
    // Diálogo para Crear o Editar una receta
    var showCrearEditarDialog by remember { mutableStateOf(false) }
    var recetaToEdit by remember { mutableStateOf<RecipeWithDetailsAnswerUpdateDTO?>(null) }
    // Diálogo para confirmar Eliminación (Desactivación)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recetaToDelete by remember { mutableStateOf<RecipeWithDetailsAnswerUpdateDTO?>(null) }
    // Diálogo para Ver los Detalles de una receta
    var showDetallesDialog by remember { mutableStateOf(false) }
    var recetaDetalles by remember { mutableStateOf<RecipeWithDetailsAnswerUpdateDTO?>(null) }
    // Diálogo para Cambiar el Estado (Activo/Inactivo)
    var showDialogCambiarEstado by remember { mutableStateOf(false) } // <--- Corregido
    var recetaParaCambiarEstado by remember { mutableStateOf<RecipeWithDetailsAnswerUpdateDTO?>(null) }



    //Otras Acciones y Estados de UI ---
    // Estado para la barra de búsqueda
    var searchQuery by remember { mutableStateOf("") }
    val estadoFiltro by viewModel.estadoFiltro.collectAsState()
    // Estado para el menú desplegable (DropdownMenu) de cada fila
    // (Guarda el ID de la receta cuyo menú está abierto)
    var showMenuReceta by remember { mutableStateOf<Int?>(null) }


    // Actualizar búsqueda
    LaunchedEffect(searchQuery) {
        viewModel.updateSearchQuery(searchQuery)
    }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(statusChangeResult) {
        statusChangeResult?.let { success ->
            if (success) {
                Log.d("RecetasScreen2", "✅ Estado cambiado con éxito, refrescando...")
            } else {
                Log.e("RecetasScreen2", "❌ Error al cambiar estado")
            }
            // Limpiar el resultado después de procesarlo
            // viewModel.clearStatusChangeResult() // Si tienes este método
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
            // ========== ENCABEZADO ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gestión de Recetas",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Administre las recetas base para las solicitudes de insumos.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = {
                            recetaToEdit = null
                            showCrearEditarDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nueva Receta",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ========== TARJETAS DE ESTADÍSTICAS ==========
            item {
                val totalRecetas = recetas.size
                val activas = recetas.count { it.estadoReceta == EstadoRecetaType.ACTIVO }
                val inactivas = recetas.count { it.estadoReceta == EstadoRecetaType.INACTIVO }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Total Recetas",
                        value = totalRecetas.toString(),
                        color = Color(0xFFFFC107),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Activas",
                        value = activas.toString(),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Inactivas",
                        value = inactivas.toString(),
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ========== BÚSQUEDA + FILTRO DE ESTADO ==========
            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ---- BARRA DE BÚSQUEDA ----
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)  // <-- Para coincidir con el dropdown
                            .padding(end = 8.dp),
                        placeholder = { Text("Buscar recetas...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // ---- DROPDOWN DE ESTADO ----
                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(56.dp)
                    ) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .fillMaxSize(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                estadoFiltro,
                                maxLines = 1
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    viewModel.setEstadoFiltro("Todos")
                                    expanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Activo") },
                                onClick = {
                                    viewModel.setEstadoFiltro("Activo")
                                    expanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Inactivo") },
                                onClick = {
                                    viewModel.setEstadoFiltro("Inactivo")
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }



            // ========== TABLA DE RECETAS ==========
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    tonalElevation = 1.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NOMBRE",
                                modifier = Modifier.weight(0.30f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "DESCRIPCIÓN",
                                modifier = Modifier.weight(0.30f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "INGREDIENTES",
                                modifier = Modifier.weight(0.15f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "ESTADO",
                                modifier = Modifier.weight(0.15f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "ACCIONES",
                                modifier = Modifier.weight(0.10f),
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
            else if (recetas.isEmpty()) {
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay recetas registradas",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            // LISTA DE RECETAS
            else {
                items(
                    items = recetas,
                    key = { it.idReceta ?: 0 }
                ) { receta ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Column {
                            RecetaRow(
                                receta = receta,// Pasa el objeto de la receta actual a la fila
                                showMenu = showMenuReceta == receta.idReceta, // Controla si el menú desplegable de ESTA fila debe estar visible
                                onMenuClick = {// IF el menú ya está abierto (el ID coincide), lo cierra (null). ELSE está cerrado, lo abre asignando el ID de la receta.
                                    showMenuReceta = if (showMenuReceta == receta.idReceta) null else receta.idReceta
                                },
                                onEdit = {
                                    recetaToEdit = receta // <-- Asignas la receta que el usuario quiere editar
                                    showCrearEditarDialog = true // <-- Activa el Diálogo de Crear/Editar
                                    showMenuReceta = null // <-- Cierra el menú desplegable
                                },
                                onDelete = {
                                    recetaToDelete = receta // <-- Asignas la receta que el usuario quiere Eliminar
                                    showDeleteDialog = true // <-- Activa el Diálogo de Eliminar (Desactivar)
                                    showMenuReceta = null // <-- Cierra el menú desplegable
                                },
                                onVerDetalles = {
                                    recetaDetalles = receta // <-- Asignas la receta que el usuario quiere Ver Detalles
                                    showDetallesDialog = true // <-- Activa el Diálogo de Ver Detalles
                                    showMenuReceta = null // <-- Cierra el menú desplegable
                                },
                                onDismissMenu = { showMenuReceta = null }, // Se llama cuando se hace clic fuera del menú desplegable. Cierra el menú desplegable

                                onCambiarEstadoClick = { r ->
                                    recetaParaCambiarEstado = r // <-- Asigna la receta para el diálogo
                                    showDialogCambiarEstado = true // <-- Activa el Diálogo de Cambiar Estado
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            }

            // CIERRE VISUAL DE LA TABLA
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
        }
    }

    // ========== DIÁLOGOS ==========

    // Diálogo para Cambiar el Estado (activo / inactivo)
    if (showDialogCambiarEstado && recetaParaCambiarEstado != null) {
        DialogCambiarEstadoReceta(
            receta = recetaParaCambiarEstado!!,
            mostrar = showDialogCambiarEstado,
            onDismiss = {
                showDialogCambiarEstado = false
                recetaParaCambiarEstado = null
            },
            onConfirm = { idReceta ->
                // 🔥 Llamar al ViewModel para cambiar el estado
                viewModel.updateChangingStatus(idReceta)
                showDialogCambiarEstado = false
                recetaParaCambiarEstado = null
            }
        )
    }

    // Diálogo de crear/editar receta
    if (showCrearEditarDialog) {
        CreateUpdateRecipeDialog(
            viewModel = viewModel,
            isSaving = isSaving,
            recetaToEdit = recetaToEdit,
            onDismiss = {
                showCrearEditarDialog = false
                recetaToEdit = null
            }
        )
    }

    // Diálogo de detalles de receta
    if (showDetallesDialog && recetaDetalles != null) {
        DetalleRecetaDialogDTO(
            receta = recetaDetalles!!,
            onDismiss = {
                showDetallesDialog = false
                recetaDetalles = null
            }
        )
    }


    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && recetaToDelete != null) {
        DialogEliminarReceta(
            receta = recetaToDelete!!,
            mostrar = showDeleteDialog,
            onDismiss = {
                showDeleteDialog = false
                recetaToDelete = null
            },
            onConfirm = {
                recetaToDelete?.let {
                    viewModel.deactivateRecipe(
                        it.idReceta ?: 0,
                        it.nombreReceta ?: "Receta"
                    )
                }
                showDeleteDialog = false
                recetaToDelete = null
            }
        )
    }
}


// ========== COMPONENTE StatCard ==========
@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========== COMPONENTE RecetaRow ==========
@Composable
private fun RecetaRow(
    receta: RecipeWithDetailsAnswerUpdateDTO,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onVerDetalles: () -> Unit,
    onDismissMenu: () -> Unit,
    onCambiarEstadoClick: (RecipeWithDetailsAnswerUpdateDTO) -> Unit
) {
    val safeNombre = receta.nombreReceta?.trim().takeIf { !it.isNullOrBlank() } ?: "Sin nombre"
    val safeDescripcion = receta.descripcionReceta?.trim().takeIf { !it.isNullOrBlank() } ?: "Sin descripción"
    val cantidadIngredientes = receta.listaItems?.size ?: 0
    val estadoReceta = receta.estadoReceta ?: EstadoRecetaType.ACTIVO
    val estadoActivo = estadoReceta == EstadoRecetaType.ACTIVO

    val icono = if (estadoActivo) Icons.Default.Check else Icons.Default.Close
    val descripcion = if (estadoActivo) "Receta activa" else "Receta inactiva"
    val colorIcono = if (estadoActivo) Color(0xFF4CAF50) else Color(0xFFF44336)

    val estadoColor = when (estadoReceta) {
        EstadoRecetaType.ACTIVO -> Color(0xFF4CAF50)
        EstadoRecetaType.INACTIVO -> Color(0xFFF44336)
    }

    var showDialogCambiarEstado by remember { mutableStateOf(false) }
    var recetaParaCambiarEstado by remember { mutableStateOf<RecipeWithDetailsAnswerUpdateDTO?>(null) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = safeNombre,
            modifier = Modifier.weight(0.30f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = safeDescripcion,
            modifier = Modifier.weight(0.30f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier.weight(0.15f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$cantidadIngredientes ingredientes",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier.weight(0.15f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = estadoColor
            ) {
                Text(
                    text = estadoReceta.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Box(
            modifier = Modifier.weight(0.10f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVerDetalles,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.RemoveRedEye,
                        contentDescription = "Ver",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón CAMBIAR ESTADO (✔ / ❌)
                IconButton(
                    onClick = { onCambiarEstadoClick(receta) },
                    modifier = Modifier.size(32.dp) // <-- Añadido
                ) {
                    val activo = receta.estadoReceta == EstadoRecetaType.ACTIVO
                    Icon(
                        imageVector = if (activo) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (activo) "Desactivar receta" else "Activar receta",
                        tint = if (activo) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }

                //Botón ELIMINAR ---
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }



                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Duplicar")
                            }
                        },
                        onClick = { /* TODO */ }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Archive,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Archivar")
                            }
                        },
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

// ========== DIÁLOGO UNIFICADO CREAR/EDITAR ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUpdateRecipeDialog(
    viewModel: RecetasViewModel2,
    isSaving: Boolean,
    recetaToEdit: RecipeWithDetailsAnswerUpdateDTO?,
    onDismiss: () -> Unit
) {
    val isEditMode = recetaToEdit != null

    var nombreReceta by remember { mutableStateOf(recetaToEdit?.nombreReceta ?: "") }
    var descripcionReceta by remember { mutableStateOf(recetaToEdit?.descripcionReceta ?: "") }
    var instrucciones by remember { mutableStateOf(recetaToEdit?.instrucciones ?: "") }
    var ingredientes by remember { mutableStateOf(recetaToEdit?.listaItems ?: emptyList()) }

    var showAgregarIngrediente by remember { mutableStateOf(false) }
    var ingredienteIndexToEdit by remember { mutableStateOf<Int?>(null) }

    val productosActivos by viewModel.productosActivos.collectAsState()
    val unidadesMedida by viewModel.unidadesMedida.collectAsState()

    var estadoReceta by remember(recetaToEdit) {
        mutableStateOf(recetaToEdit?.estadoReceta ?: EstadoRecetaType.ACTIVO)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
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
                        .background(Color(0xFFFFC107))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isEditMode) "Editar Receta" else "Nueva Receta",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Black
                        )
                    }
                }

                // Contenido
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            // Campo de Nombre (ocupa 70% del espacio)
                            OutlinedTextField(
                                value = nombreReceta,
                                onValueChange = { nombreReceta = it },
                                label = { Text("Nombre") },
                                placeholder = { Text("Nombre de la receta") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )

                            // Botón de estado (ocupa el resto)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (estadoReceta) {
                                            EstadoRecetaType.ACTIVO -> Color(0xFF4CAF50)   // Verde
                                            EstadoRecetaType.INACTIVO -> Color(0xFFE53935) // Rojo
                                            null -> Color(0xFF9E9E9E)                      // Gris si no hay estado
                                        }
                                    )
                                    .clickable {
                                        estadoReceta = when (estadoReceta) {
                                            null -> EstadoRecetaType.ACTIVO
                                            EstadoRecetaType.ACTIVO -> EstadoRecetaType.INACTIVO
                                            EstadoRecetaType.INACTIVO -> EstadoRecetaType.ACTIVO
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = estadoReceta?.name ?: "SIN ESTADO",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = descripcionReceta,
                            onValueChange = { descripcionReceta = it },
                            label = { Text("Descripción (Opcional)") },
                            placeholder = { Text("Descripción breve de la receta...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Sección de Ingredientes
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107)
                                        )
                                        Text(
                                            text = "Ingredientes (${ingredientes.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            ingredienteIndexToEdit = null
                                            showAgregarIngrediente = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Agregar ingrediente",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                if (ingredientes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    ingredientes.forEachIndexed { index, ingrediente ->
                                        IngredienteItemRow(
                                            ingrediente = ingrediente,
                                            onEdit = {
                                                ingredienteIndexToEdit = index
                                                showAgregarIngrediente = true
                                            },
                                            onDelete = {
                                                ingredientes = ingredientes.filterIndexed { i, _ -> i != index }
                                            }
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay ingredientes agregados",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = instrucciones,
                            onValueChange = { instrucciones = it },
                            label = { Text("Instrucciones (Opcional)") },
                            placeholder = {
                                Text("Paso 1: ...\nPaso 2: ...\nPaso 3: ...")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                HorizontalDivider()

                // Footer con botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (isEditMode) {
                                // Modo edición - usar RecipeWithDetailsAnswerUpdateDTO
                                val updatedReceta = recetaToEdit!!.copy(
                                    nombreReceta = nombreReceta.trim(),
                                    descripcionReceta = descripcionReceta.trim().ifBlank { null },
                                    listaItems = ingredientes,
                                    instrucciones = instrucciones.trim().ifBlank { null },
                                    estadoReceta = estadoReceta,
                                    cambioReceta = true,
                                    cambioDetalles = true
                                )
                                viewModel.updateRecipeWithDetails(updatedReceta)
                            } else {
                                // Modo creación - usar RecipeWithDetailsCreateDTO
                                viewModel.createRecipeWithDetails(
                                    nombreReceta = nombreReceta.trim(),
                                    descripcionReceta = descripcionReceta.trim(),
                                    ingredientes = ingredientes,
                                    instrucciones = instrucciones.trim(),
                                    estadoReceta = EstadoRecetaType.ACTIVO
                                )
                            }
                            onDismiss()
                        },
                        enabled = !isSaving && nombreReceta.isNotBlank() && ingredientes.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.Black
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (isEditMode) "Guardar Cambios" else "Crear Receta",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Diálogo para agregar/editar ingrediente
    if (showAgregarIngrediente) {
        val ingredienteToEdit = ingredienteIndexToEdit?.let { ingredientes.getOrNull(it) }

        AgregarIngredienteDialog(
            productosActivos = productosActivos,
            ingredienteToEdit = ingredienteToEdit,
            onDismiss = {
                showAgregarIngrediente = false
                ingredienteIndexToEdit = null
            },
            onAgregar = { nuevoIngrediente ->
                if (ingredienteIndexToEdit != null) {
                    // Editar ingrediente existente
                    ingredientes = ingredientes.toMutableList().apply {
                        set(ingredienteIndexToEdit!!, nuevoIngrediente)
                    }
                } else {
                    // Agregar nuevo ingrediente
                    ingredientes = ingredientes + nuevoIngrediente
                }
                showAgregarIngrediente = false
                ingredienteIndexToEdit = null
            }
        )
    }
}

// ========== COMPONENTE IngredienteItemRow ==========
@Composable
private fun IngredienteItemRow(
    ingrediente: RecipeItemDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "• ${ingrediente.nombreProducto ?: "Sin nombre"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ingrediente.cantUnidadMedida ?: 0.0}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = ingrediente.unidadMedida ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ========== DIÁLOGO AGREGAR/EDITAR INGREDIENTE ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteDialog(
    productosActivos: List<ProductoEntityDTO>,
    ingredienteToEdit: RecipeItemDTO? = null,
    onDismiss: () -> Unit,
    onAgregar: (RecipeItemDTO) -> Unit
) {
    val isEditMode = ingredienteToEdit != null

    var productoSeleccionado by remember {
        mutableStateOf(
            if (isEditMode) {
                productosActivos.find { it.idProducto == ingredienteToEdit?.idProducto }
            } else null
        )
    }
    var cantidad by remember { mutableStateOf(ingredienteToEdit?.cantUnidadMedida?.toString() ?: "") }
    var unidadSeleccionada by remember {
        mutableStateOf(
            if (isEditMode) ingredienteToEdit?.unidadMedida
            else null
        )
    }

    var expandedProducto by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val productosFiltrados = remember(searchQuery, productosActivos) {
        if (searchQuery.isBlank()) {
            productosActivos
        } else {
            productosActivos.filter {
                it.nombreProducto?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Edit else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isEditMode) "Editar Ingrediente" else "Agregar Ingrediente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider()

                // Selector de Producto
                ExposedDropdownMenuBox(
                    expanded = expandedProducto,
                    onExpandedChange = { expandedProducto = it }
                ) {
                    OutlinedTextField(
                        value = productoSeleccionado?.nombreProducto ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Producto *") },
                        placeholder = { Text("Seleccione un producto") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProducto)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedProducto,
                        onDismissRequest = { expandedProducto = false }
                    ) {
                        // Campo de búsqueda
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar producto...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        HorizontalDivider()

                        if (productosFiltrados.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No se encontraron productos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { }
                            )
                        } else {
                            productosFiltrados.forEach { producto ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = producto.nombreProducto ?: "Sin nombre",
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "UM: ${producto.unidadMedida ?: "N/A"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        productoSeleccionado = producto
                                        unidadSeleccionada = producto.unidadMedida
                                        expandedProducto = false
                                        searchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }

                // Cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            cantidad = it
                        }
                    },
                    label = { Text("Cantidad *") },
                    placeholder = { Text("0.0") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Scale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // Unidad de Medida (solo lectura)
                if (!unidadSeleccionada.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Unidad de Medida:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = unidadSeleccionada!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }

                    val puedeAgregar =
                        productoSeleccionado != null &&
                                cantidad.toDoubleOrNull() != null &&
                                cantidad.toDoubleOrNull()!! > 0 &&
                                !unidadSeleccionada.isNullOrBlank()

                    Button(
                        onClick = {
                            val producto = productoSeleccionado!!
                            val cantidadDouble = cantidad.toDouble()

                            val nuevoIngrediente = RecipeItemDTO(
                                idProducto = producto.idProducto,
                                nombreProducto = producto.nombreProducto,
                                unidadMedida = unidadSeleccionada,
                                cantUnidadMedida = cantidadDouble
                            )

                            onAgregar(nuevoIngrediente)
                        },
                        enabled = puedeAgregar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isEditMode) "Guardar" else "Agregar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ========== DIÁLOGO DE DETALLES ==========
@Composable
fun DetalleRecetaDialogDTO(
    receta: RecipeWithDetailsAnswerUpdateDTO,
    onDismiss: () -> Unit
) {
    var estadoActivo by remember {
        mutableStateOf(receta.estadoReceta == EstadoRecetaType.ACTIVO)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
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
                        .background(Color(0xFFFFC107))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = receta.nombreReceta ?: "Sin nombre",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = receta.descripcionReceta ?: "Sin descripción",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Black
                        )
                    }
                }

                // Contenido
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Estado
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (estadoActivo) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Estado de la receta",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (estadoActivo) "Esta receta está activa" else "Esta receta está inactiva",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = estadoActivo,
                                    onCheckedChange = { estadoActivo = it },
                                    enabled = false,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF2E7D32),
                                        checkedTrackColor = Color(0xFF81C784)
                                    )
                                )
                            }
                        }
                    }

                    // Ingredientes
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107)
                                    )
                                    Text(
                                        text = "Ingredientes (${receta.listaItems?.size ?: 0})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                receta.listaItems?.forEach { ingrediente ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• ${ingrediente.nombreProducto ?: "Sin nombre"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        val unidadMostrar =
                                            if (ingrediente.unidadMedida == "UNIDAD") "UNI" else (ingrediente.unidadMedida ?: "")
                                        Text(
                                            text = "${ingrediente.cantUnidadMedida ?: 0.0} $unidadMostrar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Instrucciones
                    if (!receta.instrucciones.isNullOrBlank()) {
                        item {
                            Card {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107)
                                        )
                                        Text(
                                            text = "Instrucciones",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = receta.instrucciones,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                                    )
                                }
                            }
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
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun DialogEliminarReceta(
    receta: RecipeWithDetailsAnswerUpdateDTO,
    mostrar: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit    // 🔥 Ahora recibe la ID
) {
    if (!mostrar) return

    var confirmText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Desactivar Receta") },
        text = {
            Column {
                Text(
                    "Para desactivar la receta \"${receta.nombreReceta}\", " +
                            "escriba la palabra ELIMINAR para confirmar."
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it.uppercase() },
                    label = { Text("Escriba ELIMINAR") },
                    placeholder = { Text("ELIMINAR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = confirmText == "ELIMINAR",
                onClick = {
                    receta.idReceta?.let { id ->
                        onConfirm(id)          // 🔥 Enviar ID al ViewModel
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Desactivar")
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
fun DialogCambiarEstadoReceta(
    receta: RecipeWithDetailsAnswerUpdateDTO,
    mostrar: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    if (!mostrar) return

    val estaActiva = receta.estadoReceta == EstadoRecetaType.ACTIVO
    val nuevoEstado = if (estaActiva) "INACTIVA" else "ACTIVA"
    val colorFondo = if (estaActiva) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val colorIcono = if (estaActiva) Color(0xFFF44336) else Color(0xFF4CAF50)
    val icono = if (estaActiva) Icons.Default.Close else Icons.Default.Check

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = RoundedCornerShape(50),
                color = colorFondo,
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorIcono,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Cambiar estado de la receta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "\"${receta.nombreReceta}\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider()

                Text(
                    text = if (estaActiva) {
                        "Esta receta pasará de ACTIVA a INACTIVA.\n\n" +
                                "Las recetas inactivas no estarán disponibles para nuevas solicitudes de insumos."
                    } else {
                        "Esta receta pasará de INACTIVA a ACTIVA.\n\n" +
                                "La receta volverá a estar disponible para solicitudes de insumos."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = colorFondo,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nuevo estado: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = nuevoEstado,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorIcono
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    receta.idReceta?.let { id ->
                        onConfirm(id)  // 🔥 Enviar ID al ViewModel
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorIcono
                )
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cambiar a $nuevoEstado",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
