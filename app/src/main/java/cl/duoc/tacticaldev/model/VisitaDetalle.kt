package cl.duoc.tacticaldev.model

/** Visita con los nombres del activo, punto de venta y operario (consulta con JOIN). */
data class VisitaDetalle(
    val id: Long,
    val activoId: Long,
    val activoCodigo: String,
    val activoTipo: String,
    val puntoVentaNombre: String,
    val usuarioId: Long,
    val usuarioNombre: String,
    val fechaHora: Long,
    val tipoVisita: String,
    val estadoEquipo: String,
    val temperatura: Double,
    val observaciones: String,
    val fotoUri: String,
    val distanciaMetros: Double,
    val ubicacionVerificada: Boolean,
    val estadoValidacion: String,
    val motivoRechazo: String?
)

// Propiedades de extensión para leer los catálogos como enums
val VisitaDetalle.tipo: TipoVisita get() = tipoVisita.aEnum(TipoVisita.MANTENCION)
val VisitaDetalle.estado: EstadoEquipo get() = estadoEquipo.aEnum(EstadoEquipo.OPERATIVO)
val VisitaDetalle.validacion: EstadoValidacion get() = estadoValidacion.aEnum(EstadoValidacion.PENDIENTE)
