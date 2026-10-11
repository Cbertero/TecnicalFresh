package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.ActivoDao
import cl.duoc.tacticaldev.data.local.dao.PuntoVentaDao
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.model.ActivoConPunto
import kotlinx.coroutines.flow.Flow

class ActivoRepository(
    private val activoDao: ActivoDao,
    private val puntoVentaDao: PuntoVentaDao
) {
    fun observarActivos(): Flow<List<ActivoConPunto>> = activoDao.observarActivosConPunto()

    fun observarPuntosVenta(): Flow<List<PuntoVentaEntity>> = puntoVentaDao.observarTodos()

    suspend fun obtenerActivo(id: Long): ActivoConPunto? = activoDao.buscarActivoConPunto(id)

    suspend fun buscarPorCodigo(codigo: String): ActivoEntity? =
        activoDao.buscarPorCodigo(codigo.trim().uppercase())

    suspend fun registrarPuntoVenta(puntoVenta: PuntoVentaEntity): Long = puntoVentaDao.insertar(puntoVenta)

    suspend fun registrarActivo(activo: ActivoEntity): Long = activoDao.insertar(activo)
}
