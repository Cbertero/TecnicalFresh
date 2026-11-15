package cl.duoc.tacticaldev.viewmodel

import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.TecnicoRepository
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.recolectar
import cl.duoc.tacticaldev.testutil.suscribir
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * La lista de vitrinas que ve cada trabajador depende de 3 cosas combinadas:
 * los locales que le asignó el supervisor (microservicio), su cargo y el buscador.
 */
class ActivosViewModelTest : FunSpec({

    // Un activo por cada local de los datos ficticios
    val activos = listOf(
        DatosPrueba.activoConPunto(id = 1, codigo = "TDV-VF-001", puntoVentaNombre = "Minimarket El Alba", comuna = "Cerrillos"),
        DatosPrueba.activoConPunto(id = 2, codigo = "TDV-VF-002", puntoVentaNombre = "Almacén Los Aromos", comuna = "Maipú"),
        DatosPrueba.activoConPunto(id = 3, codigo = "TDV-CS-002", puntoVentaNombre = "Minimarket Las Rosas", comuna = "Santiago"),
        DatosPrueba.activoConPunto(id = 4, codigo = "TDV-CS-003", puntoVentaNombre = "Botillería La Esquina", comuna = "Estación Central")
    )

    val asignaciones = MutableStateFlow<List<Tecnico>>(emptyList())
    val sesionFlow = MutableStateFlow<Sesion?>(null)

    val activoRepository: ActivoRepository = mockk {
        every { observarActivos() } returns flowOf(activos)
    }
    val sesionPreferences: SesionPreferences = mockk {
        every { sesion } returns sesionFlow
    }
    val tecnicoRepository: TecnicoRepository = mockk {
        every { asignacionesFlow } returns asignaciones
        coEvery { geTecnicos() } returns emptyList()
    }

    fun crearViewModel() = ActivosViewModel(activoRepository, sesionPreferences, tecnicoRepository)

    fun ActivosViewModel.idsVisibles() = uiState.value.activos.map { it.id }

    test("al abrirse descarga las asignaciones del microservicio") {
        crearViewModel()
        coVerify { tecnicoRepository.geTecnicos() }
    }

    context("Filtro por trabajador") {
        test("con asignación del supervisor solo ve sus locales") {
            runTest {
                sesionFlow.value = DatosPrueba.sesion(nombre = "Tomás Rivera", cargo = "Técnico de mantenimiento")
                asignaciones.value = listOf(
                    Tecnico(1, "tomás rivera ", "Técnico", "Botillería La Esquina"), // ignora mayúsculas/espacios
                    Tecnico(2, "Otra Persona", "Vendedor", "Minimarket Las Rosas")
                )
                val vm = crearViewModel()
                suscribir(vm.uiState)

                vm.idsVisibles() shouldContainExactly listOf(4L)
                vm.uiState.value.cargando shouldBe false
            }
        }

        test("sin asignación el vendedor ve Minimarket Las Rosas") {
            runTest {
                sesionFlow.value = DatosPrueba.sesion(nombre = "Valentina", cargo = "Vendedor")
                val vm = crearViewModel()
                suscribir(vm.uiState)

                vm.idsVisibles() shouldContainExactly listOf(3L)
            }
        }

        test("sin asignación el transportista ve la Botillería") {
            runTest {
                sesionFlow.value = DatosPrueba.sesion(nombre = "Diego", cargo = "Transportista")
                val vm = crearViewModel()
                suscribir(vm.uiState)

                vm.idsVisibles() shouldContainExactly listOf(4L)
            }
        }

        test("sin asignación el técnico ve El Alba y Los Aromos") {
            runTest {
                sesionFlow.value = DatosPrueba.sesion(nombre = "Tomás", cargo = "Técnico de mantenimiento")
                val vm = crearViewModel()
                suscribir(vm.uiState)

                vm.idsVisibles() shouldContainExactly listOf(1L, 2L)
            }
        }

        test("otro cargo ve todos los activos") {
            runTest {
                sesionFlow.value = DatosPrueba.sesion(nombre = "Carla", cargo = "Supervisor de operaciones")
                val vm = crearViewModel()
                suscribir(vm.uiState)

                vm.idsVisibles() shouldContainExactly listOf(1L, 2L, 3L, 4L)
            }
        }
    }

    test("el buscador filtra por código, local o comuna") {
        runTest {
            val vm = crearViewModel() // sin sesión: ve todo
            suscribir(vm.uiState)

            vm.onBusquedaChange("cs-")
            vm.idsVisibles() shouldContainExactly listOf(3L, 4L)

            vm.onBusquedaChange("aromos")
            vm.idsVisibles() shouldContainExactly listOf(2L)

            vm.onBusquedaChange("cerrillos")
            vm.idsVisibles() shouldContainExactly listOf(1L)

            vm.onBusquedaChange("   ")
            vm.idsVisibles() shouldHaveSize 4
            vm.busqueda.value shouldBe "   "
        }
    }

    context("Escáner QR") {
        test("un código existente abre el activo") {
            coEvery { activoRepository.buscarPorCodigo("TDV-VF-001") } returns DatosPrueba.activoEntity(id = 1)
            val vm = crearViewModel()
            var abierto: Long? = null

            vm.onCodigoEscaneado("TDV-VF-001") { abierto = it }

            abierto shouldBe 1L
        }

        test("un código inexistente muestra un mensaje") {
            runTest {
                coEvery { activoRepository.buscarPorCodigo(any()) } returns null
                val vm = crearViewModel()
                val mensajes = recolectar(vm.mensajes)
                var abierto: Long? = null

                vm.onCodigoEscaneado("XYZ") { abierto = it }
                vm.mostrarMensaje("Otro aviso")

                abierto.shouldBeNull()
                mensajes shouldContainExactly listOf("No existe un activo con el código XYZ", "Otro aviso")
            }
        }
    }
})
