package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.*
import com.example.kubhubsystem_gp13_dam.local.entities.AglomeradoPedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AglomeradoPedidoDAO {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(aglomerado: AglomeradoPedidoEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(aglomerados: List<AglomeradoPedidoEntity>)
    
    @Update
    suspend fun actualizar(aglomerado: AglomeradoPedidoEntity)
    
    @Delete
    suspend fun eliminar(aglomerado: AglomeradoPedidoEntity)
    
    @Query("SELECT * FROM aglomerado_pedido WHERE idPedido = :idPedido")
    suspend fun obtenerPorPedido(idPedido: Int): List<AglomeradoPedidoEntity>
    
    @Query("SELECT * FROM aglomerado_pedido WHERE idPedido = :idPedido")
    fun observarPorPedido(idPedido: Int): Flow<List<AglomeradoPedidoEntity>>
    
    @Query("SELECT * FROM aglomerado_pedido WHERE idPedido = :idPedido AND idAsignatura = :idAsignatura")
    fun observarPorPedidoYAsignatura(idPedido: Int, idAsignatura: Int): Flow<List<AglomeradoPedidoEntity>>
    
    @Query("UPDATE aglomerado_pedido SET cantidadTotal = :nuevaCantidad WHERE idAglomerado = :id")
    suspend fun actualizarCantidad(id: Int, nuevaCantidad: Double)
    
    @Query("DELETE FROM aglomerado_pedido WHERE idPedido = :idPedido")
    suspend fun eliminarPorPedido(idPedido: Int)
    
    @Query("DELETE FROM aglomerado_pedido")
    suspend fun eliminarTodos()
}