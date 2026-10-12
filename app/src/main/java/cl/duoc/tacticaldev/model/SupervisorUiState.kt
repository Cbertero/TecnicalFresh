package cl.duoc.tacticaldev.model

/** Estado de la vista del supervisor. */
data class SupervisorUiState(
    val cargando: Boolean = true,
    val filtro: EstadoValidacion = EstadoValidacion.PENDIENTE,
    val visitas: List<VisitaDetalle> = emptyList()
)

/** Estado del diálogo para rechazar una visita con motivo obligatorio. */
data class RechazoUiState(
    val visita: VisitaDetalle? = null,
    val motivo: String = "",
    val errorMotivo: String? = null
)
