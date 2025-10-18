package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.*
import com.example.kubhubsystem_gp13_dam.local.entities.SolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity)

    @Query("SELECT * FROM solicitud WHERE idSolicitud = :id")
    suspend fun obtenerPorId(id: Int): SolicitudEntity?

    @Query("SELECT * FROM solicitud")
    fun observarTodas(): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitud WHERE estadoSolicitud = :estado")
    fun observarPorEstado(estado: String): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitud WHERE idSeccion = :idSeccion")
    fun observarPorSeccion(idSeccion: Int): Flow<List<SolicitudEntity>>

    @Query("UPDATE solicitud SET estadoSolicitud = :nuevoEstado WHERE idSolicitud = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)

    @Query("SELECT COUNT(*) FROM solicitud WHERE estadoSolicitud = :estado")
    suspend fun contarPorEstado(estado: String): Int

    @Query("DELETE FROM solicitud")
    suspend fun eliminarTodas()
}