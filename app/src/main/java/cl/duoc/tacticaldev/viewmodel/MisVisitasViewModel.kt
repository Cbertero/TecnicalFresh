package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.model.VisitaDetalle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MisVisitasUiState(
    val cargando: Boolean = true,
    val visitas: List<VisitaDetalle> = emptyList()
)

/** Visitas registradas por el operario conectado. */
@OptIn(ExperimentalCoroutinesApi::class)
class MisVisitasViewModel(
    sesionPreferences: SesionPreferences,
    visitaRepository: VisitaRepository
) : ViewModel() {

    val uiState: StateFlow<MisVisitasUiState> = sesionPreferences.sesion
        .flatMapLatest { sesion ->
            if (sesion == null) flowOf(emptyList<VisitaDetalle>()) else visitaRepository.observarPorUsuario(sesion.usuarioId)
        }
        .map { MisVisitasUiState(cargando = false, visitas = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MisVisitasUiState())
}
