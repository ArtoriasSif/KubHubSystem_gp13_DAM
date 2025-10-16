package com.example.kubhubsystem_gp13_dam.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import androidx.room.Update
import com.example.kubhubsystem_gp13_dam.local.entities.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity): Int

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity): Int

    @Query("DELETE FROM usuario WHERE idUsuario = :id")
    suspend fun eliminarPorId(id: Int): Int

    @Query("SELECT * FROM usuario WHERE idUsuario = :id")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE username = :usuario AND password = :contraseña")
    suspend fun iniciarSesion(usuario: String, contraseña: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE email = :correo")
    suspend fun obtenerPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuario")
    suspend fun obtenerTodos(): List<UsuarioEntity>

    @Query("SELECT * FROM usuario WHERE idRol = :idRol")
    suspend fun obtenerPorRol(idRol: Int): List<UsuarioEntity>
}