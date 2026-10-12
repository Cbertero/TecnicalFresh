package cl.duoc.tacticaldev.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.screens.PantallaEnConstruccion
import cl.duoc.tacticaldev.ui.screens.historial.HistorialActivoScreen
import cl.duoc.tacticaldev.ui.screens.historial.MisVisitasScreen
import cl.duoc.tacticaldev.ui.screens.inicio.InicioOperarioScreen
import cl.duoc.tacticaldev.ui.screens.login.LoginScreen
import cl.duoc.tacticaldev.ui.screens.visita.ConfirmacionScreen
import cl.duoc.tacticaldev.ui.screens.visita.VisitaFormScreen
import cl.duoc.tacticaldev.viewmodel.AppViewModelProvider
import cl.duoc.tacticaldev.viewmodel.EstadoSesion
import cl.duoc.tacticaldev.viewmodel.NavigationViewModel
import cl.duoc.tacticaldev.viewmodel.SesionViewModel

/** Ruta de inicio según el rol del usuario conectado. */
fun Rol.rutaInicio(): String = when (this) {
    Rol.OPERARIO -> Screen.InicioOperario.route
    Rol.SUPERVISOR -> Screen.Supervisor.route
}

/**
 * Grafo de navegación de la app (NavHost + NavController).
 * Mientras se lee la sesión guardada se muestra un indicador de carga.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = viewModel(),
    sesionViewModel: SesionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estadoSesion by sesionViewModel.estado.collectAsState()

    if (estadoSesion is EstadoSesion.Cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // La ruta inicial se calcula una sola vez: con sesión guardada se entra directo
    val rutaInicial = remember {
        (estadoSesion as? EstadoSesion.Activa)?.sesion?.rol?.rutaInicio() ?: Screen.Login.route
    }
    val sesion = (estadoSesion as? EstadoSesion.Activa)?.sesion

    // Escucha los eventos del ViewModel y los traduce a acciones del NavController
    LaunchedEffect(Unit) {
        navigationViewModel.eventos.collect { evento ->
            manejarEvento(navController, evento)
        }
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val cerrarSesion: () -> Unit = {
        sesionViewModel.cerrarSesion {
            navigationViewModel.navegarA(Screen.Login.route, limpiarPila = true)
        }
    }

    NavHost(navController = navController, startDestination = rutaInicial) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                onLoginExitoso = { rol ->
                    navigationViewModel.navegarA(rol.rutaInicio(), limpiarPila = true)
                }
            )
        }

        composable(Screen.InicioOperario.route) {
            TacticalScaffold(
                titulo = "Hola, ${sesion?.nombre.orEmpty()}",
                itemsNavegacion = ItemsNavegacion.operario,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.InicioOperario.route) },
                onCerrarSesion = cerrarSesion
            ) { padding ->
                InicioOperarioScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    onRegistrarVisita = { navigationViewModel.navegarA(Screen.FormularioVisita.crear(it)) },
                    onVerHistorial = { navigationViewModel.navegarA(Screen.HistorialActivo.crear(it)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Screen.MisVisitas.route) {
            TacticalScaffold(
                titulo = "Mis visitas",
                itemsNavegacion = ItemsNavegacion.operario,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.InicioOperario.route) },
                onCerrarSesion = cerrarSesion
            ) { padding ->
                MisVisitasScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            Screen.FormularioVisita.route,
            arguments = listOf(navArgument(Screen.ARG_ACTIVO_ID) { type = NavType.LongType })
        ) {
            VisitaFormScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                onVolver = { navigationViewModel.volver() },
                onVisitaGuardada = { id ->
                    navigationViewModel.navegarA(
                        Screen.Confirmacion.crear(id),
                        popUpToRoute = Screen.FormularioVisita.route,
                        inclusive = true
                    )
                }
            )
        }

        composable(
            Screen.Confirmacion.route,
            arguments = listOf(navArgument(Screen.ARG_VISITA_ID) { type = NavType.LongType })
        ) {
            ConfirmacionScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                onVolverInicio = { navigationViewModel.volver() },
                onVerHistorial = { activoId ->
                    navigationViewModel.navegarA(
                        Screen.HistorialActivo.crear(activoId),
                        popUpToRoute = Screen.InicioOperario.route
                    )
                }
            )
        }

        composable(
            Screen.HistorialActivo.route,
            arguments = listOf(navArgument(Screen.ARG_ACTIVO_ID) { type = NavType.LongType })
        ) {
            HistorialActivoScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                onVolver = { navigationViewModel.volver() }
            )
        }

        composable(Screen.Supervisor.route) {
            TacticalScaffold(
                titulo = "Visitas por validar",
                itemsNavegacion = ItemsNavegacion.supervisor,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.Supervisor.route) },
                onCerrarSesion = cerrarSesion
            ) { padding ->
                PantallaEnConstruccion("Vista del supervisor", Modifier.padding(padding))
            }
        }

        composable(Screen.Administracion.route) {
            TacticalScaffold(
                titulo = "Administración",
                itemsNavegacion = ItemsNavegacion.supervisor,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.Supervisor.route) },
                onCerrarSesion = cerrarSesion
            ) { padding ->
                PantallaEnConstruccion("Administración", Modifier.padding(padding))
            }
        }
    }
}

/** Traduce un [NavigationEvent] a la llamada correspondiente del NavController. */
private fun manejarEvento(navController: NavHostController, evento: NavigationEvent) {
    when (evento) {
        is NavigationEvent.NavigateTo -> navController.navigate(evento.route) {
            when {
                evento.limpiarPila -> popUpTo(navController.graph.id) { inclusive = true }
                evento.popUpToRoute != null -> popUpTo(evento.popUpToRoute) { inclusive = evento.inclusive }
            }
            launchSingleTop = true
        }

        is NavigationEvent.NavigateTab -> navController.navigate(evento.route) {
            popUpTo(evento.rutaRaiz) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }

        NavigationEvent.PopBackStack -> navController.popBackStack()
    }
}
