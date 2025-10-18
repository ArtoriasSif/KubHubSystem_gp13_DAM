package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.*
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleSolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleSolicitudDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(detalle: DetalleSolicitudEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(detalles: List<DetalleSolicitudEntity>)

    @Update
    suspend fun actualizar(detalle: DetalleSolicitudEntity)

    @Delete
    suspend fun eliminar(detalle: DetalleSolicitudEntity)

    @Query("SELECT * FROM detalle_solicitud WHERE idSolicitud = :idSolicitud")
    suspend fun obtenerPorSolicitud(idSolicitud: Int): List<DetalleSolicitudEntity>

    @Query("SELECT * FROM detalle_solicitud WHERE idSolicitud = :idSolicitud")
    fun observarPorSolicitud(idSolicitud: Int): Flow<List<DetalleSolicitudEntity>>

    @Query("DELETE FROM detalle_solicitud WHERE idSolicitud = :idSolicitud")
    suspend fun eliminarPorSolicitud(idSolicitud: Int)

    @Query("DELETE FROM detalle_solicitud")
    suspend fun eliminarTodos()
}