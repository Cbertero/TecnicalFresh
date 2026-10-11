package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.model.ActivoConPunto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ActivosUiState(
    val cargando: Boolean = true,
    val activos: List<ActivoConPunto> = emptyList()
)

/** Expone la lista de activos como StateFlow, filtrada por el texto de búsqueda. */
class ActivosViewModel(repository: ActivoRepository) : ViewModel() {

    // El texto se expone aparte para que el TextField se actualice sin retraso
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val uiState: StateFlow<ActivosUiState> =
        combine(repository.observarActivos(), _busqueda) { activos, texto ->
            val filtro = texto.trim()
            ActivosUiState(
                cargando = false,
                activos = if (filtro.isEmpty()) activos else activos.filter {
                    it.codigo.contains(filtro, ignoreCase = true) ||
                        it.puntoVentaNombre.contains(filtro, ignoreCase = true) ||
                        it.comuna.contains(filtro, ignoreCase = true)
                }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivosUiState())

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }
}
