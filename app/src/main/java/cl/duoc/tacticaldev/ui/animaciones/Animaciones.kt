package cl.duoc.tacticaldev.ui.animaciones

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import kotlin.math.roundToInt

/** Duración estándar de las transiciones entre pantallas. */
const val DURACION_TRANSICION = 300

/** Transiciones del NavHost: la pantalla nueva entra deslizándose desde la derecha. */
object TransicionesNavegacion {
    val entrada: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(tween(DURACION_TRANSICION)) { it / 4 } + fadeIn(tween(DURACION_TRANSICION))
    }
    val salida: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(DURACION_TRANSICION))
    }
    val entradaAlVolver: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(DURACION_TRANSICION))
    }
    val salidaAlVolver: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(tween(DURACION_TRANSICION)) { it / 4 } + fadeOut(tween(DURACION_TRANSICION))
    }
}

/**
 * Sacude horizontalmente el elemento cada vez que [disparador] cambia.
 * Se usa como retroalimentación cuando un formulario tiene errores.
 */
fun Modifier.sacudir(disparador: Int): Modifier = composed {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador == 0) return@LaunchedEffect
        for (valor in listOf(-18f, 18f, -12f, 12f, -6f, 6f, 0f)) {
            desplazamiento.animateTo(valor, tween(durationMillis = 45))
        }
    }
    offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
}
