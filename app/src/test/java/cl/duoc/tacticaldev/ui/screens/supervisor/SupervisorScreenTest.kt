package cl.duoc.tacticaldev.ui.screens.supervisor

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.RechazoUiState
import cl.duoc.tacticaldev.model.SupervisorUiState
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.enDialogo
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.viewmodel.SupervisorViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Vista del supervisor: filtros por estado, validar y rechazar visitas. */
@RunWith(AndroidJUnit4::class)
class SupervisorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estado = MutableStateFlow(SupervisorUiState())
    private val rechazo = MutableStateFlow(RechazoUiState())
    private val mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val viewModel: SupervisorViewModel = mockk(relaxed = true) {
        every { uiState } returns estado
        every { this@mockk.rechazo } returns this@SupervisorScreenTest.rechazo
        every { this@mockk.mensajes } returns this@SupervisorScreenTest.mensajes
    }

    private fun mostrar() = composeRule.conTema { SupervisorScreen(viewModel = viewModel) }

    /** Los filtros son "chips" que se pueden tocar (a diferencia de la etiqueta de cada tarjeta). */
    private fun filtro(texto: String) = composeRule.onNode(hasText(texto) and hasClickAction())

    @Test
    fun mientrasCarga_muestraLosFiltrosSinLista() {
        mostrar()

        filtro("Pendiente").assertExists()
        filtro("Validada").assertExists()
        filtro("Rechazada").assertExists()
        composeRule.onNodeWithText("No hay visitas pendientes").assertDoesNotExist()
    }

    @Test
    fun tocarUnFiltro_avisaAlViewModel() {
        mostrar()

        filtro("Rechazada").performClick()

        verify { viewModel.onFiltroChange(EstadoValidacion.RECHAZADA) }
    }

    @Test
    fun sinPendientes_muestraQueTodoFueRevisado() {
        estado.value = SupervisorUiState(cargando = false, filtro = EstadoValidacion.PENDIENTE)
        mostrar()

        composeRule.onNodeWithText("No hay visitas pendientes").assertExists()
        composeRule.onNodeWithText("Todas las visitas de terreno ya fueron revisadas.").assertExists()
    }

    @Test
    fun sinValidadas_muestraMensajeGeneral() {
        estado.value = SupervisorUiState(cargando = false, filtro = EstadoValidacion.VALIDADA)
        mostrar()

        composeRule.onNodeWithText("No hay visitas validadas").assertExists()
        composeRule.onNodeWithText("Las visitas aparecerán aquí cuando cambien a este estado.").assertExists()
    }

    @Test
    fun visitaPendiente_sePuedeValidarORechazar() {
        val visita = DatosPrueba.visitaDetalle()
        estado.value = SupervisorUiState(cargando = false, visitas = listOf(visita))
        mostrar()

        composeRule.onNodeWithText("Operario: Tomás Rivera").assertExists()
        composeRule.onNode(hasText("Validar") and hasClickAction()).performClick()
        composeRule.onNode(hasText("Rechazar") and hasClickAction()).performClick()

        verify { viewModel.validar(visita) }
        verify { viewModel.abrirRechazo(visita) }
    }

    @Test
    fun visitaYaValidada_noTieneBotones() {
        val visita = DatosPrueba.visitaDetalle(estadoValidacion = EstadoValidacion.VALIDADA.name)
        estado.value = SupervisorUiState(cargando = false, filtro = EstadoValidacion.VALIDADA, visitas = listOf(visita))
        mostrar()

        composeRule.onNodeWithText("TDV-VF-001 · Minimarket El Alba").assertExists()
        composeRule.onNode(hasText("Validar") and hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun mensajeDelViewModel_apareceComoAlerta() {
        mostrar()

        composeRule.runOnIdle { mensajes.tryEmit("Visita validada") }

        composeRule.esperarTexto("Visita validada")
    }
}
