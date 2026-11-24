package cl.duoc.tacticaldev.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.ui.animaciones.sacudir
import cl.duoc.tacticaldev.ui.components.TipoAlerta
import cl.duoc.tacticaldev.ui.components.coloresEstado
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.TipoPantalla
import cl.duoc.tacticaldev.ui.utils.calcularTipoPantalla
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Tema Material 3 y colores que dependen del modo claro/oscuro. Estos valores
 * solo existen "dentro" de Compose, así que se leen dibujando un contenido mínimo.
 */
@RunWith(AndroidJUnit4::class)
class TemaComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Colores leídos desde dentro del tema. */
    private class Lectura {
        var primario = Color.Unspecified
        var fondo = Color.Unspecified
        var oscuro: Boolean? = null
        var verde = Color.Unspecified
        var verdeContenedor = Color.Unspecified
        var ambar = Color.Unspecified
        var ambarContenedor = Color.Unspecified
        var rojo = Color.Unspecified
        var rojoContenedor = Color.Unspecified
        var alertaExito = Color.Unspecified
        var alertaError = Color.Unspecified
        var alertaInfo = Color.Unspecified
        var chipPendiente: Pair<Color, Color>? = null
        var chipValidada: Pair<Color, Color>? = null
        var chipRechazada: Pair<Color, Color>? = null
    }

    private fun leerTema(oscuro: Boolean?): Lectura {
        val l = Lectura()
        composeRule.setContent {
            val contenido: @Composable () -> Unit = {
                l.primario = MaterialTheme.colorScheme.primary
                l.fondo = MaterialTheme.colorScheme.background
                l.oscuro = LocalTemaOscuro.current
                l.verde = VerdeExito
                l.verdeContenedor = VerdeExitoContenedor
                l.ambar = AmbarAlerta
                l.ambarContenedor = AmbarAlertaContenedor
                l.rojo = RojoError
                l.rojoContenedor = RojoErrorContenedor
                l.alertaExito = TipoAlerta.EXITO.color
                l.alertaError = TipoAlerta.ERROR.color
                l.alertaInfo = TipoAlerta.INFO.color
                l.chipPendiente = coloresEstado(EstadoValidacion.PENDIENTE)
                l.chipValidada = coloresEstado(EstadoValidacion.VALIDADA)
                l.chipRechazada = coloresEstado(EstadoValidacion.RECHAZADA)
            }
            if (oscuro == null) TacticalDEVTheme { contenido() } else TacticalDEVTheme(darkTheme = oscuro) { contenido() }
        }
        composeRule.waitForIdle()
        return l
    }

    @Test
    fun modoOscuro_usaLaPaletaArcticCyanOscura() {
        val l = leerTema(oscuro = true)

        l.oscuro shouldBe true
        l.primario shouldBe CianFrio
        l.fondo shouldBe FondoNocheGlacial
        l.verde shouldBe VerdeExitoOscuro
        l.verdeContenedor shouldBe VerdeExitoContenedorOscuro
        l.ambar shouldBe AmbarAlertaOscuro
        l.ambarContenedor shouldBe AmbarAlertaContenedorOscuro
        l.rojo shouldBe RojoErrorOscuro
        l.rojoContenedor shouldBe RojoErrorContenedorOscuro
    }

    @Test
    fun modoClaro_usaLaPaletaClara() {
        val l = leerTema(oscuro = false)

        l.oscuro shouldBe false
        l.primario shouldBe CianFrioOscuro
        l.fondo shouldBe FondoHieloClaro
        l.verde shouldBe VerdeExitoClaro
        l.verdeContenedor shouldBe VerdeExitoContenedorClaro
        l.ambar shouldBe AmbarAlertaClaro
        l.ambarContenedor shouldBe AmbarAlertaContenedorClaro
        l.rojo shouldBe RojoErrorClaro
        l.rojoContenedor shouldBe RojoErrorContenedorClaro
    }

    @Test
    fun porDefecto_sigueElModoDelSistema() {
        // Robolectric simula un teléfono en modo claro
        val l = leerTema(oscuro = null)

        l.oscuro shouldBe false
        l.primario shouldBe CianFrioOscuro
    }

    @Test
    fun alertasYChips_usanLosColoresSemanticos() {
        val l = leerTema(oscuro = true)

        l.alertaExito shouldBe VerdeExitoOscuro
        l.alertaError shouldBe RojoErrorOscuro
        l.alertaInfo shouldBe Color(0xFF38BDF8)
        l.chipPendiente shouldBe (AmbarAlertaContenedorOscuro to AmbarAlertaOscuro)
        l.chipValidada shouldBe (VerdeExitoContenedorOscuro to VerdeExitoOscuro)
        l.chipRechazada shouldBe (RojoErrorContenedorOscuro to RojoErrorOscuro)
    }

    @Test
    fun sacudir_seAnimaSinRomperElContenido() {
        var disparador by mutableIntStateOf(0)
        composeRule.setContent {
            Box(Modifier.size(50.dp).sacudir(disparador).testTag("caja"))
        }
        composeRule.onNodeWithTag("caja").assertExists()

        composeRule.runOnIdle { disparador = 1 } // simula un intento fallido
        composeRule.mainClock.advanceTimeBy(1_000)

        composeRule.onNodeWithTag("caja").assertExists()
    }
}

/** Cálculo del tipo de pantalla con las Window Size Classes de Material 3. */
@RunWith(AndroidJUnit4::class)
class WindowSizeUtilsComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun calcular(): TipoPantalla? {
        var tipo: TipoPantalla? = null
        composeRule.setContent { tipo = composeRule.activity.calcularTipoPantalla() }
        composeRule.waitForIdle()
        return tipo
    }

    @Test
    fun telefono_esCompacta() {
        calcular() shouldBe TipoPantalla.COMPACTA
    }

    @Test
    @Config(qualifiers = "w700dp-h1000dp")
    fun tabletVertical_esMediana() {
        calcular() shouldBe TipoPantalla.MEDIANA
    }

    @Test
    @Config(qualifiers = "w1000dp-h700dp")
    fun tabletHorizontal_esExpandida() {
        calcular() shouldBe TipoPantalla.EXPANDIDA
    }

    @Test
    fun localTipoPantalla_porDefectoEsCompacta() {
        var tipo: TipoPantalla? = null
        composeRule.setContent { tipo = LocalTipoPantalla.current }
        composeRule.waitForIdle()

        tipo shouldBe TipoPantalla.COMPACTA
    }
}
