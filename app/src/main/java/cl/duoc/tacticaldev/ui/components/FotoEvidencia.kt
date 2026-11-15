package cl.duoc.tacticaldev.ui.components

import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Componente reutilizable que muestra la foto de evidencia (equivalente al
 * ImagenInteligente de la Guía 13). Si no hay foto muestra un ícono por defecto.
 */
@Composable
fun FotoEvidencia(
    uri: String?,
    modifier: Modifier = Modifier,
    descripcion: String = "Foto de evidencia"
) {
    val forma = RoundedCornerShape(12.dp)
    if (uri.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(forma)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AddAPhoto,
                contentDescription = "Sin foto",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
    } else {
        // La foto puede venir del servidor (Base64) o ser local recién tomada (content://)
        val modelo = remember(uri) {
            if (uri.startsWith("data:image")) {
                Base64.decode(uri.substringAfter(","), Base64.DEFAULT)
            } else {
                Uri.parse(uri)
            }
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(modelo)
                .crossfade(true)
                .build(),
            contentDescription = descripcion,
            modifier = modifier.clip(forma),
            contentScale = ContentScale.Crop
        )
    }
}
