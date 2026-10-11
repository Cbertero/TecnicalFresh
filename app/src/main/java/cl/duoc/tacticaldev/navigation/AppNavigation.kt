package cl.duoc.tacticaldev.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.tacticaldev.R
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.screens.PantallaEnConstruccion
import cl.duoc.tacticaldev.viewmodel.NavigationViewModel

/**
 * Grafo de navegación de la app (NavHost + NavController).
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = viewModel()
) {
    // Escucha los eventos del ViewModel y los traduce a acciones del NavController
    LaunchedEffect(Unit) {
        navigationViewModel.eventos.collect { evento ->
            manejarEvento(navController, evento)
        }
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            // Temporal: el login real con validaciones se implementa en T-07
            LoginTemporal(
                onOperario = {
                    navigationViewModel.navegarA(Screen.InicioOperario.route, limpiarPila = true)
                },
                onSupervisor = {
                    navigationViewModel.navegarA(Screen.Supervisor.route, limpiarPila = true)
                }
            )
        }

        composable(Screen.InicioOperario.route) {
            TacticalScaffold(
                titulo = "Activos",
                itemsNavegacion = ItemsNavegacion.operario,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.InicioOperario.route) },
                onCerrarSesion = { navigationViewModel.navegarA(Screen.Login.route, limpiarPila = true) }
            ) { padding ->
                PantallaEnConstruccion("Inicio del operario", Modifier.padding(padding))
            }
        }

        composable(Screen.MisVisitas.route) {
            TacticalScaffold(
                titulo = "Mis visitas",
                itemsNavegacion = ItemsNavegacion.operario,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.InicioOperario.route) },
                onCerrarSesion = { navigationViewModel.navegarA(Screen.Login.route, limpiarPila = true) }
            ) { padding ->
                PantallaEnConstruccion("Mis visitas", Modifier.padding(padding))
            }
        }

        composable(
            Screen.FormularioVisita.route,
            arguments = listOf(navArgument(Screen.ARG_ACTIVO_ID) { type = NavType.LongType })
        ) {
            TacticalScaffold(titulo = "Registrar visita", onVolver = { navigationViewModel.volver() }) { padding ->
                PantallaEnConstruccion("Formulario de visita", Modifier.padding(padding))
            }
        }

        composable(
            Screen.Confirmacion.route,
            arguments = listOf(navArgument(Screen.ARG_VISITA_ID) { type = NavType.LongType })
        ) {
            TacticalScaffold(titulo = "Visita registrada") { padding ->
                PantallaEnConstruccion("Confirmación", Modifier.padding(padding))
            }
        }

        composable(
            Screen.HistorialActivo.route,
            arguments = listOf(navArgument(Screen.ARG_ACTIVO_ID) { type = NavType.LongType })
        ) {
            TacticalScaffold(titulo = "Historial del activo", onVolver = { navigationViewModel.volver() }) { padding ->
                PantallaEnConstruccion("Historial del activo", Modifier.padding(padding))
            }
        }

        composable(Screen.Supervisor.route) {
            TacticalScaffold(
                titulo = "Visitas por validar",
                itemsNavegacion = ItemsNavegacion.supervisor,
                rutaActual = rutaActual,
                onNavegar = { navigationViewModel.cambiarPestana(it, Screen.Supervisor.route) },
                onCerrarSesion = { navigationViewModel.navegarA(Screen.Login.route, limpiarPila = true) }
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
                onCerrarSesion = { navigationViewModel.navegarA(Screen.Login.route, limpiarPila = true) }
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

@Composable
private fun LoginTemporal(onOperario: () -> Unit, onSupervisor: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "Logo TacticalDEV",
            modifier = Modifier.size(96.dp)
        )
        Text("TacticalDEV Visitas", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onOperario, modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth()) {
            Text("Entrar como operario")
        }
        OutlinedButton(onClick = onSupervisor, modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth()) {
            Text("Entrar como supervisor")
        }
    }
}
