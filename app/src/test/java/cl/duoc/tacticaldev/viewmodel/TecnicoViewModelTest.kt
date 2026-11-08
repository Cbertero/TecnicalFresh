package cl.duoc.tacticaldev.viewmodel

import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.TecnicoRepository
import cl.duoc.tacticaldev.data.repository.UsuarioRepository
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.suscribir
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.io.IOException

/** Pantalla "Asignar Locales (API)": mensajes de estado según la conexión con el microservicio. */
class TecnicoViewModelTest : FunSpec({

    val asignaciones = MutableStateFlow<List<Tecnico>>(emptyList())
    val operarios = listOf(DatosPrueba.usuario())
    val locales = listOf(PuntoVentaEntity(1, "Almacén Los Aromos", "Dir", "Maipú", 0.0, 0.0))

    val repository: TecnicoRepository = mockk {
        every { asignacionesFlow } returns asignaciones
        coEvery { geTecnicos() } returns emptyList()
    }
    val usuarioRepository: UsuarioRepository = mockk {
        every { observarOperarios() } returns flowOf(operarios)
    }
    val activoRepository: ActivoRepository = mockk {
        every { observarPuntosVenta() } returns flowOf(locales)
    }

    fun crearViewModel() = TecnicoViewModel(repository, usuarioRepository, activoRepository)

    test("expone los operarios y locales para los selectores") {
        runTest {
            val vm = crearViewModel()
            suscribir(vm.operarios)
            suscribir(vm.locales)

            vm.operarios.value shouldBe operarios
            vm.locales.value shouldBe locales
            vm.tecnicoList shouldBeSameInstanceAs asignaciones
        }
    }

    context("Al abrir la pantalla") {
        test("si todo está sincronizado muestra conectado") {
            coEvery { repository.geTecnicos() } returns
                listOf(Tecnico(1, "Ana", "Vendedora", "Local A", sincronizado = true))

            val vm = crearViewModel()

            vm.mensajeEstado.value shouldBe "Conectado y sincronizado con el servidor MySQL"
            vm.sincronizando.value shouldBe false
        }

        test("con pendientes muestra el modo local") {
            coEvery { repository.geTecnicos() } returns listOf(
                Tecnico(1, "Ana", "Vendedora", "Local A", sincronizado = true),
                Tecnico(-5, "Luis", "Técnico", "Local B", sincronizado = false)
            )

            val vm = crearViewModel()

            vm.mensajeEstado.value shouldBe "Modo local activo: 1 asignación(es) pendientes de internet"
        }

        test("si falla el repositorio muestra sin conexión") {
            coEvery { repository.geTecnicos() } throws IOException()

            val vm = crearViewModel()

            vm.mensajeEstado.value shouldBe "Modo local (sin conexión al servidor)"
            vm.sincronizando.value shouldBe false
        }
    }

    context("Agregar asignación") {
        test("con campos vacíos no hace nada") {
            val vm = crearViewModel()

            vm.agregarTecnico("", "Vendedora", "Local A")
            vm.agregarTecnico("Ana", " ", "Local A")
            vm.agregarTecnico("Ana", "Vendedora", "")

            coVerify(exactly = 0) { repository.GuardarTecnico(any(), any(), any()) }
        }

        test("con internet avisa que se guardó en el servidor") {
            coEvery { repository.GuardarTecnico(any(), any(), any()) } returns
                Tecnico(1, "Ana", "Vendedora", "Local A", sincronizado = true)
            val vm = crearViewModel()

            vm.agregarTecnico("  Ana ", " Vendedora ", " Local A ")

            coVerify { repository.GuardarTecnico("Ana", "Vendedora", "Local A") } // llega sin espacios
            vm.mensajeEstado.value shouldBe "Asignación guardada en servidor MySQL"
        }

        test("sin internet avisa que se guardó en modo local") {
            coEvery { repository.GuardarTecnico(any(), any(), any()) } returns
                Tecnico(-1, "Ana", "Vendedora", "Local A", sincronizado = false)
            val vm = crearViewModel()

            vm.agregarTecnico("Ana", "Vendedora", "Local A")

            vm.mensajeEstado.value shouldBe "Sin internet: guardado en modo local (se subirá al reconectar)"
        }

        test("ante un error inesperado avisa el modo local") {
            coEvery { repository.GuardarTecnico(any(), any(), any()) } throws IllegalStateException()
            val vm = crearViewModel()

            vm.agregarTecnico("Ana", "Vendedora", "Local A")

            vm.mensajeEstado.value shouldBe "Guardado en modo local"
            vm.sincronizando.value shouldBe false
        }
    }

    context("Eliminar asignación") {
        test("elimina y recarga la lista") {
            coEvery { repository.eliminarTecnico(1) } just Runs
            val vm = crearViewModel()

            vm.eliminarTecnico(1)

            coVerify { repository.eliminarTecnico(1) }
            coVerify(exactly = 2) { repository.geTecnicos() } // al abrir + después de eliminar
        }

        test("si falla no revienta la app") {
            coEvery { repository.eliminarTecnico(any()) } throws IOException()
            val vm = crearViewModel()

            vm.eliminarTecnico(1)

            coVerify(exactly = 1) { repository.geTecnicos() } // no recarga si falló
        }
    }
})
