package cl.duoc.tacticaldev.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.ui.graphics.vector.ImageVector

/** Ítem de la barra inferior / riel de navegación. */
data class ItemNavegacion(val ruta: String, val etiqueta: String, val icono: ImageVector)

object ItemsNavegacion {
    val operario = listOf(
        ItemNavegacion(Screen.InicioOperario.route, "Activos", Icons.Filled.Kitchen),
        ItemNavegacion(Screen.MisVisitas.route, "Mis visitas", Icons.Filled.History)
    )
    val supervisor = listOf(
        ItemNavegacion(Screen.Supervisor.route, "Visitas", Icons.Filled.FactCheck),
        ItemNavegacion(Screen.Administracion.route, "Administración", Icons.Filled.AdminPanelSettings)
    )
}
