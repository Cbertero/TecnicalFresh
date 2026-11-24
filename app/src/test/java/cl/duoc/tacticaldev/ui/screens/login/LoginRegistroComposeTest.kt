package cl.duoc.tacticaldev.ui.screens.login

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.LoginErrores
import cl.duoc.tacticaldev.model.LoginUiState
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.viewmodel.LoginViewModel
import cl.duoc.tacticaldev.viewmodel.RegistroUiState
import cl.duoc.tacticaldev.viewmodel.RegistroViewModel
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pantalla de inicio de sesión. El ViewModel se simula con MockK: así cada
 * prueba elige el estado exacto que quiere ver y comprueba qué funciones del
 * ViewModel llama la pantalla.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estado = MutableStateFlow(LoginUiState())
    private val viewModel: LoginViewModel = mockk(relaxed = true) { every { uiState } returns estado }
    private val rolesIngresados = mutableListOf<Rol>()

    private fun mostrar() {
        composeRule.conTema { LoginScreen(viewModel = viewModel, onLoginExitoso = { rolesIngresados += it }) }
    }

    @Test
    fun muestraCabeceraFormularioYVersion() {
        mostrar()

        composeRule.onNodeWithText("Technical Fresh").assertExists()
        composeRule.onNodeWithContentDescription("Logo Technical Fresh").assertExists()
        composeRule.onNodeWithText("Bienvenido").assertExists()
        composeRule.onNodeWithText("Ingresa para registrar tus visitas").assertExists()
        composeRule.onNodeWithText("v1.0 · Bertero / Durán · 2026").assertExists()
    }

    @Test
    fun indicadorDeServidor_mientrasComprueba() {
        mostrar()

        composeRule.onNodeWithText("Comprobando servidor…").assertExists()
    }

    @Test
    fun indicadorDeServidor_enLinea() {
        estado.value = LoginUiState(servidorEnLinea = true)
        mostrar()

        composeRule.onNodeWithText("Servidor en línea").assertExists()
    }

    @Test
    fun indicadorDeServidor_sinConexion_permiteReintentar() {
        estado.value = LoginUiState(servidorEnLinea = false)
        mostrar()

        composeRule.onNodeWithText("Sin conexión con el servidor · Reintentar").performClick()

        verify { viewModel.comprobarServidor() }
    }

    @Test
    fun escribirCorreoYClave_avisaAlViewModel() {
        mostrar()

        composeRule.onNodeWithText("Correo").performTextInput("tecnico@demo.cl")
        composeRule.onNodeWithText("Clave").performTextInput("Demo1234")

        verify { viewModel.onCorreoChange("tecnico@demo.cl") }
        verify { viewModel.onClaveChange("Demo1234") }
    }

    @Test
    fun ojoDeLaClave_alternaLaVisibilidad() {
        mostrar()

        composeRule.onNodeWithContentDescription("Mostrar clave").performClick()
        verify { viewModel.alternarVisibilidadClave() }

        composeRule.runOnIdle { estado.value = estado.value.copy(claveVisible = true) }
        composeRule.onNodeWithContentDescription("Ocultar clave").assertExists()
    }

    @Test
    fun erroresDeCampo_seMuestranBajoCadaCampo() {
        estado.value = LoginUiState(errores = LoginErrores(correo = "Correo inválido", clave = "Ingresa tu clave"))
        mostrar()

        composeRule.onNodeWithText("Correo inválido").assertExists()
        composeRule.onNodeWithText("Ingresa tu clave").assertExists()
    }

    @Test
    fun claveIncorrecta_muestraElMensajeDelServidor() {
        estado.value = LoginUiState(servidorEnLinea = true, errorGeneral = "Correo o clave incorrectos", intentosFallidos = 1)
        mostrar()

        composeRule.esperarTexto("No pudimos iniciar sesión")
        composeRule.onNodeWithText("Correo o clave incorrectos").assertExists()
    }

    @Test
    fun sinInternet_muestraAvisoDeConexion() {
        estado.value = LoginUiState(servidorEnLinea = false, errorGeneral = "Sin conexión")
        mostrar()

        composeRule.esperarTexto("Necesitas internet para iniciar sesión. Revisa tu conexión e inténtalo de nuevo.")
    }

    @Test
    fun ingresar_iniciaSesionYNavegaSegunElRol() {
        val alTerminar = slot<(Rol) -> Unit>()
        every { viewModel.iniciarSesion(capture(alTerminar)) } answers { alTerminar.captured(Rol.SUPERVISOR) }
        mostrar()

        composeRule.onNodeWithText("Ingresar").performScrollTo().performClick()

        rolesIngresados shouldContainExactly listOf(Rol.SUPERVISOR)
    }

    @Test
    fun mientrasCarga_elBotonSeDeshabilita() {
        estado.value = LoginUiState(cargando = true)
        mostrar()

        composeRule.onNodeWithText("Ingresar").assertDoesNotExist()
        composeRule.onNode(hasClickAction() and hasText("Ingresar")).assertDoesNotExist()
    }
}

/** Pantalla en que el supervisor crea cuentas para su equipo. */
@RunWith(AndroidJUnit4::class)
class RegistroScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val estado = MutableStateFlow(RegistroUiState())
    private val viewModel: RegistroViewModel = mockk(relaxed = true) { every { uiState } returns estado }
    private var volvio = 0
    private var registroTerminado = 0

    private fun mostrar() {
        composeRule.conTema {
            RegistroScreen(viewModel = viewModel, onVolver = { volvio++ }, onRegistroExitoso = { registroTerminado++ })
        }
    }

    private fun botonCrear() = composeRule.onNode(hasText("Crear Cuenta") and hasClickAction())

    @Test
    fun muestraElFormularioYElPie() {
        mostrar()

        composeRule.onNodeWithText("Registro de Personal").assertExists()
        composeRule.onNodeWithText("Crea la cuenta de un nuevo integrante del equipo").assertExists()
        composeRule.onNodeWithText("TecnicalFresh • Gestión de Mantenimiento").assertExists()
        botonCrear().assertIsEnabled()
    }

    @Test
    fun escribirEnLosCampos_avisaAlViewModel() {
        mostrar()

        composeRule.onNodeWithText("Nombre completo").performTextInput("Ana Pérez")
        composeRule.onNodeWithText("Correo electrónico").performTextInput("ana@demo.cl")
        composeRule.onNodeWithText("Contraseña").performScrollTo().performTextInput("Demo1234")
        composeRule.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("Demo1234")

        verify { viewModel.onNombreChange("Ana Pérez") }
        verify { viewModel.onCorreoChange("ana@demo.cl") }
        verify { viewModel.onClaveChange("Demo1234") }
        verify { viewModel.onConfirmarClaveChange("Demo1234") }
    }

    @Test
    fun operario_eligeSuEspecialidad() {
        mostrar()

        composeRule.onNodeWithText("Especialidad en terreno:").assertExists()
        composeRule.onNodeWithText("Vendedor").performClick()
        composeRule.onNodeWithText("Supervisor").performClick()

        verify { viewModel.onCargoChange("Vendedor") }
        verify { viewModel.onRolChange(Rol.SUPERVISOR) }
    }

    @Test
    fun supervisor_noTieneEspecialidad() {
        estado.value = RegistroUiState(rol = Rol.SUPERVISOR, cargo = "Supervisor de operaciones")
        mostrar()

        composeRule.onNodeWithText("Especialidad en terreno:").assertDoesNotExist()
        composeRule.onNodeWithText("Operario").performClick()
        verify { viewModel.onRolChange(Rol.OPERARIO) }
    }

    @Test
    fun muestraLosErroresDeValidacion() {
        estado.value = RegistroUiState(
            errorNombre = "El nombre debe tener al menos 3 caracteres",
            errorCorreo = "Correo inválido",
            errorClave = "Mínimo 8 caracteres",
            errorConfirmarClave = "Las claves no coinciden",
            errorGeneral = "El correo ya está registrado",
            claveVisible = true
        )
        mostrar()

        composeRule.onNodeWithText("El nombre debe tener al menos 3 caracteres").assertExists()
        composeRule.onNodeWithText("Correo inválido").assertExists()
        composeRule.onNodeWithText("Mínimo 8 caracteres").assertExists()
        composeRule.onNodeWithText("Las claves no coinciden").assertExists()
        composeRule.esperarTexto("El correo ya está registrado")
    }

    @Test
    fun crearCuenta_llamaARegistrar() {
        mostrar()

        botonCrear().performScrollTo().performClick()

        verify { viewModel.registrar(any()) }
    }

    @Test
    fun mientrasGuarda_elBotonQuedaDeshabilitado() {
        estado.value = RegistroUiState(cargando = true)
        mostrar()

        botonCrear().assertDoesNotExist() // en su lugar aparece el indicador de carga
    }

    @Test
    fun alCrearLaCuenta_muestraConfirmacionYVuelve() {
        estado.value = RegistroUiState(nombre = "  Ana Pérez ", registroExitoso = true)
        mostrar()

        composeRule.esperarTexto("Cuenta de Ana Pérez creada")
        composeRule.mainClock.advanceTimeBy(2_500)
        composeRule.waitUntil(5_000) { registroTerminado == 1 }
        registroTerminado shouldBe 1
    }

    @Test
    fun flechaVolver_vuelveAlPanel() {
        mostrar()

        composeRule.onNodeWithContentDescription("Volver").performClick()

        volvio shouldBe 1
    }
}
