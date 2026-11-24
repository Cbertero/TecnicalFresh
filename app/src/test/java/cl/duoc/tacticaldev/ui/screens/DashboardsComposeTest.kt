package cl.duoc.tacticaldev.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.enDialogo
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.ui.screens.dashboard.DashboardScreen
import cl.duoc.tacticaldev.ui.screens.dashboard.DashboardSupervisorScreen
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Panel principal del operario. */
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val acciones = mutableListOf<String>()
    private val snackbar = SnackbarHostState()
    private lateinit var scope: CoroutineScope

    private fun mostrar(conSnackbar: Boolean = true) {
        composeRule.conTema {
            scope = rememberCoroutineScope()
            DashboardScreen(
                nombreUsuario = "Tomás",
                rolUsuario = "TÉCNICO DE MANTENIMIENTO",
                onIrAActivos = { acciones += "activos" },
                onIrAMisVisitas = { acciones += "mis_visitas" },
                onEscanearQr = { acciones += "qr" },
                onCerrarSesion = { acciones += "salir" },
                snackbarHostState = if (conSnackbar) snackbar else null
            )
        }
    }

    @Test
    fun muestraElSaludoYElCargoConFormato() {
        mostrar()

        composeRule.onNodeWithText("TecnicalFresh").assertExists()
        composeRule.onNodeWithText("¡Hola, Tomás!").assertExists()
        composeRule.onNodeWithText("Técnico de mantenimiento").assertExists()
        composeRule.onNodeWithText("TecnicalFresh • Gestión de Mantenimiento").assertExists()
    }

    @Test
    fun cadaBotonDelPanelLlamaASuAccion() {
        mostrar(conSnackbar = false)

        composeRule.onNodeWithText("Vitrinas / Activos").performScrollTo().performClick()
        composeRule.onNodeWithText("Mis Visitas").performScrollTo().performClick()
        composeRule.onNodeWithText("Escanear QR").performScrollTo().performClick()

        acciones shouldContainExactly listOf("activos", "mis_visitas", "qr")
    }

    @Test
    fun cerrarSesion_pideConfirmacionYSePuedeCancelar() {
        mostrar()

        composeRule.onNodeWithText("Mi Perfil").performScrollTo().performClick()
        composeRule.onNodeWithText("¿Estás seguro de que deseas salir de tu cuenta?").assertExists()
        composeRule.enDialogo("Cancelar").performClick()

        composeRule.onNodeWithText("¿Estás seguro de que deseas salir de tu cuenta?").assertDoesNotExist()
        acciones.shouldBeEmpty()
    }

    @Test
    fun cerrarSesion_alConfirmarSaleDeLaCuenta() {
        mostrar()

        composeRule.onNodeWithText("Mi Perfil").performScrollTo().performClick()
        composeRule.enDialogo("Cerrar sesión").performClick()

        acciones shouldContainExactly listOf("salir")
    }

    @Test
    fun losMensajesDelEscanerAparecenEnElSnackbar() {
        mostrar()

        composeRule.runOnIdle { scope.launch { snackbar.showSnackbar("El código QR no existe") } }

        composeRule.esperarTexto("El código QR no existe")
    }
}

/** Panel principal del supervisor. */
@RunWith(AndroidJUnit4::class)
class DashboardSupervisorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val acciones = mutableListOf<String>()

    private fun mostrar(cargo: String = "Supervisor de operaciones") {
        composeRule.conTema {
            DashboardSupervisorScreen(
                nombreUsuario = "Carla",
                cargoUsuario = cargo,
                onIrAVisitas = { acciones += "visitas" },
                onIrAAdministracion = { acciones += "admin" },
                onCerrarSesion = { acciones += "salir" },
                onIrAEquipo = { acciones += "equipo" },
                onIrATecnicos = { acciones += "tecnicos" }
            )
        }
    }

    @Test
    fun muestraSaludoYCargo() {
        mostrar()

        composeRule.onNodeWithText("¡Hola, Carla!").assertExists()
        composeRule.onNodeWithText("Supervisor de operaciones").assertExists()
    }

    @Test
    fun sinCargo_muestraSupervision() {
        mostrar(cargo = "")

        composeRule.onNodeWithText("Supervisión").assertExists()
    }

    @Test
    fun cadaBotonDelPanelLlamaASuAccion() {
        mostrar()

        composeRule.onNodeWithText("Validar Visitas").performScrollTo().performClick()
        composeRule.onNodeWithText("Administración").performScrollTo().performClick()
        composeRule.onNodeWithText("Equipo").performScrollTo().performClick()
        composeRule.onNodeWithText("Asignar Locales").performScrollTo().performClick()

        acciones shouldContainExactly listOf("visitas", "admin", "equipo", "tecnicos")
    }

    @Test
    fun cerrarSesion_cancelarYLuegoConfirmar() {
        mostrar()

        composeRule.onNodeWithText("Mi Perfil").performScrollTo().performClick()
        composeRule.enDialogo("Cancelar").performClick()
        acciones.shouldBeEmpty()

        composeRule.onNodeWithText("Mi Perfil").performScrollTo().performClick()
        composeRule.enDialogo("Cerrar sesión").performClick()
        acciones shouldContainExactly listOf("salir")
    }
}

/** Pantalla genérica para módulos que aún no se implementan. */
@RunWith(AndroidJUnit4::class)
class PantallaEnConstruccionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraElAvisoYPermiteVolver() {
        var volvio = false
        composeRule.conTema { PantallaEnConstruccion(titulo = "Reportes", onVolver = { volvio = true }) }

        composeRule.onNodeWithText("Reportes").assertExists()
        composeRule.onNodeWithText("Módulo en Construcción").assertExists()
        composeRule.onNodeWithText("Esta funcionalidad estará disponible en la próxima versión.").assertExists()
        composeRule.onNodeWithContentDescription("Volver").performClick()

        volvio shouldBe true
    }

    @Test
    fun aceptaUnSubtituloPersonalizado() {
        composeRule.conTema { PantallaEnConstruccion(titulo = "Mapa", subtitulo = "Pronto", onVolver = {}) }

        composeRule.onNodeWithText("Pronto").assertExists()
    }

}
