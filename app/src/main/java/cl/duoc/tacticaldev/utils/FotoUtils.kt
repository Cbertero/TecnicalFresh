package cl.duoc.tacticaldev.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Manejo de archivos de las fotos de evidencia. */
object FotoUtils {

    /**
     * Crea un archivo vacío en files/evidencias y devuelve su URI de FileProvider,
     * que es lo que recibe ActivityResultContracts.TakePicture.
     */
    fun crearUriFoto(context: Context): Uri {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val archivo = File(carpeta, "visita_$marca.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /** Borra la foto asociada al URI (por ejemplo, cuando el usuario cancela la cámara). */
    fun eliminar(context: Context, uri: Uri) {
        runCatching { context.contentResolver.delete(uri, null, null) }
    }

    /**
     * Convierte la foto tomada en un texto Base64 comprimido para enviarla al servidor.
     * Se reduce a 800 px como máximo y calidad JPEG 60 (~50-100 KB).
     */
    fun aBase64(context: Context, uriTexto: String): String? {
        if (uriTexto.isBlank()) return null
        if (uriTexto.startsWith("data:image")) return uriTexto // ya viene convertida
        return runCatching {
            val original = context.contentResolver.openInputStream(Uri.parse(uriTexto))
                ?.use { BitmapFactory.decodeStream(it) } ?: return null
            val escala = minOf(1f, 800f / maxOf(original.width, original.height))
            val reducida = if (escala < 1f) {
                Bitmap.createScaledBitmap(
                    original, (original.width * escala).toInt(), (original.height * escala).toInt(), true
                )
            } else original
            val salida = ByteArrayOutputStream()
            reducida.compress(Bitmap.CompressFormat.JPEG, 60, salida)
            "data:image/jpeg;base64," + Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
        }.getOrNull()
    }

}
