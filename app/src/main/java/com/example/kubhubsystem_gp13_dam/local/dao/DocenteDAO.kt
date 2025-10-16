package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.DocenteEntity

@Dao
interface DocenteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(docente: DocenteEntity): Long

    @Update
    suspend fun actualizar(docente: DocenteEntity): Int

    @Delete
    suspend fun eliminar(docente: DocenteEntity): Int

    @Query("DELETE FROM docente WHERE idDocente = :id")
    suspend fun eliminarPorId(id: Int): Int

    @Query("SELECT * FROM docente WHERE idDocente = :id")
    suspend fun obtenerPorId(id: Int): DocenteEntity?

    @Query("SELECT * FROM docente WHERE idUsuario = :idUsuario")
    suspend fun obtenerPorIdUsuario(idUsuario: Int): DocenteEntity?

    @Query("SELECT * FROM docente")
    suspend fun obtenerTodos(): List<DocenteEntity>

    @Query("SELECT * FROM docente WHERE :idSeccion IN (seccionesIds)")
    suspend fun obtenerPorIdSeccion(idSeccion: Int): List<DocenteEntity>
}