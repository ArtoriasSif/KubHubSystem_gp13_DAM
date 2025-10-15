package com.example.kubhubsystem_gp13_dam.repository

import com.example.kubhubsystem_gp13_dam.local.dao.InventarioDAO
import com.example.kubhubsystem_gp13_dam.local.dao.MovimientoDAO
import com.example.kubhubsystem_gp13_dam.local.entities.MovimientoEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class MovimientoRepository(
    private val movimientoDao: MovimientoDAO,
    private val inventarioDao: InventarioDAO
) {

    fun observarMovimientos(): Flow<List<MovimientoEntity>> = movimientoDao.observarTodos()

    suspend fun obtenerMovimiento(idMovimiento: Int) = movimientoDao.obtenerPorId(idMovimiento)

    /**
     * Registra un movimiento de inventario (entrada o salida) y actualiza el stock automáticamente
     * @param idInventario ID del inventario afectado
     * @param cantidadMovimiento cantidad del movimiento (positiva para entrada, negativa para salida)
     * @param tipoMovimiento "ENTRADA" o "SALIDA"
     */
    suspend fun registrarMovimiento(
        idInventario: Int,
        cantidadMovimiento: Double,
        tipoMovimiento: String
    ) {
        // Validar que el inventario existe
        val inventario = inventarioDao.obtenerPorId(idInventario)
            ?: throw IllegalArgumentException("Inventario con ID $idInventario no encontrado")

        // Calcular nuevo stock
        val nuevoStock = when (tipoMovimiento.uppercase()) {
            "ENTRADA" -> inventario.stock + cantidadMovimiento
            "SALIDA" -> {
                if (inventario.stock < cantidadMovimiento) {
                    throw IllegalArgumentException("Stock insuficiente. Stock actual: ${inventario.stock}, Cantidad solicitada: $cantidadMovimiento")
                }
                inventario.stock - cantidadMovimiento
            }
            else -> throw IllegalArgumentException("Tipo de movimiento inválido. Use 'ENTRADA' o 'SALIDA'")
        }

        // Registrar el movimiento con fecha actual
        movimientoDao.insertar(
            MovimientoEntity(
                idInventario = idInventario,
                fechaMovimiento = LocalDateTime.now(), // Captura automática de fecha
                cantidadeMovimiento = cantidadMovimiento,
                tipoMovimiento = tipoMovimiento.uppercase()
            )
        )

        // Actualizar el stock en inventario
        inventarioDao.actualizarStock(idInventario, nuevoStock)
    }

    /**
     * Método alternativo para actualizar movimientos existentes
     */
    suspend fun guardarMovimiento(
        idMovimiento: Int?,
        idInventario: Int,
        fechaMovimiento: LocalDateTime,
        cantidadeMovimiento: Double,
        tipoMovimiento: String
    ) {
        if (idMovimiento == null || idMovimiento == 0) {
            movimientoDao.insertar(
                MovimientoEntity(
                    idInventario = idInventario,
                    fechaMovimiento = fechaMovimiento,
                    cantidadeMovimiento = cantidadeMovimiento,
                    tipoMovimiento = tipoMovimiento
                )
            )
        } else {
            movimientoDao.actualizar(
                MovimientoEntity(
                    idMovimiento = idMovimiento,
                    idInventario = idInventario,
                    fechaMovimiento = fechaMovimiento,
                    cantidadeMovimiento = cantidadeMovimiento,
                    tipoMovimiento = tipoMovimiento
                )
            )
        }
    }

    suspend fun actualizarMovimiento(movimiento: MovimientoEntity) = movimientoDao.actualizar(movimiento)

    suspend fun eliminarMovimiento(movimiento: MovimientoEntity) = movimientoDao.eliminar(movimiento)

    suspend fun eliminarTodosMovimiento() = movimientoDao.eliminarTodos()
}