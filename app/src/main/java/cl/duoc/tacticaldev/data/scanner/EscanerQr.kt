package cl.duoc.tacticaldev.data.scanner

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Resultado de un intento de escaneo. */
sealed interface ResultadoEscaneo {
    data class Exito(val codigo: String) : ResultadoEscaneo
    data object Cancelado : ResultadoEscaneo
    data class Error(val mensaje: String) : ResultadoEscaneo
}

/**
 * Abre el escáner de Google Play services y devuelve el texto del QR.
 * Cada activo tiene pegado un QR con su código (ej. TDV-VF-001).
 */
object EscanerQr {
    suspend fun escanear(context: Context): ResultadoEscaneo = suspendCancellableCoroutine { continuacion ->
        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        GmsBarcodeScanning.getClient(context, opciones)
            .startScan()
            .addOnSuccessListener { codigo ->
                val texto = codigo.rawValue
                continuacion.resume(
                    if (texto.isNullOrBlank()) ResultadoEscaneo.Error("El código QR está vacío")
                    else ResultadoEscaneo.Exito(texto)
                )
            }
            .addOnCanceledListener { continuacion.resume(ResultadoEscaneo.Cancelado) }
            .addOnFailureListener { error ->
                continuacion.resume(ResultadoEscaneo.Error(error.localizedMessage ?: "No se pudo abrir el escáner"))
            }
    }
}
