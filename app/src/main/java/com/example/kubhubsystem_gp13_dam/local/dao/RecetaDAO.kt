package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.RecetaEntity

@Dao
interface RecetaDAO {

    @Query("SELECT * FROM receta ORDER BY nombreReceta ASC")
    fun observarTodas(): Flow<List<RecetaEntity>>

    @Query("SELECT * FROM receta WHERE idReceta = :id")
    suspend fun obtenerPorId(id: Int): RecetaEntity?

    @Query("SELECT * FROM receta WHERE categoriaReceta = :categoria")
    fun obtenerPorCategoria(categoria: String): Flow<List<RecetaEntity>>

    // ✅ Query para obtener categorías únicas dinámicamente
    @Query("SELECT DISTINCT categoriaReceta FROM receta ORDER BY categoriaReceta ASC")
    fun obtenerCategorias(): Flow<List<String>>

    @Query("SELECT * FROM receta WHERE nombreReceta LIKE '%' || :query || '%'")
    fun buscarPorNombre(query: String): Flow<List<RecetaEntity>>

    @Query("SELECT COUNT(*) FROM receta WHERE idReceta = :id")
    suspend fun existeReceta(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(receta: RecetaEntity): Long

    @Update
    suspend fun actualizar(receta: RecetaEntity)

    @Delete
    suspend fun eliminar(receta: RecetaEntity)

    @Query("DELETE FROM receta")
    suspend fun eliminarTodas()
}