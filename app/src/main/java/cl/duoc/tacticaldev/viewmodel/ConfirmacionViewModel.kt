package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Carga desde Room la visita recién guardada para mostrar el comprobante. */
class ConfirmacionViewModel(
    savedStateHandle: SavedStateHandle,
    visitaRepository: VisitaRepository
) : ViewModel() {

    private val visitaId: Long = checkNotNull(savedStateHandle[Screen.ARG_VISITA_ID])

    private val _visita = MutableStateFlow<VisitaDetalle?>(null)
    val visita: StateFlow<VisitaDetalle?> = _visita.asStateFlow()

    init {
        viewModelScope.launch { _visita.value = visitaRepository.obtenerDetalle(visitaId) }
    }
}
