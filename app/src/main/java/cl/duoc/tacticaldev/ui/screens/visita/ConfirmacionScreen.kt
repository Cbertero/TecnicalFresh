package cl.duoc.tacticaldev.ui.screens.visita

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.model.estado
import cl.duoc.tacticaldev.model.tipo
import cl.duoc.tacticaldev.ui.components.AlertaFlotanteAnimada
import cl.duoc.tacticaldev.ui.components.FotoEvidencia
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.theme.VerdeExito
import cl.duoc.tacticaldev.utils.Formatos
import cl.duoc.tacticaldev.viewmodel.ConfirmacionViewModel
import kotlinx.coroutines.delay

/** Comprobante de la visita guardada en el dispositivo. */
@Composable
fun ConfirmacionScreen(
    viewModel: ConfirmacionViewModel,
    onVolverInicio: () -> Unit,
    onVerHistorial: (Long) -> Unit
) {
    val visita by viewModel.visita.collectAsState()
    var mensajeAnimado by remember { mutableStateOf<String?>("¡Visita registrada exitosamente!") }

    LaunchedEffect(Unit) {
        delay(3500)
        mensajeAnimado = null
    }

    TacticalScaffold(titulo = "Visita registrada") { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val detalle = visita
            if (detalle == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                ContenidoConfirmacion(
                    visita = detalle,
                    onVolverInicio = onVolverInicio,
                    onVerHistorial = onVerHistorial,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            AlertaFlotanteAnimada(
                mensaje = mensajeAnimado,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun ContenidoConfirmacion(
    visita: VisitaDetalle,
    onVolverInicio: () -> Unit,
    onVerHistorial: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Escala del ícono: aparece con efecto "rebote"
    val escala = remember { Animatable(0f) }
    var mostrarDetalle by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        mostrarDetalle = true
    }

    Column(
        modifier = modifier
            .widthIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VerdeExito,
            modifier = Modifier
                .size(88.dp)
                .scale(escala.value)
        )
        Text("¡Visita guardada!", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Quedó registrada en el dispositivo y pendiente de validación por el supervisor.",
            style = MaterialTheme.typography.bodyMedium
        )

        AnimatedVisibility(
            visible = mostrarDetalle,
            enter = fadeIn() + slideInVertically { it / 3 }
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${visita.activoCodigo} · ${visita.puntoVentaNombre}", style = MaterialTheme.typography.titleMedium)
                    Text("Fecha y hora: ${Formatos.fechaHora(visita.fechaHora)}")
                    Text("Tipo: ${visita.tipo.etiqueta}")
                    Text("Estado del equipo: ${visita.estado.etiqueta} · ${visita.temperatura} °C")
                    Text(
                        if (visita.ubicacionVerificada) {
                            "Ubicación verificada (${Formatos.distancia(visita.distanciaMetros)})"
                        } else {
                            "Ubicación fuera de rango (${Formatos.distancia(visita.distanciaMetros)})"
                        }
                    )
                    FotoEvidencia(
                        uri = visita.fotoUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                    )
                }
            }
        }

        Button(onClick = onVolverInicio, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a mis activos")
        }
        OutlinedButton(onClick = { onVerHistorial(visita.activoId) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver historial del activo")
        }
    }
}
