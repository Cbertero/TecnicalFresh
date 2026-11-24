package cl.duoc.tacticaldev.ui.animaciones

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk

/**
 * Transiciones del NavHost. Cada una es una función que necesita un "scope" de
 * animación; aquí le pasamos uno simulado y comprobamos que siempre entrega
 * una animación real (no la transición vacía "None").
 */
class TransicionesNavegacionTest : FunSpec({

    val scope: AnimatedContentTransitionScope<NavBackStackEntry> = mockk(relaxed = true)

    test("todas las transiciones duran 300 ms") {
        DURACION_TRANSICION shouldBe 300
    }

    test("al avanzar, la pantalla nueva entra con animación") {
        TransicionesNavegacion.entrada.invoke(scope) shouldNotBe EnterTransition.None
    }

    test("al avanzar, la pantalla anterior se desvanece") {
        TransicionesNavegacion.salida.invoke(scope) shouldNotBe ExitTransition.None
    }

    test("al volver, la pantalla anterior reaparece") {
        TransicionesNavegacion.entradaAlVolver.invoke(scope) shouldNotBe EnterTransition.None
    }

    test("al volver, la pantalla actual sale deslizándose") {
        TransicionesNavegacion.salidaAlVolver.invoke(scope) shouldNotBe ExitTransition.None
    }
})
