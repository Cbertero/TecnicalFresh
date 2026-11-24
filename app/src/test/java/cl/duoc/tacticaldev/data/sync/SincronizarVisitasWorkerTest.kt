package cl.duoc.tacticaldev.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import cl.duoc.tacticaldev.TacticalApp
import cl.duoc.tacticaldev.data.remote.ApiService
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.testutil.DatosPrueba
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import androidx.work.impl.WorkManagerImpl
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Tarea de WorkManager que envía las visitas guardadas sin internet.
 * Con "work-testing" se crea un WorkManager de prueba que no espera a Android.
 */
@RunWith(AndroidJUnit4::class)
class SincronizarVisitasWorkerTest {

    /** El WorkManager de prueba queda guardado en memoria: se quita al terminar cada prueba. */
    @After
    fun quitarWorkManagerDePrueba() {
        WorkManagerImpl.setDelegate(null)
    }

    @Test
    fun programar_dejaUnaSolaTareaEnColaAunqueSeLlameDosVeces() {
        val context: Context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        )

        SincronizarVisitasWorker.programar(context)
        SincronizarVisitasWorker.programar(context) // ExistingWorkPolicy.KEEP: no se duplica

        val tareas = WorkManager.getInstance(context).getWorkInfosForUniqueWork("sincronizar_visitas").get()
        tareas shouldHaveSize 1
        // Sin cumplir la condición "con internet" la tarea queda esperando
        tareas.first().state shouldBe WorkInfo.State.ENQUEUED
    }

    @Test
    @Config(application = TacticalApp::class)
    fun doWork_sinVisitasPendientes_terminaConExito() = runBlocking<Unit> {
        val app = ApplicationProvider.getApplicationContext<TacticalApp>()
        vaciarCola(app)
        val worker = TestListenableWorkerBuilder.from(app, SincronizarVisitasWorker::class.java).build()

        worker.doWork() shouldBe ListenableWorker.Result.success()
    }

    @Test
    @Config(application = TacticalApp::class)
    fun doWork_siSeCortaInternet_pideReintentarMasTarde() = runBlocking<Unit> {
        val app = ApplicationProvider.getApplicationContext<TacticalApp>()
        // Se simula el microservicio: cualquier envío falla por falta de conexión
        val api: ApiService = mockk()
        coEvery { api.registrarVisita(any()) } throws IOException("Sin internet")
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns api
        try {
            vaciarCola(app)
            app.container.database.visitaPendienteDao().insertar(DatosPrueba.visitaPendiente().copy(id = 0))
            val worker = TestListenableWorkerBuilder.from(app, SincronizarVisitasWorker::class.java).build()

            worker.doWork() shouldBe ListenableWorker.Result.retry()
        } finally {
            vaciarCola(app) // nunca dejar visitas falsas que se puedan enviar al servidor real
            unmockkObject(RetrofitInstance)
        }
    }

    private suspend fun vaciarCola(app: TacticalApp) {
        val dao = app.container.database.visitaPendienteDao()
        dao.obtenerTodas().forEach { dao.eliminar(it.id) }
    }
}
