package cl.duoc.tacticaldev.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.UsuarioRepository
import cl.duoc.tacticaldev.domain.validacion.Validadores
import cl.duoc.tacticaldev.model.LoginUiState
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.model.aEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lógica del login: valida cada campo en tiempo real con [Validadores],
 * autentica contra Room y guarda la sesión con su rol.
 */
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesionPreferences: SesionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) = _uiState.update {
        it.copy(correo = valor, errorGeneral = null, errores = it.errores.copy(correo = Validadores.correo(valor)))
    }

    fun onClaveChange(valor: String) = _uiState.update {
        it.copy(clave = valor, errorGeneral = null, errores = it.errores.copy(clave = Validadores.clave(valor)))
    }

    fun alternarVisibilidadClave() = _uiState.update { it.copy(claveVisible = !it.claveVisible) }

    /** Valida todos los campos; devuelve true si el formulario es válido. */
    fun validarFormulario(): Boolean {
        val estado = _uiState.value
        val errores = estado.errores.copy(
            correo = Validadores.correo(estado.correo),
            clave = Validadores.clave(estado.clave)
        )
        _uiState.update { it.copy(errores = errores) }
        return errores.correo == null && errores.clave == null
    }

    fun iniciarSesion(onExito: (Rol) -> Unit) {
        if (!validarFormulario()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            val usuario = usuarioRepository.autenticar(estado.correo, estado.clave)
            if (usuario == null) {
                _uiState.update { it.copy(cargando = false, errorGeneral = "Correo o clave incorrectos") }
                return@launch
            }
            val rol = usuario.rol.aEnum(Rol.OPERARIO)
            sesionPreferences.guardar(Sesion(usuario.id, usuario.nombre, rol))
            _uiState.update { LoginUiState() }
            onExito(rol)
        }
    }
}
