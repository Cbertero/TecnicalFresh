package cl.duoc.tacticaldev.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tacticaldev.data.model.VisitaRequest

/**
 * Visita registrada SIN internet. Queda en Room solo hasta que se envía al
 * microservicio; después se borra (la copia oficial vive en MySQL).
 * La foto se guarda ya convertida a Base64, lista para enviar.
 */
@Entity(tableName = "visitas_pendientes")
data class VisitaPendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activoId: Long,
    val usuarioId: Long,
    val fechaHora: Long,
    val tipoVisita: String,
    val estadoEquipo: String,
    val temperatura: Double,
    val observaciones: String,
    val fotoBase64: String,
    val latitud: Double,
    val longitud: Double,
    val distanciaMetros: Double,
    val ubicacionVerificada: Boolean
)

fun VisitaRequest.aPendiente() = VisitaPendienteEntity(
    activoId = activoId, usuarioId = usuarioId, fechaHora = fechaHora, tipoVisita = tipoVisita,
    estadoEquipo = estadoEquipo, temperatura = temperatura, observaciones = observaciones,
    fotoBase64 = fotoUri, latitud = latitud, longitud = longitud,
    distanciaMetros = distanciaMetros, ubicacionVerificada = ubicacionVerificada
)

fun VisitaPendienteEntity.aRequest() = VisitaRequest(
    activoId = activoId, usuarioId = usuarioId, fechaHora = fechaHora, tipoVisita = tipoVisita,
    estadoEquipo = estadoEquipo, temperatura = temperatura, observaciones = observaciones,
    fotoUri = fotoBase64, latitud = latitud, longitud = longitud,
    distanciaMetros = distanciaMetros, ubicacionVerificada = ubicacionVerificada
)
