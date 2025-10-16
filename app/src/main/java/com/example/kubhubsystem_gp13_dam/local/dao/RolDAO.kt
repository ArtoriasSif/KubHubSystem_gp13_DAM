package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.RolEntity

@Dao
interface RolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rol: RolEntity): Long

    @Update
    suspend fun actualizar(rol: RolEntity): Int

    @Delete
    suspend fun eliminar(rol: RolEntity): Int

    @Query("DELETE FROM rol WHERE idRol = :id")
    suspend fun eliminarPorId(id: Int): Int

    @Query("SELECT * FROM rol WHERE idRol = :id")
    suspend fun obtenerPorId(id: Int): RolEntity?

    @Query("SELECT * FROM rol WHERE nombreRol = :nombre")
    suspend fun obtenerPorNombre(nombre: String): RolEntity?

    @Query("SELECT * FROM rol")
    suspend fun obtenerTodos(): List<RolEntity>
}