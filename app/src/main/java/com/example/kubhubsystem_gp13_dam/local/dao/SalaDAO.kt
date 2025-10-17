package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.SalaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSala(sala: SalaEntity): Long

    @Update
    suspend fun actualizarSala(sala: SalaEntity)

    @Delete
    suspend fun eliminarSala(sala: SalaEntity)

    @Query("SELECT * FROM sala WHERE idSala = :idSala")
    suspend fun obtenerSalaPorId(idSala: Int): SalaEntity?

    @Query("SELECT * FROM sala WHERE codigoSala = :codigo")
    suspend fun obtenerSalaPorCodigo(codigo: String): SalaEntity?

    @Query("SELECT * FROM sala")
    fun obtenerTodasLasSalas(): Flow<List<SalaEntity>>  // ✅ Esto debe retornar Flow

    @Query("SELECT * FROM sala WHERE codigoSala LIKE '%' || :codigo || '%'")
    suspend fun buscarSalasPorCodigo(codigo: String): List<SalaEntity>

    @Query("DELETE FROM sala WHERE idSala = :idSala")
    suspend fun eliminarSalaPorId(idSala: Int)
}