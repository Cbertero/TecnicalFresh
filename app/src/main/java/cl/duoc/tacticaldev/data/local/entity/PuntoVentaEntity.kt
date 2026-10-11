package cl.duoc.tacticaldev.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Minimarket o punto de venta donde se instalan los equipos de frío. */
@Entity(tableName = "puntos_venta")
data class PuntoVentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val direccion: String,
    val comuna: String,
    val latitud: Double,
    val longitud: Double
)
