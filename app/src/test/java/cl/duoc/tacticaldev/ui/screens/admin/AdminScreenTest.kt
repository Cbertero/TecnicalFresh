package cl.duoc.tacticaldev.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.model.ActivoErrores
import cl.duoc.tacticaldev.model.ActivoForm
import cl.duoc.tacticaldev.model.AdminUiState
import cl.duoc.tacticaldev.model.PuntoVentaErrores
import cl.duoc.tacticaldev.model.PuntoVentaForm
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.testutil.esperarTexto
import cl.duoc.tacticaldev.viewmodel.AdminViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/** Administración: registro de puntos de venta y de activos (pestañas). */
@RunWith(AndroidJUnit4::class)
class AdminScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val estado = MutableStateFlow(AdminUiState())
    private val puntos = MutableStateFlow(
        listOf(
            PuntoVentaEntity(1, "Minimarket El Alba", "Av. Ficticia 1234", "Cerrillos", -33.5, -70.7),
            PuntoVentaEntity(2, "Almacén Los Aromos", "Pasaje Demo 456", "Maipú", -33.5, -70.7)
        )
    )
    private val mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val viewModel: AdminViewModel = mockk(relaxed = true) {
        every { uiState } returns estado
        every { puntosVenta } returns puntos
        every { this@mockk.mensajes } returns this@AdminScreenTest.mensajes
    }

    private fun mostrar() = composeRule.conTema { AdminScreen(viewModel = viewModel) }

    // ---------- Pestaña Punto de venta ----------

    @Test
    fun puntoDeVenta_muestraLosCamposYLasPestanas() {
        mostrar()

        composeRule.onNodeWithText("Punto de venta").assertExists()
        composeRule.onNodeWithText("Activo").assertExists()
        composeRule.onNodeWithText("Nombre del local").assertExists()
        composeRule.onNodeWithText("Usar mi ubicación actual").assertExists()
        composeRule.onNodeWithText("Registrar punto de venta").assertExists()
        composeRule.onNodeWithText("TecnicalFresh • Gestión de Mantenimiento").assertExists()
    }

    @Test
    fun puntoDeVenta_escribirEnLosCampos_avisaAlViewModel() {
        mostrar()

        composeRule.onNodeWithText("Nombre del local").performTextInput("Kiosko")
        composeRule.onNodeWithText("Dirección").performTextInput("Calle 1")
        composeRule.onNodeWithText("Comuna").performTextInput("Maipú")
        composeRule.onNodeWithText("Latitud").performTextInput("-33.5")
        composeRule.onNodeWithText("Longitud").performTextInput("-70.7")

        verify { viewModel.onNombrePuntoChange("Kiosko") }
        verify { viewModel.onDireccionChange("Calle 1") }
        verify { viewModel.onComunaChange("Maipú") }
        verify { viewModel.onLatitudChange("-33.5") }
        verify { viewModel.onLongitudChange("-70.7") }
    }

    @Test
    fun puntoDeVenta_muestraErroresYGuarda() {
        estado.value = AdminUiState(
            puntoVenta = PuntoVentaForm(errores = PuntoVentaErrores(nombre = "Ingresa el nombre", latitud = "Latitud inválida"))
        )
        mostrar()

        composeRule.onNodeWithText("Ingresa el nombre").assertExists()
        composeRule.onNodeWithText("Latitud inválida").assertExists()
        composeRule.onNodeWithText("Registrar punto de venta").performScrollTo().performClick()

        verify { viewModel.guardarPuntoVenta() }
    }

    @Test
    fun puntoDeVenta_conPermiso_usaLaUbicacionActual() {
        shadowOf(composeRule.activity.application).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        mostrar()

        composeRule.onNodeWithText("Usar mi ubicación actual").performScrollTo().performClick()
        composeRule.waitForIdle() // Android entrega el resultado del permiso en el hilo principal

        verify { viewModel.usarUbicacionActual() }
    }

    @Test
    fun puntoDeVenta_sinPermiso_avisaQueFueDenegado() {
        mostrar()

        composeRule.onNodeWithText("Usar mi ubicación actual").performScrollTo().performClick()
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

        verify { viewModel.onPermisoUbicacionDenegado() }
    }

    @Test
    fun puntoDeVenta_mientrasObtieneUbicacionOGuarda_deshabilitaBotones() {
        estado.value = AdminUiState(obteniendoUbicacion = true, guardando = true)
        mostrar()

        composeRule.onNodeWithText("Usar mi ubicación actual").assertIsNotEnabled()
        composeRule.onNodeWithText("Registrar punto de venta").assertDoesNotExist()
    }

    @Test
    fun tocarLaPestanaActivo_cambiaDePestana() {
        mostrar()

        composeRule.onNodeWithText("Activo").performClick()

        verify { viewModel.onPestanaChange(1) }
    }

    // ---------- Pestaña Activo ----------

    @Test
    fun activo_muestraLosCamposYPermiteEscribir() {
        estado.value = AdminUiState(pestana = 1)
        mostrar()

        composeRule.onNodeWithText("Ejemplo: TDV-VF-004").assertExists()
        composeRule.onNodeWithText("Código del QR").performTextInput("TDV-VF-009")
        composeRule.onNodeWithText("Marca").performTextInput("FrioMax")
        composeRule.onNodeWithText("Modelo").performTextInput("VF-1")
        pestanaPuntoDeVenta().performClick() // vuelve a la otra pestaña

        verify { viewModel.onCodigoChange("TDV-VF-009") }
        verify { viewModel.onMarcaChange("FrioMax") }
        verify { viewModel.onModeloChange("VF-1") }
        verify { viewModel.onPestanaChange(0) }
    }

    @Test
    fun activo_elegirTipo_avisaAlViewModel() {
        estado.value = AdminUiState(pestana = 1)
        mostrar()

        composeRule.onNodeWithText("Tipo de activo").performClick()
        composeRule.onNodeWithText("Conservadora").performClick()

        verify { viewModel.onTipoActivoChange(TipoActivo.CONSERVADORA) }
    }

    @Test
    fun activo_elegirPuntoDeVenta_entregaSuId() {
        estado.value = AdminUiState(pestana = 1)
        mostrar()

        selectorPuntoDeVenta().performClick()
        composeRule.onNodeWithText("Almacén Los Aromos · Maipú").performClick()

        verify { viewModel.onPuntoVentaChange(2) }
    }

    @Test
    fun activo_muestraSeleccionErroresYGuarda() {
        estado.value = AdminUiState(
            pestana = 1,
            activo = ActivoForm(
                tipo = TipoActivo.VITRINA_FREEZER,
                puntoVentaId = 1,
                errores = ActivoErrores(codigo = "El código ya existe", marca = "Ingresa la marca")
            )
        )
        mostrar()

        composeRule.onNodeWithText("Vitrina freezer").assertExists()
        composeRule.onNodeWithText("Minimarket El Alba · Cerrillos").assertExists()
        composeRule.onNodeWithText("El código ya existe").assertExists()
        composeRule.onNodeWithText("Ingresa la marca").assertExists()
        composeRule.onNodeWithText("Registrar activo").performScrollTo().performClick()

        verify { viewModel.guardarActivo() }
    }

    // ---------- Mensajes ----------

    @Test
    fun mensajeDelViewModel_apareceComoAlerta() {
        mostrar()

        composeRule.runOnIdle { mensajes.tryEmit("Punto de venta registrado") }

        composeRule.esperarTexto("Punto de venta registrado")
    }

    // En la pestaña "Activo" el texto "Punto de venta" aparece dos veces:
    // [0] la pestaña (arriba) y [1] la lista desplegable del formulario.
    private fun pestanaPuntoDeVenta() = composeRule.onAllNodes(hasText("Punto de venta"))[0]
    private fun selectorPuntoDeVenta() = composeRule.onAllNodes(hasText("Punto de venta"))[1]
}
