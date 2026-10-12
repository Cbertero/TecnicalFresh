package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.RechazoUiState
import cl.duoc.tacticaldev.model.SupervisorUiState
import cl.duoc.tacticaldev.model.VisitaDetalle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Revisión de visitas por parte del supervisor: validar o rechazar con motivo.
 * El motivo se valida con [Validadores.motivoRechazo].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SupervisorViewModel(
    private val visitaRepository: VisitaRepository,
    private val sesionPreferences: SesionPreferences
) : ViewModel() {

    private val filtro = MutableStateFlow(EstadoValidacion.PENDIENTE)

    val uiState: StateFlow<SupervisorUiState> = filtro
        .flatMapLatest { estado ->
            visitaRepository.observarPorEstado(estado).map { SupervisorUiState(false, estado, it) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SupervisorUiState())

    private val _rechazo = MutableStateFlow(RechazoUiState())
    val rechazo: StateFlow<RechazoUiState> = _rechazo.asStateFlow()

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun onFiltroChange(estado: EstadoValidacion) {
        filtro.value = estado
    }

    fun validar(visita: VisitaDetalle) {
        viewModelScope.launch {
            val supervisorId = sesionPreferences.sesion.first()?.usuarioId ?: return@launch
            visitaRepository.validar(visita.id, supervisorId)
            _mensajes.emit("Visita de ${visita.activoCodigo} validada")
        }
    }

    fun abrirRechazo(visita: VisitaDetalle) {
        _rechazo.value = RechazoUiState(visita = visita)
    }

    fun onMotivoChange(valor: String) = _rechazo.update {
        it.copy(motivo = valor, errorMotivo = Validadores.motivoRechazo(valor))
    }

    fun cancelarRechazo() {
        _rechazo.value = RechazoUiState()
    }

    fun confirmarRechazo() {
        val estado = _rechazo.value
        val visita = estado.visita ?: return
        val error = Validadores.motivoRechazo(estado.motivo)
        if (error != null) {
            _rechazo.update { it.copy(errorMotivo = error) }
            return
        }
        viewModelScope.launch {
            val supervisorId = sesionPreferences.sesion.first()?.usuarioId ?: return@launch
            visitaRepository.rechazar(visita.id, estado.motivo, supervisorId)
            _rechazo.value = RechazoUiState()
            _mensajes.emit("Visita de ${visita.activoCodigo} rechazada")
        }
    }
}
