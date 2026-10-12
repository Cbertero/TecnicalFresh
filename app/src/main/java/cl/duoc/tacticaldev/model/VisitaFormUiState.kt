package cl.duoc.tacticaldev.model

/** Estado completo del formulario de visita. */
data class VisitaFormUiState(
    val cargando: Boolean = true,
    val activo: ActivoConPunto? = null,
    val tipoVisita: TipoVisita? = null,
    val estadoEquipo: EstadoEquipo? = null,
    val temperatura: String = "",
    val observaciones: String = "",
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val distanciaMetros: Double? = null,
    val ubicacionVerificada: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val confirmado: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val intentosFallidos: Int = 0,
    val errores: VisitaErrores = VisitaErrores()
)

/** Errores por campo del formulario de visita (null = válido). */
data class VisitaErrores(
    val tipoVisita: String? = null,
    val estadoEquipo: String? = null,
    val temperatura: String? = null,
    val observaciones: String? = null,
    val foto: String? = null,
    val ubicacion: String? = null,
    val confirmacion: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(tipoVisita, estadoEquipo, temperatura, observaciones, foto, ubicacion, confirmacion)
            .any { it != null }
}
