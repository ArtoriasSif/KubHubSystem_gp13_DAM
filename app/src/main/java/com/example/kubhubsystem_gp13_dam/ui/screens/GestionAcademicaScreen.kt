package com.example.kubhubsystem_gp13_dam.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SalaRepository
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.model.*
import com.example.kubhubsystem_gp13_dam.repository.ReservaSalaRepository
import com.example.kubhubsystem_gp13_dam.repository.SeccionRepository
import com.example.kubhubsystem_gp13_dam.repository.UsuarioRepository
import com.example.kubhubsystem_gp13_dam.viewmodel.GestionAcademicaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAcademicaScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.obtener(context.applicationContext) }

    val asignaturaRepository = remember { AsignaturaRepository(database.asignaturaDao()) }
    val seccionRepository = remember { SeccionRepository(database.seccionDao()) }
    val salaRepository = remember { SalaRepository(database.salaDao()) }
    val reservaSalaRepository = remember { ReservaSalaRepository(database.reservaSalaDao()) }
    val usuarioRepository = remember { UsuarioRepository(database.usuarioDao()) }

    val viewModel: GestionAcademicaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return GestionAcademicaViewModel(
                    asignaturaRepository = asignaturaRepository,
                    seccionRepository = seccionRepository,
                    salaRepository = salaRepository,
                    reservaSalaRepository = reservaSalaRepository,
                    usuarioRepository = usuarioRepository
                ) as T
            }
        }
    )

    val estado by viewModel.estado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showNuevaAsignaturaDialog by remember { mutableStateOf(false) }
    var asignaturaEditando by remember { mutableStateOf<Asignatura?>(null) }
    var asignaturaExpandida by remember { mutableStateOf<Int?>(null) }

    // Mostrar mensajes
    LaunchedEffect(estado.error, estado.mensajeExito) {
        estado.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
                viewModel.limpiarMensajes()
            }
        }

        estado.mensajeExito?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = mensaje,
                    duration = SnackbarDuration.Short
                )
                viewModel.limpiarMensajes()
            }
        }
    }

    // Inicializar datos
    LaunchedEffect(Unit) {
        viewModel.inicializarDatosSiEsNecesario()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión Académica") }
            )
        },
        floatingActionButton = {
            if (!estado.cargando) {
                ExtendedFloatingActionButton(
                    onClick = {
                        asignaturaEditando = null
                        showNuevaAsignaturaDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva Asignatura") },
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            }
        }
    ) { padding ->
        when {
            estado.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (estado.inicializando)
                                "Inicializando datos académicos..."
                            else
                                "Cargando asignaturas...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            estado.asignaturas.isEmpty() && !estado.cargando -> {
                PantallaInicialAcademica(
                    onInicializar = { viewModel.inicializarDatos() },
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Header con estadísticas
                    item {
                        HeaderEstadisticasAcademico(
                            totalAsignaturas = estado.totalAsignaturas,
                            totalSecciones = estado.totalSecciones,
                            totalSalas = estado.totalSalas
                        )
                    }

                    // Barra de búsqueda
                    item {
                        SearchBar(
                            searchQuery = "",
                            onSearchQueryChange = { }
                        )
                    }

                    // Lista de asignaturas
                    items(estado.asignaturas) { asignatura ->
                        AsignaturaCard(
                            asignatura = asignatura,
                            expanded = asignaturaExpandida == asignatura.idAsignatura,
                            onExpandClick = {
                                asignaturaExpandida = if (asignaturaExpandida == asignatura.idAsignatura) {
                                    null
                                } else {
                                    asignatura.idAsignatura
                                }
                            },
                            onEdit = {
                                asignaturaEditando = asignatura
                                showNuevaAsignaturaDialog = true
                            },
                            onDelete = {
                                viewModel.eliminarAsignatura(asignatura)
                            },
                            onAddSeccion = { seccion ->
                                viewModel.agregarSeccion(seccion, asignatura.idAsignatura)
                            },
                            onEditSeccion = { seccion ->
                                viewModel.actualizarSeccion(seccion, asignatura.idAsignatura)
                            },
                            onDeleteSeccion = { seccion ->
                                viewModel.eliminarSeccion(seccion)
                            },
                            onAsignarDocente = { idSeccion, idDocente ->
                                viewModel.asignarDocenteASeccion(idSeccion, idDocente, asignatura.idAsignatura)
                            },
                            onAgregarHorario = { reserva ->
                                viewModel.agregarReserva(reserva)
                            },
                            onEliminarHorario = { reserva ->
                                viewModel.eliminarReserva(reserva)
                            },
                            viewModel = viewModel
                        )
                    }

                    // Espaciado para el FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Diálogo de Nueva/Editar Asignatura
    if (showNuevaAsignaturaDialog) {
        AsignaturaDialog(
            asignatura = asignaturaEditando,
            onDismiss = {
                showNuevaAsignaturaDialog = false
                asignaturaEditando = null
            },
            onSave = { asignatura ->
                if (asignaturaEditando != null) {
                    viewModel.actualizarAsignatura(asignatura)
                } else {
                    viewModel.agregarAsignatura(asignatura)
                }
                showNuevaAsignaturaDialog = false
                asignaturaEditando = null
            }
        )
    }
}

// ============================================================================
// COMPONENTES BÁSICOS
// ============================================================================

@Composable
private fun PantallaInicialAcademica(
    onInicializar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFFFC107)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Gestión Académica",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No hay datos académicos en el sistema.\nInicialice los datos para comenzar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onInicializar,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inicializar Datos Académicos")
            }
        }
    }
}

@Composable
private fun HeaderEstadisticasAcademico(
    totalAsignaturas: Int,
    totalSecciones: Int,
    totalSalas: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC107)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaChip(
                icon = Icons.Default.School,
                valor = totalAsignaturas.toString(),
                etiqueta = "Asignaturas"
            )

            EstadisticaChip(
                icon = Icons.Default.Groups,
                valor = totalSecciones.toString(),
                etiqueta = "Secciones"
            )

            EstadisticaChip(
                icon = Icons.Default.MeetingRoom,
                valor = totalSalas.toString(),
                etiqueta = "Salas"
            )
        }
    }
}

@Composable
private fun EstadisticaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    etiqueta: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        placeholder = { Text("Buscar asignatura...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar")
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
// CONTINUACIÓN DE GestionAcademicaScreen.kt - Parte 2

@Composable
private fun AsignaturaCard(
    asignatura: Asignatura,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSeccion: (Seccion) -> Unit,
    onEditSeccion: (Seccion) -> Unit,
    onDeleteSeccion: (Seccion) -> Unit,
    onAsignarDocente: (Int, Int) -> Unit,
    onAgregarHorario: (ReservaSala) -> Unit,
    onEliminarHorario: (ReservaSala) -> Unit,
    viewModel: GestionAcademicaViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSeccionDialog by remember { mutableStateOf(false) }
    var seccionEditando by remember { mutableStateOf<Seccion?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        )
    ) {
        Column {
            // Cabecera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandClick() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asignatura.nombreAsignatura,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = asignatura.codigoAsignatura,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${asignatura.secciones.size} sección(es)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF57C00)
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFFFFA000)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Contraer" else "Expandir",
                        tint = Color.Black
                    )
                }
            }

            // Contenido expandible - Secciones
            if (expanded) {
                Divider(color = Color(0xFFFFA000))

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (asignatura.secciones.isEmpty()) {
                        Text(
                            text = "No hay secciones creadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        asignatura.secciones.forEach { seccion ->
                            SeccionItem(
                                seccion = seccion,
                                asignatura = asignatura,
                                onEdit = {
                                    seccionEditando = seccion
                                    showSeccionDialog = true
                                },
                                onDelete = {
                                    onDeleteSeccion(seccion)
                                },
                                onAsignarDocente = { idDocente ->
                                    onAsignarDocente(seccion.idSeccion, idDocente)
                                },
                                onAgregarHorario = { reserva ->
                                    onAgregarHorario(reserva)
                                },
                                onEliminarHorario = { reserva ->
                                    onEliminarHorario(reserva)
                                },
                                viewModel = viewModel
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Botón agregar sección
                    OutlinedButton(
                        onClick = {
                            seccionEditando = null
                            showSeccionDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFFFA000))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Sección", color = Color.Black)
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Asignatura") },
            text = {
                Text("¿Está seguro de que desea eliminar ${asignatura.nombreAsignatura}? Se eliminarán todas sus secciones.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
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

    // Diálogo de sección
    if (showSeccionDialog) {
        SeccionDialog(
            seccion = seccionEditando,
            onDismiss = {
                showSeccionDialog = false
                seccionEditando = null
            },
            onSave = { seccion ->
                if (seccionEditando != null) {
                    onEditSeccion(seccion)
                } else {
                    onAddSeccion(seccion)
                }
                showSeccionDialog = false
                seccionEditando = null
            },
            docentes = viewModel.obtenerDocentes()
        )
    }
}

// CORRECCIÓN: SeccionItem con acceso correcto a las salas del estado

@Composable
private fun SeccionItem(
    seccion: Seccion,
    asignatura: Asignatura,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAsignarDocente: (Int) -> Unit,
    onAgregarHorario: (ReservaSala) -> Unit,
    onEliminarHorario: (ReservaSala) -> Unit,
    viewModel: GestionAcademicaViewModel
) {
    var expandedSeccion by remember { mutableStateOf(false) }
    var showDocenteDialog by remember { mutableStateOf(false) }
    var showHorarioDialog by remember { mutableStateOf(false) }

    // ✅ CORRECCIÓN: Obtener el estado de forma reactiva
    val estado by viewModel.estado.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Cabecera de sección
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSeccion = !expandedSeccion }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sección ${seccion.nombreSeccion}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
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
                        text = "Docente: ${seccion.nombreDocente}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    if (seccion.horarios.isNotEmpty()) {
                        Text(
                            text = "${seccion.horarios.size} horario(s) asignado(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF57C00)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar sección",
                            tint = Color(0xFFFFC107)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar sección",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                    Icon(
                        if (expandedSeccion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expandedSeccion) "Contraer" else "Expandir",
                        tint = Color.Black
                    )
                }
            }

            // Contenido expandible - Horarios y botones
            if (expandedSeccion) {
                Divider(color = Color(0xFFE0E0E0))

                Column(modifier = Modifier.padding(12.dp)) {
                    // Botón asignar docente
                    OutlinedButton(
                        onClick = { showDocenteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Asignar Docente")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de horarios
                    if (seccion.horarios.isNotEmpty()) {
                        Text(
                            text = "Horarios:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        seccion.horarios.forEach { horario ->
                            HorarioItem(
                                horario = horario,
                                onEliminar = {
                                    val reserva = ReservaSala(
                                        seccion = seccion,
                                        asignatura = asignatura,
                                        sala = horario.sala,
                                        diaSemana = horario.diaSemana,
                                        bloqueHorario = horario.bloqueHorario
                                    )
                                    onEliminarHorario(reserva)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón agregar horario
                    OutlinedButton(
                        onClick = { showHorarioDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFF3E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Horario", color = Color.Black)
                    }
                }
            }
        }
    }

    // Diálogo asignar docente
    if (showDocenteDialog) {
        AsignarDocenteDialog(
            docentes = estado.docentes, // ✅ CORREGIDO: Usa estado reactivo
            docenteActual = seccion.idDocente,
            onDismiss = { showDocenteDialog = false },
            onAsignar = { idDocente ->
                onAsignarDocente(idDocente)
                showDocenteDialog = false
            }
        )
    }

    // ✅ CORRECCIÓN PRINCIPAL: Diálogo agregar horario
    if (showHorarioDialog) {
        AgregarHorarioDialog(
            seccion = seccion,
            asignatura = asignatura,
            salas = estado.salas, // ✅ Usa estado reactivo, no .value
            dias = viewModel.obtenerDiasSemanaDisponibles(),
            bloques = viewModel.obtenerBloqueDisponibles(),
            onDismiss = { showHorarioDialog = false },
            onAgregar = { reserva ->
                onAgregarHorario(reserva)
                showHorarioDialog = false
            },
            obtenerNombreBloque = { bloque -> viewModel.obtenerNombreBloque(bloque) }
        )
    }
}
@Composable
private fun HorarioItem(
    horario: HorarioBloque,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFFF57C00),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${horario.diaSemana.nombreMostrar} - Sala ${horario.sala.codigoSala}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Bloque ${horario.bloqueHorario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar horario",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
// CONTINUACIÓN DE GestionAcademicaScreen.kt - Parte 3 (Diálogos)

// ============================================================================
// DIÁLOGOS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignaturaDialog(
    asignatura: Asignatura?,
    onDismiss: () -> Unit,
    onSave: (Asignatura) -> Unit
) {
    var nombreAsignatura by remember { mutableStateOf(asignatura?.nombreAsignatura ?: "") }
    var codigoAsignatura by remember { mutableStateOf(asignatura?.codigoAsignatura ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (asignatura == null) "Nueva Asignatura" else "Editar Asignatura",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = nombreAsignatura,
                    onValueChange = { nombreAsignatura = it },
                    label = { Text("Nombre de la asignatura") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Ej: Panadería Básica") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = codigoAsignatura,
                    onValueChange = { codigoAsignatura = it.uppercase() },
                    label = { Text("Código (Ej: GAS-101)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("GAS-101") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            when {
                                nombreAsignatura.isBlank() -> {
                                    errorMessage = "El nombre es obligatorio"
                                }
                                codigoAsignatura.isBlank() -> {
                                    errorMessage = "El código es obligatorio"
                                }
                                else -> {
                                    onSave(
                                        Asignatura(
                                            idAsignatura = asignatura?.idAsignatura ?: 0,
                                            nombreAsignatura = nombreAsignatura.trim(),
                                            codigoAsignatura = codigoAsignatura.trim(),
                                            periodo = "2025-1",
                                            secciones = asignatura?.secciones ?: emptyList()
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionDialog(
    seccion: Seccion?,
    onDismiss: () -> Unit,
    onSave: (Seccion) -> Unit,
    docentes: List<com.example.kubhubsystem_gp13_dam.viewmodel.DocenteInfo>
) {
    var numeroSeccion by remember { mutableStateOf(seccion?.nombreSeccion ?: "") }
    var docenteSeleccionado by remember { mutableStateOf(seccion?.idDocente) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (seccion != null) "Editar Sección" else "Nueva Sección",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = numeroSeccion,
                    onValueChange = { numeroSeccion = it },
                    label = { Text("Número de sección (Ej: 001)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            when {
                                numeroSeccion.isBlank() -> {
                                    errorMessage = "El número de sección es obligatorio"
                                }
                                else -> {
                                    onSave(
                                        Seccion(
                                            idSeccion = seccion?.idSeccion ?: 0,
                                            nombreSeccion = numeroSeccion,
                                            idDocente = docenteSeleccionado,
                                            nombreDocente = "",
                                            horarios = seccion?.horarios ?: emptyList(),
                                            estaActiva = true
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AsignarDocenteDialog(
    docentes: List<com.example.kubhubsystem_gp13_dam.viewmodel.DocenteInfo>,
    docenteActual: Int?,
    onDismiss: () -> Unit,
    onAsignar: (Int) -> Unit
) {
    var docenteSeleccionado by remember { mutableStateOf(docenteActual) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Asignar Docente",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (docentes.isEmpty()) {
                    Text(
                        text = "No hay docentes disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(docentes) { docente ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        docenteSeleccionado = docente.idUsuario
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (docenteSeleccionado == docente.idUsuario)
                                        Color(0xFFFFF8E1)
                                    else
                                        Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = docenteSeleccionado == docente.idUsuario,
                                        onClick = { docenteSeleccionado = docente.idUsuario },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFFFC107)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = docente.nombreCompleto,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "ID: ${docente.idUsuario}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Black.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            docenteSeleccionado?.let { onAsignar(it) }
                        },
                        enabled = docenteSeleccionado != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Asignar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AgregarHorarioDialog(
    seccion: Seccion,
    asignatura: Asignatura,
    salas: List<Sala>,
    dias: List<DiaSemana>,
    bloques: List<Int>,
    onDismiss: () -> Unit,
    onAgregar: (ReservaSala) -> Unit,
    obtenerNombreBloque: (Int) -> String
) {
    var salaSeleccionada by remember { mutableStateOf<Sala?>(null) }
    var diaSeleccionado by remember { mutableStateOf<DiaSemana?>(null) }
    var bloqueSeleccionado by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agregar Horario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // Seleccionar Sala
                    item {
                        Text(
                            text = "Seleccionar Sala:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(salas) { sala ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { salaSeleccionada = sala },
                            colors = CardDefaults.cardColors(
                                containerColor = if (salaSeleccionada == sala)
                                    Color(0xFFFFF8E1)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = salaSeleccionada == sala,
                                    onClick = { salaSeleccionada = sala },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFFFC107)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sala ${sala.codigoSala}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Seleccionar Día
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Seleccionar Día:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(dias) { dia ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { diaSeleccionado = dia },
                            colors = CardDefaults.cardColors(
                                containerColor = if (diaSeleccionado == dia)
                                    Color(0xFFFFF8E1)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = diaSeleccionado == dia,
                                    onClick = { diaSeleccionado = dia },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFFFC107)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dia.nombreMostrar,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Seleccionar Bloque
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Seleccionar Bloque:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(bloques) { bloque ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { bloqueSeleccionado = bloque },
                            colors = CardDefaults.cardColors(
                                containerColor = if (bloqueSeleccionado == bloque)
                                    Color(0xFFFFF8E1)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = bloqueSeleccionado == bloque,
                                    onClick = { bloqueSeleccionado = bloque },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFFFC107)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Bloque $bloque",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = obtenerNombreBloque(bloque),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            when {
                                salaSeleccionada == null -> {
                                    errorMessage = "Debe seleccionar una sala"
                                }
                                diaSeleccionado == null -> {
                                    errorMessage = "Debe seleccionar un día"
                                }
                                bloqueSeleccionado == null -> {
                                    errorMessage = "Debe seleccionar un bloque"
                                }
                                else -> {
                                    val reserva = ReservaSala(
                                        seccion = seccion,
                                        asignatura = asignatura,
                                        sala = salaSeleccionada!!,
                                        diaSemana = diaSeleccionado!!,
                                        bloqueHorario = bloqueSeleccionado!!
                                    )
                                    onAgregar(reserva)
                                }
                            }
                        },
                        enabled = salaSeleccionada != null && diaSeleccionado != null && bloqueSeleccionado != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF57C00)
                        )
                    ) {
                        Text("Agregar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}