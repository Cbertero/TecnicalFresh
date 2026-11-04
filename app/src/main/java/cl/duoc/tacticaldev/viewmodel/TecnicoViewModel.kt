package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.repository.TecnicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TecnicoViewModel : ViewModel() {

    private val repository = TecnicoRepository()

    private val _tecnicoList = MutableStateFlow<List<Tecnico>>(emptyList())
    val tecnicoList: StateFlow<List<Tecnico>> = _tecnicoList

    init {
        fetchTecnicos()
    }

    fun fetchTecnicos() {
        viewModelScope.launch {
            try {
                _tecnicoList.value = repository.geTecnicos()
            } catch (e: Exception) {
                println("Error al obtener datos: ${e.localizedMessage}")
            }
        }
    }

    fun agregarTecnico(nombre: String, cargo: String) {
        if (nombre.isBlank() || cargo.isBlank()) return
        viewModelScope.launch {
            try {
                repository.GuardarTecnico(nombre.trim(), cargo.trim())
                fetchTecnicos()
            } catch (e: Exception) {
                println("Error al guardar técnico: ${e.localizedMessage}")
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