package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.*
import com.example.kubhubsystem_gp13_dam.local.entities.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: PedidoEntity): Long

    @Update
    suspend fun actualizar(pedido: PedidoEntity)

    @Delete
    suspend fun eliminar(pedido: PedidoEntity)

    @Query("SELECT * FROM pedido WHERE idPedido = :id")
    suspend fun obtenerPorId(id: Int): PedidoEntity?

    @Query("SELECT * FROM pedido WHERE estaActivo = 1 LIMIT 1")
    suspend fun obtenerPedidoActivo(): PedidoEntity?

    @Query("SELECT * FROM pedido WHERE estaActivo = 1 LIMIT 1")
    fun observarPedidoActivo(): Flow<PedidoEntity?>

    @Query("SELECT * FROM pedido WHERE estaActivo = 0 ORDER BY fechaCreacion DESC")
    fun observarPedidosAnteriores(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedido ORDER BY fechaCreacion DESC")
    fun observarTodos(): Flow<List<PedidoEntity>>

    @Query("UPDATE pedido SET idEstadoPedido = :nuevoEstadoId WHERE idPedido = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstadoId: Int)

    @Query("UPDATE pedido SET estaActivo = 0 WHERE idPedido = :id")
    suspend fun desactivarPedido(id: Int)

    @Query("DELETE FROM pedido")
    suspend fun eliminarTodos()
}