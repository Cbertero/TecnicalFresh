package cl.duoc.tacticaldev.navigation

/**
 * Rutas de la aplicación. Se usa una sealed class para que cada ruta sea
 * segura en tiempo de compilación (Guía 10).
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Dashboard : Screen("dashboard")
    // Operario (técnico, vendedor o transportista)
    data object InicioOperario : Screen("inicio_operario")
    data object MisVisitas : Screen("mis_visitas")
    data object FormularioVisita : Screen("formulario_visita/{$ARG_ACTIVO_ID}") {
        fun crear(activoId: Long) = "formulario_visita/$activoId"
    }
    data object Confirmacion : Screen("confirmacion/{$ARG_VISITA_ID}") {
        fun crear(visitaId: Long) = "confirmacion/$visitaId"
    }
    data object HistorialActivo : Screen("historial_activo/{$ARG_ACTIVO_ID}") {
        fun crear(activoId: Long) = "historial_activo/$activoId"
    }

    // Supervisor
    data object DashboardSupervisor : Screen("dashboard_supervisor")
    data object Supervisor : Screen("supervisor")
    data object Administracion : Screen("administracion")
    data object Equipo : Screen("equipo")

    companion object {
        const val ARG_ACTIVO_ID = "activoId"
        const val ARG_VISITA_ID = "visitaId"
    }
}
