package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.AsignaturasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignaturasScreen(
    viewModel: AsignaturasViewModel = viewModel()
) {
    val asignaturas by viewModel.asignaturasFiltradas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var asignaturaEditando by remember { mutableStateOf<Asignatura?>(null) }
    var expandedAsignaturaId by remember { mutableStateOf<Int?>(null) }

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
                    text = "Gestión de Asignaturas",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Administre asignaturas, secciones y asignaciones de profesores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    asignaturaEditando = null
                    showDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Asignatura", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            placeholder = { Text("Buscar asignaturas, códigos, profesores o secciones...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Lista de asignaturas
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(asignaturas) { asignatura ->
                AsignaturaCard(
                    asignatura = asignatura,
                    expanded = expandedAsignaturaId == asignatura.idRamo,
                    onExpandClick = {
                        expandedAsignaturaId = if (expandedAsignaturaId == asignatura.idRamo) null else asignatura.idRamo
                    },
                    onEdit = {
                        asignaturaEditando = asignatura
                        showDialog = true
                    },
                    onDelete = {
                        viewModel.eliminarAsignatura(asignatura.idRamo)
                    },
                    onAddSeccion = { seccion ->
                        viewModel.agregarSeccion(asignatura.idRamo, seccion)
                    },
                    viewModel = viewModel,  // ✅ Pasar el viewModel
                    idAsignatura = asignatura.idRamo  // ✅ Pasar el ID
                )
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

    // Diálogo para agregar/editar asignatura
    if (showDialog) {
        AsignaturaDialog(
            asignatura = asignaturaEditando,
            onDismiss = { showDialog = false },
            onSave = { asignatura ->
                if (asignaturaEditando != null) {
                    viewModel.actualizarAsignatura(asignatura)
                } else {
                    viewModel.agregarAsignatura(asignatura)
                }
                showDialog = false
                asignaturaEditando = null
            }
        )
    }
}

@Composable
fun AsignaturaCard(
    asignatura: Asignatura,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSeccion: (Seccion) -> Unit,
    viewModel: AsignaturasViewModel,  // ✅ AGREGAR
    idAsignatura: Int  // ✅ AGREGAR
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSeccionDialog by remember { mutableStateOf(false) }
    var seccionEditando by remember { mutableStateOf<Seccion?>(null) }

    val todasLasSalas by viewModel.salas.collectAsState()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Cabecera de la asignatura
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandClick() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asignatura.nombreRamo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFFFC107),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = asignatura.codigoRamo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Coordinador: ${asignatura.coordinador} • ${asignatura.creditos} créditos • ${asignatura.periodo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    // Badges de secciones
                    Surface(
                        color = Color(0xFF757575),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${asignatura.secciones.size} secciones",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${asignatura.secciones.count { it.estaActiva }} activas",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón editar
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFFFFC107)
                        )
                    }
                }
            }

            // Contenido expandible (secciones)
            if (expanded) {
                HorizontalDivider()

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (asignatura.secciones.isEmpty()) {
                        Text(
                            text = "No hay secciones creadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        asignatura.secciones.forEach { seccion ->
                            SeccionItem(seccion = seccion)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Botón para agregar sección
                    OutlinedButton(
                        onClick = {
                            seccionEditando = null
                            showSeccionDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Sección")
                    }
                }
            }
        }
    }
    // Diálogo de sección
    if (showSeccionDialog) {
        SeccionDialog(
            seccion = seccionEditando,
            onDismiss = { showSeccionDialog = false },
            onSave = { seccion ->
                // Validar antes de guardar
                val conflicto = viewModel.verificarConflictoHorarioConSala(
                    idAsignatura,
                    seccion.horarios,
                    seccion.idSeccion
                )

                if (conflicto != null) {
                    // Mostrar error
                    // TODO: Agregar estado para mostrar error
                } else {
                    onAddSeccion(seccion)
                    showSeccionDialog = false
                    seccionEditando = null
                }
            },
            getSalasDisponibles = { dia, bloque ->
                viewModel.getSalasDisponibles(dia, bloque, seccionEditando?.idSeccion)
            },  // ✅ Agregar coma también aquí
            todasLasSalas = todasLasSalas
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Asignatura") },
            text = { Text("¿Está seguro de que desea eliminar ${asignatura.nombreRamo}? Se eliminarán todas sus secciones.") },
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
    // Diálogo de sección
    if (showSeccionDialog) {
        SeccionDialog(
            seccion = seccionEditando,
            onDismiss = { showSeccionDialog = false },
            onSave = { seccion ->
                val conflicto = viewModel.verificarConflictoHorarioConSala(
                    idAsignatura,
                    seccion.horarios,
                    seccion.idSeccion
                )

                if (conflicto != null) {
                    println("❌ Conflicto de horario: $conflicto")
                } else {
                    onAddSeccion(seccion)
                    showSeccionDialog = false
                    seccionEditando = null
                }
            },
            getSalasDisponibles = { dia, bloque ->
                viewModel.getSalasDisponibles(dia, bloque, seccionEditando?.idSeccion)
            },
            todasLasSalas = todasLasSalas
        )
    }
}
@Composable
fun SeccionItem(seccion: Seccion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sección ${seccion.numeroSeccion}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (seccion.estaActiva) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Activa",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Text(
                    text = "Docente: ${seccion.docente}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (seccion.horarios.isNotEmpty()) {
                    // ✅ ACTUALIZAR para mostrar sala
                    Text(
                        text = "Horarios: ${seccion.horarios.joinToString(", ") {
                            "${it.diaSemana.displayName} ${HorarioUtils.getBloqueHorario(it.bloqueHorario)} (${it.sala.codigoSala})"
                        }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { /* TODO: Editar sección */ }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar sección")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignaturaDialog(
    asignatura: Asignatura?,
    onDismiss: () -> Unit,
    onSave: (Asignatura) -> Unit
) {
    var nombreRamo by remember { mutableStateOf(asignatura?.nombreRamo ?: "") }
    var codigoRamo by remember { mutableStateOf(asignatura?.codigoRamo ?: "") }
    var coordinador by remember { mutableStateOf(asignatura?.coordinador ?: "") }
    var creditos by remember { mutableStateOf(asignatura?.creditos?.toString() ?: "") }
    var periodo by remember { mutableStateOf(asignatura?.periodo ?: "2025-1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (asignatura != null) "Editar Asignatura" else "Nueva Asignatura") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombreRamo,
                    onValueChange = { nombreRamo = it },
                    label = { Text("Nombre de la asignatura") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = codigoRamo,
                    onValueChange = { codigoRamo = it },
                    label = { Text("Código (Ej: GAS-101)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = coordinador,
                    onValueChange = { coordinador = it },
                    label = { Text("Coordinador") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = creditos,
                    onValueChange = { if (it.all { char -> char.isDigit() }) creditos = it },
                    label = { Text("Créditos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = periodo,
                    onValueChange = { periodo = it },
                    label = { Text("Periodo (Ej: 2025-1)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombreRamo.isNotEmpty() && codigoRamo.isNotEmpty() &&
                        coordinador.isNotEmpty() && creditos.isNotEmpty()) {
                        onSave(
                            Asignatura(
                                idRamo = asignatura?.idRamo ?: 0,
                                nombreRamo = nombreRamo,
                                codigoRamo = codigoRamo,
                                coordinador = coordinador,
                                creditos = creditos.toIntOrNull() ?: 0,
                                periodo = periodo,
                                secciones = asignatura?.secciones ?: emptyList()
                            )
                        )
                    }
                },
                enabled = nombreRamo.isNotEmpty() && codigoRamo.isNotEmpty() &&
                        coordinador.isNotEmpty() && creditos.isNotEmpty()
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
