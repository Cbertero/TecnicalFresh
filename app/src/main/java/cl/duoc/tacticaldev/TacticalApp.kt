package cl.duoc.tacticaldev

import android.app.Application
import cl.duoc.tacticaldev.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Clase Application: crea el contenedor de dependencias y carga los datos ficticios. */
class TacticalApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch { container.database.sembrarSiEstaVacia() }
    }
}
