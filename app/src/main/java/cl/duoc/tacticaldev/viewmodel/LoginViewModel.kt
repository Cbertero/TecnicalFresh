package cl.duoc.tacticaldev.viewmodel

import cl.duoc.tacticaldev.data.repository.ServidorNoDisponibleException
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
 * autentica contra el microservicio y guarda la sesión con su rol.
 */
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesionPreferences: SesionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        comprobarServidor()
    }

    /** Consulta al microservicio y actualiza el indicador de la pantalla de login. */
    fun comprobarServidor() {
        viewModelScope.launch {
            _uiState.update { it.copy(servidorEnLinea = null) }
            val disponible = usuarioRepository.servidorDisponible()
            _uiState.update { it.copy(servidorEnLinea = disponible) }
        }
    }

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
        val valido = errores.correo == null && errores.clave == null
        _uiState.update {
            it.copy(errores = errores, intentosFallidos = if (valido) it.intentosFallidos else it.intentosFallidos + 1)
        }
        return valido
    }

    fun iniciarSesion(onExito: (Rol) -> Unit) {
        if (!validarFormulario()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            val usuario = try {
                usuarioRepository.autenticar(estado.correo, estado.clave)
            } catch (e: ServidorNoDisponibleException) {
                // Sin internet no se puede iniciar sesión: el login se valida solo en el microservicio
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message, servidorEnLinea = false) }
                return@launch
            }
            if (usuario == null) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "Correo o clave incorrectos",
                        intentosFallidos = it.intentosFallidos + 1,
                        servidorEnLinea = true // el servidor respondió (401)
                    )
                }
                return@launch
            }
            val rol = usuario.rol.aEnum(Rol.OPERARIO)
            sesionPreferences.guardar(Sesion(usuario.id, usuario.nombre, rol,usuario.cargo))
            _uiState.update { LoginUiState(servidorEnLinea = it.servidorEnLinea) }
            onExito(rol)
        }
    }
}
