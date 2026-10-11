package cl.duoc.tacticaldev.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AzulFrio,
    onPrimary = Color.White,
    primaryContainer = AzulContenedor,
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = CelesteHielo,
    onSecondary = Color.White,
    secondaryContainer = CelesteContenedor,
    onSecondaryContainer = Color(0xFF001F28),
    background = GrisFondo,
    surface = GrisFondo,
    error = RojoError,
    errorContainer = RojoErrorContenedor
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulFrioClaro,
    onPrimary = Color(0xFF003063),
    primaryContainer = AzulContenedorOscuro,
    onPrimaryContainer = AzulContenedor,
    secondary = CelesteHieloClaro,
    onSecondary = Color(0xFF003543),
    background = GrisSuperficieOscura,
    surface = GrisSuperficieOscura
)

/**
 * Tema Material 3 de la app. Se desactiva el color dinámico para mantener
 * la identidad visual de TacticalDEV en todos los dispositivos.
 */
@Composable
fun TacticalDEVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
