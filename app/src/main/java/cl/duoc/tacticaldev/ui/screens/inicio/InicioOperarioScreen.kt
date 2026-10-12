package cl.duoc.tacticaldev.ui.screens.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.data.scanner.EscanerQr
import cl.duoc.tacticaldev.data.scanner.ResultadoEscaneo
import cl.duoc.tacticaldev.navigation.ItemNavegacion
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.components.TarjetaActivo
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.columnas
import cl.duoc.tacticaldev.viewmodel.ActivosViewModel
import kotlinx.coroutines.launch

/**
 * Inicio del operario: lista de activos (vitrinas y conservadoras) con búsqueda
 * y botón para escanear el QR del activo. La grilla usa 1, 2 o 3 columnas
 * según el tamaño de pantalla (Guía 9).
 */
@Composable
fun InicioOperarioScreen(
    viewModel: ActivosViewModel,
    nombreUsuario: String,
    itemsNavegacion: List<ItemNavegacion>,
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    onRegistrarVisita: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val columnas = LocalTipoPantalla.current.columnas()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }

    TacticalScaffold(
        titulo = "Hola, $nombreUsuario",
        itemsNavegacion = itemsNavegacion,
        rutaActual = rutaActual,
        onNavegar = onNavegar,
        onCerrarSesion = onCerrarSesion,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Escanear QR") },
                icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = null) },
                onClick = {
                    scope.launch {
                        when (val resultado = EscanerQr.escanear(context)) {
                            is ResultadoEscaneo.Exito -> viewModel.onCodigoEscaneado(resultado.codigo, onRegistrarVisita)
                            is ResultadoEscaneo.Error -> viewModel.mostrarMensaje(resultado.mensaje)
                            ResultadoEscaneo.Cancelado -> Unit
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por código, local o comuna") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (estado.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.activos, key = { it.id }) { activo ->
                        TarjetaActivo(
                            activo = activo,
                            onRegistrarVisita = { onRegistrarVisita(activo.id) },
                            onVerHistorial = { onVerHistorial(activo.id) }
                        )
                    }
                }
            }
        }
    }
}
