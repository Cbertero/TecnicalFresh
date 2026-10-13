package cl.duoc.tacticaldev.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
    error = RojoError,
    errorContainer = RojoErrorContenedor,
    onErrorContainer = TextoBlancoPuro
)

/**
 * Tema Material 3 Arctic Cyan de TecnicalFresh.
 */
@Composable
fun TacticalDEVTheme(
    darkTheme: Boolean = true, // Activado en modo oscuro glacial
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcticDarkColorScheme,
        typography = Typography,
        content = content
    )
}