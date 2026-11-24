package cl.duoc.tacticaldev.ui.screens.inicio

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.viewmodel.ActivosUiState
import cl.duoc.tacticaldev.viewmodel.ActivosViewModel
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Lista de vitrinas/activos del operario con buscador. */
@RunWith(AndroidJUnit4::class)
class InicioOperarioScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estado = MutableStateFlow(ActivosUiState())
    private val busqueda = MutableStateFlow("")
    private val mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val viewModel: ActivosViewModel = mockk(relaxed = true) {
        every { uiState } returns estado
        every { this@mockk.busqueda } returns this@InicioOperarioScreenTest.busqueda
        every { this@mockk.mensajes } returns this@InicioOperarioScreenTest.mensajes
    }
    private val registrar = mutableListOf<Long>()
    private val historial = mutableListOf<Long>()
    private var volvio = 0

    private fun mostrar() = composeRule.conTema {
        InicioOperarioScreen(
            viewModel = viewModel,
            nombreUsuario = "Tomás",
            onVolver = { volvio++ },
            onRegistrarVisita = { registrar += it },
            onVerHistorial = { historial += it }
        )
    }

    @Test
    fun muestraSaludoYBuscador() {
        mostrar()

        composeRule.onNodeWithText("Hola, Tomás").assertExists()
        composeRule.onNodeWithText("Buscar por código, local o comuna").assertExists()
        composeRule.onNodeWithText("TecnicalFresh • Gestión de Mantenimiento").assertExists()
    }

    @Test
    fun escribirEnElBuscador_filtraEnElViewModel() {
        mostrar()

        composeRule.onNodeWithText("Buscar por código, local o comuna").performTextInput("TDV")

        verify { viewModel.onBusquedaChange("TDV") }
    }

    @Test
    fun conTextoEscrito_sePuedeLimpiarLaBusqueda() {
        busqueda.value = "Maipú"
        mostrar()

        composeRule.onNodeWithContentDescription("Limpiar búsqueda").performClick()

        verify { viewModel.onBusquedaChange("") }
    }

    @Test
    fun sinResultados_muestraEstadoVacioConLaBusqueda() {
        busqueda.value = "xyz"
        estado.value = ActivosUiState(cargando = false, activos = emptyList())
        mostrar()

        composeRule.onNodeWithText("Sin resultados").assertExists()
        composeRule.onNodeWithText(
            "No hay activos que coincidan con \"xyz\". Prueba con otro código o comuna."
        ).assertExists()
    }

    @Test
    fun mientrasCarga_noMuestraTarjetas() {
        mostrar()

        composeRule.onNodeWithText("Sin resultados").assertDoesNotExist()
        composeRule.onNodeWithText("Registrar visita").assertDoesNotExist()
    }

    @Test
    fun tarjetaDeActivo_registrarVisitaYVerHistorial() {
        estado.value = ActivosUiState(cargando = false, activos = listOf(DatosPrueba.activoConPunto(id = 7)))
        mostrar()

        composeRule.onNodeWithText("TDV-VF-001").assertExists()
        composeRule.onNodeWithText("Registrar visita").performClick()
        composeRule.onNodeWithText("Historial").performClick()

        registrar shouldContainExactly listOf(7L)
        historial shouldContainExactly listOf(7L)
    }

    @Test
    fun flechaVolver_vuelveAlPanel() {
        mostrar()

        composeRule.onNodeWithContentDescription("Volver").performClick()

        volvio shouldBe 1
    }

    @Test
    fun mensajeDelViewModel_seMuestraEnSnackbar() {
        mostrar()

        composeRule.runOnIdle { mensajes.tryEmit("No se encontró el activo") }

        composeRule.esperarTexto("No se encontró el activo")
    }
}
