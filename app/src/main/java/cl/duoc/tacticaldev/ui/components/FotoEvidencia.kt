package cl.duoc.tacticaldev.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(uri))
                .crossfade(true)
                .build(),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(forma)
        )
    }
}
