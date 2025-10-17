package com.example.kubhubsystem_gp13_dam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kubhubsystem_gp13_dam.data.repository.AsignaturaRepository
import com.example.kubhubsystem_gp13_dam.data.repository.SalaRepository
import com.example.kubhubsystem_gp13_dam.model.Asignatura
import com.example.kubhubsystem_gp13_dam.model.DiaSemana
import com.example.kubhubsystem_gp13_dam.model.HorarioConSala
import com.example.kubhubsystem_gp13_dam.model.Sala
import com.example.kubhubsystem_gp13_dam.model.Seccion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AsignaturasViewModel(
    private val repository: AsignaturaRepository = AsignaturaRepository.getInstance(),
    private val salaRepository: SalaRepository = SalaRepository.getInstance()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val salas: StateFlow<List<Sala>> = salaRepository.salas

    fun getSalasDisponibles(
        diaSemana: DiaSemana,
        bloqueHorario: Int,
        seccionExcluida: Int? = null
    ): List<Sala> {
        return salaRepository.getSalasDisponibles(diaSemana, bloqueHorario, seccionExcluida)
    }
    fun verificarConflictoHorarioConSala(
        idRamo: Int,
        horarios: List<HorarioConSala>,
        seccionExcluida: Int? = null
    ): String? {
        return repository.verificarConflictoHorarioConSala(idRamo, horarios, seccionExcluida)
    }


    val asignaturasFiltradas: StateFlow<List<Asignatura>> = combine(
        repository.asignaturas,
        _searchQuery
    ) { asignaturas, query ->
        if (query.isEmpty()) {
            asignaturas
        } else {
            asignaturas.filter { asignatura ->
                asignatura.nombreRamo.contains(query, ignoreCase = true) ||
                        asignatura.codigoRamo.contains(query, ignoreCase = true) ||
                        asignatura.coordinador.contains(query, ignoreCase = true) ||
                        asignatura.secciones.any { it.docente.contains(query, ignoreCase = true) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun agregarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            repository.agregarAsignatura(asignatura)
        }
    }

    fun actualizarAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            repository.actualizarAsignatura(asignatura)
        }
    }

    fun eliminarAsignatura(idRamo: Int) {
        viewModelScope.launch {
            repository.eliminarAsignatura(idRamo)
        }
    }

    fun agregarSeccion(idRamo: Int, seccion: Seccion) {
        viewModelScope.launch {
            repository.agregarSeccion(idRamo, seccion)
        }
    }

    fun actualizarSeccion(idRamo: Int, seccion: Seccion) {
        viewModelScope.launch {
            repository.actualizarSeccion(idRamo, seccion)
        }
    }

    fun eliminarSeccion(idRamo: Int, idSeccion: Int) {
        viewModelScope.launch {
            repository.eliminarSeccion(idRamo, idSeccion)
        }
    }

}