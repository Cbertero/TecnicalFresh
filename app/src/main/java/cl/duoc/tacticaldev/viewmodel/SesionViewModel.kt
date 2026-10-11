package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la sesión al abrir la app. */
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val sesion: Sesion) : EstadoSesion
}

class SesionViewModel(private val sesionPreferences: SesionPreferences) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = sesionPreferences.sesion
        .map { sesion -> if (sesion == null) EstadoSesion.SinSesion else EstadoSesion.Activa(sesion) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            sesionPreferences.cerrar()
            alTerminar()
        }
    }
}
