package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleSolicitudEntity

@Dao
interface DetalleSolicitudDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(detalle: DetalleSolicitudEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(detalles: List<DetalleSolicitudEntity>): List<Long>

    @Update
    suspend fun actualizar(detalle: DetalleSolicitudEntity): Int

    @Delete
    suspend fun eliminar(detalle: DetalleSolicitudEntity): Int

    @Query("DELETE FROM detalle_solicitud WHERE idDetalleSolicitud = :id")
    suspend fun eliminarPorId(id: Int): Int

    @Query("DELETE FROM detalle_solicitud WHERE idSolicitud = :idSolicitud")
    suspend fun eliminarPorSolicitud(idSolicitud: Int): Int

    @Query("SELECT * FROM detalle_solicitud WHERE idDetalleSolicitud = :id")
    suspend fun obtenerPorId(id: Int): DetalleSolicitudEntity?

    @Query("SELECT * FROM detalle_solicitud WHERE idSolicitud = :idSolicitud")
    suspend fun obtenerPorSolicitud(idSolicitud: Int): List<DetalleSolicitudEntity>

    @Query("SELECT * FROM detalle_solicitud WHERE idProducto = :idProducto")
    suspend fun obtenerPorProducto(idProducto: Int): List<DetalleSolicitudEntity>

    @Query("SELECT * FROM detalle_solicitud")
    suspend fun obtenerTodos(): List<DetalleSolicitudEntity>

    @Query("SELECT COUNT(*) FROM detalle_solicitud WHERE idSolicitud = :idSolicitud")
    suspend fun contarPorSolicitud(idSolicitud: Int): Int

    @Query("SELECT SUM(cantidaUnidadMedida) FROM detalle_solicitud WHERE idProducto = :idProducto")
    suspend fun sumarCantidadPorProducto(idProducto: Int): Double?

    @Query("SELECT * FROM detalle_solicitud WHERE idSolicitud = :idSolicitud AND idProducto = :idProducto")
    suspend fun obtenerPorSolicitudYProducto(idSolicitud: Int, idProducto: Int): DetalleSolicitudEntity?
}