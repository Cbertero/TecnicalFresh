package cl.duoc.tacticaldev.ui.screens.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.ui.components.EstadoVacio
import cl.duoc.tacticaldev.ui.components.TarjetaVisita
import cl.duoc.tacticaldev.ui.theme.Espaciado
import cl.duoc.tacticaldev.viewmodel.MisVisitasViewModel

/** Lista de las visitas registradas por el operario conectado. */
@Composable
fun MisVisitasScreen(
    viewModel: MisVisitasViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                estado.visitas.isEmpty() -> EstadoVacio(
                    icono = Icons.Filled.EventNote,
                    titulo = "Todavía no registras visitas",
                    mensaje = "Elige un activo en Vitrinas o escanea su QR para registrar tu primera visita.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(Espaciado.l),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.m),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.visitas, key = { it.id }) { visita ->
                        TarjetaVisita(visita = visita)
                    }
                }
            }
        }

        // Pie de página corporativo
        Text(
            text = "TecnicalFresh • Gestión de Mantenimiento",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}