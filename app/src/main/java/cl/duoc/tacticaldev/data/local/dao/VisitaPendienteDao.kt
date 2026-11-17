package cl.duoc.tacticaldev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.tacticaldev.data.local.entity.VisitaPendienteEntity
import kotlinx.coroutines.flow.Flow

/** Cola de visitas guardadas sin internet, a la espera de enviarse a MySQL. */
@Dao
interface VisitaPendienteDao {
    @Insert
    suspend fun insertar(visita: VisitaPendienteEntity): Long

    @Query("SELECT * FROM visitas_pendientes ORDER BY fechaHora")
    suspend fun obtenerTodas(): List<VisitaPendienteEntity>

    @Query("DELETE FROM visitas_pendientes WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("SELECT COUNT(*) FROM visitas_pendientes WHERE usuarioId = :usuarioId")
    fun contarPorUsuario(usuarioId: Long): Flow<Int>
}
