package cl.duoc.tacticaldev.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Obtiene la ubicación actual del dispositivo con el Fused Location Provider. */
class UbicacionService(private val context: Context) {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Devuelve la ubicación actual o null si no hay permiso o no se pudo obtener.
     * Con GPS desactivado la consulta podía quedar esperando, por eso tiene un tiempo máximo.
     */
    @SuppressLint("MissingPermission") // el permiso se verifica con tienePermiso()
    suspend fun obtenerUbicacionActual(): Location? {
        if (!tienePermiso()) return null
        return withTimeoutOrNull(TIEMPO_MAXIMO_MS) { consultarUbicacion() }
    }

    @SuppressLint("MissingPermission")
    private suspend fun consultarUbicacion(): Location? =
        suspendCancellableCoroutine { continuacion ->
            val token = CancellationTokenSource()
            cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .addOnSuccessListener { ubicacion -> continuacion.resume(ubicacion) }
                .addOnFailureListener { continuacion.resume(null) }
            continuacion.invokeOnCancellation { token.cancel() }
        }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 15_000L
    }
}
