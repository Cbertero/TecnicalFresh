package cl.duoc.tacticaldev.testutil

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Varios ViewModels usan `stateIn(..., SharingStarted.WhileSubscribed(5_000), ...)`.
 * Eso significa: "solo calculo el estado si alguien me está observando".
 * En la app, quien observa es la pantalla Compose. En la prueba no hay pantalla,
 * así que creamos un observador en segundo plano (backgroundScope), que se
 * cancela solo al terminar la prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.suscribir(flow: Flow<T>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
}

/**
 * Igual que [suscribir], pero además guarda cada valor emitido en una lista.
 * Útil para los SharedFlow de mensajes o eventos de navegación, que no guardan
 * el último valor: si nadie escucha en el momento, el mensaje se pierde.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.recolectar(flow: Flow<T>): List<T> {
    val valores = mutableListOf<T>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(valores) }
    return valores
}
