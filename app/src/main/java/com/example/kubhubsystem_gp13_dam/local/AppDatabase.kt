package com.example.kubhubsystem_gp13_dam.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kubhubsystem_gp13_dam.local.dao.InventarioDAO
import com.example.kubhubsystem_gp13_dam.local.entities.InventarioEntity
import com.example.kubhubsystem_gp13_dam.local.Converters
import com.example.kubhubsystem_gp13_dam.local.dao.AsignaturaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.DetalleRecetaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.DetalleSolicitudDAO
import com.example.kubhubsystem_gp13_dam.local.dao.DocenteDao
import com.example.kubhubsystem_gp13_dam.local.dao.EstadoPedidoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.MovimientoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.PedidoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.PedidoProcesadoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.PedidoSolicitudDAO
import com.example.kubhubsystem_gp13_dam.local.entities.MovimientoEntity
import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.RecetaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.ReservaSalaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.RolDao
import com.example.kubhubsystem_gp13_dam.local.dao.SalaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SeccionDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SolicitudDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SolicitudProcesadaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.UsuarioDao
import com.example.kubhubsystem_gp13_dam.local.entities.AsignaturaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleRecetaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleSolicitudEntity
import com.example.kubhubsystem_gp13_dam.local.entities.DocenteEntity
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.local.entities.RecetaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.ReservaSalaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.RolEntity
import com.example.kubhubsystem_gp13_dam.local.entities.SalaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.SeccionEntity
import com.example.kubhubsystem_gp13_dam.local.entities.SolicitudEntity
import com.example.kubhubsystem_gp13_dam.local.entities.UsuarioEntity
import com.example.kubhubsystem_gp13_dam.model.Seccion

@Database(
    entities = [
        //SCREEN INVENTARIO
        InventarioEntity::class,
        ProductoEntity::class,
        MovimientoEntity::class,
        //SCREEN RECETA
        RecetaEntity::class,
        DetalleRecetaEntity::class,
        //SCREEN USUARIO
        UsuarioEntity::class,
        RolEntity::class,
        DocenteEntity::class,
        //SCREEN ASIGNATURA
        AsignaturaEntity::class,
        SeccionEntity::class,
        SalaEntity::class,
        ReservaSalaEntity::class,
        //SCREEN SOLICITUD
        SolicitudEntity::class,
        DetalleSolicitudEntity::class
    ],
    version = 3, // Incrementé la versión por las nuevas entidades
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDAO
    abstract fun inventarioDao(): InventarioDAO
    abstract fun movimientoDao(): MovimientoDAO
    abstract fun recetaDao(): RecetaDAO
    abstract fun detalleRecetaDao(): DetalleRecetaDAO
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rolDao(): RolDao
    abstract fun docenteDao(): DocenteDao
    abstract fun asignaturaDao(): AsignaturaDAO
    abstract fun seccionDao(): SeccionDAO
    abstract fun salaDao(): SalaDAO
    abstract fun reservaSalaDao(): ReservaSalaDAO
    abstract fun solicitudDao(): SolicitudDAO
    abstract fun detalleSolicitudDao(): DetalleSolicitudDAO

    abstract fun pedidoSolicitudDao(): PedidoSolicitudDAO
    abstract fun pedidoDao(): PedidoDAO
    abstract fun estadoPedidoDao(): EstadoPedidoDAO
    abstract fun pedidoProcesadoDao(): PedidoProcesadoDAO
    abstract fun solicitudProcesadaDao(): SolicitudProcesadaDAO











    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: construirBaseDatos(context).also { INSTANCIA = it }
            }

        private fun construirBaseDatos(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "Hub.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}