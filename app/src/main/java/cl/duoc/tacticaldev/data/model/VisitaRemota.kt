package cl.duoc.tacticaldev.data.model

// Lo que se ENVÍA al registrar una visita (igual a VisitaRequest.java)
data class VisitaRequest(
    val activoId: Long,
    val usuarioId: Long,
    val fechaHora: Long,
    val tipoVisita: String,
    val estadoEquipo: String,
    val temperatura: Double,
    val observaciones: String,
    val fotoUri: String,
    val latitud: Double,
    val longitud: Double,
    val distanciaMetros: Double,
    val ubicacionVerificada: Boolean
)

// Lo que se envía al validar o rechazar (igual a ValidacionRequest.java)
data class ValidacionRequest(val supervisorId: Long, val motivo: String? = null)