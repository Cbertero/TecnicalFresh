package cl.duoc.tacticaldev.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.duoc.tacticaldev.TacticalApp
import java.util.concurrent.TimeUnit

/**
 * Tarea en segundo plano (WorkManager) que envía a MySQL las visitas guardadas
 * sin internet. Android la ejecuta solo cuando hay conexión, aunque la app esté cerrada.
 */
class SincronizarVisitasWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val repositorio = (applicationContext as TacticalApp).container.visitaRepository
        // Si quedó alguna pendiente (se volvió a cortar internet), WorkManager reintenta más tarde
        return if (repositorio.sincronizarPendientes()) Result.success() else Result.retry()
    }

    companion object {
        private const val NOMBRE = "sincronizar_visitas"

        /** Programa el envío para cuando haya internet. Si ya está programado, no lo duplica. */
        fun programar(contexto: Context) {
            val solicitud = OneTimeWorkRequestBuilder<SincronizarVisitasWorker>()
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(contexto)
                .enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.KEEP, solicitud)
        }
    }
}
