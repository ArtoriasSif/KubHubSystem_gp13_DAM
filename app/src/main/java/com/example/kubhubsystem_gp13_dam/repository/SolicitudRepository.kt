package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.*
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleSolicitudEntity
import com.example.kubhubsystem_gp13_dam.local.entities.SolicitudEntity
import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestión de Solicitudes
 * ✅ ACTUALIZADO: Compatible con modelo Usuario del backend
 * ⚠️ TEMPORAL: Aún usa DAOs locales (migrar gradualmente al backend)
 */
class SolicitudRepository(
    private val solicitudDao: SolicitudDAO,
    private val detalleSolicitudDao: DetalleSolicitudDAO,
    private val usuarioDao: UsuarioDao,
    private val seccionDao: SeccionDAO,
    private val reservaSalaDao: ReservaSalaDAO,
    private val productoDao: ProductoDAO,
    private val asignaturaDao: AsignaturaDAO,
    private val salaDao: SalaDAO
) {

    // ============================================
    // MAPPERS: Entity -> Domain
    // ============================================

    /**
     * Convierte SolicitudEntity a Solicitud (modelo de dominio)
     * ✅ ACTUALIZADO: Usa atributos correctos del modelo Usuario del backend
     */
    private suspend fun SolicitudEntity.toDomain(): Solicitud {
        // Obtener usuario gestor
        val usuarioEntity = usuarioDao.obtenerPorId(idUsuario)
        val gestorPedidos = usuarioEntity?.let {
            Usuario(
                idUsuario = it.idUsuario,
                rol = Rol2.desdeId(it.idRol) ?: Rol2.GESTOR_PEDIDOS,
                primerNombre = it.primeroNombre,      // ✅ Corregido
                segundoNombre = it.segundoNombre,     // ✅ Nullable
                apellidoPaterno = it.apellidoPaterno, // ✅ Nullable
                apellidoMaterno = it.apellidoMaterno, // ✅ Nullable
                email = it.email,
                username = it.username,               // ✅ Nullable
                password = it.password ?: "",
                activo = true                         // ✅ Agregado
            )
        } ?: Usuario(
            idUsuario = 0,
            rol = Rol2.GESTOR_PEDIDOS,
            primerNombre = "Desconocido",             // ✅ Corregido
            segundoNombre = null,                     // ✅ Nullable
            apellidoPaterno = null,                   // ✅ Nullable
            apellidoMaterno = null,                   // ✅ Nullable
            email = "",
            username = null,                          // ✅ Nullable
            password = "",
            activo = false                            // ✅ Agregado
        )

        // Obtener sección
        val seccionEntity = seccionDao.obtenerSeccionPorId(idSeccion)
        val docenteEntity = seccionEntity?.idDocente?.let { usuarioDao.obtenerPorId(it) }
        val seccion = seccionEntity?.let {
            Seccion(
                idSeccion = it.idSeccion,
                nombreSeccion = it.nombreSeccion,
                idDocente = it.idDocente,
                nombreDocente = docenteEntity?.let { d ->
                    "${d.primeroNombre} ${d.apellidoPaterno ?: ""}"  // ✅ Corregido
                } ?: ""
            )
        } ?: Seccion(idSeccion = 0, nombreSeccion = "Sin sección")

        // Obtener docente de la sección
        val docenteSeccion = docenteEntity?.let {
            Usuario(
                idUsuario = it.idUsuario,
                rol = Rol2.desdeId(it.idRol) ?: Rol2.DOCENTE,
                primerNombre = it.primeroNombre,       // ✅ Corregido
                segundoNombre = it.segundoNombre,     // ✅ Nullable
                apellidoPaterno = it.apellidoPaterno, // ✅ Nullable
                apellidoMaterno = it.apellidoMaterno, // ✅ Nullable
                email = it.email,
                username = it.username,               // ✅ Nullable
                password = it.password ?: "",
                activo = true                         // ✅ Agregado
            )
        } ?: Usuario(
            idUsuario = 0,
            rol = Rol2.DOCENTE,
            primerNombre = "Sin",                     // ✅ Corregido
            segundoNombre = null,                     // ✅ Nullable
            apellidoPaterno = "Docente",              // ✅ Nullable
            apellidoMaterno = null,                   // ✅ Nullable
            email = "",
            username = null,                          // ✅ Nullable
            password = "",
            activo = false                            // ✅ Agregado
        )

        // Obtener reserva de sala
        val reservaEntity = reservaSalaDao.obtenerPorId(idReservaSala)
        val salaEntity = reservaEntity?.let { salaDao.obtenerSalaPorId(it.idSala) }
        val asignaturaEntity = seccionEntity?.let { asignaturaDao.obtenerAsignaturaPorId(it.idAsignatura) }

        val reservaSala = reservaEntity?.let {
            ReservaSala(
                idReservaSala = it.idReservaSala,
                seccion = seccion,
                asignatura = asignaturaEntity?.let { asig ->
                    Asignatura(
                        idAsignatura = asig.idAsignatura,
                        nombreAsignatura = asig.nombreAsignatura,
                        codigoAsignatura = asig.codigoAsignatura,
                        periodo = ""
                    )
                } ?: Asignatura(0, "", "", ""),
                sala = salaEntity?.let { s -> Sala(s.idSala, s.codigoSala) } ?: Sala(0, ""),
                diaSemana = DiaSemana.valueOf(it.diaSemana),
                bloqueHorario = it.bloque
            )
        } ?: ReservaSala(
            idReservaSala = 0,
            seccion = seccion,
            asignatura = Asignatura(0, "", "", ""),
            sala = Sala(0, ""),
            diaSemana = DiaSemana.LUNES,
            bloqueHorario = 1
        )

        // Obtener detalles de la solicitud
        val detallesEntity = detalleSolicitudDao.obtenerPorSolicitud(idSolicitud)
        val detalles = detallesEntity.map { detalle ->
            val producto = productoDao.obtenerPorId(detalle.idProducto)?.let { p ->
                Producto(
                    idProducto = p.idProducto,
                    nombreProducto = p.nombreProducto,
                    categoria = p.categoria,
                    unidadMedida = p.unidad
                )
            } ?: Producto(0, "Desconocido", "", "")

            DetalleSolicitud(
                idDetalleSolicitud = detalle.idDetalleSolicitud,
                idSolicitud = detalle.idSolicitud,
                producto = producto,
                cantidadUnidadMedida = detalle.cantidaUnidadMedida
            )
        }

        return Solicitud(
            idSolicitud = idSolicitud,
            detalleSolicitud = detalles,
            gestorPedidos = gestorPedidos,
            seccion = seccion,
            docenteSeccion = docenteSeccion,
            reservaSala = reservaSala,
            cantidadPersonas = cantidadPersonas,
            fechaSolicitud = fechaSolicitudPlanificada,
            fechaCreacion = fechaCreacion,
            estado = estadoSolicitud
        )
    }

    /**
     * Convierte Solicitud (modelo de dominio) a SolicitudEntity
     */
    private fun Solicitud.toEntity(): SolicitudEntity {
        return SolicitudEntity(
            idSolicitud = idSolicitud,
            idUsuario = gestorPedidos.idUsuario,
            idSeccion = seccion.idSeccion,
            idReservaSala = reservaSala.idReservaSala,
            cantidadPersonas = cantidadPersonas,
            estadoSolicitud = estado,
            fechaSolicitudPlanificada = fechaSolicitud,
            fechaCreacion = fechaCreacion
        )
    }

    // ============================================
    // OPERACIONES CRUD
    // ============================================

    /**
     * Crea una nueva solicitud con sus detalles
     */
    suspend fun crearSolicitud(solicitud: Solicitud): Long {
        val idSolicitud = solicitudDao.insertar(solicitud.toEntity())

        // Insertar detalles
        val detalles = solicitud.detalleSolicitud.map { detalle ->
            DetalleSolicitudEntity(
                idDetalleSolicitud = 0,
                idSolicitud = idSolicitud.toInt(),
                idProducto = detalle.producto.idProducto,
                cantidaUnidadMedida = detalle.cantidadUnidadMedida
            )
        }
        detalleSolicitudDao.insertarVarios(detalles)

        return idSolicitud
    }

    /**
     * Obtiene una solicitud por su ID
     */
    suspend fun obtenerSolicitud(idSolicitud: Int): Solicitud? {
        val entity = solicitudDao.obtenerPorId(idSolicitud) ?: return null
        return entity.toDomain()
    }

    /**
     * Observa todas las solicitudes (Flow reactivo)
     */
    fun observarTodasSolicitudes(): Flow<List<Solicitud>> {
        return solicitudDao.observarTodas().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Observa solicitudes por estado (Flow reactivo)
     */
    fun observarSolicitudesPorEstado(estado: String): Flow<List<Solicitud>> {
        return solicitudDao.observarPorEstado(estado).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Actualiza el estado de una solicitud
     */
    suspend fun actualizarEstadoSolicitud(idSolicitud: Int, nuevoEstado: String) {
        solicitudDao.actualizarEstado(idSolicitud, nuevoEstado)
    }

    /**
     * Actualiza una solicitud completa con sus detalles
     */
    suspend fun actualizarSolicitud(solicitud: Solicitud) {
        solicitudDao.actualizar(solicitud.toEntity())

        // Actualizar detalles (eliminar y reinsertar)
        detalleSolicitudDao.eliminarPorSolicitud(solicitud.idSolicitud)
        val detalles = solicitud.detalleSolicitud.map { detalle ->
            DetalleSolicitudEntity(
                idDetalleSolicitud = 0,
                idSolicitud = solicitud.idSolicitud,
                idProducto = detalle.producto.idProducto,
                cantidaUnidadMedida = detalle.cantidadUnidadMedida
            )
        }
        detalleSolicitudDao.insertarVarios(detalles)
    }

    /**
     * Elimina una solicitud y sus detalles
     */
    suspend fun eliminarSolicitud(solicitud: Solicitud) {
        detalleSolicitudDao.eliminarPorSolicitud(solicitud.idSolicitud)
        solicitudDao.eliminar(solicitud.toEntity())
    }

    /**
     * Cuenta solicitudes por estado
     */
    suspend fun contarSolicitudesPorEstado(estado: String): Int {
        return solicitudDao.contarPorEstado(estado)
    }
}