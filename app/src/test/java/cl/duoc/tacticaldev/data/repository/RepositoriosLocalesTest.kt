package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.ActivoDao
import cl.duoc.tacticaldev.data.local.dao.PuntoVentaDao
import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.dao.VisitaDao
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.utils.Seguridad
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

/**
 * Pruebas de los repositorios que usan Room.
 *
 * Aquí NO usamos la base de datos real: reemplazamos cada DAO por un *mock*
 * de MockK. Un mock es un objeto "de mentira" al que le decimos qué responder:
 *   - coEvery { dao.funcion() } returns valor   -> programa la respuesta (funciones suspend)
 *   - every   { dao.funcion() } returns valor   -> lo mismo para funciones normales
 *   - coVerify { dao.funcion(...) }             -> comprueba que se llamó con esos datos
 *
 * En Kotest el cuerpo de cada `test` ya es una corrutina, así que podemos
 * llamar funciones `suspend` directamente.
 */
class UsuarioRepositoryTest : FunSpec({

    // Gracias a IsolationMode.InstancePerLeaf (ver ProjectConfig) cada prueba
    // recibe un mock y un repositorio nuevos.
    val dao: UsuarioDao = mockk()
    val repository = UsuarioRepository(dao)

    test("autenticar con la clave correcta devuelve el usuario") {
        val usuario = DatosPrueba.usuario(claveHash = Seguridad.sha256("Clave123"))
        coEvery { dao.buscarPorCorreo("tecnico@demo.cl") } returns usuario

        val resultado = repository.autenticar("tecnico@demo.cl", "Clave123")

        resultado shouldBeSameInstanceAs usuario
    }

    test("autenticar normaliza el correo quitando espacios y mayúsculas") {
        val usuario = DatosPrueba.usuario(claveHash = Seguridad.sha256("Clave123"))
        coEvery { dao.buscarPorCorreo("tecnico@demo.cl") } returns usuario

        repository.autenticar("  TECNICO@Demo.cl ", "Clave123") shouldBeSameInstanceAs usuario
    }

    test("autenticar con la clave incorrecta devuelve null") {
        coEvery { dao.buscarPorCorreo(any()) } returns DatosPrueba.usuario(claveHash = Seguridad.sha256("Clave123"))

        repository.autenticar("tecnico@demo.cl", "otraClave").shouldBeNull()
    }

    test("autenticar con un correo inexistente devuelve null") {
        coEvery { dao.buscarPorCorreo(any()) } returns null

        repository.autenticar("nadie@demo.cl", "Clave123").shouldBeNull()
    }

    test("registrar un correo nuevo guarda el hash y los datos limpios") {
        coEvery { dao.buscarPorCorreo(any()) } returns null
        val guardado = slot<UsuarioEntity>() // "slot" captura el objeto que se le pasó al DAO
        coEvery { dao.insertar(capture(guardado)) } returns 7L

        val resultado = repository.registrar(" Ana Pérez ", " ANA@Demo.cl", "secreta1", "OPERARIO", " Vendedor ")

        resultado.getOrNull() shouldBe 7L
        guardado.captured.nombre shouldBe "Ana Pérez"
        guardado.captured.correo shouldBe "ana@demo.cl"
        guardado.captured.cargo shouldBe "Vendedor"
        guardado.captured.claveHash shouldBe Seguridad.sha256("secreta1") // nunca texto plano
    }

    test("registrar un correo repetido devuelve error y no inserta") {
        coEvery { dao.buscarPorCorreo("tecnico@demo.cl") } returns DatosPrueba.usuario()

        val resultado = repository.registrar("Tomás", "tecnico@demo.cl", "123456", "OPERARIO", "Técnico")

        resultado.isFailure shouldBe true
        resultado.exceptionOrNull()?.message shouldBe "Ya existe una cuenta con este correo"
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    test("observarOperarios delega en el DAO") {
        val lista = listOf(DatosPrueba.usuario())
        every { dao.observarOperarios() } returns flowOf(lista)

        repository.observarOperarios().first() shouldBe lista
    }
})

class VisitaRepositoryTest : FunSpec({

    val dao: VisitaDao = mockk()
    val repository = VisitaRepository(dao)

    test("registrar devuelve el id que entrega Room") {
        val visita = mockk<VisitaEntity>()
        coEvery { dao.insertar(visita) } returns 42L

        repository.registrar(visita) shouldBe 42L
    }

    test("obtenerDetalle delega en el DAO") {
        val detalle = DatosPrueba.visitaDetalle()
        coEvery { dao.buscarDetalle(10) } returns detalle

        repository.obtenerDetalle(10) shouldBeSameInstanceAs detalle
    }

    test("las consultas observables pasan el parámetro correcto al DAO") {
        val lista = listOf(DatosPrueba.visitaDetalle())
        every { dao.observarPorActivo(1) } returns flowOf(lista)
        every { dao.observarPorUsuario(2) } returns flowOf(lista)
        every { dao.observarPorEstado("RECHAZADA") } returns flowOf(lista)

        repository.observarPorActivo(1).first() shouldBe lista
        repository.observarPorUsuario(2).first() shouldBe lista
        repository.observarPorEstado(EstadoValidacion.RECHAZADA).first() shouldBe lista
    }

    test("validar marca la visita como validada y sin motivo") {
        coEvery { dao.actualizarValidacion(any(), any(), any(), any()) } just Runs

        repository.validar(visitaId = 10, supervisorId = 4)

        coVerify { dao.actualizarValidacion(10, "VALIDADA", null, 4) }
    }

    test("rechazar guarda el motivo sin espacios extra") {
        coEvery { dao.actualizarValidacion(any(), any(), any(), any()) } just Runs

        repository.rechazar(visitaId = 10, motivo = "  Foto borrosa del equipo  ", supervisorId = 4)

        coVerify { dao.actualizarValidacion(10, "RECHAZADA", "Foto borrosa del equipo", 4) }
    }
})

class ActivoRepositoryTest : FunSpec({

    val activoDao: ActivoDao = mockk()
    val puntoVentaDao: PuntoVentaDao = mockk()
    val repository = ActivoRepository(activoDao, puntoVentaDao)

    test("observar activos y puntos de venta delega en los DAO") {
        val activos = listOf(DatosPrueba.activoConPunto())
        val puntos = listOf(PuntoVentaEntity(1, "Local", "Dir", "Comuna", 0.0, 0.0))
        every { activoDao.observarActivosConPunto() } returns flowOf(activos)
        every { puntoVentaDao.observarTodos() } returns flowOf(puntos)

        repository.observarActivos().first() shouldBe activos
        repository.observarPuntosVenta().first() shouldBe puntos
    }

    test("obtenerActivo delega en el DAO") {
        val activo = DatosPrueba.activoConPunto(id = 3)
        coEvery { activoDao.buscarActivoConPunto(3) } returns activo

        repository.obtenerActivo(3) shouldBeSameInstanceAs activo
    }

    test("buscarPorCodigo normaliza a mayúsculas y sin espacios") {
        val activo = DatosPrueba.activoEntity()
        coEvery { activoDao.buscarPorCodigo("TDV-VF-001") } returns activo

        repository.buscarPorCodigo("  tdv-vf-001 ") shouldBeSameInstanceAs activo
    }

    test("registrar devuelve los ids generados por Room") {
        val punto = PuntoVentaEntity(nombre = "Local", direccion = "Dir", comuna = "Maipú", latitud = 0.0, longitud = 0.0)
        val activo = DatosPrueba.activoEntity(id = 0)
        coEvery { puntoVentaDao.insertar(punto) } returns 5L
        coEvery { activoDao.insertar(activo) } returns 9L

        repository.registrarPuntoVenta(punto) shouldBe 5L
        repository.registrarActivo(activo) shouldBe 9L
    }
})
