package cl.duoc.tacticaldev

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import androidx.work.testing.WorkManagerTestInitHelper
import cl.duoc.tacticaldev.data.local.AppDatabase
import cl.duoc.tacticaldev.di.AppContainer
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Clase Application: Robolectric la crea y llama a onCreate() igual que Android
 * al abrir la app. Se comprueba que arme el contenedor, cargue los datos ficticios
 * y programe la sincronización de visitas pendientes.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = TacticalApp::class)
class TacticalAppTest {

    private val app: TacticalApp = ApplicationProvider.getApplicationContext()

    @Test
    fun onCreate_creaElContenedorDeDependencias() {
        app.container.database shouldBeSameInstanceAs AppDatabase.obtener(app)
    }

    @Test
    fun onCreate_cargaLosDatosFicticios() = runBlocking<Unit> {
        // onCreate los carga en segundo plano; volver a llamarlo espera a que termine sin duplicar
        app.container.database.sembrarSiEstaVacia()

        app.container.database.usuarioDao().contar() shouldBeGreaterThan 0
    }

    @Test
    fun onCreate_programaLaSincronizacionDeVisitas() {
        // WorkManager de prueba (en memoria) para no depender del de otras pruebas
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        try {
            app.onCreate()
            WorkManager.getInstance(app).getWorkInfosForUniqueWork("sincronizar_visitas").get().shouldNotBeEmpty()
        } finally {
            WorkManagerImpl.setDelegate(null)
        }
    }

    @Test
    fun workManager_usaLaConfiguracionDeLaApp() {
        app.workManagerConfiguration.minimumLoggingLevel shouldBe android.util.Log.INFO
    }
}

/** Contenedor de dependencias: cada repositorio se crea una sola vez (by lazy). */
@RunWith(AndroidJUnit4::class)
class AppContainerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val container = AppContainer(context)

    @Test
    fun cadaDependenciaSeCreaUnaSolaVez() {
        container.usuarioRepository shouldBeSameInstanceAs container.usuarioRepository
        container.activoRepository shouldBeSameInstanceAs container.activoRepository
        container.visitaRepository shouldBeSameInstanceAs container.visitaRepository
        container.climaRepository shouldBeSameInstanceAs container.climaRepository
        container.tecnicoRepository shouldBeSameInstanceAs container.tecnicoRepository
        container.sesionPreferences shouldBeSameInstanceAs container.sesionPreferences
        container.ubicacionService shouldBeSameInstanceAs container.ubicacionService
    }

    @Test
    fun laBaseDeDatosEsLaUnicaDeLaApp() {
        container.database shouldBeSameInstanceAs AppDatabase.obtener(context)
    }

    @Test
    fun dosContenedoresCompartenLaBasePeroNoLosRepositorios() {
        val otro = AppContainer(context)

        otro.database shouldBeSameInstanceAs container.database
        (otro.usuarioRepository === container.usuarioRepository) shouldBe false
    }
}
