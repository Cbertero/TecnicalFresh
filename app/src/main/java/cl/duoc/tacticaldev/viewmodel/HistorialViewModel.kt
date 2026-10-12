package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.model.ActivoConPunto
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistorialUiState(
    val cargando: Boolean = true,
    val activo: ActivoConPunto? = null,
    val visitas: List<VisitaDetalle> = emptyList()
)

/** Historial único y consultable de un activo (reemplaza WhatsApp/Excel/papel). */
class HistorialViewModel(
    savedStateHandle: SavedStateHandle,
    activoRepository: ActivoRepository,
    visitaRepository: VisitaRepository
) : ViewModel() {

    private val activoId: Long = checkNotNull(savedStateHandle[Screen.ARG_ACTIVO_ID])
    private val activo = MutableStateFlow<ActivoConPunto?>(null)

    init {
        viewModelScope.launch { activo.value = activoRepository.obtenerActivo(activoId) }
    }

    val uiState: StateFlow<HistorialUiState> =
        combine(activo, visitaRepository.observarPorActivo(activoId)) { activo, visitas ->
            HistorialUiState(cargando = false, activo = activo, visitas = visitas)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())
}
