package cl.duoc.tacticaldev.ui.utils

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** Tamaños de pantalla que maneja la app (Guía 9: Window Size Classes). */
enum class TipoPantalla { COMPACTA, MEDIANA, EXPANDIDA }

/**
 * Calcula el tipo de pantalla del dispositivo a partir de las Window Size Classes
 * oficiales de Material 3. Se reutiliza en toda la app.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Activity.calcularTipoPantalla(): TipoPantalla =
    when (calculateWindowSizeClass(this).widthSizeClass) {
        WindowWidthSizeClass.Compact -> TipoPantalla.COMPACTA
        WindowWidthSizeClass.Medium -> TipoPantalla.MEDIANA
        else -> TipoPantalla.EXPANDIDA
    }

/** Número de columnas sugerido para listas en grilla según el tamaño. */
fun TipoPantalla.columnas(): Int = when (this) {
    TipoPantalla.COMPACTA -> 1
    TipoPantalla.MEDIANA -> 2
    TipoPantalla.EXPANDIDA -> 3
}

/** Permite leer el tipo de pantalla desde cualquier composable sin pasarlo por parámetro. */
val LocalTipoPantalla = staticCompositionLocalOf { TipoPantalla.COMPACTA }
