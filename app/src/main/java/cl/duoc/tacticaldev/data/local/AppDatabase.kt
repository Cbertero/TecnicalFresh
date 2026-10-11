package cl.duoc.tacticaldev.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import cl.duoc.tacticaldev.data.local.dao.ActivoDao
import cl.duoc.tacticaldev.data.local.dao.PuntoVentaDao
import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.dao.VisitaDao
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity

/**
 * Base de datos local (Room/SQLite). Permite trabajar sin conexión en terreno:
 * todas las visitas quedan guardadas en el dispositivo.
 */
@Database(
    entities = [UsuarioEntity::class, PuntoVentaEntity::class, ActivoEntity::class, VisitaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntoVentaDao(): PuntoVentaDao
    abstract fun activoDao(): ActivoDao
    abstract fun visitaDao(): VisitaDao

    /** Carga los datos ficticios solo la primera vez que se abre la app. */
    suspend fun sembrarSiEstaVacia() {
        if (usuarioDao().contar() > 0) return
        withTransaction {
            usuarioDao().insertarTodos(DatosFicticios.usuarios())
            puntoVentaDao().insertarTodos(DatosFicticios.puntosVenta())
            activoDao().insertarTodos(DatosFicticios.activos())
            visitaDao().insertarTodas(DatosFicticios.visitas())
        }
    }

    companion object {
        private const val NOMBRE_BD = "tacticaldev.db"

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                ).build().also { instancia = it }
            }
    }
}
