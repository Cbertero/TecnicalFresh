package cl.duoc.tacticaldev.model

/** Activo junto a los datos de su punto de venta y la fecha de su última visita. */
data class ActivoConPunto(
    val id: Long,
    val codigo: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val puntoVentaId: Long,
    val puntoVentaNombre: String,
    val direccion: String,
    val comuna: String,
    val latitud: Double,
    val longitud: Double,
    val ultimaVisita: Long?
)

// Propiedad de extensión (fuera de la clase para que Room no la trate como columna)
val ActivoConPunto.tipoActivo: TipoActivo get() = tipo.aEnum(TipoActivo.VITRINA_FREEZER)
