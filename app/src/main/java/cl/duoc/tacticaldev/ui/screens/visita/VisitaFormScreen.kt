package cl.duoc.tacticaldev.ui.screens.visita

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.ActivoConPunto
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.model.VisitaFormUiState
import cl.duoc.tacticaldev.model.tipoActivo
import cl.duoc.tacticaldev.ui.components.CampoTexto
import cl.duoc.tacticaldev.ui.components.Selector
import cl.duoc.tacticaldev.ui.components.TacticalScaffold
import cl.duoc.tacticaldev.ui.theme.AmbarAlerta
import cl.duoc.tacticaldev.ui.theme.VerdeExito
import cl.duoc.tacticaldev.viewmodel.VisitaViewModel

/** Formulario de registro de una visita al activo seleccionado. */
@Composable
fun VisitaFormScreen(
    viewModel: VisitaViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    TacticalScaffold(
        titulo = "Registrar visita",
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    ) { padding ->
        if (estado.cargando) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@TacticalScaffold
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                estado.activo?.let { ResumenActivo(it) }

                Selector(
                    etiqueta = "Tipo de visita",
                    opciones = TipoVisita.entries,
                    seleccion = estado.tipoVisita,
                    textoOpcion = { it.etiqueta },
                    onSeleccion = viewModel::onTipoVisitaChange,
                    icono = Icons.AutoMirrored.Filled.Assignment,
                    error = estado.errores.tipoVisita
                )

                SelectorEstadoEquipo(
                    seleccion = estado.estadoEquipo,
                    error = estado.errores.estadoEquipo,
                    onSeleccion = viewModel::onEstadoEquipoChange
                )

                CampoTexto(
                    valor = estado.temperatura,
                    onValorChange = viewModel::onTemperaturaChange,
                    etiqueta = "Temperatura medida",
                    icono = Icons.Filled.Thermostat,
                    error = estado.errores.temperatura,
                    ayuda = "Entre ${Validadores.TEMPERATURA_MIN.toInt()} y ${Validadores.TEMPERATURA_MAX.toInt()} °C",
                    sufijo = "°C",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                CampoTexto(
                    valor = estado.observaciones,
                    onValorChange = viewModel::onObservacionesChange,
                    etiqueta = "Observaciones",
                    icono = Icons.AutoMirrored.Filled.Notes,
                    error = estado.errores.observaciones,
                    ayuda = "${estado.observaciones.length}/${Validadores.OBSERVACIONES_MAX}",
                    singleLine = false,
                    minLines = 3
                )

                SeccionEvidencia(estado)

                SeccionUbicacion(estado)

                FilaConfirmacion(
                    confirmado = estado.confirmado,
                    error = estado.errores.confirmacion,
                    onCambio = viewModel::onConfirmacionChange
                )

                Button(
                    onClick = viewModel::enviar,
                    enabled = !estado.guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Guardar visita")
                }
            }
        }
    }
}

@Composable
private fun ResumenActivo(activo: ActivoConPunto) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("${activo.codigo} · ${activo.tipoActivo.etiqueta}", style = MaterialTheme.typography.titleMedium)
            Text("${activo.puntoVentaNombre} · ${activo.direccion}, ${activo.comuna}")
            Text(
                "La fecha y hora se registran automáticamente al guardar",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SelectorEstadoEquipo(
    seleccion: EstadoEquipo?,
    error: String?,
    onSeleccion: (EstadoEquipo) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        Text("Estado del equipo", style = MaterialTheme.typography.titleSmall)
        EstadoEquipo.entries.forEach { estadoEquipo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = estadoEquipo == seleccion,
                        onClick = { onSeleccion(estadoEquipo) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 2.dp)
            ) {
                RadioButton(selected = estadoEquipo == seleccion, onClick = null)
                Icon(
                    iconoEstado(estadoEquipo),
                    contentDescription = null,
                    tint = colorEstado(estadoEquipo),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(estadoEquipo.etiqueta)
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun iconoEstado(estado: EstadoEquipo): ImageVector = when (estado) {
    EstadoEquipo.OPERATIVO -> Icons.Filled.CheckCircle
    EstadoEquipo.CON_OBSERVACIONES -> Icons.Filled.Warning
    EstadoEquipo.CON_FALLA -> Icons.Filled.Error
}

@Composable
private fun colorEstado(estado: EstadoEquipo) = when (estado) {
    EstadoEquipo.OPERATIVO -> VerdeExito
    EstadoEquipo.CON_OBSERVACIONES -> AmbarAlerta
    EstadoEquipo.CON_FALLA -> MaterialTheme.colorScheme.error
}

/** Evidencia fotográfica de la revisión. */
@Composable
private fun SeccionEvidencia(estado: VisitaFormUiState) {
    SeccionConIcono(
        titulo = "Evidencia fotográfica",
        icono = Icons.Filled.PhotoCamera,
        error = estado.errores.foto
    ) {
        Text(if (estado.fotoUri == null) "Sin foto" else "Foto capturada")
    }
}

/** Ubicación donde se realiza la visita. */
@Composable
private fun SeccionUbicacion(estado: VisitaFormUiState) {
    SeccionConIcono(
        titulo = "Ubicación de la visita",
        icono = Icons.Filled.LocationOn,
        error = estado.errores.ubicacion
    ) {
        Text(if (estado.latitud == null) "Sin ubicación" else "Ubicación registrada")
    }
}

@Composable
private fun SeccionConIcono(
    titulo: String,
    icono: ImageVector,
    error: String?,
    contenido: @Composable () -> Unit
) {
    OutlinedCard(
        border = if (error != null) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(titulo, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp))
            }
            contenido()
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FilaConfirmacion(confirmado: Boolean, error: String?, onCambio: (Boolean) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = confirmado, onCheckedChange = onCambio)
            Text("Confirmo que estuve en el punto de venta y que la información es verídica")
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
