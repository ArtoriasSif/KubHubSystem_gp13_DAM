package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.ReservaSalaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaSalaDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReserva(reserva: ReservaSalaEntity): Long

    @Update
    suspend fun actualizarReserva(reserva: ReservaSalaEntity)

    @Delete
    suspend fun eliminarReserva(reserva: ReservaSalaEntity)

    @Query("SELECT * FROM reserva_sala WHERE idReservaSala = :idReserva")
    suspend fun obtenerReservaPorId(idReserva: Int): ReservaSalaEntity?

    @Query("SELECT * FROM reserva_sala")
    fun obtenerTodasLasReservas(): Flow<List<ReservaSalaEntity>>

    @Query("SELECT * FROM reserva_sala WHERE idSeccion = :idSeccion")
    suspend fun obtenerReservasPorSeccion(idSeccion: Int): List<ReservaSalaEntity>

    @Query("SELECT * FROM reserva_sala WHERE idSala = :idSala")
    suspend fun obtenerReservasPorSala(idSala: Int): List<ReservaSalaEntity>

    @Query("SELECT * FROM reserva_sala WHERE diaSemana = :dia AND bloque = :bloque")
    suspend fun obtenerReservasPorDiaYBloque(dia: String, bloque: Int): List<ReservaSalaEntity>

    @Query("SELECT COUNT(*) FROM reserva_sala WHERE idSala = :idSala AND diaSemana = :dia AND bloque = :bloque")
    suspend fun verificarDisponibilidad(idSala: Int, dia: String, bloque: Int): Int

    @Query("DELETE FROM reserva_sala WHERE idReservaSala = :idReserva")
    suspend fun eliminarReservaPorId(idReserva: Int)

    @Query("SELECT COUNT(*) FROM reserva_sala WHERE idReservaSala = :idReserva")
    suspend fun existeReserva(idReserva: Int): Int
}
