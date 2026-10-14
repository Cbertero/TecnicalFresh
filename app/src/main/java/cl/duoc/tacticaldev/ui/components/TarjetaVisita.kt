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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.model.estado
import cl.duoc.tacticaldev.model.tipo
import cl.duoc.tacticaldev.model.validacion
import cl.duoc.tacticaldev.ui.theme.AmbarAlerta
import cl.duoc.tacticaldev.ui.theme.Espaciado
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Espaciado.l),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila superior: Foto + Textos + Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                FotoEvidencia(uri = visita.fotoUri, modifier = Modifier.size(72.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val esFreezer = visita.activoTipo == cl.duoc.tacticaldev.model.TipoActivo.VITRINA_FREEZER.name
                    val tempCritica = if (esFreezer) visita.temperatura > -12.0 else (visita.temperatura > 7.0 || visita.temperatura < -2.0)

                    Text(
                        text = Formatos.fechaHora(visita.fechaHora),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (mostrarActivo) {
                        Text(
                            text = "${visita.activoCodigo} · ${visita.puntoVentaNombre}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (mostrarOperario) {
                        Text(
                            text = "Operario: ${visita.usuarioNombre}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Tipo de visita, estado y temperatura
                    val textoTemp = if (tempCritica) "⚠️ ${visita.temperatura} °C" else "${visita.temperatura} °C"
                    Text(
                        text = "${visita.tipo.etiqueta} · ${visita.estado.etiqueta} · $textoTemp",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (tempCritica) AmbarAlerta else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (tempCritica) FontWeight.Bold else FontWeight.Normal
                    )
                }

                ChipEstado(visita.validacion)
            }

            // Fila de Ubicación GPS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (visita.ubicacionVerificada) Icons.Filled.VerifiedUser else Icons.Filled.WrongLocation,
                    contentDescription = null,
                    tint = if (visita.ubicacionVerificada) VerdeExito else AmbarAlerta,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (visita.ubicacionVerificada) {
                        "Ubicación verificada · ${Formatos.distancia(visita.distanciaMetros)}"
                    } else {
                        "Fuera de rango · ${Formatos.distancia(visita.distanciaMetros)} del punto"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            // Observaciones
            if (visita.observaciones.isNotBlank()) {
                Text(
                    text = visita.observaciones,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Motivo de rechazo (si aplica)
            if (visita.validacion == EstadoValidacion.RECHAZADA && !visita.motivoRechazo.isNullOrBlank()) {
                Text(
                    text = "Motivo del rechazo: ${visita.motivoRechazo}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botones de acción del supervisor (Validar / Rechazar)
            if (acciones != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.s, Alignment.End),
                    content = acciones
                )
            }
        }
    }
}


@Composable
private fun ChipEstado(estado: EstadoValidacion) {
    val (colorFondo, colorTexto) = when (estado) {
        EstadoValidacion.PENDIENTE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        EstadoValidacion.VALIDADA -> VerdeExito.copy(alpha = 0.2f) to VerdeExito
        EstadoValidacion.RECHAZADA -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    androidx.compose.material3.Surface(
        color = colorFondo,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Text(
            text = estado.etiqueta,
            color = colorTexto,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}