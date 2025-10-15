package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDAO {

    @Query("SELECT * FROM movimiento")
    fun observarTodos(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimiento WHERE idMovimiento = :id")
    suspend fun obtenerPorId(id: Int): MovimientoEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertar(expense: MovimientoEntity): Long

    @Update
    suspend fun actualizar(expense: MovimientoEntity)

    @Delete
    suspend fun eliminar(expense: MovimientoEntity)

    @Query("DELETE FROM movimiento")
    suspend fun eliminarTodos()


}