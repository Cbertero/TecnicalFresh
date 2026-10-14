package cl.duoc.tacticaldev.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
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
import cl.duoc.tacticaldev.ui.animaciones.TransicionesNavegacion
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.screens.admin.AdminScreen
import cl.duoc.tacticaldev.ui.screens.dashboard.DashboardScreen
import cl.duoc.tacticaldev.ui.screens.historial.HistorialActivoScreen
import cl.duoc.tacticaldev.ui.screens.historial.MisVisitasScreen
import cl.duoc.tacticaldev.ui.screens.inicio.InicioOperarioScreen
import cl.duoc.tacticaldev.ui.screens.login.LoginScreen
import cl.duoc.tacticaldev.ui.screens.supervisor.SupervisorScreen
import cl.duoc.tacticaldev.ui.screens.visita.ConfirmacionScreen
import cl.duoc.tacticaldev.ui.screens.visita.VisitaFormScreen
import cl.duoc.tacticaldev.viewmodel.AdminViewModel
import cl.duoc.tacticaldev.viewmodel.AppViewModelProvider
import cl.duoc.tacticaldev.viewmodel.EstadoSesion
import cl.duoc.tacticaldev.viewmodel.NavigationViewModel
import cl.duoc.tacticaldev.viewmodel.SesionViewModel
import cl.duoc.tacticaldev.viewmodel.SupervisorViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import cl.duoc.tacticaldev.data.scanner.EscanerQr
import cl.duoc.tacticaldev.data.scanner.ResultadoEscaneo
import cl.duoc.tacticaldev.ui.screens.PantallaEnConstruccion
import cl.duoc.tacticaldev.ui.screens.dashboard.DashboardSupervisorScreen
import cl.duoc.tacticaldev.ui.screens.login.RegistroScreen
import cl.duoc.tacticaldev.viewmodel.ActivosViewModel
import kotlinx.coroutines.launch

/** Ruta de inicio según el rol del usuario conectado. */
fun Rol.rutaInicio(): String = when (this) {
    Rol.OPERARIO -> Screen.Dashboard.route
    Rol.SUPERVISOR -> Screen.DashboardSupervisor.route
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

    NavHost(
        navController = navController,
        startDestination = rutaInicial,
        enterTransition = TransicionesNavegacion.entrada,
        exitTransition = TransicionesNavegacion.salida,
        popEnterTransition = TransicionesNavegacion.entradaAlVolver,
        popExitTransition = TransicionesNavegacion.salidaAlVolver
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                onLoginExitoso = { rol ->
                    navigationViewModel.navegarA(rol.rutaInicio(), limpiarPila = true)
                },
                onIrARegistro = {
                    navigationViewModel.navegarA(Screen.Registro.route)
                }
            )
        }
        composable(Screen.Registro.route) {
            RegistroScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                onVolver = { navigationViewModel.volver() },
                onRegistroExitoso = {
                    navigationViewModel.volver() // Vuelve al login para iniciar sesión con su nueva cuenta
                }
            )
        }
        composable(Screen.Dashboard.route) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val activosViewModel: ActivosViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val snackbarHostState = remember { SnackbarHostState() }

            // Escuchar mensajes de error del escáner (ej: si el QR no existe)
            LaunchedEffect(Unit) {
                activosViewModel.mensajes.collect { mensaje ->
                    snackbarHostState.showSnackbar(mensaje)
                }
            }

            DashboardScreen(
                nombreUsuario = sesion?.nombre.orEmpty(),
                rolUsuario = sesion?.cargo?.ifBlank { sesion.rol.etiqueta }.orEmpty(),
                snackbarHostState = snackbarHostState,
                onIrAActivos = { navigationViewModel.navegarA(Screen.InicioOperario.route) },
                onIrAMisVisitas = { navigationViewModel.navegarA(Screen.MisVisitas.route) },
                onEscanearQr = {
                    scope.launch {
                        when (val resultado = EscanerQr.escanear(context)) {
                            is ResultadoEscaneo.Exito -> {
                                activosViewModel.onCodigoEscaneado(resultado.codigo) { activoId ->
                                    navigationViewModel.navegarA(Screen.FormularioVisita.crear(activoId))
                                }
                            }
                            is ResultadoEscaneo.Error -> {
                                activosViewModel.mostrarMensaje(resultado.mensaje)
                            }
                            ResultadoEscaneo.Cancelado -> Unit
                        }
                    }
                },
                onCerrarSesion = cerrarSesion
            )
        }
        composable(Screen.InicioOperario.route) {
            InicioOperarioScreen(
                viewModel = viewModel(factory = AppViewModelProvider.Factory),
                nombreUsuario = sesion?.nombre.orEmpty(),
                onVolver = { navigationViewModel.volver() }, // <-- Vuelve al Dashboard
                onRegistrarVisita = { navigationViewModel.navegarA(Screen.FormularioVisita.crear(it)) },
                onVerHistorial = { navigationViewModel.navegarA(Screen.HistorialActivo.crear(it)) }
            )
        }
        composable(Screen.MisVisitas.route) {
            TacticalScaffold(
                titulo = "Mis visitas",
                itemsNavegacion = emptyList(), // <-- Sin barra inferior
                onVolver = { navigationViewModel.volver() } // <-- Flecha para volver al Dashboard
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
        composable(Screen.DashboardSupervisor.route) {
            DashboardSupervisorScreen(
                nombreUsuario = sesion?.nombre.orEmpty(),
                cargoUsuario = sesion?.cargo?.ifBlank { "Supervisora de operaciones" }.orEmpty(),
                onIrAVisitas = { navigationViewModel.navegarA(Screen.Supervisor.route) },
                onIrAAdministracion = { navigationViewModel.navegarA(Screen.Administracion.route) },
                onIrAEquipo = { navigationViewModel.navegarA(Screen.Equipo.route) }, // <-- Agregas esta línea
                onCerrarSesion = cerrarSesion
            )
        }
        composable(Screen.Equipo.route) {
            PantallaEnConstruccion(
                titulo = "Gestión de Equipo",
                subtitulo = "Monitoreo en tiempo real de operarios y rutas en terreno.",
                onVolver = { navigationViewModel.volver() }
            )
        }
        composable(Screen.Supervisor.route) {
            val supervisorViewModel: SupervisorViewModel = viewModel(factory = AppViewModelProvider.Factory)



            TacticalScaffold(
                titulo = "Visitas de terreno",
                itemsNavegacion = emptyList(),
                rutaActual = rutaActual,
                onVolver = { navigationViewModel.volver() },
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.Supervisor.route) },

            ) { padding ->
                SupervisorScreen(viewModel = supervisorViewModel, modifier = Modifier.padding(padding))
            }
        }

        composable(Screen.Administracion.route) {
            val adminViewModel: AdminViewModel = viewModel(factory = AppViewModelProvider.Factory)

            TacticalScaffold(
                titulo = "Administración",
                itemsNavegacion = emptyList(),
                rutaActual = rutaActual,
                onVolver = { navigationViewModel.volver() },
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.Supervisor.route) },

            ) { padding ->
                AdminScreen(viewModel = adminViewModel, modifier = Modifier.padding(padding))
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
