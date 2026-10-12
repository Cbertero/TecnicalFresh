package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.model.VisitaFormUiState
import cl.duoc.tacticaldev.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lógica del formulario de visita. Toda la validación se delega a [Validadores]
 * y el estado se expone como StateFlow para que la vista solo lo observe.
 */
class VisitaViewModel(
    savedStateHandle: SavedStateHandle,
    private val activoRepository: ActivoRepository
) : ViewModel() {

    private val activoId: Long = checkNotNull(savedStateHandle[Screen.ARG_ACTIVO_ID])

    private val _uiState = MutableStateFlow(VisitaFormUiState())
    val uiState: StateFlow<VisitaFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activo = activoRepository.obtenerActivo(activoId)
            _uiState.update { it.copy(cargando = false, activo = activo) }
        }
    }

    fun onTipoVisitaChange(tipo: TipoVisita) = _uiState.update {
        it.copy(tipoVisita = tipo, errores = it.errores.copy(tipoVisita = null))
    }

    fun onEstadoEquipoChange(estado: EstadoEquipo) = _uiState.update {
        it.copy(
            estadoEquipo = estado,
            errores = it.errores.copy(
                estadoEquipo = null,
                // Si cambia el estado, la regla de observaciones también cambia
                observaciones = Validadores.observaciones(it.observaciones, estado)
            )
        )
    }

    fun onTemperaturaChange(valor: String) = _uiState.update {
        it.copy(temperatura = valor, errores = it.errores.copy(temperatura = Validadores.temperatura(valor)))
    }

    fun onObservacionesChange(valor: String) = _uiState.update {
        it.copy(
            observaciones = valor,
            errores = it.errores.copy(observaciones = Validadores.observaciones(valor, it.estadoEquipo))
        )
    }

    fun onConfirmacionChange(valor: Boolean) = _uiState.update {
        it.copy(confirmado = valor, errores = it.errores.copy(confirmacion = Validadores.confirmacion(valor)))
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    /** Valida todos los campos y actualiza los errores. Devuelve true si todo es válido. */
    fun validarFormulario(): Boolean {
        val s = _uiState.value
        val errores = s.errores.copy(
            tipoVisita = Validadores.seleccion(s.tipoVisita, "el tipo de visita"),
            estadoEquipo = Validadores.seleccion(s.estadoEquipo, "el estado del equipo"),
            temperatura = Validadores.temperatura(s.temperatura),
            observaciones = Validadores.observaciones(s.observaciones, s.estadoEquipo),
            foto = Validadores.foto(s.fotoUri),
            ubicacion = Validadores.ubicacion(s.latitud, s.longitud),
            confirmacion = Validadores.confirmacion(s.confirmado)
        )
        _uiState.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

    fun enviar() {
        if (validarFormulario()) {
            _uiState.update { it.copy(mensaje = "Formulario válido") }
        } else {
            _uiState.update { it.copy(mensaje = "Revisa los campos marcados en rojo") }
        }
    }
}
