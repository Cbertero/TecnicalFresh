package cl.duoc.tacticaldev.ui.screens.historial

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.viewmodel.HistorialUiState
import cl.duoc.tacticaldev.viewmodel.HistorialViewModel
import cl.duoc.tacticaldev.viewmodel.MisVisitasUiState
import cl.duoc.tacticaldev.viewmodel.MisVisitasViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Historial de visitas de un activo. */
@RunWith(AndroidJUnit4::class)
class HistorialActivoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estado = MutableStateFlow(HistorialUiState())
    private val viewModel: HistorialViewModel = mockk(relaxed = true) { every { uiState } returns estado }
    private var volvio = 0

    private fun mostrar() = composeRule.conTema { HistorialActivoScreen(viewModel = viewModel, onVolver = { volvio++ }) }

    @Test
    fun mientrasCarga_soloMuestraElTitulo() {
        mostrar()

        composeRule.onNodeWithText("Historial del activo").assertExists()
        composeRule.onNodeWithText("Sin visitas registradas").assertDoesNotExist()
    }

    @Test
    fun activoSinVisitas_muestraEstadoVacio() {
        estado.value = HistorialUiState(cargando = false, activo = DatosPrueba.activoConPunto())
        mostrar()

        composeRule.onNodeWithText("TDV-VF-001 · Vitrina freezer").assertExists()
        composeRule.onNodeWithText("Minimarket El Alba · Av. Ficticia 1234, Cerrillos").assertExists()
        composeRule.onNodeWithText("0 visita(s) registradas").assertExists()
        composeRule.onNodeWithText("Sin visitas registradas").assertExists()
    }

    @Test
    fun conVisitas_muestraCadaVisitaConSuOperario() {
        estado.value = HistorialUiState(
            cargando = false,
            activo = DatosPrueba.activoConPunto(),
            visitas = listOf(DatosPrueba.visitaDetalle())
        )
        mostrar()

        composeRule.onNodeWithText("1 visita(s) registradas").assertExists()
        composeRule.onNodeWithText("Operario: Tomás Rivera").assertExists()
        composeRule.onNodeWithText("Sin visitas registradas").assertDoesNotExist()
    }

    @Test
    fun sinActivoEncontrado_igualMuestraLaLista() {
        estado.value = HistorialUiState(cargando = false, activo = null)
        mostrar()

        composeRule.onNodeWithText("Sin visitas registradas").assertExists()
    }

    @Test
    fun flechaVolver_vuelve() {
        mostrar()

        composeRule.onNodeWithContentDescription("Volver").performClick()

        volvio shouldBe 1
    }
}

/** "Mis visitas" del operario, con el aviso de visitas guardadas sin internet. */
@RunWith(AndroidJUnit4::class)
class MisVisitasScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estado = MutableStateFlow(MisVisitasUiState())
    private val viewModel: MisVisitasViewModel = mockk(relaxed = true) { every { uiState } returns estado }

    private fun mostrar() = composeRule.conTema { MisVisitasScreen(viewModel = viewModel) }

    @Test
    fun mientrasCarga_noMuestraListaNiAviso() {
        mostrar()

        composeRule.onNodeWithText("Todavía no registras visitas").assertDoesNotExist()
        composeRule.onNodeWithText("TecnicalFresh • Gestión de Mantenimiento").assertExists()
    }

    @Test
    fun sinVisitas_muestraEstadoVacio() {
        estado.value = MisVisitasUiState(cargando = false)
        mostrar()

        composeRule.onNodeWithText("Todavía no registras visitas").assertExists()
        composeRule.onNodeWithText(
            "Elige un activo en Vitrinas o escanea su QR para registrar tu primera visita."
        ).assertExists()
    }

    @Test
    fun conVisitas_muestraLasTarjetas() {
        estado.value = MisVisitasUiState(cargando = false, visitas = listOf(DatosPrueba.visitaDetalle()))
        mostrar()

        composeRule.onNodeWithText("TDV-VF-001 · Minimarket El Alba").assertExists()
    }

    @Test
    fun unaVisitaPendiente_usaSingular() {
        estado.value = MisVisitasUiState(cargando = false, pendientes = 1)
        mostrar()

        composeRule.onNodeWithText("1 visita está esperando internet para enviarse").assertExists()
    }

    @Test
    fun variasVisitasPendientes_usaPlural() {
        estado.value = MisVisitasUiState(cargando = false, pendientes = 3)
        mostrar()

        composeRule.onNodeWithText("3 visitas están esperando internet para enviarse").assertExists()
    }
}
