package cl.duoc.tacticaldev.ui.screens.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.tipoActivo
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.components.TarjetaVisita
import cl.duoc.tacticaldev.viewmodel.HistorialViewModel

/** Historial de visitas de un activo, de la más reciente a la más antigua. */
@Composable
fun HistorialActivoScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()

    TacticalScaffold(titulo = "Historial del activo", onVolver = onVolver) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                estado.activo?.let { activo ->
                    item {
                        Column(Modifier.fillMaxWidth()) {
                            Text("${activo.codigo} · ${activo.tipoActivo.etiqueta}", style = MaterialTheme.typography.titleLarge)
                            Text("${activo.puntoVentaNombre} · ${activo.direccion}, ${activo.comuna}")
                            Text(
                                "${estado.visitas.size} visita(s) registradas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (estado.visitas.isEmpty()) {
                    item { Text("Este activo aún no tiene visitas.") }
                }
                items(estado.visitas, key = { it.id }) { visita ->
                    TarjetaVisita(visita = visita, mostrarActivo = false, mostrarOperario = true)
                }
            }
        }
    }
}
