package cl.duoc.tacticaldev.ui.screens.tecnico

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.viewmodel.TecnicoViewModel
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** "Asignar Locales (API)": el supervisor asigna un local a un trabajador. */
@RunWith(AndroidJUnit4::class)
class TecnicoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val tecnicos = MutableStateFlow<List<Tecnico>>(emptyList())
    private val operarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    private val locales = MutableStateFlow<List<PuntoVentaEntity>>(emptyList())
    private val mensajeEstado = MutableStateFlow<String?>(null)
    private val sincronizando = MutableStateFlow(false)
    private val viewModel: TecnicoViewModel = mockk(relaxed = true) {
        every { tecnicoList } returns tecnicos
        every { this@mockk.operarios } returns this@TecnicoScreenTest.operarios
        every { this@mockk.locales } returns this@TecnicoScreenTest.locales
        every { this@mockk.mensajeEstado } returns this@TecnicoScreenTest.mensajeEstado
        every { this@mockk.sincronizando } returns this@TecnicoScreenTest.sincronizando
    }

    private val tomas = DatosPrueba.usuario() // Tomás Rivera · Técnico de mantenimiento
    private val valentina = UsuarioEntity(2, "Valentina Soto", "vendedor@demo.cl", "", "OPERARIO", "")
    private val alba = PuntoVentaEntity(1, "Minimarket El Alba", "Av. Ficticia 1234", "Cerrillos", -33.5, -70.7)
    private val rosas = PuntoVentaEntity(3, "Minimarket Las Rosas", "Calle Ejemplo 789", "Santiago", -33.4, -70.6)

    private fun mostrar() = composeRule.conTema { TecnicoScreen(viewModel = viewModel) }

    @Test
    fun sinMensaje_muestraLaDescripcionYElContador() {
        mostrar()

        composeRule.onNodeWithText("Asignación de locales a personal en terreno").assertExists()
        composeRule.onNodeWithText("Asignaciones registradas (0)").assertExists()
    }

    @Test
    fun muestraElEstadoDeLaConexion_yPermiteSincronizar() {
        mensajeEstado.value = "Conectado y sincronizado con el servidor MySQL"
        mostrar()

        composeRule.onNodeWithText("Conectado y sincronizado con el servidor MySQL").assertExists()
        composeRule.onNodeWithContentDescription("Sincronizar con servidor").performClick()

        verify { viewModel.fetchTecnicos() }
    }

    @Test
    fun sinTrabajadoresNiLocales_noSePuedeAsignar() {
        mostrar()

        composeRule.onNodeWithText("Asignar Local al Trabajador").assertIsNotEnabled()
    }

    @Test
    fun seleccionaPorDefectoElPrimerTrabajadorYLocal_yAsigna() {
        operarios.value = listOf(tomas, valentina)
        locales.value = listOf(alba, rosas)
        mostrar()

        composeRule.onNodeWithText("Asignar Local al Trabajador").assertIsEnabled().performClick()

        verify { viewModel.agregarTecnico("Tomás Rivera", "Técnico de mantenimiento", "Minimarket El Alba") }
    }

    @Test
    fun elegirOtroTrabajadorYOtroLocal() {
        operarios.value = listOf(tomas, valentina)
        locales.value = listOf(alba, rosas)
        mostrar()

        composeRule.onNodeWithText("Seleccionar trabajador del equipo").performClick()
        composeRule.onNodeWithText("Valentina Soto").performClick()
        composeRule.onNodeWithText("Local comercial a revisar").performClick()
        composeRule.onNodeWithText("Minimarket Las Rosas").performClick()
        composeRule.onNodeWithText("Asignar Local al Trabajador").performClick()

        // Sin cargo registrado se usa "Operario"
        verify { viewModel.agregarTecnico("Valentina Soto", "Operario", "Minimarket Las Rosas") }
    }

    @Test
    fun mientrasSincroniza_elBotonDiceGuardando() {
        operarios.value = listOf(tomas)
        locales.value = listOf(alba)
        sincronizando.value = true
        mostrar()

        composeRule.onNodeWithText("Guardando...").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Sincronizar con servidor").assertIsNotEnabled()
    }

    @Test
    fun listaDeAsignaciones_sincronizadaYPendiente() {
        tecnicos.value = listOf(
            Tecnico(id = 5, nombre = "Ana", cargo = "Vendedor", localAsignado = "Local A", sincronizado = true),
            Tecnico(id = null, nombre = "Luis", cargo = "Transportista", localAsignado = "", sincronizado = false),
            Tecnico(id = null, nombre = "Rosa", cargo = "Técnico", localAsignado = null, sincronizado = false)
        )
        mostrar()

        composeRule.onNodeWithText("Asignaciones registradas (3)").assertExists()
        composeRule.onNodeWithText("Ana").assertExists()
        composeRule.onNodeWithText("Cargo: Vendedor").assertExists()
        composeRule.onNodeWithText("Local asignado: Local A").assertExists()
        composeRule.onNodeWithText("Sincronizado en Servidor (MySQL)").assertExists()
        composeRule.onNodeWithText("Cargo: Transportista").assertExists()
        // Luis (local vacío) y Rosa (local nulo) muestran "Sin especificar" y quedan pendientes
        composeRule.onAllNodesWithText("Local asignado: Sin especificar").fetchSemanticsNodes() shouldHaveSize 2
        composeRule.onAllNodesWithText("Guardado en Local (Pendiente de Internet)").fetchSemanticsNodes() shouldHaveSize 2

        // Solo la asignación con id (guardada en el servidor) se puede eliminar
        composeRule.onNodeWithContentDescription("Eliminar").performClick()
        verify { viewModel.eliminarTecnico(5) }
    }
}
