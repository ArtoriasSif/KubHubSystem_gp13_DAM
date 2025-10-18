package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.SeccionEntity

@Dao
interface SeccionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSeccion(seccion: SeccionEntity): Long

    @Update
    suspend fun actualizarSeccion(seccion: SeccionEntity)

    @Delete
    suspend fun eliminarSeccion(seccion: SeccionEntity)

    @Query("SELECT * FROM seccion WHERE idSeccion = :idSeccion")
    suspend fun obtenerSeccionPorId(idSeccion: Int): SeccionEntity?

    @Query("SELECT * FROM seccion")
    fun obtenerTodasLasSecciones(): Flow<List<SeccionEntity>>

    @Query("SELECT * FROM seccion WHERE idAsignatura = :idAsignatura")
    suspend fun obtenerSeccionesPorAsignatura(idAsignatura: Int): List<SeccionEntity>

    @Query("SELECT * FROM seccion WHERE nombreSeccion LIKE '%' || :nombre || '%'")
    suspend fun buscarSeccionesPorNombre(nombre: String): List<SeccionEntity>

    @Query("SELECT COUNT(*) FROM seccion WHERE idSeccion = :idSeccion")
    suspend fun existeSeccion(idSeccion: Int): Int
    @Query("SELECT * FROM seccion WHERE idAsignatura = :idAsignatura")
    suspend fun obtenerPorAsignatura(idAsignatura: Int): List<SeccionEntity>
}