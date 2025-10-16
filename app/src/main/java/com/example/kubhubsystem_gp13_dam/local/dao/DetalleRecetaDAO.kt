package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleRecetaEntity
import com.example.kubhubsystem_gp13_dam.model.DetalleConProducto

@Dao
interface DetalleRecetaDAO {

    @Query("SELECT * FROM detalle_receta WHERE idReceta = :idReceta")
    fun observarDetallesPorReceta(idReceta: Int): Flow<List<DetalleRecetaEntity>>

    @Query("SELECT * FROM detalle_receta WHERE idReceta = :idReceta")
    suspend fun obtenerDetallesPorReceta(idReceta: Int): List<DetalleRecetaEntity>

    // ✅ JOIN optimizado para obtener detalles CON información del producto
    @Query("""
        SELECT 
            dr.idDetalleReceta,
            dr.idReceta,
            dr.idProducto,
            dr.cantidaUnidadMedida,
            p.nombreProducto,
            p.unidad,
            p.categoria
        FROM detalle_receta dr
        INNER JOIN producto p ON dr.idProducto = p.idProducto
        WHERE dr.idReceta = :idReceta
    """)
    suspend fun obtenerDetallesConProducto(idReceta: Int): List<DetalleConProducto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(detalle: DetalleRecetaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(detalles: List<DetalleRecetaEntity>)

    @Update
    suspend fun actualizar(detalle: DetalleRecetaEntity)

    @Delete
    suspend fun eliminar(detalle: DetalleRecetaEntity)

    @Query("DELETE FROM detalle_receta WHERE idReceta = :idReceta")
    suspend fun eliminarPorReceta(idReceta: Int)

    @Query("DELETE FROM detalle_receta WHERE idDetalleReceta = :idDetalle")
    suspend fun eliminarPorId(idDetalle: Int)

    @Query("SELECT COUNT(*) FROM detalle_receta WHERE idReceta = :idReceta")
    suspend fun contarDetalles(idReceta: Int): Int
}

