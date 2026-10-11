package cl.duoc.tacticaldev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.model.ActivoConPunto
import kotlinx.coroutines.flow.Flow

private const val SELECT_ACTIVO_CON_PUNTO = """
    SELECT a.id, a.codigo, a.tipo, a.marca, a.modelo, a.puntoVentaId,
           p.nombre AS puntoVentaNombre, p.direccion, p.comuna, p.latitud, p.longitud,
           (SELECT MAX(v.fechaHora) FROM visitas v WHERE v.activoId = a.id) AS ultimaVisita
    FROM activos a
    INNER JOIN puntos_venta p ON p.id = a.puntoVentaId
"""

@Dao
interface ActivoDao {
    @Query("$SELECT_ACTIVO_CON_PUNTO ORDER BY p.nombre, a.codigo")
    fun observarActivosConPunto(): Flow<List<ActivoConPunto>>

    @Query("$SELECT_ACTIVO_CON_PUNTO WHERE a.id = :id")
    suspend fun buscarActivoConPunto(id: Long): ActivoConPunto?

    @Query("SELECT * FROM activos WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): ActivoEntity?

    @Insert
    suspend fun insertar(activo: ActivoEntity): Long

    @Insert
    suspend fun insertarTodos(activos: List<ActivoEntity>): List<Long>
}
