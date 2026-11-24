package cl.duoc.tacticaldev.testutil

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import cl.duoc.tacticaldev.ui.theme.TacticalDEVTheme

/**
 * Ayudas para las pruebas de pantallas Compose (se ejecutan con Robolectric).
 *
 * Cómo se lee una prueba de UI:
 *   composeRule.conTema { MiPantalla(...) }          // 1. dibujar la pantalla
 *   composeRule.onNodeWithText("Ingresar").performClick()  // 2. interactuar
 *   verify { viewModel.iniciarSesion(any()) }        // 3. comprobar
 */
fun ComposeContentTestRule.conTema(oscuro: Boolean = true, contenido: @Composable () -> Unit) {
    setContent { TacticalDEVTheme(darkTheme = oscuro) { contenido() } }
}

/** Busca un texto que esté dentro de un diálogo (AlertDialog). */
fun SemanticsNodeInteractionsProvider.enDialogo(texto: String): SemanticsNodeInteraction =
    onNode(hasText(texto) and hasAnyAncestor(isDialog()))

/** Espera (avanzando el reloj de Compose) hasta que aparezca el texto indicado. */
fun ComposeTestRule.esperarTexto(texto: String, substring: Boolean = false, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(texto, substring = substring).fetchSemanticsNodes().isNotEmpty()
    }
}
