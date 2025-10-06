package com.example.kubhubsystem_gp13_dam.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.kubhubsystem_gp13_dam.model.DiaSemana

import com.example.kubhubsystem_gp13_dam.model.HorarioConSala
import com.example.kubhubsystem_gp13_dam.model.HorarioUtils
import com.example.kubhubsystem_gp13_dam.model.Sala
import com.example.kubhubsystem_gp13_dam.model.Seccion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionDialog(
    seccion: Seccion?,
    onDismiss: () -> Unit,
    onSave: (Seccion) -> Unit,
    getSalasDisponibles: (DiaSemana, Int) -> List<Sala>,
    todasLasSalas: List<Sala> // ✅ CAMBIAR parámetro
) {
    var numeroSeccion by remember { mutableStateOf(seccion?.numeroSeccion ?: "") }
    var docente by remember { mutableStateOf(seccion?.docente ?: "") }
    var horariosSeleccionados by remember { mutableStateOf(seccion?.horarios ?: emptyList()) }
    var showHorariosPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (seccion != null) "Editar Sección" else "Nueva Sección") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = numeroSeccion,
                        onValueChange = { numeroSeccion = it },
                        label = { Text("Número de sección (Ej: 001)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = docente,
                        onValueChange = { docente = it },
                        label = { Text("Nombre del docente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        )
                    )
                }

                item {
                    Text(
                        text = "Horarios asignados:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (horariosSeleccionados.isEmpty()) {
                    item {
                        Text(
                            text = "No hay horarios asignados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(horariosSeleccionados.size) { index ->
                        val horario = horariosSeleccionados[index]
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${horario.diaSemana.displayName} - Bloque ${horario.bloqueHorario}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sala: ${horario.sala.codigoSala} - ${HorarioUtils.getBloqueHorario(horario.bloqueHorario)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        horariosSeleccionados = horariosSeleccionados.filter { it != horario }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar horario",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showHorariosPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar Horarios")
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (numeroSeccion.isNotEmpty() && docente.isNotEmpty()) {
                        onSave(
                            Seccion(
                                idSeccion = seccion?.idSeccion ?: 0,
                                numeroSeccion = numeroSeccion,
                                docente = docente,
                                horarios = horariosSeleccionados,
                                estaActiva = seccion?.estaActiva ?: true
                            )
                        )
                    }
                },
                enabled = numeroSeccion.isNotEmpty() && docente.isNotEmpty() && horariosSeleccionados.isNotEmpty()
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

    if (showHorariosPicker) {
        HorarioPickerDialog(
            onDismiss = { showHorariosPicker = false },
            onConfirm = { nuevosHorarios ->
                horariosSeleccionados = horariosSeleccionados + nuevosHorarios
                showHorariosPicker = false
                errorMessage = null
            },
            salasDisponibles = getSalasDisponibles,
            todasLasSalas = todasLasSalas  // ✅ Pasar el parámetro
        )
    }
}
@Composable
fun HorarioPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<HorarioConSala>) -> Unit,
    salasDisponibles: (DiaSemana, Int) -> List<Sala>,
    todasLasSalas: List<Sala>  // Lista completa de salas
) {
    var selectedSala by remember { mutableStateOf<Sala?>(null) }
    var selectedDia by remember { mutableStateOf<DiaSemana?>(null) }
    var selectedBloques by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var horariosSeleccionados by remember { mutableStateOf<List<HorarioConSala>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Horarios y Salas") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 550.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Horarios ya agregados
                if (horariosSeleccionados.isNotEmpty()) {
                    item {
                        Text(
                            text = "Horarios agregados (${horariosSeleccionados.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(horariosSeleccionados.size) { index ->
                        val horario = horariosSeleccionados[index]
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${horario.sala.codigoSala} - ${horario.diaSemana.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Bloque ${horario.bloqueHorario}: ${HorarioUtils.getBloqueHorarioCorto(horario.bloqueHorario)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        horariosSeleccionados = horariosSeleccionados.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Agregar más horarios:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // PASO 1: Seleccionar sala
                item {
                    Text(
                        text = "1. Seleccione la sala:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(todasLasSalas.size) { index ->
                    val sala = todasLasSalas[index]
                    val isSelected = selectedSala?.idSala == sala.idSala

                    OutlinedCard(
                        onClick = {
                            selectedSala = sala
                            selectedDia = null
                            selectedBloques = emptySet()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sala.codigoSala,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = sala.tipoSala.displayName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Capacidad: ${sala.capacidad}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // PASO 2: Seleccionar día (solo si hay sala seleccionada)
                if (selectedSala != null) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "2. Seleccione el día:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DiaSemana.values().take(6).forEach { dia ->
                                FilterChip(
                                    selected = selectedDia == dia,
                                    onClick = {
                                        selectedDia = dia
                                        selectedBloques = emptySet()
                                    },
                                    label = { Text(dia.displayName.take(3)) }
                                )
                            }
                        }
                    }
                }

                // PASO 3: Seleccionar bloques (solo si hay sala y día)
                if (selectedSala != null && selectedDia != null) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "3. Seleccione bloques (${selectedBloques.size} seleccionados):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(20) { index ->
                        val bloque = index + 1
                        val isSelected = selectedBloques.contains(bloque)

                        // Verificar si la sala está disponible en este horario
                        val estaDisponible = salasDisponibles(selectedDia!!, bloque)
                            .any { it.idSala == selectedSala!!.idSala }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = estaDisponible) {
                                    if (estaDisponible) {
                                        selectedBloques = if (isSelected) {
                                            selectedBloques - bloque
                                        } else {
                                            selectedBloques + bloque
                                        }
                                    }
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = when {
                                        !estaDisponible -> Color.Red
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            color = when {
                                !estaDisponible -> Color.Red.copy(alpha = 0.1f)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = when {
                                            !estaDisponible -> Color.Red
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = bloque.toString(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!estaDisponible || isSelected)
                                                Color.White
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Módulo $bloque",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!estaDisponible)
                                                Color.Red
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = HorarioUtils.getBloqueHorario(bloque),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (!estaDisponible)
                                                Color.Red.copy(alpha = 0.7f)
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                when {
                                    !estaDisponible -> Icon(
                                        Icons.Default.Close,
                                        contentDescription = "No disponible",
                                        tint = Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    isSelected -> Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Botón para agregar bloques seleccionados
                    if (selectedBloques.isNotEmpty()) {
                        item {
                            Button(
                                onClick = {
                                    val nuevosHorarios = selectedBloques.map { bloque ->
                                        HorarioConSala(
                                            diaSemana = selectedDia!!,
                                            bloqueHorario = bloque,
                                            sala = selectedSala!!
                                        )
                                    }
                                    horariosSeleccionados = horariosSeleccionados + nuevosHorarios

                                    // Reset para agregar más
                                    selectedDia = null
                                    selectedBloques = emptySet()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar ${selectedBloques.size} bloque(s) a la lista")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(horariosSeleccionados)
                },
                enabled = horariosSeleccionados.isNotEmpty()
            ) {
                Text("Confirmar (${horariosSeleccionados.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}