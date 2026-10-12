package cl.duoc.tacticaldev.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.model.estado
import cl.duoc.tacticaldev.model.tipo
import cl.duoc.tacticaldev.model.validacion
import cl.duoc.tacticaldev.ui.theme.AmbarAlerta
import cl.duoc.tacticaldev.ui.theme.VerdeExito
import cl.duoc.tacticaldev.utils.Formatos

/**
 * Tarjeta con el resumen de una visita. Se reutiliza en el historial del activo,
 * en "Mis visitas" y en la vista del supervisor (con [acciones]).
 */
@Composable
fun TarjetaVisita(
    visita: VisitaDetalle,
    modifier: Modifier = Modifier,
    mostrarActivo: Boolean = true,
    mostrarOperario: Boolean = false,
    acciones: (@Composable RowScope.() -> Unit)? = null
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                FotoEvidencia(uri = visita.fotoUri, modifier = Modifier.size(72.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(Formatos.fechaHora(visita.fechaHora), style = MaterialTheme.typography.titleMedium)
                    if (mostrarActivo) {
                        Text("${visita.activoCodigo} · ${visita.puntoVentaNombre}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (mostrarOperario) {
                        Text("Operario: ${visita.usuarioNombre}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "${visita.tipo.etiqueta} · ${visita.estado.etiqueta} · ${visita.temperatura} °C",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ChipEstado(visita.validacion)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (visita.ubicacionVerificada) Icons.Filled.VerifiedUser else Icons.Filled.WrongLocation,
                    contentDescription = null,
                    tint = if (visita.ubicacionVerificada) VerdeExito else AmbarAlerta,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    if (visita.ubicacionVerificada) {
                        "Ubicación verificada · ${Formatos.distancia(visita.distanciaMetros)}"
                    } else {
                        "Fuera de rango · ${Formatos.distancia(visita.distanciaMetros)} del punto"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (visita.observaciones.isNotBlank()) {
                Text(visita.observaciones, style = MaterialTheme.typography.bodyMedium)
            }

            if (visita.validacion == EstadoValidacion.RECHAZADA && !visita.motivoRechazo.isNullOrBlank()) {
                Text(
                    "Motivo del rechazo: ${visita.motivoRechazo}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (acciones != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth(),
                    content = acciones
                )
            }
        }
    }
}
