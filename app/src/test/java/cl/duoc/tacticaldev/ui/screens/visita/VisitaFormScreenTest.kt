package cl.duoc.tacticaldev.ui.screens.visita

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.Clima
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.model.VisitaErrores
import cl.duoc.tacticaldev.model.VisitaFormUiState
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.utils.Formatos
import cl.duoc.tacticaldev.viewmodel.VisitaViewModel
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
import org.robolectric.Shadows.shadowOf

/**
 * Formulario de registro de visita (la pantalla más completa de la app).
 * Se usa una Activity real para poder simular los permisos y la cámara.
 */
@RunWith(AndroidJUnit4::class)
class VisitaFormScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val estado = MutableStateFlow(VisitaFormUiState())
    private val viewModel: VisitaViewModel = mockk(relaxed = true) { every { uiState } returns estado }
    private var volvio = 0
    private val guardadas = mutableListOf<Long>()

    /** Formulario ya cargado con el activo de prueba (vitrina freezer). */
    private fun cargado(modificar: VisitaFormUiState.() -> VisitaFormUiState = { this }) {
        estado.value = VisitaFormUiState(cargando = false, activo = DatosPrueba.activoConPunto()).modificar()
    }

    private fun mostrar() = composeRule.conTema {
        VisitaFormScreen(viewModel = viewModel, onVolver = { volvio++ }, onVisitaGuardada = { guardadas += it })
    }

    private fun concederPermisos(vararg permisos: String) {
        shadowOf(composeRule.activity.application).grantPermissions(*permisos)
    }

    /** Responde "denegado" a la última solicitud de permisos que hizo la pantalla. */
    private fun denegarUltimaSolicitudDePermisos() {
        val solicitud = shadowOf(composeRule.activity).lastRequestedPermission
        composeRule.runOnUiThread {
            @Suppress("DEPRECATION")
            composeRule.activity.onRequestPermissionsResult(
                solicitud.requestCode,
                solicitud.requestedPermissions,
                IntArray(solicitud.requestedPermissions.size) { PackageManager.PERMISSION_DENIED }
            )
        }
        composeRule.waitForIdle()
    }

    // ---------- Carga y encabezado ----------

    @Test
    fun mientrasCarga_soloMuestraElTitulo() {
        mostrar()

        composeRule.onNodeWithText("Registrar visita").assertExists()
        composeRule.onNodeWithText("Guardar visita").assertDoesNotExist()
    }

    @Test
    fun muestraElResumenDelActivo() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("TDV-VF-001 · Vitrina freezer").assertExists()
        composeRule.onNodeWithText("Minimarket El Alba · Av. Ficticia 1234, Cerrillos").assertExists()
        composeRule.onNodeWithText("La fecha y hora se registran automáticamente al guardar").assertExists()
    }

    @Test
    fun flechaVolver_vuelve() {
        cargado()
        mostrar()

        composeRule.onNodeWithContentDescription("Volver").performClick()

        volvio shouldBe 1
    }

    // ---------- Clima (API externa) ----------

    @Test
    fun clima_mientrasConsulta() {
        cargado { copy(cargandoClima = true) }
        mostrar()

        composeRule.onNodeWithText("Consultando el clima…").assertExists()
    }

    @Test
    fun clima_diaCaluroso_muestraAdvertencia() {
        cargado { copy(clima = Clima(temperatura = 28.0, humedad = 40, descripcion = "Despejado")) }
        mostrar()

        composeRule.onNodeWithText("28,0 °C · Despejado").assertExists()
        composeRule.onNodeWithText("Humedad 40%").assertExists()
        composeRule.onNodeWithText(
            "Día caluroso: verifica que el equipo mantenga su temperatura de operación"
        ).assertExists()
        composeRule.onNodeWithText("Fuente: Open-Meteo (API externa)").assertExists()
    }

    @Test
    fun clima_diaFresco_sinAdvertencia() {
        cargado { copy(clima = Clima(temperatura = 15.5, humedad = 70, descripcion = "Nublado")) }
        mostrar()

        composeRule.onNodeWithText("15,5 °C · Nublado").assertExists()
        composeRule.onNodeWithText(
            "Día caluroso: verifica que el equipo mantenga su temperatura de operación"
        ).assertDoesNotExist()
    }

    @Test
    fun clima_siFalla_permiteReintentar() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("No se pudo obtener el clima").assertExists()
        composeRule.onNodeWithText("Reintentar").performClick()

        verify { viewModel.actualizarClima() }
    }

    // ---------- Campos del formulario ----------

    @Test
    fun elegirTipoDeVisita() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("Tipo de visita").performScrollTo().performClick()
        composeRule.onNodeWithText("Visita de venta").performClick()

        verify { viewModel.onTipoVisitaChange(TipoVisita.VENTA) }
    }

    @Test
    fun elegirEstadoDelEquipo() {
        cargado { copy(estadoEquipo = EstadoEquipo.OPERATIVO) }
        mostrar()

        composeRule.onNodeWithText("Estado del equipo").assertExists()
        composeRule.onNodeWithText("Con falla").performScrollTo().performClick()
        composeRule.onNodeWithText("Con observaciones").performScrollTo().performClick()

        verify { viewModel.onEstadoEquipoChange(EstadoEquipo.CON_FALLA) }
        verify { viewModel.onEstadoEquipoChange(EstadoEquipo.CON_OBSERVACIONES) }
    }

    @Test
    fun escribirTemperaturaYObservaciones() {
        cargado { copy(observaciones = "abc") }
        mostrar()

        composeRule.onNodeWithText("Entre -35 y 50 °C").assertExists()
        composeRule.onNodeWithText("3/500").assertExists()
        composeRule.onNodeWithText("Temperatura medida").performScrollTo().performTextInput("-18")
        composeRule.onNodeWithText("Observaciones").performScrollTo().performTextInput(" ok")

        verify { viewModel.onTemperaturaChange("-18") }
        verify { viewModel.onObservacionesChange(any()) }
    }

    @Test
    fun freezerSobreMenos12Grados_muestraAlertaDeTemperaturaCritica() {
        cargado { copy(temperatura = "-5,0") }
        mostrar()

        composeRule.esperarTexto("Alerta de Temperatura Crítica")
        composeRule.onNodeWithText(
            "Rango anormal para este equipo. Posible falla no detectada en compresor o pérdida de gas."
        ).assertExists()
    }

    @Test
    fun freezerBienFrio_noMuestraAlerta() {
        cargado { copy(temperatura = "-20") }
        mostrar()

        composeRule.onNodeWithText("Alerta de Temperatura Crítica").assertDoesNotExist()
    }

    @Test
    fun conservadoraFueraDeRango_muestraAlerta() {
        estado.value = VisitaFormUiState(
            cargando = false,
            activo = DatosPrueba.activoConPunto().copy(tipo = TipoActivo.CONSERVADORA.name),
            temperatura = "10"
        )
        mostrar()

        composeRule.esperarTexto("Alerta de Temperatura Crítica")
    }

    @Test
    fun temperaturaNoNumerica_noMuestraAlerta() {
        cargado { copy(temperatura = "abc") }
        mostrar()

        composeRule.onNodeWithText("Alerta de Temperatura Crítica").assertDoesNotExist()
    }

    @Test
    fun erroresDeValidacion_seMuestranEnCadaSeccion() {
        cargado {
            copy(
                errores = VisitaErrores(
                    tipoVisita = "Selecciona el tipo de visita",
                    estadoEquipo = "Selecciona el estado del equipo",
                    temperatura = "Ingresa la temperatura",
                    foto = "La foto es obligatoria",
                    ubicacion = "Registra tu ubicación",
                    confirmacion = "Debes confirmar la visita"
                )
            )
        }
        mostrar()

        composeRule.onNodeWithText("Selecciona el tipo de visita").assertExists()
        composeRule.onNodeWithText("Selecciona el estado del equipo").assertExists()
        composeRule.onNodeWithText("Ingresa la temperatura").assertExists()
        composeRule.onNodeWithText("La foto es obligatoria").assertExists()
        composeRule.onNodeWithText("Registra tu ubicación").assertExists()
        composeRule.onNodeWithText("Debes confirmar la visita").assertExists()
    }

    // ---------- Foto de evidencia ----------

    @Test
    fun sinFoto_ofreceTomarFoto() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("Evidencia fotográfica").assertExists()
        composeRule.onNodeWithContentDescription("Sin foto").assertExists()
        composeRule.onNodeWithText("Tomar foto").assertExists()
    }

    @Test
    fun conFoto_ofreceTomarOtra() {
        cargado { copy(fotoUri = "content://cl.duoc.tacticaldev.fileprovider/evidencias/a.jpg") }
        mostrar()

        composeRule.onNodeWithText("Tomar otra foto").assertExists()
        composeRule.onNodeWithContentDescription("Foto de evidencia").assertExists()
    }

    @Test
    fun sinPermisoDeCamara_alDenegarloMuestraAviso() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("Tomar foto").performScrollTo().performClick()
        denegarUltimaSolicitudDePermisos()

        composeRule.onNodeWithText("Sin permiso de cámara no se puede registrar la evidencia").assertExists()
    }

    // ---------- Ubicación ----------

    @Test
    fun sinUbicacion_pideRegistrarla() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("Registra tu ubicación para verificar que estás en el punto de venta").assertExists()
        composeRule.onNodeWithText("Obtener ubicación").assertExists()
    }

    @Test
    fun ubicacionVerificada_muestraCoordenadasYDistancia() {
        cargado { copy(latitud = -33.516, longitud = -70.717, distanciaMetros = 12.0, ubicacionVerificada = true) }
        mostrar()

        composeRule.onNodeWithText("Lat ${"%.5f".format(-33.516)} · Lon ${"%.5f".format(-70.717)}").assertExists()
        composeRule.onNodeWithText("Ubicación verificada · a ${Formatos.distancia(12.0)} del punto").assertExists()
        composeRule.onNodeWithText("Actualizar ubicación").assertExists()
    }

    @Test
    fun ubicacionLejana_muestraFueraDeRango() {
        cargado { copy(latitud = -33.5, longitud = -70.7, distanciaMetros = 500.0, ubicacionVerificada = false) }
        mostrar()

        composeRule.onNodeWithText("Fuera de rango · a ${Formatos.distancia(500.0)} (máx. 150 m)").assertExists()
    }

    @Test
    fun mientrasObtieneUbicacion_elBotonSeDeshabilita() {
        cargado { copy(obteniendoUbicacion = true) }
        mostrar()

        composeRule.onNodeWithText("Obteniendo ubicación...").assertIsNotEnabled()
    }

    @Test
    fun sinPermisoDeUbicacion_avisaQueFueDenegado() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("Obtener ubicación").performScrollTo().performClick()
        denegarUltimaSolicitudDePermisos()

        verify { viewModel.onPermisoUbicacionDenegado() }
    }

    // ---------- Confirmación y guardado ----------

    @Test
    fun marcarLaConfirmacion() {
        cargado()
        mostrar()

        composeRule.onNodeWithText("Confirmo que estuve en el punto de venta y que la información es verídica")
            .performScrollTo()
        composeRule.onNode(isToggleable()).performClick()

        verify { viewModel.onConfirmacionChange(true) }
    }

    @Test
    fun guardar_alTerminarNavegaALaConfirmacion() {
        val alGuardar = slot<(Long) -> Unit>()
        every { viewModel.guardar(capture(alGuardar)) } answers { alGuardar.captured(42L) }
        cargado {
            copy(
                tipoVisita = TipoVisita.MANTENCION, estadoEquipo = EstadoEquipo.OPERATIVO, temperatura = "-18",
                fotoUri = "content://foto", latitud = -33.5, longitud = -70.7, distanciaMetros = 5.0,
                ubicacionVerificada = true, confirmado = true
            )
        }
        mostrar()

        composeRule.onNodeWithText("Guardar visita").performScrollTo().performClick()

        guardadas shouldContainExactly listOf(42L)
    }

    @Test
    fun mientrasGuarda_noSeMuestraElTextoDelBoton() {
        cargado { copy(guardando = true, intentosFallidos = 1) }
        mostrar()

        composeRule.onNodeWithText("Guardar visita").assertDoesNotExist()
    }

    @Test
    fun mensajeDelViewModel_seMuestraYLuegoSeMarcaComoVisto() {
        cargado { copy(mensaje = "Completa los campos marcados") }
        mostrar()

        composeRule.esperarTexto("Completa los campos marcados")
        composeRule.mainClock.advanceTimeBy(3_500)
        composeRule.waitUntil(5_000) {
            runCatching { verify { viewModel.onMensajeMostrado() } }.isSuccess
        }
    }
}
