package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.TecnicoRepository
import cl.duoc.tacticaldev.model.ActivoConPunto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActivosUiState(
    val cargando: Boolean = true,
    val activos: List<ActivoConPunto> = emptyList()
)

/**
 * Expone la lista de activos como StateFlow, filtrada por:
 * 1) Los locales asignados al operario conectado (desde el microservicio o modo local).
 * 2) Si aún no tiene asignaciones manuales del supervisor, asigna locales distintos según su cargo
 *    para que Técnico, Vendedor y Transportista no vean los mismos locales.
 * 3) El texto de búsqueda ingresado por el usuario.
 */
class ActivosViewModel(
    private val repository: ActivoRepository,
    sesionPreferences: SesionPreferences,
    private val tecnicoRepository: TecnicoRepository
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    init {
        viewModelScope.launch {
            tecnicoRepository.geTecnicos()
        }
    }

    val uiState: StateFlow<ActivosUiState> = combine(
        repository.observarActivos(),
        _busqueda,
        sesionPreferences.sesion,
        tecnicoRepository.asignacionesFlow
    ) { activos, texto, sesion, asignaciones ->
        val nombreUsuario = sesion?.nombre?.trim().orEmpty()
        val cargoUsuario = sesion?.cargo?.trim().orEmpty()

        // 1. Buscar locales asignados explícitamente por el supervisor a este trabajador
        val localesAsignadosAlUsuario = asignaciones
            .filter {
                it.nombre.trim().equals(nombreUsuario, ignoreCase = true) &&
                    !it.localAsignado.isNullOrBlank()
            }
            .mapNotNull { it.localAsignado?.trim()?.lowercase() }
            .toSet()

        // 2. Filtrar activos según asignación del supervisor, o por cargo por defecto
        val activosDelOperario = when {
            localesAsignadosAlUsuario.isNotEmpty() -> {
                activos.filter { it.puntoVentaNombre.trim().lowercase() in localesAsignadosAlUsuario }
            }
            cargoUsuario.contains("Vendedor", ignoreCase = true) -> {
                activos.filter { it.puntoVentaNombre.equals("Minimarket Las Rosas", ignoreCase = true) }
            }
            cargoUsuario.contains("Transportista", ignoreCase = true) -> {
                activos.filter { it.puntoVentaNombre.equals("Botillería La Esquina", ignoreCase = true) }
            }
            cargoUsuario.contains("Técnico", ignoreCase = true) -> {
                activos.filter {
                    it.puntoVentaNombre.equals("Minimarket El Alba", ignoreCase = true) ||
                        it.puntoVentaNombre.equals("Almacén Los Aromos", ignoreCase = true)
                }
            }
            else -> activos
        }

        // 3. Aplicar filtro del buscador de texto
        val filtro = texto.trim()
        ActivosUiState(
            cargando = false,
            activos = if (filtro.isEmpty()) activosDelOperario else activosDelOperario.filter {
                it.codigo.contains(filtro, ignoreCase = true) ||
                    it.puntoVentaNombre.contains(filtro, ignoreCase = true) ||
                    it.comuna.contains(filtro, ignoreCase = true)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivosUiState())

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }

    /** Busca el activo del QR escaneado; si existe abre directamente su formulario. */
    fun onCodigoEscaneado(codigo: String, onEncontrado: (Long) -> Unit) {
        viewModelScope.launch {
            val activo = repository.buscarPorCodigo(codigo)
            if (activo != null) onEncontrado(activo.id) else _mensajes.emit("No existe un activo con el código $codigo")
        }
    }

    fun mostrarMensaje(texto: String) {
        _mensajes.tryEmit(texto)
    }
}
