package cl.duoc.tacticaldev.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Usuario de la app (datos ficticios). La clave se guarda como hash SHA-256. */
@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val claveHash: String,
    val rol: String,
    val cargo: String
)
