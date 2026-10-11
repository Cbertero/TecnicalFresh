package cl.duoc.tacticaldev.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.tacticaldev.model.EstadoValidacion

/**
 * Registro de una visita en terreno. Contiene la información que pide el caso:
 * activo, fecha y hora, tipo de visita, estado del equipo, observaciones,
 * evidencia fotográfica y ubicación donde se realizó.
 */
@Entity(
    tableName = "visitas",
    foreignKeys = [
        ForeignKey(
            entity = ActivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["activoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("activoId"), Index("usuarioId")]
)
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val ubicacionVerificada: Boolean,
    val estadoValidacion: String = EstadoValidacion.PENDIENTE.name,
    val motivoRechazo: String? = null,
    val supervisorId: Long? = null
)
