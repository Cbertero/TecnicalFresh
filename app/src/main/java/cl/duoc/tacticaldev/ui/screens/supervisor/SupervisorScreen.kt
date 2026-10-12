package cl.duoc.tacticaldev.ui.screens.supervisor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.RechazoUiState
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.ui.components.TarjetaVisita
import cl.duoc.tacticaldev.ui.utils.LocalTipoPantalla
import cl.duoc.tacticaldev.ui.utils.columnas
import cl.duoc.tacticaldev.viewmodel.SupervisorViewModel

/** Vista del supervisor: revisa la evidencia de cada visita y la valida o rechaza. */
@Composable
fun SupervisorScreen(
    viewModel: SupervisorViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsState()
    val rechazo by viewModel.rechazo.collectAsState()
    val columnas = LocalTipoPantalla.current.columnas()

    Column(modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            EstadoValidacion.entries.forEach { opcion ->
                FilterChip(
                    selected = estado.filtro == opcion,
                    onClick = { viewModel.onFiltroChange(opcion) },
                    label = { Text(opcion.etiqueta) },
                    leadingIcon = { Icon(iconoFiltro(opcion), contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                estado.visitas.isEmpty() -> Text(
                    "No hay visitas en este estado.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(estado.visitas, key = { it.id }) { visita ->
                        TarjetaVisita(
                            visita = visita,
                            // La tarjeta sale de la lista con animación al validarla o rechazarla
                            modifier = Modifier.animateItem(),
                            mostrarOperario = true,
                            acciones = if (visita.estadoValidacion == EstadoValidacion.PENDIENTE.name) {
                                {
                                    OutlinedButton(
                                        onClick = { viewModel.abrirRechazo(visita) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                        Text("Rechazar", modifier = Modifier.padding(start = 4.dp))
                                    }
                                    Button(onClick = { viewModel.validar(visita) }) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                        Text("Validar", modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }

    if (rechazo.visita != null) {
        DialogoRechazo(
            estado = rechazo,
            onMotivoChange = viewModel::onMotivoChange,
            onConfirmar = viewModel::confirmarRechazo,
            onCancelar = viewModel::cancelarRechazo
        )
    }
}

private fun iconoFiltro(estado: EstadoValidacion): ImageVector = when (estado) {
    EstadoValidacion.PENDIENTE -> Icons.Filled.HourglassTop
    EstadoValidacion.VALIDADA -> Icons.Filled.CheckCircle
    EstadoValidacion.RECHAZADA -> Icons.Filled.Cancel
}

@Composable
private fun DialogoRechazo(
    estado: RechazoUiState,
    onMotivoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Rechazar visita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${estado.visita?.activoCodigo} · ${estado.visita?.usuarioNombre}")
                CampoTexto(
                    valor = estado.motivo,
                    onValorChange = onMotivoChange,
                    etiqueta = "Motivo del rechazo",
                    icono = Icons.AutoMirrored.Filled.Comment,
                    error = estado.errorMotivo,
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) { Text("Rechazar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
