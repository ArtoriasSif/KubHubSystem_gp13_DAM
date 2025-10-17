package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDAO {

    @Query("SELECT * FROM producto ORDER BY nombreProducto DESC, idProducto DESC")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM producto WHERE categoria = :categoria")
    fun obtenerPorCategoria(categoria: String): Flow<List<ProductoEntity>>

    @Query("SELECT DISTINCT categoria FROM producto ORDER BY categoria ASC")
    fun obtenerCategorias(): Flow<List<String>>

    @Query("UPDATE producto SET nombreProducto = :nuevoNombre WHERE idProducto=:id  ")
    fun actualizarNombreProducto(id: Int,nuevoNombre: String, )

    @Query("SELECT * FROM producto WHERE idProducto IN (:ids)")
    suspend fun obtenerPorIds(ids: List<Int>): List<ProductoEntity>

    @Query("SELECT * FROM producto WHERE nombreProducto = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): ProductoEntity?

    // ✅ CORREGIDO: Ahora es suspend para no bloquear el hilo principal
    @Query("SELECT nombreProducto FROM producto WHERE idProducto = :id")
    suspend fun buscarNombrePorId(id: Int): String?

    @Query("SELECT * FROM producto WHERE idProducto = :id")
    suspend fun obtenerPorId(id: Int): ProductoEntity?

    @Query("SELECT COUNT(*) FROM producto WHERE idProducto = :id")
    suspend fun existeProducto(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertar(expense: ProductoEntity): Long

    @Update
    suspend fun actualizar(expense: ProductoEntity)

    @Delete
    suspend fun eliminar(expense: ProductoEntity)

    @Query("DELETE FROM producto")
    suspend fun eliminarTodos()
}