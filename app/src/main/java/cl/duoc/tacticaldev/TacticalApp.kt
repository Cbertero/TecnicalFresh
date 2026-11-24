package cl.duoc.tacticaldev

import android.app.Application
import androidx.work.Configuration
import cl.duoc.tacticaldev.data.sync.SincronizarVisitasWorker
import cl.duoc.tacticaldev.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase Application: crea el contenedor de dependencias y carga los datos ficticios.
 *
 * Implementa [Configuration.Provider] para que WorkManager se pueda iniciar "a pedido"
 * la primera vez que se usa. En el teléfono no cambia nada (lo inicia Android al abrir
 * la app), pero permite crear la app en las pruebas con Robolectric, donde Android no
 * ejecuta los inicializadores automáticos.
 */
class TacticalApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch { runCatching { container.database.sembrarSiEstaVacia() } }
        // Si quedaron visitas sin enviar de una sesión anterior, se envían al haber internet
        SincronizarVisitasWorker.programar(this)
    }
}
