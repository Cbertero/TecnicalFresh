package cl.duoc.tacticaldev.navigation

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.TacticalApp
import cl.duoc.tacticaldev.data.remote.ApiService
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoApi
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoInstance
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.data.scanner.EscanerQr
import cl.duoc.tacticaldev.data.scanner.ResultadoEscaneo
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.enDialogo
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.viewmodel.EstadoSesion
import cl.duoc.tacticaldev.viewmodel.NavigationViewModel
import cl.duoc.tacticaldev.viewmodel.SesionViewModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Grafo de navegación completo. Se usa la app real (TacticalApp) para que cada
 * pantalla cree su ViewModel con la fábrica; la sesión se simula y las APIs se
 * reemplazan por mocks "sin internet", así ninguna prueba llama al servidor.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = TacticalApp::class)
class AppNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estadoSesion = MutableStateFlow<EstadoSesion>(EstadoSesion.Cargando)
    private val alCerrarSesion = slot<() -> Unit>()
    private val sesionViewModel: SesionViewModel = mockk(relaxed = true) {
        every { estado } returns estadoSesion
        every { cerrarSesion(capture(alCerrarSesion)) } answers { alCerrarSesion.captured() }
    }
    private val navigationViewModel = NavigationViewModel()
    private lateinit var navController: NavHostController

    @Before
    fun simularInternetCaido() {
        mockkObject(RetrofitInstance, OpenMeteoInstance, EscanerQr)
        every { RetrofitInstance.api } returns mockk<ApiService>()
        every { OpenMeteoInstance.api } returns mockk<OpenMeteoApi>()
    }

    @After
    fun limpiar() {
        unmockkObject(RetrofitInstance, OpenMeteoInstance, EscanerQr)
    }

    private fun mostrar(estado: EstadoSesion) {
        estadoSesion.value = estado
        composeRule.conTema {
            navController = rememberNavController()
            AppNavigation(
                navController = navController,
                navigationViewModel = navigationViewModel,
                sesionViewModel = sesionViewModel
            )
        }
        composeRule.waitForIdle()
    }

    // Con Robolectric la prueba corre en el hilo principal: se puede leer el NavController directo
    private fun rutaActual(): String? = navController.currentDestination?.route

    private fun irA(ruta: String, popUpTo: String? = null, inclusive: Boolean = false) {
        composeRule.runOnIdle { navigationViewModel.navegarA(ruta, popUpToRoute = popUpTo, inclusive = inclusive) }
        composeRule.waitUntil(5_000) { rutaActual() == rutaPatron(ruta) }
    }

    /** "formulario_visita/1" se registra en el NavHost como "formulario_visita/{activoId}". */
    private fun rutaPatron(ruta: String): String = when {
        ruta.startsWith("formulario_visita/") -> Screen.FormularioVisita.route
        ruta.startsWith("confirmacion/") -> Screen.Confirmacion.route
        ruta.startsWith("historial_activo/") -> Screen.HistorialActivo.route
        else -> ruta
    }

    private fun volver() {
        composeRule.onNodeWithContentDescription("Volver").performClick()
        composeRule.waitForIdle()
    }

    // ---------- Inicio según la sesión ----------

    @Test
    fun mientrasLeeLaSesion_noMuestraNingunaPantalla() {
        mostrar(EstadoSesion.Cargando)

        composeRule.onNodeWithText("Bienvenido").assertDoesNotExist()
        composeRule.onNodeWithText("TecnicalFresh").assertDoesNotExist()
    }

    @Test
    fun sinSesion_empiezaEnElLogin() {
        mostrar(EstadoSesion.SinSesion)

        composeRule.esperarTexto("Bienvenido")
        rutaActual() shouldBe Screen.Login.route
    }

    @Test
    fun supervisor_empiezaEnSuPanel() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion(nombre = "Carla", rol = Rol.SUPERVISOR, cargo = "")))

        composeRule.esperarTexto("¡Hola, Carla!")
        composeRule.onNodeWithText("Supervisor de operaciones").assertExists() // cargo vacío
        rutaActual() shouldBe Screen.DashboardSupervisor.route
    }

    @Test
    fun operario_empiezaEnSuPanelConSuCargo() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion()))

        composeRule.esperarTexto("¡Hola, Tomás Rivera!")
        composeRule.onNodeWithText("Técnico de mantenimiento").assertExists()
        rutaActual() shouldBe Screen.Dashboard.route
    }

    @Test
    fun operarioSinCargo_muestraElNombreDelRol() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion(cargo = "")))

        composeRule.esperarTexto("Operario")
    }

    // ---------- Recorridos del supervisor ----------

    @Test
    fun supervisor_recorreTodasSusPantallasYVuelve() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion(nombre = "Carla", rol = Rol.SUPERVISOR)))

        composeRule.onNodeWithText("Validar Visitas").performScrollTo().performClick()
        composeRule.esperarTexto("Visitas de terreno")
        rutaActual() shouldBe Screen.Supervisor.route
        volver()

        composeRule.onNodeWithText("Equipo").performScrollTo().performClick()
        composeRule.esperarTexto("Registro de Personal")
        rutaActual() shouldBe Screen.Equipo.route
        volver()

        composeRule.onNodeWithText("Asignar Locales").performScrollTo().performClick()
        composeRule.esperarTexto("Asignar Locales (API)")
        rutaActual() shouldBe Screen.Tecnicos.route
        volver()

        composeRule.onNodeWithText("Administración").performScrollTo().performClick()
        composeRule.waitUntil(5_000) { rutaActual() == Screen.Administracion.route }
        composeRule.esperarTexto("Usar mi ubicación actual")
        volver()

        composeRule.waitUntil(5_000) { rutaActual() == Screen.DashboardSupervisor.route }
    }

    @Test
    fun cambiarDePestana_usaNavegacionPorPestanas() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion(rol = Rol.SUPERVISOR)))

        composeRule.runOnIdle { navigationViewModel.cambiarPestana(Screen.Supervisor.route, Screen.Supervisor.route) }

        composeRule.waitUntil(5_000) { rutaActual() == Screen.Supervisor.route }
    }

    // ---------- Recorridos del operario ----------

    @Test
    fun operario_vaASusActivosYASusVisitas() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion(nombre = "Tomás")))

        composeRule.onNodeWithText("Vitrinas / Activos").performScrollTo().performClick()
        composeRule.esperarTexto("Hola, Tomás")
        rutaActual() shouldBe Screen.InicioOperario.route
        volver()

        composeRule.onNodeWithText("Mis Visitas").performScrollTo().performClick()
        composeRule.esperarTexto("Mis visitas")
        rutaActual() shouldBe Screen.MisVisitas.route
        volver()

        composeRule.waitUntil(5_000) { rutaActual() == Screen.Dashboard.route }
    }

    @Test
    fun operario_formularioConfirmacionEHistorial() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion()))

        irA(Screen.FormularioVisita.crear(1))
        composeRule.esperarTexto("Registrar visita")

        // Al guardar sin internet se reemplaza el formulario por la confirmación "en cola"
        irA(Screen.Confirmacion.crear(VisitaRepository.ID_EN_COLA), popUpTo = Screen.FormularioVisita.route, inclusive = true)
        composeRule.esperarTexto("Visita sin enviar")

        irA(Screen.HistorialActivo.crear(1))
        composeRule.esperarTexto("Historial del activo")
    }

    @Test
    fun escanearQrConError_muestraElMensaje() {
        coEvery { EscanerQr.escanear(any<Context>()) } returns ResultadoEscaneo.Error("QR dañado")
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion()))

        composeRule.onNodeWithText("Escanear QR").performScrollTo().performClick()

        composeRule.esperarTexto("QR dañado")
    }

    @Test
    fun escanearQrCancelado_noHaceNada() {
        coEvery { EscanerQr.escanear(any<Context>()) } returns ResultadoEscaneo.Cancelado
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion()))

        composeRule.onNodeWithText("Escanear QR").performScrollTo().performClick()
        composeRule.waitForIdle()

        rutaActual() shouldBe Screen.Dashboard.route
    }

    @Test
    fun escanearQrDeActivoInexistente_avisaQueNoExiste() {
        coEvery { EscanerQr.escanear(any<Context>()) } returns ResultadoEscaneo.Exito("NO-EXISTE-999")
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion()))

        composeRule.onNodeWithText("Escanear QR").performScrollTo().performClick()

        composeRule.esperarTexto("No existe un activo con el código NO-EXISTE-999")
    }

    // ---------- Cerrar sesión ----------

    @Test
    fun cerrarSesion_vuelveAlLoginSinHistorial() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion()))

        composeRule.onNodeWithText("Mi Perfil").performScrollTo().performClick()
        composeRule.enDialogo("Cerrar sesión").performClick()

        composeRule.esperarTexto("Bienvenido")
        rutaActual() shouldBe Screen.Login.route
        navController.previousBackStackEntry shouldBe null
    }

    @Test
    fun supervisorCierraSesion_tambienVuelveAlLogin() {
        mostrar(EstadoSesion.Activa(DatosPrueba.sesion(rol = Rol.SUPERVISOR)))

        composeRule.onNodeWithText("Mi Perfil").performScrollTo().performClick()
        composeRule.enDialogo("Cerrar sesión").performClick()

        composeRule.esperarTexto("Bienvenido")
    }
}
