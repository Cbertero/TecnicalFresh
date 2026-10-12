package cl.duoc.tacticaldev.model

/** Estado del formulario de inicio de sesión. */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val intentosFallidos: Int = 0,
    val errores: LoginErrores = LoginErrores()
)

/** Error de validación de cada campo (null = sin error). */
data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
)
