package cl.duoc.tacticaldev.model

/** Formulario de registro de un punto de venta. */
data class PuntoVentaForm(
    val nombre: String = "",
    val direccion: String = "",
    val comuna: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val errores: PuntoVentaErrores = PuntoVentaErrores()
)

data class PuntoVentaErrores(
    val nombre: String? = null,
    val direccion: String? = null,
    val comuna: String? = null,
    val latitud: String? = null,
    val longitud: String? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, direccion, comuna, latitud, longitud).any { it != null }
}

/** Formulario de registro de un activo (vitrina o conservadora). */
data class ActivoForm(
    val codigo: String = "",
    val tipo: TipoActivo? = null,
    val marca: String = "",
    val modelo: String = "",
    val puntoVentaId: Long? = null,
    val errores: ActivoErrores = ActivoErrores()
)

data class ActivoErrores(
    val codigo: String? = null,
    val tipo: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val puntoVenta: String? = null
) {
    val hayErrores: Boolean get() = listOf(codigo, tipo, marca, modelo, puntoVenta).any { it != null }
}

/** Estado de la pantalla de administración. */
data class AdminUiState(
    val pestana: Int = 0,
    val puntoVenta: PuntoVentaForm = PuntoVentaForm(),
    val activo: ActivoForm = ActivoForm(),
    val obteniendoUbicacion: Boolean = false,
    val guardando: Boolean = false
)
