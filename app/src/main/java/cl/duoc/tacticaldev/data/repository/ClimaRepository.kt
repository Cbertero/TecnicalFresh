package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoApi
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoInstance
import cl.duoc.tacticaldev.model.Clima
import cl.duoc.tacticaldev.model.descripcionClima
import kotlin.coroutines.cancellation.CancellationException

/**
 * Consulta el clima actual en la API externa Open-Meteo.
 * Si falla (sin internet o la API no responde) devuelve null: el formulario
 * de visita sigue funcionando igual, el clima es solo información de apoyo.
 */
class ClimaRepository(private val api: OpenMeteoApi = OpenMeteoInstance.api) {

    suspend fun obtenerClima(latitud: Double, longitud: Double): Clima? = try {
        api.obtenerClimaActual(latitud, longitud).actual?.let {
            Clima(
                temperatura = it.temperatura,
                humedad = it.humedad,
                descripcion = descripcionClima(it.codigo)
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
