package com.example.kubhubsystem_gp13_dam.local.dao

import androidx.room.*
import com.example.kubhubsystem_gp13_dam.local.entities.ReservaSalaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaSalaDAO {

    // ============================================
    // MÉTODOS DE INSERCIÓN
    // ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(reserva: ReservaSalaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReserva(reserva: ReservaSalaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarias(reservas: List<ReservaSalaEntity>)

    // ============================================
    // MÉTODOS DE ACTUALIZACIÓN
    // ============================================

    @Update
    suspend fun actualizar(reserva: ReservaSalaEntity)

    @Update
    suspend fun actualizarReserva(reserva: ReservaSalaEntity)

    // ============================================
    // MÉTODOS DE ELIMINACIÓN
    // ============================================

    @Delete
    suspend fun eliminar(reserva: ReservaSalaEntity)

    @Delete
    suspend fun eliminarReserva(reserva: ReservaSalaEntity)

    @Query("DELETE FROM reserva_sala")
    suspend fun eliminarTodas()

    // ============================================
    // MÉTODOS DE CONSULTA - BÁSICOS
    // ============================================

    @Query("SELECT * FROM reserva_sala WHERE idReservaSala = :id")
    suspend fun obtenerPorId(id: Int): ReservaSalaEntity?

    @Query("SELECT * FROM reserva_sala WHERE idReservaSala = :idReserva")
    suspend fun obtenerReservaPorId(idReserva: Int): ReservaSalaEntity?

    @Query("SELECT * FROM reserva_sala")
    fun observarTodas(): Flow<List<ReservaSalaEntity>>

    @Query("SELECT * FROM reserva_sala")
    fun obtenerTodasLasReservas(): Flow<List<ReservaSalaEntity>>

    // ============================================
    // MÉTODOS DE CONSULTA - POR SECCIÓN
    // ============================================

    @Query("SELECT * FROM reserva_sala WHERE idSeccion = :idSeccion")
    fun observarPorSeccion(idSeccion: Int): Flow<List<ReservaSalaEntity>>

    @Query("SELECT * FROM reserva_sala WHERE idSeccion = :idSeccion")
    suspend fun obtenerReservasPorSeccion(idSeccion: Int): List<ReservaSalaEntity>
    // ============================================
    // MÉTODOS DE CONSULTA - POR SALA
    // ============================================

    @Query("SELECT * FROM reserva_sala WHERE idSala = :idSala")
    suspend fun obtenerReservasPorSala(idSala: Int): List<ReservaSalaEntity>

    @Query("SELECT * FROM reserva_sala WHERE idSala = :idSala")
    fun observarPorSala(idSala: Int): Flow<List<ReservaSalaEntity>>

    // ============================================
    // MÉTODOS DE CONSULTA - POR DÍA Y BLOQUE
    // ============================================

    @Query("SELECT * FROM reserva_sala WHERE diaSemana = :dia AND bloque = :bloque")
    suspend fun obtenerReservasPorDiaYBloque(dia: String, bloque: Int): List<ReservaSalaEntity>

    @Query("SELECT * FROM reserva_sala WHERE diaSemana = :dia AND bloque = :bloque")
    fun observarPorDiaYBloque(dia: String, bloque: Int): Flow<List<ReservaSalaEntity>>

    // ============================================
    // MÉTODOS DE VERIFICACIÓN
    // ============================================

    /**
     * Verifica si existe una reserva con el ID especificado
     * Retorna 1 si existe, 0 si no existe
     */
    @Query("SELECT COUNT(*) FROM reserva_sala WHERE idReservaSala = :id")
    suspend fun existeReserva(id: Int): Int

    /**
     * Verifica si hay una reserva para una sala específica en un día y bloque
     * Retorna el número de reservas que coinciden (0 = disponible, >0 = ocupado)
     */
    @Query("""
        SELECT COUNT(*) 
        FROM reserva_sala 
        WHERE idSala = :idSala 
        AND diaSemana = :diaSemana 
        AND bloque = :bloque
    """)
    suspend fun verificarDisponibilidad(idSala: Int, diaSemana: String, bloque: Int): Int

    /**
     * Verifica si hay conflicto de horario para una sección en un día y bloque específico
     * (excluyendo la reserva actual si se está editando)
     */
    @Query("""
        SELECT COUNT(*) 
        FROM reserva_sala 
        WHERE idSeccion = :idSeccion 
        AND diaSemana = :diaSemana 
        AND bloque = :bloque
        AND idReservaSala != :idReservaActual
    """)
    suspend fun verificarConflictoSeccion(
        idSeccion: Int,
        diaSemana: String,
        bloque: Int,
        idReservaActual: Int = 0
    ): Int

    // ============================================
    // MÉTODOS DE CONSULTA - AVANZADOS
    // ============================================

    /**
     * Obtiene todas las reservas de un día específico ordenadas por bloque
     */
    @Query("""
        SELECT * FROM reserva_sala 
        WHERE diaSemana = :dia 
        ORDER BY bloque ASC
    """)
    suspend fun obtenerReservasPorDia(dia: String): List<ReservaSalaEntity>

    /**
     * Obtiene todas las reservas de un bloque específico ordenadas por día
     */
    @Query("""
        SELECT * FROM reserva_sala 
        WHERE bloque = :bloque 
        ORDER BY 
            CASE diaSemana
                WHEN 'LUNES' THEN 1
                WHEN 'MARTES' THEN 2
                WHEN 'MIERCOLES' THEN 3
                WHEN 'JUEVES' THEN 4
                WHEN 'VIERNES' THEN 5
                WHEN 'SABADO' THEN 6
                WHEN 'DOMINGO' THEN 7
            END
    """)
    suspend fun obtenerReservasPorBloque(bloque: Int): List<ReservaSalaEntity>

    /**
     * Busca reservas por código de taller
     */
    @Query("SELECT * FROM reserva_sala WHERE codigoTaller LIKE '%' || :query || '%'")
    suspend fun buscarPorCodigoTaller(query: String): List<ReservaSalaEntity>

    /**
     * Obtiene el conteo de reservas por sala
     */
    @Query("""
        SELECT idSala, COUNT(*) as total 
        FROM reserva_sala 
        GROUP BY idSala
    """)
    suspend fun obtenerConteoReservasPorSala(): List<ConteoReservasSala>

    @Query("SELECT * FROM reserva_sala WHERE idSeccion = :idSeccion")
    suspend fun obtenerPorSeccion(idSeccion: Int): List<ReservaSalaEntity>
    /**
     * Obtiene las salas disponibles para un día y bloque específico
     */
    @Query("""
        SELECT s.* FROM sala s
        WHERE s.idSala NOT IN (
            SELECT idSala FROM reserva_sala 
            WHERE diaSemana = :dia AND bloque = :bloque
        )
    """)
    suspend fun obtenerSalasDisponibles(dia: String, bloque: Int): List<com.example.kubhubsystem_gp13_dam.local.entities.SalaEntity>
}

// ============================================
// DATA CLASS PARA CONTEO
// ============================================

data class ConteoReservasSala(
    val idSala: Int,
    val total: Int
)