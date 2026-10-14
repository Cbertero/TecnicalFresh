package cl.duoc.tacticaldev.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.ui.theme.AmbarAlerta
import cl.duoc.tacticaldev.ui.theme.AmbarAlertaContenedor
import cl.duoc.tacticaldev.ui.theme.RojoError
import cl.duoc.tacticaldev.ui.theme.RojoErrorContenedor
import cl.duoc.tacticaldev.ui.theme.VerdeExito
import cl.duoc.tacticaldev.ui.theme.VerdeExitoContenedor

/** Etiqueta de color con el estado de validación de una visita. */
@Composable
fun ChipEstado(estado: EstadoValidacion, modifier: Modifier = Modifier) {
    val (fondoObjetivo, textoObjetivo) = coloresEstado(estado)
    // Transición suave de color cuando la visita cambia de estado
    val fondo by animateColorAsState(fondoObjetivo, label = "fondo_chip")
    val texto by animateColorAsState(textoObjetivo, label = "texto_chip")
    Surface(color = fondo, shape = MaterialTheme.shapes.small, modifier = modifier) {
        Text(
            estado.etiqueta,
            color = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun coloresEstado(estado: EstadoValidacion): Pair<Color, Color> = when (estado) {
    EstadoValidacion.PENDIENTE -> AmbarAlertaContenedor to AmbarAlerta
    EstadoValidacion.VALIDADA -> VerdeExitoContenedor to VerdeExito
    EstadoValidacion.RECHAZADA -> RojoErrorContenedor to RojoError
}
