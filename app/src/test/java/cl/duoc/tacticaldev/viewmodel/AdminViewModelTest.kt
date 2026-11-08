package cl.duoc.tacticaldev.viewmodel

import android.location.Location
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.location.UbicacionService
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.recolectar
import cl.duoc.tacticaldev.testutil.suscribir
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/** Administración: registro de puntos de venta y activos (vitrinas/conservadoras). */
class AdminViewModelTest : FunSpec({

    val puntos = listOf(PuntoVentaEntity(1, "Minimarket El Alba", "Av. Ficticia", "Cerrillos", -33.5, -70.7))
    val activoRepository: ActivoRepository = mockk {
        every { observarPuntosVenta() } returns flowOf(puntos)
    }
    val ubicacionService: UbicacionService = mockk()
    val viewModel = AdminViewModel(activoRepository, ubicacionService)

    fun llenarPuntoValido() {
        viewModel.onNombrePuntoChange("Minimarket Sol")
        viewModel.onDireccionChange("Av. Siempre Viva 742")
        viewModel.onComunaChange("Maipú")
        viewModel.onLatitudChange("-33,51")
        viewModel.onLongitudChange("-70.75")
    }

    fun llenarActivoValido() {
        viewModel.onCodigoChange("tdv-vf-009")
        viewModel.onTipoActivoChange(TipoActivo.VITRINA_FREEZER)
        viewModel.onMarcaChange("FrioMax")
        viewModel.onModeloChange("VF-300")
        viewModel.onPuntoVentaChange(1)
    }

    test("expone la lista de puntos de venta del repositorio") {
        runTest {
            suscribir(viewModel.puntosVenta)
            viewModel.puntosVenta.value shouldBe puntos
        }
    }

    test("onPestanaChange cambia la pestaña") {
        viewModel.onPestanaChange(1)
        viewModel.uiState.value.pestana shouldBe 1
    }

    context("Punto de venta") {
        test("los campos se validan mientras se escribe") {
            viewModel.onNombrePuntoChange("ab")
            viewModel.onDireccionChange("")
            viewModel.onComunaChange("x")
            viewModel.onLatitudChange("100")
            viewModel.onLongitudChange("abc")

            val e = viewModel.uiState.value.puntoVenta.errores
            e.nombre.shouldNotBeNull()
            e.direccion.shouldNotBeNull()
            e.comuna.shouldNotBeNull()
            e.latitud.shouldNotBeNull()
            e.longitud.shouldNotBeNull()
        }

        test("guardar un punto inválido no llama al repositorio") {
            viewModel.guardarPuntoVenta()

            viewModel.uiState.value.puntoVenta.errores.hayErrores shouldBe true
            coVerify(exactly = 0) { activoRepository.registrarPuntoVenta(any()) }
        }

        test("guardar un punto válido lo registra, limpia el formulario y avisa") {
            runTest {
                val mensajes = recolectar(viewModel.mensajes)
                val punto = slot<PuntoVentaEntity>()
                coEvery { activoRepository.registrarPuntoVenta(capture(punto)) } returns 5L
                llenarPuntoValido()

                viewModel.guardarPuntoVenta()

                punto.captured.nombre shouldBe "Minimarket Sol"
                punto.captured.latitud shouldBe -33.51 // "-33,51" -> -33.51
                viewModel.uiState.value.puntoVenta.nombre shouldBe ""
                viewModel.uiState.value.guardando shouldBe false
                mensajes shouldContainExactly listOf("Punto de venta \"Minimarket Sol\" registrado")
            }
        }

        test("usar la ubicación actual completa latitud y longitud") {
            val ubicacion: Location = mockk {
                every { latitude } returns -33.5
                every { longitude } returns -70.75
            }
            coEvery { ubicacionService.obtenerUbicacionActual() } returns ubicacion

            viewModel.usarUbicacionActual()

            val punto = viewModel.uiState.value.puntoVenta
            punto.latitud shouldBe "-33.500000"
            punto.longitud shouldBe "-70.750000"
            viewModel.uiState.value.obteniendoUbicacion shouldBe false
        }

        test("sin GPS avisa que no se pudo obtener la ubicación") {
            runTest {
                val mensajes = recolectar(viewModel.mensajes)
                coEvery { ubicacionService.obtenerUbicacionActual() } returns null

                viewModel.usarUbicacionActual()

                mensajes shouldContainExactly listOf("No se pudo obtener la ubicación")
            }
        }

        test("sin permiso de ubicación avisa") {
            runTest {
                val mensajes = recolectar(viewModel.mensajes)

                viewModel.onPermisoUbicacionDenegado()

                mensajes shouldContainExactly listOf("Se necesita el permiso de ubicación")
            }
        }
    }

    context("Activo") {
        test("el código se guarda en mayúsculas") {
            viewModel.onCodigoChange("tdv-cs-001")
            viewModel.uiState.value.activo.codigo shouldBe "TDV-CS-001"
            viewModel.uiState.value.activo.errores.codigo.shouldBeNull()
        }

        test("guardar un activo inválido marca todos los errores") {
            viewModel.guardarActivo()

            val e = viewModel.uiState.value.activo.errores
            e.codigo.shouldNotBeNull()
            e.tipo.shouldNotBeNull()
            e.marca.shouldNotBeNull()
            e.modelo.shouldNotBeNull()
            e.puntoVenta.shouldNotBeNull()
        }

        test("un código repetido muestra error y no guarda") {
            coEvery { activoRepository.buscarPorCodigo("TDV-VF-009") } returns DatosPrueba.activoEntity(codigo = "TDV-VF-009")
            llenarActivoValido()

            viewModel.guardarActivo()

            viewModel.uiState.value.activo.errores.codigo shouldBe "Ya existe un activo con este código"
            coVerify(exactly = 0) { activoRepository.registrarActivo(any()) }
        }

        test("un activo válido se guarda y limpia el formulario") {
            runTest {
                val mensajes = recolectar(viewModel.mensajes)
                val activo = slot<ActivoEntity>()
                coEvery { activoRepository.buscarPorCodigo(any()) } returns null
                coEvery { activoRepository.registrarActivo(capture(activo)) } returns 9L
                llenarActivoValido()

                viewModel.guardarActivo()

                activo.captured.codigo shouldBe "TDV-VF-009"
                activo.captured.tipo shouldBe "VITRINA_FREEZER"
                activo.captured.puntoVentaId shouldBe 1L
                viewModel.uiState.value.activo.codigo shouldBe ""
                mensajes shouldContainExactly listOf("Activo TDV-VF-009 registrado")
            }
        }
    }
})
