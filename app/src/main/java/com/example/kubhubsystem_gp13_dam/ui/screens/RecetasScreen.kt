package com.example.kubhubsystem_gp13_dam.ui.screens

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
import com.example.kubhubsystem_gp13_dam.ui.model.CategoriaReceta
import com.example.kubhubsystem_gp13_dam.ui.model.Receta

import com.example.kubhubsystem_gp13_dam.ui.viewmodel.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetasScreen(
    viewModel: RecetasViewModel = viewModel()
) {
    val recetas by viewModel.recetasFiltradas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var recetaEditando by remember { mutableStateOf<Receta?>(null) }
    var showCategoriaMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gestión de Recetas",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Administre las recetas del sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    recetaEditando = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Receta", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Barra de búsqueda y filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar recetas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Box {
                OutlinedButton(
                    onClick = { showCategoriaMenu = true },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedCategoria?.displayName ?: "Todas")
                }

                DropdownMenu(
                    expanded = showCategoriaMenu,
                    onDismissRequest = { showCategoriaMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las categorías") },
                        onClick = {
                            viewModel.updateSelectedCategoria(null)
                            showCategoriaMenu = false
                        }
                    )
                    CategoriaReceta.values().forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.displayName) },
                            onClick = {
                                viewModel.updateSelectedCategoria(categoria)
                                showCategoriaMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Contador
        Text(
            text = "${recetas.size} receta(s) encontrada(s)",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tabla de recetas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Encabezados
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("NOMBRE", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("CATEGORÍA", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("ASIGNATURA", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("ESTADO", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("ACCIONES", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                if (recetas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron recetas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn {
                        items(recetas) { receta ->
                            RecetaRow(
                                receta = receta,
                                onEdit = {
                                    recetaEditando = receta
                                    showDialog = true
                                },
                                onDelete = {
                                    viewModel.eliminarReceta(receta.idReceta)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "© 2025 KuHub System | Version 0.1",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDialog) {
        RecetaDialog(
            receta = recetaEditando,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onSave = { receta ->
                if (recetaEditando != null) {
                    viewModel.actualizarReceta(receta)
                } else {
                    viewModel.agregarReceta(receta)
                }
                showDialog = false
                recetaEditando = null
            }
        )
    }
}

@Composable
fun RecetaRow(
    receta: Receta,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenuOptions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = receta.nombre,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = receta.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = receta.categoria.displayName,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = receta.asignaturaRelacionada?.nombreRamo ?: "Sin asignar",
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (receta.asignaturaRelacionada == null)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF4CAF50).copy(alpha = 0.2f)
        ) {
            Text(
                text = if (receta.estaActiva) "Activa" else "Inactiva",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32)
            )
        }

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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Receta") },
            text = { Text("¿Está seguro de que desea eliminar ${receta.nombre}?") },
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