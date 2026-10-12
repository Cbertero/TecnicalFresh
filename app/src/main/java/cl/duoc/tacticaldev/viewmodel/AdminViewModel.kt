package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.location.UbicacionService
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.ActivoForm
import cl.duoc.tacticaldev.model.AdminUiState
import cl.duoc.tacticaldev.model.PuntoVentaForm
import cl.duoc.tacticaldev.model.TipoActivo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/** Registro de nuevos puntos de venta y activos por parte del supervisor. */
class AdminViewModel(
    private val activoRepository: ActivoRepository,
    private val ubicacionService: UbicacionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    val puntosVenta: StateFlow<List<PuntoVentaEntity>> = activoRepository.observarPuntosVenta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    fun onPestanaChange(indice: Int) = _uiState.update { it.copy(pestana = indice) }

    // ---------- Punto de venta ----------

    private fun actualizarPunto(cambio: (PuntoVentaForm) -> PuntoVentaForm) =
        _uiState.update { it.copy(puntoVenta = cambio(it.puntoVenta)) }

    fun onNombrePuntoChange(v: String) = actualizarPunto {
        it.copy(nombre = v, errores = it.errores.copy(nombre = Validadores.textoRequerido(v, "el nombre")))
    }

    fun onDireccionChange(v: String) = actualizarPunto {
        it.copy(direccion = v, errores = it.errores.copy(direccion = Validadores.textoRequerido(v, "la dirección", max = 80)))
    }

    fun onComunaChange(v: String) = actualizarPunto {
        it.copy(comuna = v, errores = it.errores.copy(comuna = Validadores.textoRequerido(v, "la comuna", max = 40)))
    }

    fun onLatitudChange(v: String) = actualizarPunto {
        it.copy(latitud = v, errores = it.errores.copy(latitud = Validadores.latitud(v)))
    }

    fun onLongitudChange(v: String) = actualizarPunto {
        it.copy(longitud = v, errores = it.errores.copy(longitud = Validadores.longitud(v)))
    }

    /** Completa latitud y longitud con la ubicación actual (útil al instalar un equipo). */
    fun usarUbicacionActual() {
        viewModelScope.launch {
            _uiState.update { it.copy(obteniendoUbicacion = true) }
            val ubicacion = ubicacionService.obtenerUbicacionActual()
            _uiState.update { it.copy(obteniendoUbicacion = false) }
            if (ubicacion == null) {
                _mensajes.emit("No se pudo obtener la ubicación")
            } else {
                onLatitudChange(String.format(Locale.US, "%.6f", ubicacion.latitude))
                onLongitudChange(String.format(Locale.US, "%.6f", ubicacion.longitude))
            }
        }
    }

    fun onPermisoUbicacionDenegado() {
        _mensajes.tryEmit("Se necesita el permiso de ubicación")
    }

    fun guardarPuntoVenta() {
        val f = _uiState.value.puntoVenta
        val errores = f.errores.copy(
            nombre = Validadores.textoRequerido(f.nombre, "el nombre"),
            direccion = Validadores.textoRequerido(f.direccion, "la dirección", max = 80),
            comuna = Validadores.textoRequerido(f.comuna, "la comuna", max = 40),
            latitud = Validadores.latitud(f.latitud),
            longitud = Validadores.longitud(f.longitud)
        )
        actualizarPunto { it.copy(errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            activoRepository.registrarPuntoVenta(
                PuntoVentaEntity(
                    nombre = f.nombre.trim(),
                    direccion = f.direccion.trim(),
                    comuna = f.comuna.trim(),
                    latitud = f.latitud.replace(',', '.').toDouble(),
                    longitud = f.longitud.replace(',', '.').toDouble()
                )
            )
            _uiState.update { it.copy(guardando = false, puntoVenta = PuntoVentaForm()) }
            _mensajes.emit("Punto de venta \"${f.nombre.trim()}\" registrado")
        }
    }

    // ---------- Activo ----------

    private fun actualizarActivo(cambio: (ActivoForm) -> ActivoForm) =
        _uiState.update { it.copy(activo = cambio(it.activo)) }

    fun onCodigoChange(v: String) = actualizarActivo {
        it.copy(codigo = v.uppercase(), errores = it.errores.copy(codigo = Validadores.codigoActivo(v)))
    }

    fun onTipoActivoChange(tipo: TipoActivo) = actualizarActivo {
        it.copy(tipo = tipo, errores = it.errores.copy(tipo = null))
    }

    fun onMarcaChange(v: String) = actualizarActivo {
        it.copy(marca = v, errores = it.errores.copy(marca = Validadores.textoRequerido(v, "la marca", min = 2, max = 30)))
    }

    fun onModeloChange(v: String) = actualizarActivo {
        it.copy(modelo = v, errores = it.errores.copy(modelo = Validadores.textoRequerido(v, "el modelo", min = 2, max = 30)))
    }

    fun onPuntoVentaChange(id: Long) = actualizarActivo {
        it.copy(puntoVentaId = id, errores = it.errores.copy(puntoVenta = null))
    }

    fun guardarActivo() {
        val f = _uiState.value.activo
        val errores = f.errores.copy(
            codigo = Validadores.codigoActivo(f.codigo),
            tipo = Validadores.seleccion(f.tipo, "el tipo de activo"),
            marca = Validadores.textoRequerido(f.marca, "la marca", min = 2, max = 30),
            modelo = Validadores.textoRequerido(f.modelo, "el modelo", min = 2, max = 30),
            puntoVenta = Validadores.seleccion(f.puntoVentaId, "el punto de venta")
        )
        actualizarActivo { it.copy(errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            // El código del QR debe ser único
            if (activoRepository.buscarPorCodigo(f.codigo) != null) {
                actualizarActivo { it.copy(errores = it.errores.copy(codigo = "Ya existe un activo con este código")) }
                return@launch
            }
            _uiState.update { it.copy(guardando = true) }
            activoRepository.registrarActivo(
                ActivoEntity(
                    codigo = f.codigo.trim().uppercase(),
                    tipo = checkNotNull(f.tipo).name,
                    marca = f.marca.trim(),
                    modelo = f.modelo.trim(),
                    puntoVentaId = checkNotNull(f.puntoVentaId)
                )
            )
            _uiState.update { it.copy(guardando = false, activo = ActivoForm()) }
            _mensajes.emit("Activo ${f.codigo.trim().uppercase()} registrado")
        }
    }
}
