package cl.duoc.tacticaldev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.model.VisitaDetalle
import kotlinx.coroutines.flow.Flow

private const val SELECT_VISITA_DETALLE = """
    SELECT v.id, v.activoId, a.codigo AS activoCodigo, a.tipo AS activoTipo,
           p.nombre AS puntoVentaNombre, v.usuarioId, u.nombre AS usuarioNombre,
           v.fechaHora, v.tipoVisita, v.estadoEquipo, v.temperatura, v.observaciones,
           v.fotoUri, v.distanciaMetros, v.ubicacionVerificada, v.estadoValidacion, v.motivoRechazo
    FROM visitas v
    INNER JOIN activos a ON a.id = v.activoId
    INNER JOIN puntos_venta p ON p.id = a.puntoVentaId
    INNER JOIN usuarios u ON u.id = v.usuarioId
"""

@Dao
interface VisitaDao {
    @Insert
    suspend fun insertar(visita: VisitaEntity): Long

    @Insert
    suspend fun insertarTodas(visitas: List<VisitaEntity>): List<Long>

    @Query("$SELECT_VISITA_DETALLE WHERE v.id = :id")
    suspend fun buscarDetalle(id: Long): VisitaDetalle?

    @Query("$SELECT_VISITA_DETALLE WHERE v.activoId = :activoId ORDER BY v.fechaHora DESC")
    fun observarPorActivo(activoId: Long): Flow<List<VisitaDetalle>>

    @Query("$SELECT_VISITA_DETALLE WHERE v.usuarioId = :usuarioId ORDER BY v.fechaHora DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<VisitaDetalle>>

    @Query("$SELECT_VISITA_DETALLE WHERE v.estadoValidacion = :estado ORDER BY v.fechaHora DESC")
    fun observarPorEstado(estado: String): Flow<List<VisitaDetalle>>

    @Query(
        """
        UPDATE visitas
        SET estadoValidacion = :estado, motivoRechazo = :motivo, supervisorId = :supervisorId
        WHERE id = :id
        """
    )
    suspend fun actualizarValidacion(id: Long, estado: String, motivo: String?, supervisorId: Long)
}
