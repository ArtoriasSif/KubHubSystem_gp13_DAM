package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.*
import com.example.kubhubsystem_gp13_dam.local.entities.EstadoPedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadoPedidoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(estado: EstadoPedidoEntity): Long

    @Query("SELECT * FROM estado_pedido WHERE idEstadoPedido = :id")
    suspend fun obtenerPorId(id: Int): EstadoPedidoEntity?

    @Query("SELECT * FROM estado_pedido")
    fun observarTodos(): Flow<List<EstadoPedidoEntity>>

    @Query("SELECT COUNT(*) FROM estado_pedido")
    suspend fun contarEstados(): Int

    @Query("DELETE FROM estado_pedido")
    suspend fun eliminarTodos()
}