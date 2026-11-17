package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.SavedStateHandle
import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.recolectar
import cl.duoc.tacticaldev.testutil.suscribir
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class ConfirmacionViewModelTest : FunSpec({

    test("carga la visita recién guardada") {
        val detalle = DatosPrueba.visitaDetalle(id = 77)
        val repo: VisitaRepository = mockk { coEvery { obtenerDetalle(77) } returns detalle }

        val vm = ConfirmacionViewModel(SavedStateHandle(mapOf("visitaId" to 77L)), repo)

        vm.visita.value shouldBe detalle
    }

    test("si la visita quedó en cola no la busca en el servidor") {
        val repo: VisitaRepository = mockk()

        val vm = ConfirmacionViewModel(
            SavedStateHandle(mapOf("visitaId" to VisitaRepository.ID_EN_COLA)), repo
        )

        vm.enCola shouldBe true
        vm.visita.value shouldBe null
        coVerify(exactly = 0) { repo.obtenerDetalle(any()) }
    }
})

class HistorialViewModelTest : FunSpec({

    test("combina el activo con sus visitas") {
        runTest {
            val activo = DatosPrueba.activoConPunto(id = 3)
            val visitas = listOf(DatosPrueba.visitaDetalle(id = 1), DatosPrueba.visitaDetalle(id = 2))
            val activoRepo: ActivoRepository = mockk { coEvery { obtenerActivo(3) } returns activo }
            val visitaRepo: VisitaRepository = mockk { every { observarPorActivo(3) } returns flowOf(visitas) }

            val vm = HistorialViewModel(SavedStateHandle(mapOf("activoId" to 3L)), activoRepo, visitaRepo)
            suscribir(vm.uiState)

            val s = vm.uiState.value
            s.cargando shouldBe false
            s.activo shouldBe activo
            s.visitas shouldBe visitas
        }
    }
})

class MisVisitasViewModelTest : FunSpec({

    val sesionFlow = MutableStateFlow<Sesion?>(null)
    val sesionPreferences: SesionPreferences = mockk { every { sesion } returns sesionFlow }
    val visitas = listOf(DatosPrueba.visitaDetalle())
    val visitaRepo: VisitaRepository = mockk {
        every { observarPorUsuario(1) } returns flowOf(visitas)
        every { observarPendientes(1) } returns flowOf(0)
    }

    test("con sesión muestra las visitas del operario") {
        runTest {
            sesionFlow.value = DatosPrueba.sesion(usuarioId = 1)
            val vm = MisVisitasViewModel(sesionPreferences, visitaRepo)
            suscribir(vm.uiState)

            vm.uiState.value.visitas shouldBe visitas
            vm.uiState.value.cargando shouldBe false
        }
    }

    test("muestra cuántas visitas esperan internet") {
        runTest {
            every { visitaRepo.observarPendientes(1) } returns flowOf(2)
            sesionFlow.value = DatosPrueba.sesion(usuarioId = 1)
            val vm = MisVisitasViewModel(sesionPreferences, visitaRepo)
            suscribir(vm.uiState)

            vm.uiState.value.pendientes shouldBe 2
        }
    }

    test("sin sesión la lista queda vacía") {
        runTest {
            val vm = MisVisitasViewModel(sesionPreferences, visitaRepo)
            suscribir(vm.uiState)

            vm.uiState.value.visitas.shouldBeEmpty()
            vm.uiState.value.cargando shouldBe false
        }
    }
})

/** Supervisor: filtra por estado, valida y rechaza (con motivo obligatorio). */
class SupervisorViewModelTest : FunSpec({

    val pendiente = DatosPrueba.visitaDetalle(id = 1, activoCodigo = "TDV-VF-001")
    val validada = DatosPrueba.visitaDetalle(id = 2, estadoValidacion = EstadoValidacion.VALIDADA.name)

    val sesionFlow = MutableStateFlow<Sesion?>(DatosPrueba.sesion(usuarioId = 4))
    val sesionPreferences: SesionPreferences = mockk { every { sesion } returns sesionFlow }
    val visitaRepository: VisitaRepository = mockk {
        every { observarPorEstado(EstadoValidacion.PENDIENTE) } returns flowOf(listOf(pendiente))
        every { observarPorEstado(EstadoValidacion.VALIDADA) } returns flowOf(listOf(validada))
        coEvery { validar(any(), any()) } just Runs
        coEvery { rechazar(any(), any(), any()) } just Runs
    }
    val vm = SupervisorViewModel(visitaRepository, sesionPreferences)

    test("por defecto muestra las pendientes y permite cambiar el filtro") {
        runTest {
            suscribir(vm.uiState)
            vm.uiState.value.filtro shouldBe EstadoValidacion.PENDIENTE
            vm.uiState.value.visitas shouldContainExactly listOf(pendiente)

            vm.onFiltroChange(EstadoValidacion.VALIDADA)

            vm.uiState.value.filtro shouldBe EstadoValidacion.VALIDADA
            vm.uiState.value.visitas shouldContainExactly listOf(validada)
        }
    }

    context("Validar") {
        test("guarda con el id del supervisor y avisa") {
            runTest {
                val mensajes = recolectar(vm.mensajes)

                vm.validar(pendiente)

                coVerify { visitaRepository.validar(1, 4) }
                mensajes shouldContainExactly listOf("Visita de TDV-VF-001 validada")
            }
        }

        test("si falla la conexión al validar avisa al supervisor") {
            runTest {
                coEvery { visitaRepository.validar(any(), any()) } throws java.io.IOException()
                val mensajes = recolectar(vm.mensajes)

                vm.validar(pendiente)

                mensajes shouldContainExactly listOf("No se pudo validar la visita. Revisa tu conexión")
            }
        }

        test("sin sesión no hace nada") {
            sesionFlow.value = null

            vm.validar(pendiente)

            coVerify(exactly = 0) { visitaRepository.validar(any(), any()) }
        }
    }

    context("Rechazar") {
        test("abrir y cancelar maneja el diálogo") {
            vm.abrirRechazo(pendiente)
            vm.rechazo.value.visita shouldBe pendiente

            vm.cancelarRechazo()
            vm.rechazo.value.visita.shouldBeNull()
        }

        test("el motivo se valida mientras se escribe") {
            vm.onMotivoChange("corto")
            vm.rechazo.value.errorMotivo.shouldNotBeNull()

            vm.onMotivoChange("La foto no muestra el equipo")
            vm.rechazo.value.errorMotivo.shouldBeNull()
        }

        test("con un motivo inválido no rechaza") {
            vm.abrirRechazo(pendiente)

            vm.confirmarRechazo()

            vm.rechazo.value.errorMotivo shouldBe "Ingresa el motivo del rechazo"
            coVerify(exactly = 0) { visitaRepository.rechazar(any(), any(), any()) }
        }

        test("sin una visita abierta no hace nada") {
            vm.confirmarRechazo()
            coVerify(exactly = 0) { visitaRepository.rechazar(any(), any(), any()) }
        }

        test("con un motivo válido rechaza, cierra el diálogo y avisa") {
            runTest {
                val mensajes = recolectar(vm.mensajes)
                vm.abrirRechazo(pendiente)
                vm.onMotivoChange("La foto no muestra el equipo")

                vm.confirmarRechazo()

                coVerify { visitaRepository.rechazar(1, "La foto no muestra el equipo", 4) }
                vm.rechazo.value.visita.shouldBeNull()
                mensajes shouldContainExactly listOf("Visita de TDV-VF-001 rechazada")
            }
        }

        test("si falla la conexión al rechazar avisa y mantiene el diálogo abierto") {
            runTest {
                coEvery { visitaRepository.rechazar(any(), any(), any()) } throws java.io.IOException()
                val mensajes = recolectar(vm.mensajes)
                vm.abrirRechazo(pendiente)
                vm.onMotivoChange("La foto no muestra el equipo")

                vm.confirmarRechazo()

                vm.rechazo.value.visita shouldBe pendiente // no se pierde lo que escribió
                mensajes shouldContainExactly listOf("No se pudo rechazar la visita. Revisa tu conexión")
            }
        }

        test("sin sesión no rechaza") {
            sesionFlow.value = null
            vm.abrirRechazo(pendiente)
            vm.onMotivoChange("La foto no muestra el equipo")

            vm.confirmarRechazo()

            coVerify(exactly = 0) { visitaRepository.rechazar(any(), any(), any()) }
        }
    }
})
