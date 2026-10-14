package cl.duoc.tacticaldev.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Indica si el tema activo es oscuro (lo provee [TacticalDEVTheme] según el sistema). */
val LocalTemaOscuro = staticCompositionLocalOf { true }

// Paleta Arctic Cyan — modo oscuro (Cadena de Frío & Dark Tech)
val FondoNocheGlacial = Color(0xFF131D2E)       // Fondo azul marino templado
val SuperficieTarjeta = Color(0xFF1D2B42)       // Tarjetas elevadas azul acero
val BordeTarjetaSutil = Color(0xFF2E4366)       // Borde tenue de contraste
val BordeCianGlacial = Color(0xFF22D3EE)        // Cian brillante para acentos de frío

val CianFrio = Color(0xFF38BDF8)               // Primario en botones y destacados
val CianFrioOscuro = Color(0xFF0369A1)
val CianContenedor = Color(0xFF0C4A6E)
val TextoBlancoPuro = Color(0xFFF8FAFC)
val TextoSecundarioGris = Color(0xFF94A3B8)

// Paleta Arctic Cyan — modo claro
val FondoHieloClaro = Color(0xFFF1F7FB)         // Fondo claro con tinte frío
val SuperficieClara = Color(0xFFFFFFFF)         // Tarjetas sobre fondo claro
val SuperficieVarianteClara = Color(0xFFE2EEF6)
val CianContenedorClaro = Color(0xFFE0F2FE)
val CianSecundarioClaro = Color(0xFF0E7490)
val TextoPizarraOscuro = Color(0xFF0F172A)
val TextoSecundarioPizarra = Color(0xFF475569)

// Colores semánticos (variantes para cada modo, con buen contraste sobre su fondo)
internal val VerdeExitoOscuro = Color(0xFF10B981)
internal val VerdeExitoContenedorOscuro = Color(0xFF064E3B)
internal val AmbarAlertaOscuro = Color(0xFFF59E0B)
internal val AmbarAlertaContenedorOscuro = Color(0xFF78350F)
internal val RojoErrorOscuro = Color(0xFFF43F5E)
internal val RojoErrorContenedorOscuro = Color(0xFF881337)

internal val VerdeExitoClaro = Color(0xFF047857)
internal val VerdeExitoContenedorClaro = Color(0xFFD1FAE5)
internal val AmbarAlertaClaro = Color(0xFFB45309)
internal val AmbarAlertaContenedorClaro = Color(0xFFFEF3C7)
internal val RojoErrorClaro = Color(0xFFE11D48)
internal val RojoErrorContenedorClaro = Color(0xFFFFE4E6)

val VerdeExito: Color
    @Composable @ReadOnlyComposable get() = if (LocalTemaOscuro.current) VerdeExitoOscuro else VerdeExitoClaro
val VerdeExitoContenedor: Color
    @Composable @ReadOnlyComposable get() = if (LocalTemaOscuro.current) VerdeExitoContenedorOscuro else VerdeExitoContenedorClaro
val AmbarAlerta: Color
    @Composable @ReadOnlyComposable get() = if (LocalTemaOscuro.current) AmbarAlertaOscuro else AmbarAlertaClaro
val AmbarAlertaContenedor: Color
    @Composable @ReadOnlyComposable get() = if (LocalTemaOscuro.current) AmbarAlertaContenedorOscuro else AmbarAlertaContenedorClaro
val RojoError: Color
    @Composable @ReadOnlyComposable get() = if (LocalTemaOscuro.current) RojoErrorOscuro else RojoErrorClaro
val RojoErrorContenedor: Color
    @Composable @ReadOnlyComposable get() = if (LocalTemaOscuro.current) RojoErrorContenedorOscuro else RojoErrorContenedorClaro
