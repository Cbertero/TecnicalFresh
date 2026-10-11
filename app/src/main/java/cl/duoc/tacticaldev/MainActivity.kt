package cl.duoc.tacticaldev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import cl.duoc.tacticaldev.navigation.AppNavigation
import cl.duoc.tacticaldev.ui.theme.TacticalDEVTheme
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.calcularTipoPantalla

/**
 * Actividad única: calcula el tamaño de pantalla y delega todo al grafo de navegación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TacticalDEVTheme {
                CompositionLocalProvider(LocalTipoPantalla provides calcularTipoPantalla()) {
                    AppNavigation()
                }
            }
        }
    }
}
