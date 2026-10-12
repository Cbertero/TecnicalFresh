package cl.duoc.tacticaldev.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
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
}
