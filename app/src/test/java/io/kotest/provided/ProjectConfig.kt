package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Configuración global de Kotest. Kotest la detecta sola porque está en el
 * paquete `io.kotest.provided` con el nombre `ProjectConfig`.
 *
 * 1. isolationMode = InstancePerLeaf
 *    Por defecto Kotest crea UNA instancia de la clase de prueba y la comparte
 *    entre todos los `test(...)`. Con InstancePerLeaf se crea una instancia nueva
 *    por cada prueba, así cada una parte con mocks y ViewModels "limpios"
 *    (igual que hacía JUnit con @Before).
 *
 * 2. Dispatchers.Main
 *    Los ViewModels lanzan corrutinas en `viewModelScope`, que usa el hilo
 *    principal de Android. En la JVM ese hilo no existe, así que antes de
 *    ejecutar cualquier prueba lo reemplazamos por un dispatcher de pruebas.
 *    UnconfinedTestDispatcher ejecuta las corrutinas de inmediato: al terminar
 *    de llamar una función del ViewModel ya podemos revisar su estado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectConfig : AbstractProjectConfig() {

    override val isolationMode = IsolationMode.InstancePerLeaf

    override suspend fun beforeProject() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    override suspend fun afterProject() {
        Dispatchers.resetMain()
    }
}
