package cl.duoc.tacticaldev.ui.screens.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.ui.components.TarjetaVisita
import cl.duoc.tacticaldev.viewmodel.MisVisitasViewModel

/** Lista de las visitas registradas por el operario conectado. */
@Composable
fun MisVisitasScreen(
    viewModel: MisVisitasViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsState()

    Box(modifier.fillMaxSize()) {
        when {
            estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            estado.visitas.isEmpty() -> Text(
                "Todavía no registras visitas.",
                modifier = Modifier.align(Alignment.Center)
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.visitas, key = { it.id }) { visita ->
                    TarjetaVisita(visita = visita)
                }
            }
        }
    }
}
