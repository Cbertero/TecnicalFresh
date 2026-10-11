package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.navigation.NavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Centraliza la navegación (Guía 10): las pantallas solicitan navegar y el
 * NavHost escucha estos eventos, separando la lógica de la vista.
 */
class NavigationViewModel : ViewModel() {

    private val _eventos = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val eventos: SharedFlow<NavigationEvent> = _eventos.asSharedFlow()

    fun navegarA(
        ruta: String,
        popUpToRoute: String? = null,
        inclusive: Boolean = false,
        limpiarPila: Boolean = false
    ) = emitir(NavigationEvent.NavigateTo(ruta, popUpToRoute, inclusive, limpiarPila))

    fun cambiarPestana(ruta: String, rutaRaiz: String) =
        emitir(NavigationEvent.NavigateTab(ruta, rutaRaiz))

    fun volver() = emitir(NavigationEvent.PopBackStack)

    private fun emitir(evento: NavigationEvent) {
        viewModelScope.launch { _eventos.emit(evento) }
    }
}
