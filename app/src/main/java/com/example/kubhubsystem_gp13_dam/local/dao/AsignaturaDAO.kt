package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.AsignaturaEntity
import com.example.kubhubsystem_gp13_dam.model.Seccion

@Dao
interface AsignaturaDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignatura(asignatura: AsignaturaEntity): Long

    @Update
    suspend fun actualizarAsignatura(asignatura: AsignaturaEntity)

    @Delete
    suspend fun eliminarAsignatura(asignatura: AsignaturaEntity)

    @Query("SELECT * FROM asignatura WHERE idAsignatura = :idAsignatura")
    suspend fun obtenerAsignaturaPorId(idAsignatura: Int): AsignaturaEntity?

    @Query("SELECT * FROM asignatura WHERE codigoAsignatura = :codigo")
    suspend fun obtenerAsignaturaPorCodigo(codigo: String): AsignaturaEntity?

    @Query("SELECT * FROM asignatura")
    fun obtenerTodasLasAsignaturas(): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM asignatura WHERE nombreAsignatura LIKE '%' || :nombre || '%'")
    suspend fun buscarAsignaturasPorNombre(nombre: String): List<AsignaturaEntity>

    @Query("DELETE FROM asignatura WHERE idAsignatura = :idAsignatura")
    suspend fun eliminarAsignaturaPorId(idAsignatura: Int)

    @Query("SELECT COUNT(*) FROM asignatura WHERE idAsignatura = :idAsignatura")
    suspend fun existeAsignatura(idAsignatura: Int): Int

    @Query("SELECT * FROM asignatura")
    fun observarTodas(): Flow<List<AsignaturaEntity>>

    @Query("SELECT a.* FROM asignatura a JOIN seccion s ON a.idAsignatura = s.idAsignatura where s.idSeccion= :idSeccion")
    suspend fun findAsignaturaByidSeccion(idSeccion: Int): AsignaturaEntity?
}