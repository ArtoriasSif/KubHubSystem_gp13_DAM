package com.example.kubhubsystem_gp13_dam.ui.screens.mainMenu.recetas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.RecetaRepository
import com.example.kubhubsystem_gp13_dam.local.AppDatabase
import com.example.kubhubsystem_gp13_dam.repository.ProductoRepository
import com.example.kubhubsystem_gp13_dam.ui.viewmodel.RecetasViewModel

@Composable
fun RecetasScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context.applicationContext) }

    // Repositorios
    val productoRepository = remember { ProductoRepository(database.productoDao()) }
    val recetaRepository = remember { RecetaRepository.getInstance() }
    val asignaturaRepository = remember { AsignaturaRepository.getInstance() }

    // ViewModel con dependencias
    val viewModel = remember {
        RecetasViewModel(
            recetaRepository = recetaRepository,
            productoRepository = productoRepository,
            asignaturaRepository = asignaturaRepository
        )
    }

    // Estados del ViewModel
    val recetasFiltradas by viewModel.recetasFiltradas.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val asignaturas by viewModel.asignaturas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()

    // UI de Recetas
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión de Recetas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar recetas...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de recetas
        Text("Total de recetas: ${recetasFiltradas.size}")
        Text("Productos disponibles: ${productos.size}")
        Text("Asignaturas: ${asignaturas.size}")

        // Aquí agregarías el resto de tu UI de recetas
    }
}