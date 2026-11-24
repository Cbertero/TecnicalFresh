package cl.duoc.tacticaldev.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.navigation.ItemNavegacion
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.conTema
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.TipoPantalla
import cl.duoc.tacticaldev.utils.Formatos
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Componentes reutilizables de la interfaz (ui/components). */
@RunWith(AndroidJUnit4::class)
class ComponentesComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---------- AlertaFlotanteAnimada ----------

    @Test
    fun alerta_sinMensaje_noMuestraNada() {
        composeRule.conTema { AlertaFlotanteAnimada(mensaje = null) }

        composeRule.onNodeWithText("Visita validada").assertDoesNotExist()
    }

    @Test
    fun alerta_mensajeEnBlanco_noMuestraNada() {
        composeRule.conTema { AlertaFlotanteAnimada(mensaje = "   ") }

        composeRule.onNodeWithText("   ").assertDoesNotExist()
    }

    @Test
    fun alerta_conMensaje_loMuestra() {
        composeRule.conTema { AlertaFlotanteAnimada(mensaje = "Visita validada") }

        composeRule.onNodeWithText("Visita validada").assertExists()
    }

    @Test
    fun alerta_mensajeDeRechazo_tambienSeMuestra() {
        composeRule.conTema { AlertaFlotanteAnimada(mensaje = "Visita rechazada") }

        composeRule.onNodeWithText("Visita rechazada").assertExists()
    }

    @Test
    fun alerta_tipoInfo_seMuestra() {
        composeRule.conTema(oscuro = false) { AlertaFlotanteAnimada(mensaje = "Sincronizando", tipo = TipoAlerta.INFO) }

        composeRule.onNodeWithText("Sincronizando").assertExists()
    }

    // ---------- CampoTexto ----------

    @Test
    fun campoTexto_muestraEtiquetaYEnviaLoEscrito() {
        val escrito = mutableListOf<String>()
        composeRule.conTema {
            CampoTexto(valor = "", onValorChange = { escrito += it }, etiqueta = "Correo", icono = Icons.Filled.Email)
        }

        composeRule.onNodeWithText("Correo").performTextInput("ana@demo.cl")

        escrito shouldContainExactly listOf("ana@demo.cl")
    }

    @Test
    fun campoTexto_conError_muestraMensajeEIcono() {
        composeRule.conTema {
            CampoTexto(
                valor = "x", onValorChange = {}, etiqueta = "Correo", icono = Icons.Filled.Email,
                error = "Correo inválido", ayuda = "No se debe ver"
            )
        }

        composeRule.onNodeWithText("Correo inválido").assertExists()
        composeRule.onNodeWithContentDescription("Error").assertExists()
        composeRule.onNodeWithText("No se debe ver").assertDoesNotExist()
    }

    @Test
    fun campoTexto_sinError_muestraAyudaYSufijo() {
        composeRule.conTema {
            CampoTexto(
                valor = "-18", onValorChange = {}, etiqueta = "Temperatura", icono = Icons.Filled.Email,
                ayuda = "Entre -35 y 50", sufijo = "°C", singleLine = false, minLines = 2
            )
        }

        composeRule.onNodeWithText("Entre -35 y 50").assertExists()
        composeRule.onNodeWithText("°C").assertExists()
        composeRule.onNodeWithContentDescription("Error").assertDoesNotExist()
    }

    @Test
    fun campoTexto_iconoFinalPersonalizado_yDeshabilitado() {
        var tocado = false
        composeRule.conTema {
            CampoTexto(
                valor = "", onValorChange = {}, etiqueta = "Clave", icono = Icons.Filled.Email,
                error = "Requerida", enabled = false,
                trailingIcon = {
                    IconButton(onClick = { tocado = true }) { Icon(Icons.Filled.Visibility, contentDescription = "Mostrar") }
                }
            )
        }

        // Con un ícono propio no se muestra el ícono de error
        composeRule.onNodeWithContentDescription("Error").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Mostrar").performClick()
        tocado shouldBe true
        composeRule.onNodeWithText("Clave").assertIsNotEnabled()
    }

    // ---------- ChipEstado y EstadoVacio ----------

    @Test
    fun chipEstado_muestraLaEtiquetaDeCadaEstado() {
        composeRule.conTema {
            androidx.compose.foundation.layout.Column {
                EstadoValidacion.entries.forEach { ChipEstado(it) }
            }
        }

        composeRule.onNodeWithText("Pendiente").assertExists()
        composeRule.onNodeWithText("Validada").assertExists()
        composeRule.onNodeWithText("Rechazada").assertExists()
    }

    @Test
    fun estadoVacio_muestraTituloYMensaje() {
        composeRule.conTema {
            EstadoVacio(icono = Icons.Filled.History, titulo = "Sin visitas", mensaje = "Registra la primera")
        }

        composeRule.onNodeWithText("Sin visitas").assertExists()
        composeRule.onNodeWithText("Registra la primera").assertExists()
    }

    // ---------- FotoEvidencia ----------

    @Test
    fun fotoEvidencia_sinFoto_muestraIconoPorDefecto() {
        composeRule.conTema { FotoEvidencia(uri = null) }

        composeRule.onNodeWithContentDescription("Sin foto").assertExists()
    }

    @Test
    fun fotoEvidencia_textoVacio_tambienEsSinFoto() {
        composeRule.conTema { FotoEvidencia(uri = "") }

        composeRule.onNodeWithContentDescription("Sin foto").assertExists()
    }

    @Test
    fun fotoEvidencia_fotoLocal_muestraLaImagen() {
        composeRule.conTema { FotoEvidencia(uri = "content://cl.duoc.tacticaldev.fileprovider/evidencias/a.jpg") }

        composeRule.onNodeWithContentDescription("Foto de evidencia").assertExists()
        composeRule.onNodeWithContentDescription("Sin foto").assertDoesNotExist()
    }

    @Test
    fun fotoEvidencia_fotoEnBase64DelServidor_muestraLaImagen() {
        composeRule.conTema { FotoEvidencia(uri = "data:image/jpeg;base64,AAAA", descripcion = "Evidencia del servidor") }

        composeRule.onNodeWithContentDescription("Evidencia del servidor").assertExists()
    }

    // ---------- Selector ----------

    @Test
    fun selector_alElegirUnaOpcion_laEntrega() {
        val elegidas = mutableListOf<TipoVisita>()
        composeRule.conTema {
            Selector(
                etiqueta = "Tipo de visita",
                opciones = TipoVisita.entries,
                seleccion = null,
                textoOpcion = { it.etiqueta },
                onSeleccion = { elegidas += it },
                icono = Icons.Filled.History
            )
        }

        composeRule.onNodeWithText("Tipo de visita").performClick()
        composeRule.onNodeWithText("Revisión por falla").performClick()

        elegidas shouldContainExactly listOf(TipoVisita.FALLA)
        // al elegir, el menú se cierra
        composeRule.onNodeWithText("Entrega o retiro").assertDoesNotExist()
    }

    @Test
    fun selector_muestraLaSeleccionYElError() {
        composeRule.conTema {
            Selector(
                etiqueta = "Tipo de activo",
                opciones = TipoActivo.entries,
                seleccion = TipoActivo.CONSERVADORA,
                textoOpcion = { it.etiqueta },
                onSeleccion = {},
                icono = Icons.Filled.History,
                error = "Selecciona un tipo"
            )
        }

        composeRule.onNodeWithText("Conservadora").assertExists()
        composeRule.onNodeWithText("Selecciona un tipo").assertExists()
    }

    // ---------- TacticalScaffold ----------

    private val items = listOf(
        ItemNavegacion("inicio", "Inicio", Icons.Filled.Home),
        ItemNavegacion("historial", "Historial", Icons.Filled.History)
    )

    @Test
    fun scaffold_muestraTituloYBotonesDeVolverYSalir() {
        var volvio = false
        var salio = false
        composeRule.conTema {
            TacticalScaffold(
                titulo = "Mis visitas",
                onVolver = { volvio = true },
                onCerrarSesion = { salio = true },
                snackbarHostState = SnackbarHostState(),
                floatingActionButton = { Button(onClick = {}) { Text("Nuevo") } }
            ) { Text("Contenido") }
        }

        composeRule.onNodeWithText("Mis visitas").assertExists()
        composeRule.onNodeWithText("Contenido").assertExists()
        composeRule.onNodeWithText("Nuevo").assertExists()
        composeRule.onNodeWithContentDescription("Volver").performClick()
        composeRule.onNodeWithContentDescription("Cerrar sesión").performClick()
        volvio shouldBe true
        salio shouldBe true
    }

    @Test
    fun scaffold_sinAcciones_noMuestraBotones() {
        composeRule.conTema { TacticalScaffold(titulo = "Simple") { Text("Hola") } }

        composeRule.onNodeWithContentDescription("Volver").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Cerrar sesión").assertDoesNotExist()
    }

    @Test
    fun scaffold_enTelefono_usaBarraInferior() {
        val rutas = mutableListOf<String>()
        composeRule.conTema {
            TacticalScaffold(titulo = "App", itemsNavegacion = items, rutaActual = "inicio", onNavegar = { rutas += it }) {
                Text("Pantalla")
            }
        }

        composeRule.onNodeWithText("Historial").performClick()

        rutas shouldContainExactly listOf("historial")
    }

    @Test
    fun scaffold_enTablet_usaRielLateral() {
        val rutas = mutableListOf<String>()
        composeRule.conTema {
            CompositionLocalProvider(LocalTipoPantalla provides TipoPantalla.MEDIANA) {
                TacticalScaffold(titulo = "App", itemsNavegacion = items, rutaActual = "historial", onNavegar = { rutas += it }) {
                    Text("Pantalla")
                }
            }
        }

        composeRule.onNodeWithText("Pantalla").assertExists()
        composeRule.onNodeWithText("Inicio").performClick()

        rutas shouldContainExactly listOf("inicio")
    }

    // ---------- TarjetaActivo ----------

    @Test
    fun tarjetaActivo_muestraDatosYBotones() {
        var registrar = 0
        var historial = 0
        composeRule.conTema {
            TarjetaActivo(DatosPrueba.activoConPunto(), onRegistrarVisita = { registrar++ }, onVerHistorial = { historial++ })
        }

        composeRule.onNodeWithText("TDV-VF-001").assertExists()
        composeRule.onNodeWithText("Vitrina freezer · FrioMax VF-300").assertExists()
        composeRule.onNodeWithText("Minimarket El Alba · Av. Ficticia 1234, Cerrillos").assertExists()
        composeRule.onNodeWithText("Sin visitas registradas").assertExists()
        composeRule.onNodeWithContentDescription("Vitrina freezer").assertExists()

        composeRule.onNodeWithText("Registrar visita").performClick()
        composeRule.onNodeWithText("Historial").performClick()
        registrar shouldBe 1
        historial shouldBe 1
    }

    @Test
    fun tarjetaActivo_conservadoraConVisitaAnterior() {
        val activo = DatosPrueba.activoConPunto().copy(tipo = TipoActivo.CONSERVADORA.name, ultimaVisita = 86_400_000L)
        composeRule.conTema { TarjetaActivo(activo, onRegistrarVisita = {}, onVerHistorial = {}) }

        composeRule.onNodeWithContentDescription("Conservadora").assertExists()
        composeRule.onNodeWithText("Última visita: ${Formatos.fechaHora(86_400_000L)}").assertExists()
    }

    // ---------- TarjetaVisita ----------

    @Test
    fun tarjetaVisita_muestraElResumenDeLaVisita() {
        composeRule.conTema { TarjetaVisita(visita = DatosPrueba.visitaDetalle()) }

        composeRule.onNodeWithText(Formatos.fechaHora(1_000L)).assertExists()
        composeRule.onNodeWithText("TDV-VF-001 · Minimarket El Alba").assertExists()
        composeRule.onNodeWithText("Mantención preventiva · Operativo · -18.0 °C").assertExists()
        composeRule.onNodeWithText("Pendiente").assertExists()
        composeRule.onNodeWithText("Ubicación verificada · 12 m").assertExists()
        composeRule.onNodeWithText("Todo en orden").assertExists()
        composeRule.onNodeWithText("Operario: Tomás Rivera").assertDoesNotExist()
    }

    @Test
    fun tarjetaVisita_freezerSobreMenos12Grados_marcaTemperaturaCritica() {
        val visita = DatosPrueba.visitaDetalle().copy(temperatura = -5.0)
        composeRule.conTema { TarjetaVisita(visita = visita, mostrarActivo = false, mostrarOperario = true) }

        composeRule.onNodeWithText("Mantención preventiva · Operativo · ⚠️ -5.0 °C").assertExists()
        composeRule.onNodeWithText("Operario: Tomás Rivera").assertExists()
        composeRule.onNodeWithText("TDV-VF-001 · Minimarket El Alba").assertDoesNotExist()
    }

    @Test
    fun tarjetaVisita_conservadora_fueraDeRangoEsCritica() {
        val base = DatosPrueba.visitaDetalle().copy(activoTipo = TipoActivo.CONSERVADORA.name)
        composeRule.conTema {
            androidx.compose.foundation.layout.Column {
                TarjetaVisita(visita = base.copy(id = 1, temperatura = 8.0))
                TarjetaVisita(visita = base.copy(id = 2, temperatura = -3.0))
                TarjetaVisita(visita = base.copy(id = 3, temperatura = 4.0))
            }
        }

        composeRule.onNodeWithText("Mantención preventiva · Operativo · ⚠️ 8.0 °C").assertExists()
        composeRule.onNodeWithText("Mantención preventiva · Operativo · ⚠️ -3.0 °C").assertExists()
        composeRule.onNodeWithText("Mantención preventiva · Operativo · 4.0 °C").assertExists()
    }

    @Test
    fun tarjetaVisita_rechazadaFueraDeRango_muestraMotivo() {
        val visita = DatosPrueba.visitaDetalle(estadoValidacion = EstadoValidacion.RECHAZADA.name).copy(
            motivoRechazo = "Foto borrosa",
            ubicacionVerificada = false,
            distanciaMetros = 1_500.0,
            observaciones = ""
        )
        composeRule.conTema { TarjetaVisita(visita = visita) }

        composeRule.onNodeWithText("Rechazada").assertExists()
        composeRule.onNodeWithText("Motivo del rechazo: Foto borrosa").assertExists()
        composeRule.onNodeWithText("Fuera de rango · ${Formatos.distancia(1_500.0)} del punto").assertExists()
    }

    @Test
    fun tarjetaVisita_validada_conAccionesDelSupervisor() {
        var validada = false
        val visita = DatosPrueba.visitaDetalle(estadoValidacion = EstadoValidacion.VALIDADA.name)
        composeRule.conTema {
            TarjetaVisita(visita = visita, acciones = { Button(onClick = { validada = true }) { Text("Validar") } })
        }

        composeRule.onNodeWithText("Validada").assertExists()
        composeRule.onNodeWithText("Validar").performClick()
        validada shouldBe true
    }
}
