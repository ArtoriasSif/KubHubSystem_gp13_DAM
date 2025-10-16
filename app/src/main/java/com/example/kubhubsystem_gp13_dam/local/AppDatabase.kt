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
import com.example.kubhubsystem_gp13_dam.local.dao.DocenteDAO
import com.example.kubhubsystem_gp13_dam.local.dao.EstadoPedidoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.MovimientoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.PedidoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.PedidoProcesadoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.PedidoSolicitudDAO
import com.example.kubhubsystem_gp13_dam.local.entities.MovimientoEntity
import com.example.kubhubsystem_gp13_dam.local.dao.ProductoDAO
import com.example.kubhubsystem_gp13_dam.local.dao.RecetaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SalaSeccionDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SeccionDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SolicitudDAO
import com.example.kubhubsystem_gp13_dam.local.dao.SolicitudProcesadaDAO
import com.example.kubhubsystem_gp13_dam.local.dao.UsuarioDAO
import com.example.kubhubsystem_gp13_dam.local.entities.DetalleRecetaEntity
import com.example.kubhubsystem_gp13_dam.local.entities.ProductoEntity
import com.example.kubhubsystem_gp13_dam.local.entities.RecetaEntity

@Database(
    entities = [
        InventarioEntity::class,
        ProductoEntity::class,
        MovimientoEntity::class,
        RecetaEntity::class,
        DetalleRecetaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDAO
    abstract fun inventarioDao(): InventarioDAO
    abstract fun movimientoDao(): MovimientoDAO

    abstract fun detalleRecetaDao(): DetalleRecetaDAO

    abstract fun recetaDao(): RecetaDAO

    abstract fun solicitudDao(): SolicitudDAO

    abstract fun pedidoSolicitudDao(): PedidoSolicitudDAO

    abstract fun pedidoDao(): PedidoDAO

    abstract fun estadoPedidoDao(): EstadoPedidoDAO

    abstract fun pedidoProcesadoDao(): PedidoProcesadoDAO

    abstract fun solicitudProcesadaDao(): SolicitudProcesadaDAO

    abstract fun usuarioDao(): UsuarioDAO

    abstract fun docenteDao(): DocenteDAO

    abstract fun seccionDao(): SeccionDAO

    abstract fun asignaturaDao(): AsignaturaDAO

    abstract fun salaSeccionDao(): SalaSeccionDAO


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "Hub.db"
            )
                // ⚠️ SOLO PARA DESARROLLO - Eliminar en producción
                // .allowMainThreadQueries()

                // ✅ MEJOR: Usar fallbackToDestructiveMigration para desarrollo
                .fallbackToDestructiveMigration()

                .build()
        }
    }
}