package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.location.UbicacionService
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.model.VisitaFormUiState
import cl.duoc.tacticaldev.navigation.Screen
import cl.duoc.tacticaldev.utils.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lógica del formulario de visita. Toda la validación se delega a [Validadores]
 * y el estado se expone como StateFlow para que la vista solo lo observe.
 */
class VisitaViewModel(
    savedStateHandle: SavedStateHandle,
    private val activoRepository: ActivoRepository,
    private val visitaRepository: VisitaRepository,
    private val sesionPreferences: SesionPreferences,
    private val ubicacionService: UbicacionService
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

    /** Recibe el URI de la foto tomada con la cámara. */
    fun onFotoCapturada(uri: String) = _uiState.update {
        it.copy(fotoUri = uri, errores = it.errores.copy(foto = null))
    }

    /**
     * Obtiene la ubicación GPS y calcula la distancia al punto de venta.
     * Si está dentro del radio, la visita queda como "ubicación verificada".
     */
    fun obtenerUbicacion() {
        val activo = _uiState.value.activo ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(obteniendoUbicacion = true) }
            val ubicacion = ubicacionService.obtenerUbicacionActual()
            if (ubicacion == null) {
                _uiState.update {
                    it.copy(
                        obteniendoUbicacion = false,
                        errores = it.errores.copy(ubicacion = "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo")
                    )
                }
                return@launch
            }
            val distancia = GeoUtils.distanciaMetros(
                ubicacion.latitude, ubicacion.longitude, activo.latitud, activo.longitud
            )
            _uiState.update {
                it.copy(
                    obteniendoUbicacion = false,
                    latitud = ubicacion.latitude,
                    longitud = ubicacion.longitude,
                    distanciaMetros = distancia,
                    ubicacionVerificada = GeoUtils.estaDentroDelRadio(distancia),
                    errores = it.errores.copy(ubicacion = null)
                )
            }
        }
    }

    fun onPermisoUbicacionDenegado() = _uiState.update {
        it.copy(errores = it.errores.copy(ubicacion = "Se necesita el permiso de ubicación para verificar la visita"))
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

    /**
     * Valida y guarda la visita en Room con la fecha y hora actuales.
     * Al terminar entrega el id de la visita para mostrar la confirmación.
     */
    fun guardar(onGuardada: (Long) -> Unit) {
        if (!validarFormulario()) {
            _uiState.update {
                it.copy(mensaje = "Revisa los campos marcados en rojo", intentosFallidos = it.intentosFallidos + 1)
            }
            return
        }
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val sesion = sesionPreferences.sesion.first()
            if (sesion == null) {
                _uiState.update { it.copy(guardando = false, mensaje = "La sesión expiró, vuelve a ingresar") }
                return@launch
            }
            val visita = VisitaEntity(
                activoId = activoId,
                usuarioId = sesion.usuarioId,
                fechaHora = System.currentTimeMillis(),
                tipoVisita = checkNotNull(s.tipoVisita).name,
                estadoEquipo = checkNotNull(s.estadoEquipo).name,
                temperatura = s.temperatura.replace(',', '.').toDouble(),
                observaciones = s.observaciones.trim(),
                fotoUri = s.fotoUri.orEmpty(),
                latitud = checkNotNull(s.latitud),
                longitud = checkNotNull(s.longitud),
                distanciaMetros = s.distanciaMetros ?: 0.0,
                ubicacionVerificada = s.ubicacionVerificada
            )
            val id = visitaRepository.registrar(visita)
            _uiState.update { it.copy(guardando = false) }
            onGuardada(id)
        }
    }
}
