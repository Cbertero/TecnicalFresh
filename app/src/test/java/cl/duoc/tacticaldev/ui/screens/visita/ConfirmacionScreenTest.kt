package cl.duoc.tacticaldev.ui.screens.visita

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.utils.Formatos
import cl.duoc.tacticaldev.viewmodel.ConfirmacionViewModel
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pantalla que aparece después de guardar una visita. */
@RunWith(AndroidJUnit4::class)
class ConfirmacionScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val visita = MutableStateFlow<VisitaDetalle?>(null)
    private var volvio = 0
    private val historial = mutableListOf<Long>()

    private fun mostrar(enCola: Boolean) {
        val viewModel: ConfirmacionViewModel = mockk(relaxed = true) {
            every { this@mockk.visita } returns this@ConfirmacionScreenTest.visita
            every { this@mockk.enCola } returns enCola
        }
        composeRule.conTema {
            ConfirmacionScreen(viewModel = viewModel, onVolverInicio = { volvio++ }, onVerHistorial = { historial += it })
        }
    }

    @Test
    fun sinInternet_explicaQueSeEnviaraDespues() {
        mostrar(enCola = true)

        composeRule.onNodeWithText("Visita sin enviar").assertExists()
        composeRule.onNodeWithText("Visita guardada sin conexión").assertExists()
        composeRule.onNodeWithText("Visita guardada en el teléfono").assertExists()
        composeRule.onNodeWithText("Volver al inicio").performClick()

        volvio shouldBe 1
    }

    @Test
    fun mientrasCargaElDetalle_muestraSoloElTitulo() {
        mostrar(enCola = false)

        composeRule.onNodeWithText("Visita registrada").assertExists()
        composeRule.onNodeWithText("¡Visita registrada exitosamente!").assertExists()
        composeRule.onNodeWithText("¡Visita guardada!").assertDoesNotExist()
    }

    @Test
    fun conDetalle_muestraElResumenYLosBotones() {
        visita.value = DatosPrueba.visitaDetalle()
        mostrar(enCola = false)

        composeRule.onNodeWithText("¡Visita guardada!").assertExists()
        composeRule.onNodeWithText(
            "Quedó registrada en el dispositivo y pendiente de validación por el supervisor."
        ).assertExists()
        // El detalle aparece cuando termina la animación del ícono
        composeRule.esperarTexto("TDV-VF-001 · Minimarket El Alba")
        composeRule.onNodeWithText("Fecha y hora: ${Formatos.fechaHora(1_000L)}").assertExists()
        composeRule.onNodeWithText("Tipo: Mantención preventiva").assertExists()
        composeRule.onNodeWithText("Estado del equipo: Operativo · -18.0 °C").assertExists()
        composeRule.onNodeWithText("Ubicación verificada (12 m)").assertExists()

        composeRule.onNodeWithText("Volver a mis activos").performScrollTo().performClick()
        composeRule.onNodeWithText("Ver historial del activo").performScrollTo().performClick()
        volvio shouldBe 1
        historial shouldContainExactly listOf(1L)
    }

    @Test
    fun ubicacionFueraDeRango_seIndica() {
        visita.value = DatosPrueba.visitaDetalle().copy(ubicacionVerificada = false, distanciaMetros = 1_500.0)
        mostrar(enCola = false)

        composeRule.esperarTexto("Ubicación fuera de rango (${Formatos.distancia(1_500.0)})")
    }

    @Test
    fun laAlertaDesapareceDespuesDeUnosSegundos() {
        mostrar(enCola = true)
        composeRule.onNodeWithText("Visita guardada en el teléfono").assertExists()

        composeRule.mainClock.advanceTimeBy(5_000)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Visita guardada en el teléfono").assertDoesNotExist()
    }
}
