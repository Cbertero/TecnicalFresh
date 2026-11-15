package cl.duoc.tacticaldev.data.repository

import android.content.Context
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.model.ValidacionRequest
import cl.duoc.tacticaldev.data.model.VisitaRequest
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.VisitaDetalle
import cl.duoc.tacticaldev.utils.FotoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/** Visitas guardadas en MySQL a través del microservicio. */
@OptIn(ExperimentalCoroutinesApi::class)
class VisitaRepository(private val context: Context) {

    // Room avisaba solo cuando cambiaban los datos. Con la API, cada vez que
    // registramos, validamos o rechazamos, sumamos 1 a este contador y todas
    // las listas que se estén mostrando se vuelven a pedir al servidor.
    private val cambios = MutableStateFlow(0)

    suspend fun registrar(visita: VisitaEntity): Long {
        val foto = withContext(Dispatchers.IO) { FotoUtils.aBase64(context, visita.fotoUri) }.orEmpty()
        val creada = RetrofitInstance.api.registrarVisita(
            VisitaRequest(
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
        )
        cambios.update { it + 1 }
        return creada.id
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