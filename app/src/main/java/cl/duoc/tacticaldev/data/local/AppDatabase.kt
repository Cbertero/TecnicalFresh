package cl.duoc.tacticaldev.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.withTransaction
import cl.duoc.tacticaldev.data.local.dao.ActivoDao
import cl.duoc.tacticaldev.data.local.dao.PuntoVentaDao
import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.dao.VisitaDao
import cl.duoc.tacticaldev.data.local.dao.VisitaPendienteDao
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaPendienteEntity

/**
 * Base de datos local (Room/SQLite). Permite trabajar sin conexión en terreno:
 * todas las visitas quedan guardadas en el dispositivo.
 */
@Database(
    entities = [
        UsuarioEntity::class, PuntoVentaEntity::class, ActivoEntity::class,
        VisitaEntity::class, VisitaPendienteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntoVentaDao(): PuntoVentaDao
    abstract fun activoDao(): ActivoDao
    abstract fun visitaDao(): VisitaDao
    abstract fun visitaPendienteDao(): VisitaPendienteDao

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

        /**
         * Versión 1 → 2: agrega la tabla de visitas pendientes de envío y borra
         * los hashes de claves que guardaba el login sin internet.
         * Con una migración no se borran los datos que ya tenía el teléfono.
         */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `visitas_pendientes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `activoId` INTEGER NOT NULL,
                        `usuarioId` INTEGER NOT NULL,
                        `fechaHora` INTEGER NOT NULL,
                        `tipoVisita` TEXT NOT NULL,
                        `estadoEquipo` TEXT NOT NULL,
                        `temperatura` REAL NOT NULL,
                        `observaciones` TEXT NOT NULL,
                        `fotoBase64` TEXT NOT NULL,
                        `latitud` REAL NOT NULL,
                        `longitud` REAL NOT NULL,
                        `distanciaMetros` REAL NOT NULL,
                        `ubicacionVerificada` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // El login ahora se valida solo en el microservicio: se borran las claves guardadas
                db.execSQL("UPDATE usuarios SET claveHash = ''")
            }
        }

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instancia = it }
            }
    }
}
