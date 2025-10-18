package com.example.kubhubsystem_gp13_dam.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kubhubsystem_gp13_dam.local.dao.*
import com.example.kubhubsystem_gp13_dam.local.entities.*

@Database(
    entities = [
        // SCREEN INVENTARIO
        InventarioEntity::class,
        ProductoEntity::class,
        MovimientoEntity::class,
        // SCREEN RECETA
        RecetaEntity::class,
        DetalleRecetaEntity::class,
        // SCREEN USUARIO
        UsuarioEntity::class,
        RolEntity::class,
        DocenteEntity::class,
        // SCREEN ASIGNATURA
        AsignaturaEntity::class,
        SeccionEntity::class,
        SalaEntity::class,
        ReservaSalaEntity::class,
        // SCREEN SOLICITUD Y PEDIDO
        SolicitudEntity::class,
        DetalleSolicitudEntity::class,
        PedidoEntity::class,
        PedidoSolicitudEntity::class,
        EstadoPedidoEntity::class,
        AglomeradoPedidoEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // DAOs INVENTARIO
    abstract fun productoDao(): ProductoDAO
    abstract fun inventarioDao(): InventarioDAO
    abstract fun movimientoDao(): MovimientoDAO

    // DAOs RECETA
    abstract fun recetaDao(): RecetaDAO
    abstract fun detalleRecetaDao(): DetalleRecetaDAO

    // DAOs USUARIO
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rolDao(): RolDao
    abstract fun docenteDao(): DocenteDao

    // DAOs ASIGNATURA
    abstract fun asignaturaDao(): AsignaturaDAO
    abstract fun seccionDao(): SeccionDAO
    abstract fun salaDao(): SalaDAO
    abstract fun reservaSalaDao(): ReservaSalaDAO

    // DAOs SOLICITUD Y PEDIDO
    abstract fun solicitudDao(): SolicitudDAO
    abstract fun detalleSolicitudDao(): DetalleSolicitudDAO
    abstract fun pedidoDao(): PedidoDAO
    abstract fun pedidoSolicitudDao(): PedidoSolicitudDAO
    abstract fun estadoPedidoDao(): EstadoPedidoDAO
    abstract fun aglomeradoPedidoDao(): AglomeradoPedidoDAO

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