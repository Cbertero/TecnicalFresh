package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.TecnicoRepository
import cl.duoc.tacticaldev.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TecnicoViewModel(
    private val repository: TecnicoRepository,
    usuarioRepository: UsuarioRepository,
    activoRepository: ActivoRepository
) : ViewModel() {

    val tecnicoList: StateFlow<List<Tecnico>> = repository.asignacionesFlow

    val operarios: StateFlow<List<UsuarioEntity>> = usuarioRepository.observarOperarios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val locales: StateFlow<List<PuntoVentaEntity>> = activoRepository.observarPuntosVenta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensajeEstado = MutableStateFlow<String?>(null)
    val mensajeEstado: StateFlow<String?> = _mensajeEstado.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    init {
        fetchTecnicos()
    }

    fun fetchTecnicos() {
        viewModelScope.launch {
            _sincronizando.value = true
            try {
                val lista = repository.geTecnicos()
                val pendientes = lista.count { !it.sincronizado }
                _mensajeEstado.value = if (pendientes > 0) {
                    "Modo local activo: $pendientes asignación(es) pendientes de internet"
                } else {
                    "Conectado y sincronizado con el servidor MySQL"
                }
            } catch (e: Exception) {
                _mensajeEstado.value = "Modo local (sin conexión al servidor)"
            } finally {
                _sincronizando.value = false
            }
        }
    }

    fun agregarTecnico(nombre: String, cargo: String, localAsignado: String) {
        if (nombre.isBlank() || cargo.isBlank() || localAsignado.isBlank()) return
        viewModelScope.launch {
            _sincronizando.value = true
            try {
                val guardado = repository.GuardarTecnico(
                    nombre = nombre.trim(),
                    cargo = cargo.trim(),
                    localAsignado = localAsignado.trim()
                )
                _mensajeEstado.value = if (guardado.sincronizado) {
                    "Asignación guardada en servidor MySQL"
                } else {
                    "Sin internet: guardado en modo local (se subirá al reconectar)"
                }
            } catch (e: Exception) {
                _mensajeEstado.value = "Guardado en modo local"
            } finally {
                _sincronizando.value = false
            }
        }
    }

    fun eliminarTecnico(id: Long) {
        viewModelScope.launch {
            try {
                repository.eliminarTecnico(id)
                fetchTecnicos()
            } catch (e: Exception) {
                println("Error al eliminar técnico: ${e.localizedMessage}")
            }
        }
    }
}
