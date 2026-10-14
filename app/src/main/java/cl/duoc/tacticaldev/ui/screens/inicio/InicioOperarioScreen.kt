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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.ui.components.EstadoVacio
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.components.TarjetaActivo
import cl.duoc.tacticaldev.ui.theme.Espaciado
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.columnas
import cl.duoc.tacticaldev.viewmodel.ActivosViewModel

/**
 * Inicio del operario: lista de activos (vitrinas y conservadoras) con búsqueda.
 * La grilla usa 1, 2 o 3 columnas según el tamaño de pantalla.
 */
@Composable
fun InicioOperarioScreen(
    viewModel: ActivosViewModel,
    nombreUsuario: String,
    onVolver :() -> Unit,
    onRegistrarVisita: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val columnas = LocalTipoPantalla.current.columnas()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }

    TacticalScaffold(
        titulo = "Hola, $nombreUsuario",
        itemsNavegacion = emptyList(),
        onVolver = onVolver,
        snackbarHostState = snackbarHostState,

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
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onBusquedaChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Espaciado.l, vertical = Espaciado.s)
            )

            if (estado.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (estado.activos.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Filled.SearchOff,
                    titulo = "Sin resultados",
                    mensaje = "No hay activos que coincidan con \"$busqueda\". Prueba con otro código o comuna."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    // Margen inferior extra para que el botón flotante no tape la última tarjeta
                    contentPadding = PaddingValues(start = Espaciado.l, end = Espaciado.l, top = Espaciado.s, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.m),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.m),
                    modifier = Modifier.weight(1f)
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
            Text(
                text = "TecnicalFresh • Gestión de Mantenimiento",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }


}
