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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.tacticaldev.model.tipoActivo
import cl.duoc.tacticaldev.ui.components.EstadoVacio
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.components.TarjetaVisita
import cl.duoc.tacticaldev.ui.theme.Espaciado
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
                contentPadding = PaddingValues(Espaciado.l),
                verticalArrangement = Arrangement.spacedBy(Espaciado.m),
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
                    item {
                        EstadoVacio(
                            icono = Icons.Filled.History,
                            titulo = "Sin visitas registradas",
                            mensaje = "Cuando alguien registre una visita a este activo aparecerá aquí con su evidencia."
                        )
                    }
                }
                items(estado.visitas, key = { it.id }) { visita ->
                    TarjetaVisita(visita = visita, mostrarActivo = false, mostrarOperario = true)
                }
            }
        }
    }
}
