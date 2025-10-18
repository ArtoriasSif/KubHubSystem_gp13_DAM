package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.*
import com.example.kubhubsystem_gp13_dam.local.entities.AglomeradoPedidoEntity
import com.example.kubhubsystem_gp13_dam.local.entities.PedidoEntity
import com.example.kubhubsystem_gp13_dam.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class PedidoRepository(
    private val pedidoDao: PedidoDAO,
    private val aglomeradoPedidoDao: AglomeradoPedidoDAO,
    private val solicitudDao: SolicitudDAO,
    private val detalleSolicitudDao: DetalleSolicitudDAO,
    private val estadoPedidoDao: EstadoPedidoDAO,
    private val productoDao: ProductoDAO,
    private val asignaturaDao: AsignaturaDAO,
    private val solicitudRepository: SolicitudRepository
) {

    // ============================================
    // MAPPERS: Entity -> Domain
    // ============================================

    private suspend fun PedidoEntity.toDomain(): Pedido {
        // Obtener estado
        val estadoEntity = estadoPedidoDao.obtenerPorId(idEstadoPedido)
        val estadoPedido = estadoEntity?.let {
            EstadoPedido.desdeNombre(it.tipoEstado) ?: EstadoPedido.EN_PROCESO
        } ?: EstadoPedido.EN_PROCESO

        // Obtener solicitudes del pedido
        val solicitudesEntity = solicitudDao.obtenerPorId(idPedido) // Necesitarás ajustar esto
        val solicitudes = emptyList<Solicitud>() // Por ahora vacío, luego implementarás la relación

        // Obtener aglomerado
        val aglomeradosEntity = aglomeradoPedidoDao.obtenerPorPedido(idPedido)
        val aglomerado = aglomeradosEntity.map { it.toDomain() }

        return Pedido(
            idPedido = idPedido,
            fechaInicioRango = fechaInicioRango,
            fechaFinRango = fechaFinRango,
            fechaCreacion = fechaCreacion,
            estadoPedido = estadoPedido,
            solicitudes = solicitudes,
            aglomerado = aglomerado,
            estaActivo = estaActivo
        )
    }

    private suspend fun AglomeradoPedidoEntity.toDomain(): AglomeradoPedido {
        val productoEntity = productoDao.obtenerPorId(idProducto)
        val producto = productoEntity?.let {
            Producto(
                idProducto = it.idProducto,
                nombreProducto = it.nombreProducto,
                categoria = it.categoria,
                unidadMedida = it.unidad
            )
        } ?: Producto(0, "Desconocido", "", "")

        val asignatura = idAsignatura?.let { id ->
            asignaturaDao.obtenerAsignaturaPorId(id)?.let { asig ->
                Asignatura(
                    idAsignatura = asig.idAsignatura,
                    nombreAsignatura = asig.nombreAsignatura,
                    codigoAsignatura = asig.codigoAsignatura,
                    periodo = ""
                )
            }
        }

        return AglomeradoPedido(
            idAglomerado = idAglomerado,
            producto = producto,
            cantidadTotal = cantidadTotal,
            asignatura = asignatura
        )
    }

    private fun Pedido.toEntity(): PedidoEntity {
        return PedidoEntity(
            idPedido = idPedido,
            fechaInicioRango = fechaInicioRango,
            fechaFinRango = fechaFinRango,
            fechaCreacion = fechaCreacion,
            idEstadoPedido = estadoPedido.orden,
            estaActivo = estaActivo
        )
    }

    // ============================================
    // OPERACIONES CRUD
    // ============================================

    suspend fun inicializarEstadosPedido() {
        // Verificar si ya existen los estados
        val existentes = estadoPedidoDao.contarEstados()
        if (existentes == 0) {
            EstadoPedido.values().forEach { estado ->
                estadoPedidoDao.insertar(
                    com.example.kubhubsystem_gp13_dam.local.entities.EstadoPedidoEntity(
                        idEstadoPedido = estado.orden,
                        tipoEstado = estado.name
                    )
                )
            }
        }
    }

    suspend fun crearPedido(fechaInicio: LocalDateTime, fechaFin: LocalDateTime): Long {
        // Desactivar pedido anterior si existe
        val pedidoActivo = pedidoDao.obtenerPedidoActivo()
        pedidoActivo?.let {
            pedidoDao.desactivarPedido(it.idPedido)
        }

        // Crear nuevo pedido
        val nuevoPedido = PedidoEntity(
            idPedido = 0,
            fechaInicioRango = fechaInicio,
            fechaFinRango = fechaFin,
            fechaCreacion = LocalDateTime.now(),
            idEstadoPedido = EstadoPedido.EN_PROCESO.orden,
            estaActivo = true
        )

        return pedidoDao.insertar(nuevoPedido)
    }

    suspend fun obtenerPedidoActivo(): Pedido? {
        val entity = pedidoDao.obtenerPedidoActivo() ?: return null
        return entity.toDomain()
    }

    fun observarPedidoActivo(): Flow<Pedido?> {
        return pedidoDao.observarPedidoActivo().map { entity ->
            entity?.toDomain()
        }
    }

    fun observarPedidosAnteriores(): Flow<List<Pedido>> {
        return pedidoDao.observarPedidosAnteriores().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun actualizarEstadoPedido(idPedido: Int, nuevoEstado: EstadoPedido) {
        pedidoDao.actualizarEstado(idPedido, nuevoEstado.orden)
    }

    suspend fun recalcularAglomerado(idPedido: Int) {
        // Eliminar aglomerado anterior
        aglomeradoPedidoDao.eliminarPorPedido(idPedido)

        // Obtener todas las solicitudes aprobadas o pendientes
        val todasSolicitudes = solicitudDao.obtenerPorId(idPedido) // Ajustar query
        
        // Agrupar por producto y sumar cantidades
        val aglomeradoMap = mutableMapOf<Int, MutableMap<String, Any>>()

        // Aquí necesitarás iterar sobre las solicitudes y sus detalles
        // Por ahora lo dejo simplificado
        
        // Insertar nuevo aglomerado
        // aglomeradoPedidoDao.insertarVarios(nuevosAglomerados)
    }

    fun observarAglomeradoPorPedido(idPedido: Int): Flow<List<AglomeradoPedido>> {
        return aglomeradoPedidoDao.observarPorPedido(idPedido).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observarAglomeradoPorAsignatura(idPedido: Int, idAsignatura: Int): Flow<List<AglomeradoPedido>> {
        return aglomeradoPedidoDao.observarPorPedidoYAsignatura(idPedido, idAsignatura).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun actualizarCantidadAglomerado(idAglomerado: Int, nuevaCantidad: Double) {
        aglomeradoPedidoDao.actualizarCantidad(idAglomerado, nuevaCantidad)
    }

    suspend fun agregarProductoAglomerado(idPedido: Int, producto: Producto, cantidad: Double, idAsignatura: Int? = null) {
        val nuevoAglomerado = AglomeradoPedidoEntity(
            idAglomerado = 0,
            idPedido = idPedido,
            idProducto = producto.idProducto,
            cantidadTotal = cantidad,
            idAsignatura = idAsignatura
        )
        aglomeradoPedidoDao.insertar(nuevoAglomerado)
    }

    suspend fun calcularProgresoPedido(idPedido: Int): Float {
        val totalSolicitudes = solicitudDao.contarPorEstado("Pendiente") +
                solicitudDao.contarPorEstado("Aprobado") +
                solicitudDao.contarPorEstado("Rechazado")
        
        if (totalSolicitudes == 0) return 0f

        val aprobadas = solicitudDao.contarPorEstado("Aprobado")
        val rechazadas = solicitudDao.contarPorEstado("Rechazado")
        val procesadas = aprobadas + rechazadas

        return procesadas.toFloat() / totalSolicitudes.toFloat()
    }
}