package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.repository.UsuarioRepository
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val rol: Rol = Rol.OPERARIO,
    val cargo: String = "Técnico de mantenimiento",
    val claveVisible: Boolean = false,
    val cargando: Boolean = false,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorConfirmarClave: String? = null,
    val errorGeneral: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, errorGeneral = null) }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    fun onConfirmarClaveChange(valor: String) {
        _uiState.update { it.copy(confirmarClave = valor, errorConfirmarClave = null, errorGeneral = null) }
    }

    fun onRolChange(rol: Rol) {
        val nuevoCargo = if (rol == Rol.SUPERVISOR) "Supervisora de operaciones" else "Técnico de mantenimiento"
        _uiState.update { it.copy(rol = rol, cargo = nuevoCargo) }
    }

    fun onCargoChange(cargo: String) {
        _uiState.update { it.copy(cargo = cargo) }
    }

    fun toggleClaveVisible() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    private fun validar(): Boolean {
        val s = _uiState.value
        val errNombre = if (s.nombre.trim().length < 3) "El nombre debe tener al menos 3 caracteres" else null
        val errCorreo = Validadores.correo(s.correo)
        val errClave = Validadores.clave(s.clave)
        val errConfirmar = when {
            s.confirmarClave.isBlank() -> "Confirma tu clave"
            s.clave != s.confirmarClave -> "Las contraseñas no coinciden"
            else -> null
        }

        _uiState.update {
            it.copy(
                errorNombre = errNombre,
                errorCorreo = errCorreo,
                errorClave = errClave,
                errorConfirmarClave = errConfirmar
            )
        }
        return errNombre == null && errCorreo == null && errClave == null && errConfirmar == null
    }
    fun registrar(onExito: () -> Unit) {
        if (!validar()) return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            val resultado = usuarioRepository.registrar(
                nombre = s.nombre,
                correo = s.correo,
                clave = s.clave,
                rol = s.rol.name,
                cargo = s.cargo
            )
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(cargando = false, registroExitoso = true) }
                    onExito()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(cargando = false, errorGeneral = error.localizedMessage ?: "Error al registrar")
                    }
                }
            )
        }
    }
}