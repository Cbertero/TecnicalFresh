package cl.duoc.tacticaldev.navigation

/** Eventos de navegación que emite el NavigationViewModel y consume el NavHost. */
sealed class NavigationEvent {
    /** Navega a [route]. Si [limpiarPila] es true se borra el historial (login / cerrar sesión). */
    data class NavigateTo(
        val route: String,
        val popUpToRoute: String? = null,
        val inclusive: Boolean = false,
        val limpiarPila: Boolean = false
    ) : NavigationEvent()

    /** Cambia de pestaña en la barra de navegación sin apilar pantallas. */
    data class NavigateTab(val route: String, val rutaRaiz: String) : NavigationEvent()

    data object PopBackStack : NavigationEvent()
}
