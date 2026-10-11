package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.VisitaDao
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.VisitaDetalle
import kotlinx.coroutines.flow.Flow

class VisitaRepository(private val visitaDao: VisitaDao) {

    suspend fun registrar(visita: VisitaEntity): Long = visitaDao.insertar(visita)

    suspend fun obtenerDetalle(id: Long): VisitaDetalle? = visitaDao.buscarDetalle(id)

    fun observarPorActivo(activoId: Long): Flow<List<VisitaDetalle>> = visitaDao.observarPorActivo(activoId)

    fun observarPorUsuario(usuarioId: Long): Flow<List<VisitaDetalle>> = visitaDao.observarPorUsuario(usuarioId)

    fun observarPorEstado(estado: EstadoValidacion): Flow<List<VisitaDetalle>> =
        visitaDao.observarPorEstado(estado.name)

    suspend fun validar(visitaId: Long, supervisorId: Long) =
        visitaDao.actualizarValidacion(visitaId, EstadoValidacion.VALIDADA.name, null, supervisorId)

    suspend fun rechazar(visitaId: Long, motivo: String, supervisorId: Long) =
        visitaDao.actualizarValidacion(visitaId, EstadoValidacion.RECHAZADA.name, motivo.trim(), supervisorId)
}
