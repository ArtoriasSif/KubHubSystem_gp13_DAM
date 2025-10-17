package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.InventarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDAO {

    @Query("SELECT COUNT(*) FROM inventario WHERE idInventario = :id")
    suspend fun existeInventario(id: Int): Int

    @Query("SELECT * FROM inventario ")
    fun observarTodos(): Flow<List<InventarioEntity>>

    @Query("SELECT * FROM inventario WHERE idInventario = :id")
    suspend fun obtenerPorId(id: Int): InventarioEntity?

    @Query("SELECT * FROM inventario WHERE idProducto = :idProducto LIMIT 1")
    suspend fun obtenerPorProducto(idProducto: Int): InventarioEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertar(expense: InventarioEntity): Long

    @Query("UPDATE inventario SET stock = :nuevoStock WHERE idInventario = :id")
    suspend fun actualizarStock(id: Int, nuevoStock: Double)

    @Update
    suspend fun actualizar(expense: InventarioEntity)

    @Delete
    suspend fun eliminar(expense: InventarioEntity)

    @Query("DELETE FROM inventario")
    suspend fun eliminarTodos()




}