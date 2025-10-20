package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.PeriodoRecoleccion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class PeriodoRepository {

    private val _periodoActual = MutableStateFlow<PeriodoRecoleccion?>(null)
    val periodoActual: StateFlow<PeriodoRecoleccion?> = _periodoActual.asStateFlow()

    private val _periodos = MutableStateFlow<List<PeriodoRecoleccion>>(emptyList())
    val periodos: StateFlow<List<PeriodoRecoleccion>> = _periodos.asStateFlow()

    fun iniciarPeriodo(fechaCierre: LocalDate) {
        val nuevoId = (_periodos.value.maxOfOrNull { it.idPeriodo } ?: 0) + 1
        val nuevoPeriodo = PeriodoRecoleccion(
            idPeriodo = nuevoId,
            fechaInicio = LocalDate.now(),
            fechaCierre = fechaCierre,
            estaActivo = true
        )

        // Cerrar periodo anterior si existe
        if (_periodoActual.value != null) {
            cerrarPeriodo(_periodoActual.value!!.idPeriodo)
        }

        _periodoActual.value = nuevoPeriodo
        _periodos.value = _periodos.value + nuevoPeriodo
    }

    fun cerrarPeriodo(idPeriodo: Int) {
        _periodos.value = _periodos.value.map { periodo ->
            if (periodo.idPeriodo == idPeriodo) {
                periodo.copy(estaActivo = false)
            } else {
                periodo
            }
        }

        if (_periodoActual.value?.idPeriodo == idPeriodo) {
            _periodoActual.value = null
        }
    }
    fun sincronizarConPedido(
        idPedido: Int,
        fechaInicio: LocalDate,
        fechaFin: LocalDate,
        estaActivo: Boolean
    ) {
        if (estaActivo) {
            _periodoActual.value = PeriodoRecoleccion(
                idPeriodo = idPedido,
                fechaInicio = fechaInicio,
                fechaCierre = fechaFin,
                estaActivo = true
            )
        } else {
            _periodoActual.value = null
        }
    }

    fun agregarSolicitudAPeriodo(idPeriodo: Int, idSolicitud: Int) {
        _periodos.value = _periodos.value.map { periodo ->
            if (periodo.idPeriodo == idPeriodo) {
                periodo.copy(solicitudesIds = periodo.solicitudesIds + idSolicitud)
            } else {
                periodo
            }
        }

        if (_periodoActual.value?.idPeriodo == idPeriodo) {
            _periodoActual.value = _periodoActual.value?.copy(
                solicitudesIds = _periodoActual.value!!.solicitudesIds + idSolicitud
            )
        }
    }

    companion object {
        @Volatile
        private var instance: PeriodoRepository? = null

        fun getInstance(): PeriodoRepository {
            return instance ?: synchronized(this) {
                instance ?: PeriodoRepository().also { instance = it }
            }
        }
    }
}
