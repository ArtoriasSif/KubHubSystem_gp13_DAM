package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.SolicitudEntity

@Dao
interface SolicitudDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity): Int

    @Delete
    suspend fun eliminar(solicitud: SolicitudEntity): Int

    @Query("DELETE FROM solicitud WHERE idSolicitud = :id")
    suspend fun eliminarPorId(id: Int): Int

    @Query("SELECT * FROM solicitud WHERE idSolicitud = :id")
    suspend fun obtenerPorId(id: Int): SolicitudEntity?

    @Query("SELECT * FROM solicitud WHERE idUsuario = :idUsuario")
    suspend fun obtenerPorUsuario(idUsuario: Int): List<SolicitudEntity>

    @Query("SELECT * FROM solicitud WHERE idSeccion = :idSeccion")
    suspend fun obtenerPorSeccion(idSeccion: Int): List<SolicitudEntity>

    @Query("SELECT * FROM solicitud")
    suspend fun obtenerTodas(): List<SolicitudEntity>

    @Query("SELECT COUNT(*) FROM solicitud WHERE idSolicitud = :id")
    suspend fun existeSolicitud(id: Int): Int

    @Query("SELECT * FROM solicitud WHERE idUsuario = :idUsuario AND idSeccion = :idSeccion")
    suspend fun obtenerPorUsuarioYSeccion(idUsuario: Int, idSeccion: Int): List<SolicitudEntity>

    @Query("SELECT * FROM solicitud ORDER BY idSolicitud DESC LIMIT 1")
    suspend fun obtenerUltimaSolicitud(): SolicitudEntity?
}