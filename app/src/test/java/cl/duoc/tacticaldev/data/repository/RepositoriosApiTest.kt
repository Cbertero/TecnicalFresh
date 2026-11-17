package cl.duoc.tacticaldev.data.repository

import android.content.Context
import cl.duoc.tacticaldev.data.local.dao.ActivoDao
import cl.duoc.tacticaldev.data.local.dao.PuntoVentaDao
import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.dao.VisitaPendienteDao
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaPendienteEntity
import cl.duoc.tacticaldev.data.model.LoginRequest
import cl.duoc.tacticaldev.data.model.RegistroRequest
import cl.duoc.tacticaldev.data.model.UsuarioDto
import cl.duoc.tacticaldev.data.model.ValidacionRequest
import cl.duoc.tacticaldev.data.model.VisitaRequest
import cl.duoc.tacticaldev.data.remote.ApiService
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.recolectar
import cl.duoc.tacticaldev.utils.FotoUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Pruebas de los repositorios conectados al microservicio.
 *
 * Igual que en TecnicoRepositoryTest, `mockkObject(RetrofitInstance)` reemplaza
 * el cliente real por un ApiService simulado. Así probamos los 3 escenarios
 * sin internet real:
 *   - el servidor responde bien            -> coEvery { api.x() } returns ...
 *   - el servidor responde con error HTTP  -> throws httpError(401)
 *   - no hay conexión                      -> throws IOException()
 */

/** Crea la excepción que lanza Retrofit cuando el servidor responde con un código de error. */
private fun httpError(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))

class UsuarioRepositoryTest : FunSpec({

    val api: ApiService = mockk()
    val dao: UsuarioDao = mockk(relaxed = true) // relaxed: responde valores por defecto sin programarlo
    val repository = UsuarioRepository(dao)
    val tomas = UsuarioDto(1, "Tomás Rivera", "tecnico@demo.cl", "OPERARIO", "Técnico de mantenimiento")

    beforeTest {
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns api
    }
    afterTest { unmockkAll() }

    context("Login (solo en el microservicio)") {
        test("con internet envía la clave al servidor y no guarda nada en el teléfono") {
            coEvery { api.loginUsuario(any()) } returns tomas

            val usuario = repository.autenticar("  TECNICO@Demo.cl ", "Clave123")

            // Al servidor viaja la clave (por HTTPS), con el correo normalizado
            coVerify { api.loginUsuario(LoginRequest("tecnico@demo.cl", "Clave123")) }
            usuario.shouldNotBeNull()
            usuario.nombre shouldBe "Tomás Rivera"
            usuario.claveHash shouldBe "" // en el teléfono no queda ninguna clave
            coVerify(exactly = 0) { dao.insertar(any()) }
        }

        test("si el servidor responde 401 la clave es incorrecta") {
            coEvery { api.loginUsuario(any()) } throws httpError(401)

            repository.autenticar("tecnico@demo.cl", "otraClave").shouldBeNull()
        }

        test("sin internet no deja entrar ni consulta Room") {
            coEvery { api.loginUsuario(any()) } throws IOException()

            shouldThrow<ServidorNoDisponibleException> {
                repository.autenticar("tecnico@demo.cl", "Clave123")
            }
            coVerify(exactly = 0) { dao.buscarPorCorreo(any()) }
        }

        test("si el servidor falla (500) tampoco deja entrar") {
            coEvery { api.loginUsuario(any()) } throws httpError(500)

            shouldThrow<ServidorNoDisponibleException> {
                repository.autenticar("tecnico@demo.cl", "Clave123")
            }
        }
    }

    context("Registro (solo en el microservicio)") {
        test("con internet registra en MySQL y devuelve el id del servidor") {
            val nueva = UsuarioDto(9, "Ana Pérez", "ana@demo.cl", "OPERARIO", "Vendedor")
            coEvery { api.registrarUsuario(any()) } returns nueva
            coEvery { api.getUsuarios(any()) } returns listOf(nueva)

            val resultado = repository.registrar(" Ana Pérez ", " ANA@Demo.cl", "secreta1", "OPERARIO", " Vendedor ")

            resultado.getOrNull() shouldBe 9L
            coVerify { api.registrarUsuario(RegistroRequest("Ana Pérez", "ana@demo.cl", "secreta1", "OPERARIO", "Vendedor")) }
            coVerify { api.getUsuarios("OPERARIO") } // refresca la lista de trabajadores
            coVerify(exactly = 0) { dao.insertar(any()) }
        }

        test("un correo repetido (409) devuelve error") {
            coEvery { api.registrarUsuario(any()) } throws httpError(409)

            val resultado = repository.registrar("Ana", "ana@demo.cl", "secreta1", "OPERARIO", "Vendedor")

            resultado.exceptionOrNull()?.message shouldBe "Ya existe una cuenta con este correo"
        }

        test("sin internet devuelve error de conexión y no guarda en el teléfono") {
            coEvery { api.registrarUsuario(any()) } throws IOException()

            val resultado = repository.registrar("Ana", "ana@demo.cl", "secreta1", "OPERARIO", "Vendedor")

            resultado.exceptionOrNull().shouldBeInstanceOf<ServidorNoDisponibleException>()
            coVerify(exactly = 0) { dao.insertar(any()) }
        }

        test("si el servidor falla (500) devuelve error de conexión") {
            coEvery { api.registrarUsuario(any()) } throws httpError(500)

            repository.registrar("Ana", "ana@demo.cl", "secreta1", "OPERARIO", "Vendedor")
                .exceptionOrNull().shouldBeInstanceOf<ServidorNoDisponibleException>()
        }
    }

    context("Estado del servidor") {
        test("servidorDisponible es true si el microservicio responde y false si no") {
            coEvery { api.getPuntosVenta() } returns emptyList()
            repository.servidorDisponible() shouldBe true

            coEvery { api.getPuntosVenta() } throws IOException()
            repository.servidorDisponible() shouldBe false
        }
    }

    context("Lista de operarios") {
        test("con internet trae los operarios del servidor") {
            coEvery { api.getUsuarios("OPERARIO") } returns listOf(tomas)

            val lista = repository.observarOperarios().first()

            lista.map { it.nombre } shouldContainExactly listOf("Tomás Rivera")
            lista[0].claveHash shouldBe "" // el servidor nunca envía el hash
        }

        test("sin internet usa los operarios guardados en el teléfono") {
            coEvery { api.getUsuarios(any()) } throws IOException()
            every { dao.observarOperarios() } returns flowOf(listOf(DatosPrueba.usuario()))

            repository.refrescarOperarios() shouldHaveSize 1
        }

        test("sin internet ni datos locales mantiene la lista anterior") {
            coEvery { api.getUsuarios(any()) } returns listOf(tomas)
            repository.refrescarOperarios()
            coEvery { api.getUsuarios(any()) } throws IOException()
            every { dao.observarOperarios() } returns flowOf(emptyList())

            repository.refrescarOperarios().map { it.id } shouldContainExactly listOf(1L)
        }
    }
})

class ActivoRepositoryTest : FunSpec({

    val api: ApiService = mockk()
    val activoDao: ActivoDao = mockk(relaxed = true)
    val puntoVentaDao: PuntoVentaDao = mockk(relaxed = true)
    val repository = ActivoRepository(activoDao, puntoVentaDao)

    val rosas = PuntoVentaEntity(2, "Minimarket Las Rosas", "Calle 1", "Santiago", -33.44, -70.66)
    val alba = PuntoVentaEntity(1, "Minimarket El Alba", "Av. 2", "Cerrillos", -33.51, -70.71)
    val activos = listOf(
        ActivoEntity(3, "TDV-VF-002", "VITRINA_FREEZER", "FrioMax", "VF-450", 2),
        ActivoEntity(1, "TDV-VF-001", "VITRINA_FREEZER", "FrioMax", "VF-300", 1),
        ActivoEntity(2, "TDV-CS-001", "CONSERVADORA", "GlaciaPro", "CS-150", 1),
        ActivoEntity(9, "TDV-XX-999", "CONSERVADORA", "Sin", "Local", 77) // su local no existe
    )

    beforeTest {
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns api
    }
    afterTest { unmockkAll() }

    fun conInternet() {
        coEvery { api.getPuntosVenta() } returns listOf(rosas, alba)
        coEvery { api.getActivos() } returns activos
    }

    fun sinInternet() {
        coEvery { api.getPuntosVenta() } throws IOException()
        coEvery { api.getActivos() } throws IOException()
    }

    context("Consultas") {
        test("une cada activo con su local, ordena y descarta los que no tienen local") {
            conInternet()

            val lista = repository.observarActivos().first()

            lista.map { it.codigo } shouldContainExactly listOf("TDV-CS-001", "TDV-VF-001", "TDV-VF-002")
            lista[0].puntoVentaNombre shouldBe "Minimarket El Alba"
            lista[0].comuna shouldBe "Cerrillos"
            coVerify { activoDao.insertarTodos(activos) } // guarda una copia para usar sin internet
        }

        test("los locales vienen ordenados por nombre") {
            conInternet()

            repository.observarPuntosVenta().first().map { it.nombre } shouldContainExactly
                listOf("Minimarket El Alba", "Minimarket Las Rosas")
        }

        test("sin internet usa los datos guardados en el teléfono") {
            sinInternet()
            every { puntoVentaDao.observarTodos() } returns flowOf(listOf(alba))
            every { activoDao.observarActivosConPunto() } returns flowOf(listOf(DatosPrueba.activoConPunto()))

            repository.refrescarDatos()

            repository.observarPuntosVenta().first() shouldContainExactly listOf(alba)
            repository.observarActivos().first() shouldHaveSize 1
        }

        test("sin internet ni base local las listas quedan vacías") {
            sinInternet()

            val sinBase = ActivoRepository()
            sinBase.observarActivos().first().shouldBeEmpty()
            sinBase.observarPuntosVenta().first().shouldBeEmpty()
        }
    }

    context("Buscar un activo") {
        test("obtenerActivo descarga los datos si aún no están cargados") {
            conInternet()

            repository.obtenerActivo(3)?.codigo shouldBe "TDV-VF-002"
        }

        test("si no está en la lista lo busca en el teléfono") {
            conInternet()
            val local = DatosPrueba.activoConPunto(id = 50)
            coEvery { activoDao.buscarActivoConPunto(50) } returns local

            repository.obtenerActivo(50) shouldBeSameInstanceAs local
        }

        test("buscarPorCodigo normaliza el código y consulta al servidor") {
            coEvery { api.getActivoPorCodigo("TDV-VF-001") } returns activos[1]

            repository.buscarPorCodigo("  tdv-vf-001 ") shouldBe activos[1]
        }

        test("buscarPorCodigo sin internet busca en el teléfono") {
            coEvery { api.getActivoPorCodigo(any()) } throws IOException()
            coEvery { activoDao.buscarPorCodigo("TDV-VF-001") } returns activos[1]

            repository.buscarPorCodigo("tdv-vf-001") shouldBe activos[1]
        }
    }

    context("Registrar") {
        test("un punto de venta con internet devuelve el id del servidor") {
            conInternet()
            val nuevo = PuntoVentaEntity(nombre = "Local Sol", direccion = "Dir", comuna = "Maipú", latitud = 0.0, longitud = 0.0)
            coEvery { api.guardarPuntoVenta(nuevo) } returns nuevo.copy(id = 5)

            repository.registrarPuntoVenta(nuevo) shouldBe 5L
            coVerify { api.getPuntosVenta() } // refresca la lista
        }

        test("un punto de venta sin internet se guarda en el teléfono") {
            sinInternet()
            val nuevo = PuntoVentaEntity(nombre = "Local Sol", direccion = "Dir", comuna = "Maipú", latitud = 0.0, longitud = 0.0)
            coEvery { api.guardarPuntoVenta(any()) } throws IOException()
            coEvery { puntoVentaDao.insertar(nuevo) } returns 8L

            repository.registrarPuntoVenta(nuevo) shouldBe 8L
            ActivoRepository().registrarPuntoVenta(nuevo) shouldBe 0L // sin base local
        }

        test("un activo se envía con el código en mayúsculas") {
            conInternet()
            val enviado = slot<ActivoEntity>()
            coEvery { api.guardarActivo(capture(enviado)) } answers { enviado.captured.copy(id = 10) }

            repository.registrarActivo(ActivoEntity(0, " tdv-cs-009 ", "CONSERVADORA", "Marca", "M1", 1)) shouldBe 10L
            enviado.captured.codigo shouldBe "TDV-CS-009"
        }

        test("un activo sin internet se guarda en el teléfono") {
            sinInternet()
            coEvery { api.guardarActivo(any()) } throws IOException()
            coEvery { activoDao.insertar(any()) } returns 11L
            val activo = ActivoEntity(0, "TDV-CS-009", "CONSERVADORA", "Marca", "M1", 1)

            repository.registrarActivo(activo) shouldBe 11L
            ActivoRepository().registrarActivo(activo) shouldBe 0L // sin base local
        }
    }
})

class VisitaRepositoryTest : FunSpec({

    val api: ApiService = mockk()
    val contexto: Context = mockk()
    val repository = VisitaRepository(contexto)
    val detalle = DatosPrueba.visitaDetalle(id = 77)

    val visita = VisitaEntity(
        activoId = 1, usuarioId = 1, fechaHora = 1_000L, tipoVisita = "MANTENCION",
        estadoEquipo = "OPERATIVO", temperatura = -18.5, observaciones = "Todo bien",
        fotoUri = "content://foto.jpg", latitud = -33.5, longitud = -70.7,
        distanciaMetros = 12.0, ubicacionVerificada = true
    )

    beforeTest {
        mockkObject(RetrofitInstance, FotoUtils)
        every { RetrofitInstance.api } returns api
        // La conversión real de la foto necesita Android; aquí simulamos el resultado
        every { FotoUtils.aBase64(any(), any()) } returns "data:image/jpeg;base64,AAA"
    }
    afterTest { unmockkAll() }

    test("registrar envía la visita con la foto en Base64 y devuelve el id del servidor") {
        val enviada = slot<VisitaRequest>()
        coEvery { api.registrarVisita(capture(enviada)) } returns detalle

        repository.registrar(visita) shouldBe 77L

        enviada.captured.fotoUri shouldBe "data:image/jpeg;base64,AAA"
        enviada.captured.temperatura shouldBe -18.5
        enviada.captured.ubicacionVerificada shouldBe true
    }

    test("registrar sin foto envía el campo vacío") {
        every { FotoUtils.aBase64(any(), any()) } returns null
        val enviada = slot<VisitaRequest>()
        coEvery { api.registrarVisita(capture(enviada)) } returns detalle

        repository.registrar(visita.copy(fotoUri = ""))

        enviada.captured.fotoUri shouldBe ""
    }

    context("Visitas sin internet (cola en Room)") {
        val pendienteDao: VisitaPendienteDao = mockk(relaxed = true)
        var programada = 0
        val repoConCola = VisitaRepository(contexto, pendienteDao) { programada++ }

        test("sin internet guarda la visita en Room, programa el envío y devuelve ID_EN_COLA") {
            coEvery { api.registrarVisita(any()) } throws IOException()
            val guardada = slot<VisitaPendienteEntity>()
            coEvery { pendienteDao.insertar(capture(guardada)) } returns 1L

            repoConCola.registrar(visita) shouldBe VisitaRepository.ID_EN_COLA

            guardada.captured.fotoBase64 shouldBe "data:image/jpeg;base64,AAA"
            guardada.captured.fechaHora shouldBe 1_000L // conserva la hora real de la visita
            programada shouldBe 1
        }

        test("si el servidor responde con error no la deja en cola") {
            coEvery { api.registrarVisita(any()) } throws httpError(400)

            shouldThrow<HttpException> { repoConCola.registrar(visita) }
            coVerify(exactly = 0) { pendienteDao.insertar(any()) }
        }

        test("sin base local el error de conexión llega al ViewModel") {
            coEvery { api.registrarVisita(any()) } throws IOException()

            shouldThrow<IOException> { repository.registrar(visita) }
        }

        test("sincronizar envía cada pendiente a MySQL y la borra de Room") {
            val p1 = DatosPrueba.visitaPendiente(id = 1)
            val p2 = DatosPrueba.visitaPendiente(id = 2)
            coEvery { pendienteDao.obtenerTodas() } returns listOf(p1, p2)
            coEvery { api.registrarVisita(any()) } returns detalle

            repoConCola.sincronizarPendientes() shouldBe true

            coVerify(exactly = 2) { api.registrarVisita(any()) }
            coVerify { pendienteDao.eliminar(1) }
            coVerify { pendienteDao.eliminar(2) }
        }

        test("si se corta internet al sincronizar, no borra y pide reintentar") {
            coEvery { pendienteDao.obtenerTodas() } returns listOf(DatosPrueba.visitaPendiente(id = 1))
            coEvery { api.registrarVisita(any()) } throws IOException()

            repoConCola.sincronizarPendientes() shouldBe false
            coVerify(exactly = 0) { pendienteDao.eliminar(any()) }
        }

        test("un 500 del servidor también se reintenta más tarde") {
            coEvery { pendienteDao.obtenerTodas() } returns listOf(DatosPrueba.visitaPendiente(id = 1))
            coEvery { api.registrarVisita(any()) } throws httpError(500)

            repoConCola.sincronizarPendientes() shouldBe false
            coVerify(exactly = 0) { pendienteDao.eliminar(any()) }
        }

        test("una visita rechazada por datos inválidos (4xx) se descarta") {
            coEvery { pendienteDao.obtenerTodas() } returns listOf(DatosPrueba.visitaPendiente(id = 5))
            coEvery { api.registrarVisita(any()) } throws httpError(400)

            repoConCola.sincronizarPendientes() shouldBe true
            coVerify { pendienteDao.eliminar(5) }
        }

        test("observarPendientes cuenta las del usuario; sin base local es 0") {
            every { pendienteDao.contarPorUsuario(1) } returns flowOf(3)

            repoConCola.observarPendientes(1).first() shouldBe 3
            repository.observarPendientes(1).first() shouldBe 0
            repository.sincronizarPendientes() shouldBe true
        }
    }

    test("obtenerDetalle devuelve la visita, o null si falla la conexión") {
        coEvery { api.getVisita(77) } returns detalle
        repository.obtenerDetalle(77) shouldBe detalle

        coEvery { api.getVisita(77) } throws IOException()
        repository.obtenerDetalle(77).shouldBeNull()
    }

    test("las consultas pasan el filtro correcto al servidor") {
        coEvery { api.getVisitas(usuarioId = 1) } returns listOf(detalle)
        coEvery { api.getVisitas(activoId = 2) } returns listOf(detalle)
        coEvery { api.getVisitas(estado = "PENDIENTE") } returns listOf(detalle)

        repository.observarPorUsuario(1).first() shouldContainExactly listOf(detalle)
        repository.observarPorActivo(2).first() shouldContainExactly listOf(detalle)
        repository.observarPorEstado(EstadoValidacion.PENDIENTE).first() shouldContainExactly listOf(detalle)
    }

    test("sin internet las consultas devuelven una lista vacía") {
        coEvery { api.getVisitas(any(), any(), any()) } throws IOException()

        repository.observarPorUsuario(1).first().shouldBeEmpty()
    }

    test("validar llama al servidor y vuelve a pedir las listas visibles") {
        runTest {
            coEvery { api.getVisitas(estado = "PENDIENTE") } returnsMany listOf(listOf(detalle), emptyList())
            coEvery { api.validarVisita(77, ValidacionRequest(4)) } returns detalle
            val emisiones = recolectar(repository.observarPorEstado(EstadoValidacion.PENDIENTE))

            repository.validar(77, 4)

            // 1ª emisión: con la visita pendiente; 2ª (tras validar): ya no está
            emisiones.map { it.size } shouldContainExactly listOf(1, 0)
        }
    }

    test("rechazar envía el motivo sin espacios extra") {
        coEvery { api.rechazarVisita(any(), any()) } returns detalle

        repository.rechazar(77, "  Foto borrosa del equipo  ", 4)

        coVerify { api.rechazarVisita(77, ValidacionRequest(4, "Foto borrosa del equipo")) }
    }

    test("refrescar vuelve a consultar al servidor") {
        runTest {
            coEvery { api.getVisitas(usuarioId = 1) } returns listOf(detalle)
            val emisiones = recolectar(repository.observarPorUsuario(1))

            repository.refrescar()

            emisiones shouldHaveSize 2
        }
    }
})
