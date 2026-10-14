package cl.duoc.tacticaldev.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.tacticaldev.ui.theme.RojoError
import cl.duoc.tacticaldev.ui.theme.SuperficieTarjeta
import cl.duoc.tacticaldev.ui.theme.VerdeExito

enum class TipoAlerta(val icono: ImageVector) {
    EXITO(Icons.Default.CheckCircle),
    ERROR(Icons.Default.Cancel),
    INFO(Icons.Default.Info);

    val color: Color
        @Composable get() = when (this) {
            EXITO -> VerdeExito
            ERROR -> RojoError
            INFO -> Color(0xFF38BDF8)
        }
}
/**
 * Banner flotante superior con animación de entrada elástica y rebote.
 */
@Composable
fun AlertaFlotanteAnimada(
    mensaje: String?,
    modifier: Modifier = Modifier,
    tipo: TipoAlerta = if (mensaje?.contains("rechazada", ignoreCase = true) == true) TipoAlerta.ERROR else TipoAlerta.EXITO
) {
    AnimatedVisibility(
        visible = !mensaje.isNullOrBlank(),
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }) + scaleOut(targetScale = 0.9f),
        modifier = modifier
    ) {
        if (!mensaje.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SuperficieTarjeta.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, tipo.color.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = tipo.icono,
                        contentDescription = null,
                        tint = tipo.color,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}