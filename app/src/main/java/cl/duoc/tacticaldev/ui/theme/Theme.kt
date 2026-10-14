package cl.duoc.tacticaldev.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val ArcticDarkColorScheme = darkColorScheme(
    primary = CianFrio,
    onPrimary = Color(0xFF082F49),
    primaryContainer = CianContenedor,
    onPrimaryContainer = TextoBlancoPuro,
    secondary = BordeCianGlacial,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    background = FondoNocheGlacial,
    onBackground = TextoBlancoPuro,
    surface = SuperficieTarjeta,
    onSurface = TextoBlancoPuro,
    surfaceVariant = SuperficieTarjeta,
    onSurfaceVariant = TextoSecundarioGris,
    outline = TextoSecundarioGris,
    error = RojoErrorOscuro,
    errorContainer = RojoErrorContenedorOscuro,
    onErrorContainer = TextoBlancoPuro
)

private val ArcticLightColorScheme = lightColorScheme(
    primary = CianFrioOscuro,
    onPrimary = Color.White,
    primaryContainer = CianContenedorClaro,
    onPrimaryContainer = CianContenedor,
    secondary = CianSecundarioClaro,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    background = FondoHieloClaro,
    onBackground = TextoPizarraOscuro,
    surface = SuperficieClara,
    onSurface = TextoPizarraOscuro,
    surfaceVariant = SuperficieVarianteClara,
    onSurfaceVariant = TextoSecundarioPizarra,
    outline = Color(0xFF64748B),
    error = RojoErrorClaro,
    errorContainer = RojoErrorContenedorClaro,
    onErrorContainer = Color(0xFF881337)
)

/**
 * Tema Material 3 Arctic Cyan de TecnicalFresh.
 * Sigue la configuración del dispositivo: modo claro u oscuro.
 */
@Composable
fun TacticalDEVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTemaOscuro provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) ArcticDarkColorScheme else ArcticLightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
