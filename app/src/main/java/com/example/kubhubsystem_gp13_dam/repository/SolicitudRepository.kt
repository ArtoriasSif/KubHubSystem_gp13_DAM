package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class SolicitudRepository {

    private val _solicitudes = MutableStateFlow<List<Solicitud>>(emptyList())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    init {
        // Cargar solicitudes de prueba
        cargarSolicitudesPrueba()
    }

    private fun cargarSolicitudesPrueba() {
        val asignaturaRepo = AsignaturaRepository.getInstance()
        val productoRepo = ProductoRepository.getInstance()

        val asignaturas = asignaturaRepo.asignaturas.value

        if (asignaturas.isNotEmpty()) {
            _solicitudes.value = listOf(
                Solicitud(
                    idSolicitud = 1,
                    asignatura = asignaturas[0],
                    seccion = asignaturas[0].secciones[0],
                    profesor = asignaturas[0].secciones[0].docente,
                    fechaSolicitud = LocalDateTime.now().minusDays(5),
                    fechaClase = LocalDateTime.now().plusDays(10),
                    productos = listOf(
                        ProductoSolicitado(
                            idProductoSolicitado = 1,
                            producto = productoRepo.productos.value[0],
                            cantidadSolicitada = 2.0,
                            unidad = "kg"
                        ),
                        ProductoSolicitado(
                            idProductoSolicitado = 2,
                            producto = productoRepo.productos.value[1],
                            cantidadSolicitada = 1.0,
                            unidad = "l"
                        )
                    ),
                    estado = EstadoSolicitud.PENDIENTE
                ),
                Solicitud(
                    idSolicitud = 2,
                    asignatura = asignaturas[1],
                    seccion = asignaturas[1].secciones[0],
                    profesor = asignaturas[1].secciones[0].docente,
                    fechaSolicitud = LocalDateTime.now().minusDays(3),
                    fechaClase = LocalDateTime.now().plusDays(8),
                    productos = listOf(
                        ProductoSolicitado(
                            idProductoSolicitado = 3,
                            producto = productoRepo.productos.value[2],
                            cantidadSolicitada = 0.5,
                            unidad = "kg"
                        )
                    ),
                    estado = EstadoSolicitud.APROBADO
                ),
                Solicitud(
                    idSolicitud = 3,
                    asignatura = asignaturas[2],
                    seccion = asignaturas[2].secciones[0],
                    profesor = asignaturas[2].secciones[0].docente,
                    fechaSolicitud = LocalDateTime.now().minusDays(2),
                    fechaClase = LocalDateTime.now().plusDays(5),
                    productos = emptyList(),
                    estado = EstadoSolicitud.ENTREGADO
                ),
                Solicitud(
                    idSolicitud = 4,
                    asignatura = asignaturas[3],
                    seccion = asignaturas[3].secciones[0],
                    profesor = asignaturas[3].secciones[0].docente,
                    fechaSolicitud = LocalDateTime.now().minusDays(1),
                    fechaClase = LocalDateTime.now().plusDays(4),
                    productos = emptyList(),
                    estado = EstadoSolicitud.RECHAZADO
                ),
                Solicitud(
                    idSolicitud = 5,
                    asignatura = asignaturas[0],
                    seccion = asignaturas[0].secciones[1],
                    profesor = asignaturas[0].secciones[1].docente,
                    fechaSolicitud = LocalDateTime.now(),
                    fechaClase = LocalDateTime.now().plusDays(3),
                    productos = emptyList(),
                    estado = EstadoSolicitud.PENDIENTE
                )
            )
        }
    }

    fun agregarSolicitud(solicitud: Solicitud) {
        val nuevoId = (_solicitudes.value.maxOfOrNull { it.idSolicitud } ?: 0) + 1
        val nuevaSolicitud = solicitud.copy(idSolicitud = nuevoId)
        _solicitudes.value = _solicitudes.value + nuevaSolicitud
    }

    fun actualizarSolicitud(solicitud: Solicitud) {
        _solicitudes.value = _solicitudes.value.map {
            if (it.idSolicitud == solicitud.idSolicitud) solicitud else it
        }
    }

    fun eliminarSolicitud(idSolicitud: Int) {
        _solicitudes.value = _solicitudes.value.filter { it.idSolicitud != idSolicitud }
    }

    fun cambiarEstado(idSolicitud: Int, nuevoEstado: EstadoSolicitud) {
        _solicitudes.value = _solicitudes.value.map { solicitud ->
            if (solicitud.idSolicitud == idSolicitud) {
                solicitud.copy(estado = nuevoEstado)
            } else {
                solicitud
            }
        }
    }

    fun getSolicitudesPorEstado(estado: EstadoSolicitud): List<Solicitud> {
        return _solicitudes.value.filter { it.estado == estado }
    }

    companion object {
        @Volatile
        private var instance: SolicitudRepository? = null

        fun getInstance(): SolicitudRepository {
            return instance ?: synchronized(this) {
                instance ?: SolicitudRepository().also { instance = it }
            }
        }
    }
}