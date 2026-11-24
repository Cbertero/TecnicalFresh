package cl.duoc.tacticaldev.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaPendienteEntity
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.model.TipoActivo
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Base de datos Room y sus DAO. Robolectric simula Android en la JVM y la base
 * se crea "en memoria": es real (SQLite), pero desaparece al terminar cada prueba.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: AppDatabase

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    // ---------- Datos de apoyo ----------

    private fun punto(id: Long, nombre: String) =
        PuntoVentaEntity(id, nombre, "Dirección $id", "Comuna $id", -33.0 - id, -70.0 - id)

    private fun usuario(id: Long, nombre: String, rol: Rol = Rol.OPERARIO) =
        UsuarioEntity(id, nombre, "u$id@demo.cl", "hash", rol.name, "Cargo $id")

    private fun visita(
        activoId: Long,
        usuarioId: Long,
        fecha: Long,
        estado: EstadoValidacion = EstadoValidacion.PENDIENTE
    ) = VisitaEntity(
        activoId = activoId, usuarioId = usuarioId, fechaHora = fecha,
        tipoVisita = "MANTENCION", estadoEquipo = "OPERATIVO", temperatura = -18.0,
        observaciones = "Ok", fotoUri = "content://foto", latitud = -33.0, longitud = -70.0,
        distanciaMetros = 10.0, ubicacionVerificada = true, estadoValidacion = estado.name
    )

    private fun pendiente(usuarioId: Long, fecha: Long) = VisitaPendienteEntity(
        activoId = 1, usuarioId = usuarioId, fechaHora = fecha, tipoVisita = "MANTENCION",
        estadoEquipo = "OPERATIVO", temperatura = -18.0, observaciones = "Sin señal",
        fotoBase64 = "data:image/jpeg;base64,AAA", latitud = -33.0, longitud = -70.0,
        distanciaMetros = 12.0, ubicacionVerificada = true
    )

    /** Un punto de venta, dos activos y dos usuarios listos para registrar visitas. */
    private suspend fun prepararDatosBase() {
        db.puntoVentaDao().insertarTodos(listOf(punto(1, "Minimarket El Alba"), punto(2, "Almacén Los Aromos")))
        db.activoDao().insertarTodos(
            listOf(
                ActivoEntity(1, "TDV-VF-001", TipoActivo.VITRINA_FREEZER.name, "FrioMax", "VF-300", 1),
                ActivoEntity(2, "TDV-CS-001", TipoActivo.CONSERVADORA.name, "GlaciaPro", "CS-150", 2)
            )
        )
        db.usuarioDao().insertarTodos(listOf(usuario(1, "Tomás"), usuario(2, "Carla", Rol.SUPERVISOR)))
    }

    // ---------- Datos iniciales ----------

    @Test
    fun sembrarSiEstaVacia_cargaLosDatosFicticiosUnaSolaVez() = runBlocking<Unit> {
        db.sembrarSiEstaVacia()
        db.sembrarSiEstaVacia() // la segunda vez no debe duplicar nada

        db.usuarioDao().contar() shouldBe DatosFicticios.usuarios().size
        db.puntoVentaDao().observarTodos().first() shouldHaveSize DatosFicticios.puntosVenta().size
        db.activoDao().observarActivosConPunto().first() shouldHaveSize DatosFicticios.activos().size
        val totalVisitas = EstadoValidacion.entries.sumOf { estado ->
            db.visitaDao().observarPorEstado(estado.name).first().size
        }
        totalVisitas shouldBe DatosFicticios.visitas().size
    }

    // ---------- UsuarioDao ----------

    @Test
    fun usuarioDao_buscaPorCorreoYCuenta() = runBlocking<Unit> {
        db.usuarioDao().contar() shouldBe 0
        val id = db.usuarioDao().insertar(usuario(7, "Ana"))

        id shouldBe 7
        db.usuarioDao().contar() shouldBe 1
        db.usuarioDao().buscarPorCorreo("u7@demo.cl")?.nombre shouldBe "Ana"
        db.usuarioDao().buscarPorCorreo("nadie@demo.cl").shouldBeNull()
    }

    @Test
    fun usuarioDao_observarOperarios_soloOperariosOrdenadosPorNombre() = runBlocking<Unit> {
        db.usuarioDao().insertarTodos(
            listOf(usuario(1, "Zoe"), usuario(2, "Carla", Rol.SUPERVISOR), usuario(3, "Ana"))
        )

        db.usuarioDao().observarOperarios().first().map { it.nombre } shouldContainExactly listOf("Ana", "Zoe")
    }

    // ---------- PuntoVentaDao ----------

    @Test
    fun puntoVentaDao_insertaYOrdenaPorNombre() = runBlocking<Unit> {
        db.puntoVentaDao().insertar(punto(1, "Minimarket"))
        db.puntoVentaDao().insertar(punto(2, "Almacén"))

        db.puntoVentaDao().observarTodos().first().map { it.nombre } shouldContainExactly
            listOf("Almacén", "Minimarket")
    }

    // ---------- ActivoDao ----------

    @Test
    fun activoDao_uneElActivoConSuPuntoDeVenta() = runBlocking<Unit> {
        prepararDatosBase()

        val activo = db.activoDao().buscarActivoConPunto(1).shouldNotBeNull()
        activo.codigo shouldBe "TDV-VF-001"
        activo.puntoVentaNombre shouldBe "Minimarket El Alba"
        activo.comuna shouldBe "Comuna 1"
        activo.ultimaVisita.shouldBeNull() // todavía no tiene visitas

        db.activoDao().buscarActivoConPunto(99).shouldBeNull()
    }

    @Test
    fun activoDao_ultimaVisitaEsLaMasReciente() = runBlocking<Unit> {
        prepararDatosBase()
        db.visitaDao().insertarTodas(listOf(visita(1, 1, 1_000), visita(1, 1, 5_000), visita(2, 1, 9_000)))

        db.activoDao().buscarActivoConPunto(1)?.ultimaVisita shouldBe 5_000L
    }

    @Test
    fun activoDao_listaOrdenadaPorLocalYCodigo() = runBlocking<Unit> {
        prepararDatosBase()
        db.activoDao().insertar(ActivoEntity(3, "TDV-AA-000", TipoActivo.CONSERVADORA.name, "X", "Y", 1))

        db.activoDao().observarActivosConPunto().first().map { it.codigo } shouldContainExactly
            listOf("TDV-CS-001", "TDV-AA-000", "TDV-VF-001") // Almacén... antes que Minimarket...
    }

    @Test
    fun activoDao_buscaPorCodigo() = runBlocking<Unit> {
        prepararDatosBase()

        db.activoDao().buscarPorCodigo("TDV-CS-001")?.id shouldBe 2
        db.activoDao().buscarPorCodigo("NO-EXISTE").shouldBeNull()
    }

    // ---------- VisitaDao ----------

    @Test
    fun visitaDao_detalleTraeLosNombresDelJoin() = runBlocking<Unit> {
        prepararDatosBase()
        val id = db.visitaDao().insertar(visita(1, 1, 1_000))

        val detalle = db.visitaDao().buscarDetalle(id).shouldNotBeNull()
        detalle.activoCodigo shouldBe "TDV-VF-001"
        detalle.activoTipo shouldBe TipoActivo.VITRINA_FREEZER.name
        detalle.puntoVentaNombre shouldBe "Minimarket El Alba"
        detalle.usuarioNombre shouldBe "Tomás"
        detalle.estadoValidacion shouldBe EstadoValidacion.PENDIENTE.name
        detalle.motivoRechazo.shouldBeNull()

        db.visitaDao().buscarDetalle(999).shouldBeNull()
    }

    @Test
    fun visitaDao_filtraPorActivoUsuarioYEstado_masRecientePrimero() = runBlocking<Unit> {
        prepararDatosBase()
        db.visitaDao().insertarTodas(
            listOf(
                visita(1, 1, 1_000),
                visita(1, 2, 3_000, EstadoValidacion.VALIDADA),
                visita(2, 1, 2_000)
            )
        )

        db.visitaDao().observarPorActivo(1).first().map { it.fechaHora } shouldContainExactly listOf(3_000L, 1_000L)
        db.visitaDao().observarPorUsuario(1).first().map { it.fechaHora } shouldContainExactly listOf(2_000L, 1_000L)
        db.visitaDao().observarPorEstado(EstadoValidacion.VALIDADA.name).first() shouldHaveSize 1
        db.visitaDao().observarPorEstado(EstadoValidacion.RECHAZADA.name).first().shouldBeEmpty()
    }

    @Test
    fun visitaDao_actualizarValidacionGuardaEstadoYMotivo() = runBlocking<Unit> {
        prepararDatosBase()
        val id = db.visitaDao().insertar(visita(1, 1, 1_000))

        db.visitaDao().actualizarValidacion(id, EstadoValidacion.RECHAZADA.name, "Foto borrosa", supervisorId = 2)

        val detalle = db.visitaDao().buscarDetalle(id).shouldNotBeNull()
        detalle.estadoValidacion shouldBe EstadoValidacion.RECHAZADA.name
        detalle.motivoRechazo shouldBe "Foto borrosa"
    }

    // ---------- VisitaPendienteDao (cola sin internet) ----------

    @Test
    fun visitaPendienteDao_colaOrdenadaCuentaYElimina() = runBlocking<Unit> {
        val dao = db.visitaPendienteDao()
        val primera = dao.insertar(pendiente(usuarioId = 1, fecha = 5_000))
        dao.insertar(pendiente(usuarioId = 1, fecha = 1_000))
        dao.insertar(pendiente(usuarioId = 2, fecha = 3_000))

        dao.obtenerTodas().map { it.fechaHora } shouldContainExactly listOf(1_000L, 3_000L, 5_000L)
        dao.contarPorUsuario(1).first() shouldBe 2
        dao.contarPorUsuario(2).first() shouldBe 1

        dao.eliminar(primera)

        dao.contarPorUsuario(1).first() shouldBe 1
        dao.obtenerTodas() shouldHaveSize 2
    }

    // ---------- Migración y singleton ----------

    @Test
    fun migracion1a2_creaLaColaYBorraLasClavesGuardadas() {
        // Base "antigua" (versión 1) con solo la tabla de usuarios y una clave guardada
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // en memoria
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY NOT NULL, claveHash TEXT NOT NULL)")
                    db.execSQL("INSERT INTO usuarios (id, claveHash) VALUES (1, 'hash-antiguo')")
                }

                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val baseAntigua = helper.writableDatabase

        AppDatabase.MIGRACION_1_2.startVersion shouldBe 1
        AppDatabase.MIGRACION_1_2.endVersion shouldBe 2
        AppDatabase.MIGRACION_1_2.migrate(baseAntigua)

        baseAntigua.query("SELECT claveHash FROM usuarios").use { cursor ->
            cursor.moveToFirst() shouldBe true
            cursor.getString(0) shouldBe ""
        }
        baseAntigua.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'visitas_pendientes'")
            .use { cursor -> cursor.count shouldBe 1 }
        helper.close()
    }

    @Test
    fun obtener_devuelveSiempreLaMismaInstancia() {
        val primera = AppDatabase.obtener(context)
        val segunda = AppDatabase.obtener(context)

        primera shouldBeSameInstanceAs segunda
    }
}
