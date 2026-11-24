package cl.duoc.tacticaldev

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.data.remote.ApiService
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Actividad principal: al abrir la app sin sesión guardada debe mostrar el login.
 * El microservicio se simula "sin conexión" para no llamar a internet.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = TacticalApp::class)
class MainActivityTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private var escenario: ActivityScenario<MainActivity>? = null

    @Before
    fun preparar() {
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns mockk<ApiService>() // cualquier llamada falla = sin internet
        val app = ApplicationProvider.getApplicationContext<TacticalApp>()
        runBlocking { app.container.sesionPreferences.cerrar() } // sin sesión guardada
    }

    @After
    fun limpiar() {
        escenario?.close()
        unmockkObject(RetrofitInstance)
    }

    @Test
    fun sinSesion_abreLaPantallaDeLogin() {
        escenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Bienvenido").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Ingresar").assertExists()
        composeRule.onNodeWithText("Technical Fresh").assertExists()
    }
}
