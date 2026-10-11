package cl.duoc.tacticaldev.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Vitrina freezer o conservadora instalada en un punto de venta. */
@Entity(
    tableName = "activos",
    foreignKeys = [
        ForeignKey(
            entity = PuntoVentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["puntoVentaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("puntoVentaId"), Index(value = ["codigo"], unique = true)]
)
data class ActivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val tipo: String,
    val marca: String,
    val modelo: String,
    val puntoVentaId: Long
)
