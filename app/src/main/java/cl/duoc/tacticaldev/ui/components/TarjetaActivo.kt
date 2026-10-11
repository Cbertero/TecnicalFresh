package cl.duoc.tacticaldev.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.ActivoConPunto
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.model.tipoActivo
import cl.duoc.tacticaldev.utils.Formatos

/** Tarjeta de un activo con su punto de venta y acciones principales. */
@Composable
fun TarjetaActivo(
    activo: ActivoConPunto,
    onRegistrarVisita: () -> Unit,
    onVerHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (activo.tipoActivo == TipoActivo.VITRINA_FREEZER) Icons.Filled.Kitchen else Icons.Filled.AcUnit,
                        contentDescription = activo.tipoActivo.etiqueta,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(activo.codigo, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${activo.tipoActivo.etiqueta} · ${activo.marca} ${activo.modelo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    "${activo.puntoVentaNombre} · ${activo.direccion}, ${activo.comuna}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = activo.ultimaVisita?.let { "Última visita: ${Formatos.fechaHora(it)}" } ?: "Sin visitas registradas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRegistrarVisita, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.AddTask, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text("Registrar visita", modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(onClick = onVerHistorial) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text("Historial", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}
