package cl.duoc.tacticaldev.data.repository

import android.content.Context
import cl.duoc.tacticaldev.data.local.dao.VisitaPendienteDao
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.local.entity.aPendiente
import cl.duoc.tacticaldev.data.local.entity.aRequest
import cl.duoc.tacticaldev.data.model.ValidacionRequest
import cl.duoc.tacticaldev.data.model.VisitaRequest
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.data.sync.SincronizarVisitasWorker
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.utils.FotoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Visitas guardadas en MySQL a través del microservicio.
 * Sin internet, la visita queda en Room (tabla visitas_pendientes) y
 * [SincronizarVisitasWorker] la envía cuando vuelve la conexión.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VisitaRepository(
    private val context: Context,
    private val pendienteDao: VisitaPendienteDao? = null,
    // En las pruebas se reemplaza por una función vacía (WorkManager necesita Android)
    private val programarSincronizacion: () -> Unit = { SincronizarVisitasWorker.programar(context) }
) {

    companion object {
        /** Id que devuelve [registrar] cuando la visita quedó en el teléfono esperando internet. */
        const val ID_EN_COLA = -1L
    }

    // Room avisaba solo cuando cambiaban los datos. Con la API, cada vez que
    // registramos, validamos o rechazamos, sumamos 1 a este contador y todas
    // las listas que se estén mostrando se vuelven a pedir al servidor.
    private val cambios = MutableStateFlow(0)

    /**
     * Envía la visita al microservicio y devuelve el id de MySQL.
     * Sin internet la guarda en Room, programa el envío y devuelve [ID_EN_COLA].
     */
    suspend fun registrar(visita: VisitaEntity): Long {
        val foto = withContext(Dispatchers.IO) { FotoUtils.aBase64(context, visita.fotoUri) }.orEmpty()
        val solicitud = VisitaRequest(
            activoId = visita.activoId,
            usuarioId = visita.usuarioId,
            fechaHora = visita.fechaHora,
            tipoVisita = visita.tipoVisita,
            estadoEquipo = visita.estadoEquipo,
            temperatura = visita.temperatura,
            observaciones = visita.observaciones,
            fotoUri = foto,
            latitud = visita.latitud,
            longitud = visita.longitud,
            distanciaMetros = visita.distanciaMetros,
            ubicacionVerificada = visita.ubicacionVerificada
        )
        return try {
            val creada = RetrofitInstance.api.registrarVisita(solicitud)
            cambios.update { it + 1 }
            creada.id
        } catch (e: IOException) {
            // IOException = no hubo conexión con el servidor
            val dao = pendienteDao ?: throw e
            dao.insertar(solicitud.aPendiente())
            programarSincronizacion()
            ID_EN_COLA
        }
    }

    /** Cantidad de visitas del usuario que siguen en el teléfono esperando internet. */
    fun observarPendientes(usuarioId: Long): Flow<Int> =
        pendienteDao?.contarPorUsuario(usuarioId) ?: flowOf(0)

    /**
     * Envía a MySQL cada visita pendiente y la borra de Room al recibir la respuesta.
     * Devuelve true si no quedó ninguna pendiente; false si se cortó la conexión.
     */
    suspend fun sincronizarPendientes(): Boolean {
        val dao = pendienteDao ?: return true
        var enviadas = 0
        for (pendiente in dao.obtenerTodas()) {
            try {
                RetrofitInstance.api.registrarVisita(pendiente.aRequest())
                dao.eliminar(pendiente.id)
                enviadas++
            } catch (e: HttpException) {
                // 4xx: el servidor rechaza los datos (por ejemplo, el activo ya no existe);
                // reintentar no lo arregla, así que se descarta. 5xx: se reintenta más tarde.
                if (e.code() in 400..499) dao.eliminar(pendiente.id) else return false
            } catch (e: IOException) {
                return false
            }
        }
        if (enviadas > 0) cambios.update { it + 1 } // las listas abiertas muestran las nuevas
        return true
    }

    suspend fun obtenerDetalle(id: Long): VisitaDetalle? =
        runCatching { RetrofitInstance.api.getVisita(id) }.getOrNull()

    fun observarPorActivo(activoId: Long): Flow<List<VisitaDetalle>> =
        consultar { RetrofitInstance.api.getVisitas(activoId = activoId) }

    fun observarPorUsuario(usuarioId: Long): Flow<List<VisitaDetalle>> =
        consultar { RetrofitInstance.api.getVisitas(usuarioId = usuarioId) }

    fun observarPorEstado(estado: EstadoValidacion): Flow<List<VisitaDetalle>> =
        consultar { RetrofitInstance.api.getVisitas(estado = estado.name) }

    suspend fun validar(visitaId: Long, supervisorId: Long) {
        RetrofitInstance.api.validarVisita(visitaId, ValidacionRequest(supervisorId))
        cambios.update { it + 1 }
    }

    suspend fun rechazar(visitaId: Long, motivo: String, supervisorId: Long) {
        RetrofitInstance.api.rechazarVisita(visitaId, ValidacionRequest(supervisorId, motivo.trim()))
        cambios.update { it + 1 }
    }

    /** Vuelve a pedir todas las listas visibles (por ejemplo, desde un botón "actualizar"). */
    fun refrescar() = cambios.update { it + 1 }

    // Cada vez que cambia el contador, repite la consulta. Sin internet devuelve una lista vacía.
    private fun consultar(peticion: suspend () -> List<VisitaDetalle>): Flow<List<VisitaDetalle>> =
        cambios.flatMapLatest {
            flow {
                val lista = try {
                    peticion()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emptyList()
                }
                emit(lista)
            }
        }
}