package cl.duoc.tacticaldev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoVentaDao {
    @Query("SELECT * FROM puntos_venta ORDER BY nombre")
    fun observarTodos(): Flow<List<PuntoVentaEntity>>

    @Insert
    suspend fun insertar(puntoVenta: PuntoVentaEntity): Long

    @Insert
    suspend fun insertarTodos(puntosVenta: List<PuntoVentaEntity>): List<Long>
}
